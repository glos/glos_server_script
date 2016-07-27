#include <stdio.h>
#include <string.h>
#include <stdlib.h>
#include "lua.h"
#include "lualib.h"
#include "lauxlib.h"
#include "netcdf.h"

#define BINARY_NAME "obs2nc"
#define LUA_COMMON_FILE "common.lua"
#define LUA_COMMON_FUN "initPlatformInfo"
#define LUA_NC_ROOT "NCs"
#define LUA_VARS_ROOT "vars"
#define VAR_TIME "time"
#define VAR_OBS "obs"
#define VAR_FEATURE_TYPE_INSTANCE "feature_type_instance"
#define VAR_Z "height"
#define VAR_TEXT "text"
#define DEPTH_LEVEL "depthLevel"
#define TEXT_VAR_DIM_LENGTH "maxTextVarLength"
#define VALUE "value"
#define VTYPE "vtype"
#define MISSING_VALUE "missing_value"
#define FEATURE_TYPE "featureType"
#define TIMESERIES "timeSeries"
#define TIMESERIES_PROFILE "timeSeriesProfile"
#define TS_MIN "tsstart"
#define TS_MAX "tsend"
#define VAR_ATTRIBUTES "attributes"
#define DATA_SOURCE "target"
#define TMS_COUNT "count"
#define DEPTHS "depths"

#define NC_FILE_NAME "nc_name"
#define GLOBAL_ATTR "GlobalAttributes"

#define LUA_ERRCODE 6
#define NC_ERRCODE 9

typedef enum{FALSE,TRUE}BOOL;
struct __obs2nc_context;
typedef void (*CTX_Callback)(struct __obs2nc_context*,const char*,void*,int);
typedef struct __obs2nc_context{
    int ncid;
    int attrscope;
    int var_obs_id;
    int var_time_id;
    int var_depth_id;
    size_t len;
    double missingval;
    lua_State *L;
    void *param;
    CTX_Callback pfunCallback;
    double *obs;
    double *time;
    double *height;
    char *target;
}Context;
typedef struct _dim{
    int dim0[1];
    int dim1[1];
    int dim1t[1];
    int dim1z[1];
    int dim2[2];
    int dim3[3];
    int dlevel;
    size_t tmscnt;
}DimRef;


static inline void nc_error(int err,unsigned int line){
    fprintf(stderr,"NetCDF Error: %s at line:%d\n", nc_strerror(err),line);
    exit(NC_ERRCODE);
}
static void loopTable(Context *pctx,const char* pstrTableName){
    const char *pstrItem=NULL;
    double dItem;
    int type;
    if(!pctx)return;
    if(pstrTableName){
        lua_pushstring(pctx->L,pstrTableName);
        lua_gettable(pctx->L,-2);//the table is at index -2 and the subtable name is at index -1
    }
    if(lua_istable(pctx->L,-1)&&pctx->pfunCallback){
	lua_pushnil(pctx->L);
	while(lua_next(pctx->L,-2)){
	    if(lua_type(pctx->L,-2)==LUA_TSTRING){
		type=lua_type(pctx->L,-1);
	 	switch(type){
		    case LUA_TSTRING:
			pstrItem=lua_tostring(pctx->L,-1);
		        pctx->pfunCallback(pctx,lua_tostring(pctx->L,-2),(void*)pstrItem,LUA_TSTRING);
		        break;
		    case LUA_TNUMBER:
			dItem=(double)lua_tonumber(pctx->L,-1);
		        pctx->pfunCallback(pctx,lua_tostring(pctx->L,-2),(void*)&dItem,LUA_TNUMBER);
			break;
		    case LUA_TTABLE:
			pctx->pfunCallback(pctx,lua_tostring(pctx->L,-2),NULL,LUA_TTABLE);
			break;
		    default:break;
		}
	    }
	    lua_pop(pctx->L,1);
	}
    }
    if(pstrTableName)
        lua_pop(pctx->L,1);
}
static inline void setAttrText(int ncid,int varid,const char *key,const char *val){
    int ret;
    if(NC_NOERR!=(ret=nc_put_att_text(ncid,varid,key,strlen(val),val)))
	nc_error(ret,__LINE__);
}
static inline void setAttrDouble(int ncid,int varid,const char *key,double *val){
    int ret;
    if(NC_NOERR!=(ret=nc_put_att_double(ncid,varid,key,NC_DOUBLE,1,val)))
	nc_error(ret,__LINE__);
}
static void populateAttrs(Context *pCtx,const char *key,void *value,int type){
    if(!pCtx||pCtx->ncid<=0)return;
    if(LUA_TNUMBER==type){
	double *pd=(double*)value;
	setAttrDouble(pCtx->ncid,pCtx->attrscope,key,pd);
//printf("%d::%s=%f\n",pCtx->attrscope,key,pd);
    }else if(LUA_TSTRING==type){
	setAttrText(pCtx->ncid,pCtx->attrscope,key,(const char*)value);
//printf("%d::%s=%s\n",pCtx->attrscope,key,(const char*)value);
    }
}
static int query1DVarType(lua_State *L,const char* pstrKey){
    int type=-1;
    if(!L||!lua_istable(L,-1))return -1;
    if(lua_istable(L,-1)){
	lua_pushstring(L,pstrKey);
	lua_gettable(L,-2);
	if(lua_isnumber(L,-1))
	    type=LUA_TNUMBER;
	else if(lua_isstring(L,-1))
	    type=LUA_TSTRING;
	lua_pop(L,1);
    }
    return type;
}
static void* queryValueByKey(lua_State *L,const char* pstrTableName,const char* pstrKey){
    void *pval=NULL;
    double *pdval=NULL;
    char *pstrval=NULL;
    const char *pstr=NULL;
    if(!L)return NULL;
    if(pstrTableName){
        lua_pushstring(L,pstrTableName);
        lua_gettable(L,-2);
    }
    if(lua_istable(L,-1)){
	lua_pushstring(L,pstrKey);
	lua_gettable(L,-2);
	if(lua_isnumber(L,-1)){
	    pdval=(double*)malloc(sizeof(double));
	    *pdval=lua_tonumber(L,-1);
	    pval=(void*)pdval;
	}
	else if(lua_isstring(L,-1)){
	    pstr=lua_tostring(L,-1);
	    pstrval=(char*)malloc(sizeof(char)*strlen(pstr)+1);
	    *pstrval=0;
	    strcat(pstrval,pstr);
	    pval=(void*)pstrval;
	}
	lua_pop(L,1);
	
    }
    if(pstrTableName)
        lua_pop(L,1);
    return pval;
}
//static BOOL isTrue(lua_State *L,const char* pstrTableName,const char* pstrKey)
size_t start1[1]={0};
size_t count1[1];
size_t start2[2]={0,0};
size_t count2[2];
size_t start3[3]={0,0,0};
size_t count3[3];

static void onData(Context *pCtx,const char *var,void *value,int type){
    int ret,t,varid=-1;
    char *pstrVal=NULL;
    double *pvVal=NULL;
    if(pCtx&&var){
	if(strncmp(VAR_TIME,var,strlen(VAR_TIME))&&strncmp(VAR_OBS,var,strlen(VAR_OBS))&&strncmp(VAR_Z,var,strlen(VAR_Z))){//we don't handle these 3
	    t=query1DVarType(pCtx->L,VALUE);
	    if(t==LUA_TSTRING){
	        pstrVal=(char*)queryValueByKey(pCtx->L,NULL,VALUE);
	        if(pstrVal){
		    if(NC_NOERR!=(ret=nc_inq_varid(pCtx->ncid,var,&varid)))
	   	        nc_error(ret,__LINE__);
		    count1[0]=strlen(pstrVal)+1;
		    if(NC_NOERR!=(ret=nc_put_vara_text(pCtx->ncid,varid,start1,count1,(const char*)pstrVal)))
			nc_error(ret,__LINE__);
		    free(pstrVal);
	        }
	    }
	    else if(t==LUA_TNUMBER){
		pvVal=(double*)queryValueByKey(pCtx->L,NULL,VALUE);
		if(pvVal){
		    char *pszType=(char*)queryValueByKey(pCtx->L,NULL,VTYPE);
		    if(pszType){
		        if(0==strncmp(pszType,"int",3)){
			    int val=(int)*pvVal;
		            if(NC_NOERR!=(ret=nc_inq_varid(pCtx->ncid,var,&varid)))
	   	                nc_error(ret,__LINE__);
		            if(NC_NOERR!=(ret=nc_put_var_int(pCtx->ncid,varid,&val)))
			        nc_error(ret,__LINE__);
			}
		        free(pszType);
			free(pvVal);
			return;
		    }
	            if(NC_NOERR!=(ret=nc_inq_varid(pCtx->ncid,var,&varid)))
	   	        nc_error(ret,__LINE__);
		    if(NC_NOERR!=(ret=nc_put_var_double(pCtx->ncid,varid,pvVal)))
			nc_error(ret,__LINE__);
		    free(pvVal);
		}
	    }
	}
    }
}
static void onVars(Context *pCtx,const char *var,void *value,int type){
    int ret,t,varid=-1;
    double *szCnt=NULL;
    double *pdDepth=NULL;
    char *pstrFt=NULL;
    DimRef *pdr=(DimRef*)pCtx->param;
    if(!pdr||!pCtx||pCtx->ncid<=0||!pCtx->param||!var)return;
    if(0==strncmp(VAR_TIME,var,strlen(VAR_TIME))){
	if(NC_NOERR!=(ret=nc_def_var(pCtx->ncid,VAR_TIME,NC_DOUBLE,1,pdr->dim1t,&varid)))
	    nc_error(ret,__LINE__);
	pCtx->var_time_id=varid;
	szCnt=(double*)queryValueByKey(pCtx->L,NULL,TMS_COUNT);
	if(szCnt){
	    pdr->tmscnt=(size_t)*szCnt;
	    free(szCnt);
	}
    }else if(0==strncmp(VAR_OBS,var,strlen(VAR_OBS))){
	if(pdr->dlevel<=1){//timeseries
	    if(NC_NOERR!=(ret=nc_def_var(pCtx->ncid,VAR_OBS,NC_DOUBLE,2,pdr->dim2,&varid)))
	        nc_error(ret,__LINE__);
	}   
	else{
	    if(NC_NOERR!=(ret=nc_def_var(pCtx->ncid,VAR_OBS,NC_DOUBLE,3,pdr->dim3,&varid)))
	    	    nc_error(ret,__LINE__);
	}
	pCtx->var_obs_id=varid;
    }else if(0==strncmp(VAR_Z,var,strlen(VAR_Z))){
	if(NC_NOERR!=(ret=nc_def_var(pCtx->ncid,VAR_Z,NC_DOUBLE,1,pdr->dim1z,&varid)))
	    nc_error(ret,__LINE__);
	pCtx->var_depth_id=varid;
	if(pdr->dlevel<=1){ 
	    pdDepth=(double*)queryValueByKey(pCtx->L,NULL,VALUE);
	    if(pdDepth){
		pCtx->height=(double*)malloc(sizeof(double));
		*(pCtx->height)=*pdDepth;
		free(pdDepth);
	    }
	    //FIXME TODO abort if depth value is not provided for time series
	}
    }else{
	t=query1DVarType(pCtx->L,VALUE);
	if(t!=-1){
		if(t==LUA_TNUMBER){
		    char *pszType=(char*)queryValueByKey(pCtx->L,NULL,VTYPE);

		    if(pszType&&0==strncmp(pszType,"int",3))
			t=NC_INT;
		    else
			t=NC_DOUBLE;
		    if(pszType)free(pszType);
		}
		else if(t==LUA_TSTRING)t=NC_CHAR;
		if(t==NC_CHAR){
		    if(NC_NOERR!=(ret=nc_def_var(pCtx->ncid,var,t,1,pdr->dim0,&varid)))
			nc_error(ret,__LINE__);
	 	}else{
		    if(NC_NOERR!=(ret=nc_def_var(pCtx->ncid,var,t,0,NULL,&varid)))
			nc_error(ret,__LINE__);
		}
	}
    }
    if(varid!=-1){
	pCtx->pfunCallback=populateAttrs;
	pCtx->attrscope=varid;
	loopTable(pCtx,VAR_ATTRIBUTES);
    }
    pCtx->pfunCallback=onVars;
}
static void onV(Context *pCtx,const char *var,void *value,int type){
    char *pstrNcName=NULL;
    double *pdZ=NULL;
    int ret,txtlen=128;
    int dvalid=1;
    DimRef dr;
    if(pCtx&&pCtx->L&&type==LUA_TTABLE){
	pstrNcName=(char*)queryValueByKey(pCtx->L,NULL,NC_FILE_NAME);
	if(pstrNcName){
	    if(NC_NOERR!=(ret=nc_create(pstrNcName,NC_NOCLOBBER|NC_CLASSIC_MODEL,&(pCtx->ncid))))
	        nc_error(ret,__LINE__);
	    free(pstrNcName);
	    pCtx->pfunCallback=populateAttrs;
    	    pCtx->attrscope=NC_GLOBAL;
	    loopTable(pCtx,GLOBAL_ATTR);
	    if(NC_NOERR!=(ret=nc_def_dim(pCtx->ncid,VAR_FEATURE_TYPE_INSTANCE,1,&(dr.dim1[0]))))
	        nc_error(ret,__LINE__);
	    if(NC_NOERR!=(ret=nc_def_dim(pCtx->ncid,VAR_TIME,NC_UNLIMITED,&(dr.dim2[0]))))
	        nc_error(ret,__LINE__);	    
	    pdZ=queryValueByKey(pCtx->L,LUA_VARS_ROOT,DEPTH_LEVEL);
	    if(pdZ){
		dr.dlevel=(int)*pdZ;
 		if(dr.dlevel<=0){dvalid=0;dr.dlevel=1;}//only unlimited dim allows 0 (NC_UNLIMITED) here
		if(NC_NOERR!=(ret=nc_def_dim(pCtx->ncid,VAR_Z,dr.dlevel,&(dr.dim3[1]))))
	            nc_error(ret,__LINE__);
		free(pdZ);
		pdZ=queryValueByKey(pCtx->L,LUA_VARS_ROOT,TEXT_VAR_DIM_LENGTH);
	        if(pdZ){//it's for the text dim length this time
		    if(*(int*)pdZ>0)txtlen=*(int*)pdZ;
		    free(pdZ);
	        }
	        if(NC_NOERR!=(ret=nc_def_dim(pCtx->ncid,VAR_TEXT,txtlen,dr.dim0)))
	            nc_error(ret,__LINE__);
	        dr.dim2[1]=dr.dim3[2]=dr.dim1[0];
	        dr.dim1t[0]=dr.dim3[0]=dr.dim2[0];//it appears netcdf classic requires unlimited dimension is always at the beginning of the dim array
	        dr.dim1z[0]=dr.dim3[1];
	        pCtx->param=(void*)&dr;
	        pCtx->pfunCallback=onVars;
	        loopTable(pCtx,LUA_VARS_ROOT);
	        
	    }
	    //FIXME TODO: abort coz of missing depth level
	    
	    if(NC_NOERR!=(ret=nc_enddef(pCtx->ncid)))
		nc_error(ret,__LINE__);
	    pCtx->target=(char*)queryValueByKey(pCtx->L,NULL,DATA_SOURCE);
	    if(pCtx->target){
		if(strlen(pCtx->target)){//make sure we appear to have big chunk data ready to read
	            pCtx->pfunCallback=onData;//populate other single-value-variables first
	            loopTable(pCtx,LUA_VARS_ROOT);
		    FILE *f=NULL;
		    if(dvalid)
		        fopen(pCtx->target,"r");
		    if(f){
			int i=0;
			size_t rowcnt=dr.tmscnt*dr.dlevel;
			pCtx->time=(double*)malloc(sizeof(double)*dr.tmscnt);
			pCtx->obs=(double*)malloc(sizeof(double)*dr.tmscnt*dr.dlevel);
		        if(dr.dlevel==1){
			    if(NC_NOERR!=(ret=nc_put_var_double(pCtx->ncid,pCtx->var_depth_id,pCtx->height)))
				nc_error(ret,__LINE__);
			    ret=2;
			    while(!feof(f)&&ret==2&&i<rowcnt){
			        ret=fscanf(f,"%lf,%lf",pCtx->time+i,pCtx->obs+i);
	    		        ++i;
			    }
			    count1[0]=dr.tmscnt;
			    if(NC_NOERR!=(ret=nc_put_vara_double(pCtx->ncid,pCtx->var_time_id,start1,count1,pCtx->time)))
				nc_error(ret,__LINE__);
			    count2[0]=dr.tmscnt;
			    count2[1]=1;
			    if(NC_NOERR!=(ret=nc_put_vara_double(pCtx->ncid,pCtx->var_obs_id,start2,count2,pCtx->obs)))
				nc_error(ret,__LINE__);
		        }else{
			    pCtx->height=(double*)malloc(sizeof(double)*dr.dlevel);
			    ret=3;
			    double dh=0.,dt=0.;
			    double *itr=pCtx->height;
			    double *itr1=pCtx->time;
			    double *end=pCtx->time+dr.tmscnt;
			    int dcnt=dr.dlevel;
			    while(!feof(f)&&ret==3&&i<rowcnt){
			        ret=fscanf(f,"%lf,%lf,%lf",&dt,pCtx->obs+i,&dh);
				if(3==ret){
				    if(itr&&--dcnt>=0)*(itr++)=dh;
				    else itr=NULL;
				    if(itr1==pCtx->time)*(itr1++)=dt;
				    else if(dt!=*itr1&&itr1<end)*(itr1++)=dt;
				    ++i;
				}
				//FIXME TODO: what if ret is not equal to 3
	    		    }
			    count1[0]=dr.tmscnt;
			    if(NC_NOERR!=(ret=nc_put_vara_double(pCtx->ncid,pCtx->var_time_id,start1,count1,pCtx->time)))
				nc_error(ret,__LINE__);
			    count1[0]=dr.dlevel;
			    if(NC_NOERR!=(ret=nc_put_vara_double(pCtx->ncid,pCtx->var_depth_id,start1,count1,pCtx->height)))
				nc_error(ret,__LINE__);
			    count3[0]=dr.tmscnt;
			    count3[1]=dr.dlevel;
			    count3[2]=1;
			    if(NC_NOERR!=(ret=nc_put_vara_double(pCtx->ncid,pCtx->var_obs_id,start3,count3,pCtx->obs)))
				nc_error(ret,__LINE__);
			}
			if(pCtx->time)free(pCtx->time);
			if(pCtx->obs)free(pCtx->obs);
			if(pCtx->height)free(pCtx->height);
			pCtx->time=NULL;
			pCtx->obs=NULL;
			pCtx->height=NULL;
			fclose(f);
		    }else{
			exit(-1);
		    }
		}
		free(pCtx->target);
	    }
	    //FIXME TODO: abort coz of missing target file
	    if(NC_NOERR!=(ret=nc_close(pCtx->ncid)))
    	        nc_error(ret,__LINE__);
	    pCtx->pfunCallback=onV;
	}
    }
}

int main(int argc,char **argv){
    char path[1024]={0};
    if(argc<2)return -1;
    Context ctx={-1,-1,0,0,0,0,-9999.0,NULL,NULL,NULL,NULL,NULL,NULL,NULL};
    int ret;
    const char *pcStr=strstr(argv[0],BINARY_NAME);
    if(!pcStr){
	printf("Error: execute %s?\n",argv[0]);
	return 1;
    }
    ctx.L=lua_open();
    luaL_openlibs(ctx.L);
    if(pcStr==argv[0])
        ret=luaL_dofile(ctx.L,LUA_COMMON_FILE);
    else{
	if(pcStr-argv[0]+strlen(LUA_COMMON_FILE)>1023){
	    printf("Error: Oops, path is too long for the buffer\n");
	    return 2;
	}
	strncpy(path,argv[0],pcStr-argv[0]);
	strcpy(path+(pcStr-argv[0]),LUA_COMMON_FILE);
	ret=luaL_dofile(ctx.L,path);
    }
    if(ret!= 0){
  	printf("Error occurs when calling luaL_dofile() LUA_COMMON_FILE 0x%x\n",ret);
  	printf("Error: %s\n", lua_tostring(ctx.L,-1));
	return 3;
    }
    lua_getglobal(ctx.L,LUA_COMMON_FUN);
    ret=luaL_dofile(ctx.L,argv[1]);
    if(ret!= 0){
  	printf("Error occurs when calling luaL_dofile() %s 0x%x\n",argv[1],ret);
  	printf("Error: %s\n", lua_tostring(ctx.L,-1));
	return 4;
    }
    lua_getglobal(ctx.L,"Platform");
    if(!lua_istable(ctx.L,-1))return LUA_ERRCODE;
    lua_pcall(ctx.L,1,1,0);
    
    ctx.pfunCallback=onV;
    loopTable(&ctx,LUA_NC_ROOT);
 
    return 0;
}

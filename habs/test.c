#include <stdio.h>
#include <string.h>
#include <stdlib.h>
#include "lua.h"
#include "lualib.h"
#include "lauxlib.h"
#include "netcdf.h"

#define LUA_COMMON_FILE "common.lua"
#define LUA_COMMON_FUN "initPlatformInfo"
#define LUA_NC_ROOT "NCs"
#define LUA_VARS_ROOT "vars"
#define VAR_TIME "time"
#define VAR_OBS "obs"
#define VAR_FEATURE_TYPE_INSTANCE "feature_type_instance"
#define VAR_Z "height"
#define DEPTH_LEVEL "depthLevel"
#define VALUE "value"
#define MISSING_VALUE "missing_value"
#define FEATURE_TYPE "featureType"
#define TIMESERIES "timeSeries"
#define TIMESERIES_PROFILE "timeSeriesProfile"
#define TS_MIN "tsstart"
#define TS_MAX "tsend"
#define VAR_ATTRIBUTES "attributes"
#define DATA_SOURCE "source"
#define VAR_META "meta"
#define OBS_COUNT "tscount"
#define DEPTHS "depths"

#define LUA_ERRCODE 6
#define NC_ERRCODE 9

typedef enum{FALSE,TRUE}BOOL;
struct __obs2nc_context;
typedef void (*CTX_Callback)(struct __obs2nc_context,const char*,void*,int,int);
typedef struct __obs2nc_context{
    int ncid;
    int attrscope;
    size_t len;
    double missingval;
    lua_State *L;
    void *param;
    CTX_Callback pfunCallback;
    double *obs;
    double *time;
    double *height;
}Context;
typedef struct _dim{
    int dim1[1];
    int dim1t[1];
    int dim1z[1];
    int dim2[2];
    int dim3[3];
}DimRef;
const char* pstrGlobalAttributes="GlobalAttributes";


static inline void nc_error(int err,unsigned int line){
    fprintf(stderr,"NetCDF Error: %s at line:%d\n", nc_strerror(err),line);
    exit(NC_ERRCODE);
}
/*static inline void lua_error(lua_State *L,unsigned int line){
    fprintf(stderr, "LUA Error: %s: at line:%d\n",lua_tostring(L, -1),line);
    lua_pop(L,1);
    exit(LUA_ERRCODE);
}*/
static void loopTable(Context *pctx,const char* pstrTableName,int idx){
    const char *pstrItem=NULL;
    double dItem;
    if(!pctx||!lua_istable(pctx->L,-1))return;
    if(pstrTableName){
        lua_pushstring(pctx->L,pstrTableName);
        lua_gettable(pctx->L,-2-idx);//the table is at index -2 and the subtable name is at index -1
    }
    if(lua_istable(pctx->L,-1-idx)&&pctx->pfunCallback){
	lua_pushnil(pctx->L);
	while(lua_next(pctx->L,-2-idx)){
	    if(lua_type(pctx->L,-2-idx)==LUA_TSTRING){
		if(lua_type(pctx->L,-1-idx)==LUA_TSTRING){
		    pstrItem=lua_tostring(pctx->L,-1-idx);
		    pctx->pfunCallback(*pctx,lua_tostring(pctx->L,-2-idx),(void*)pstrItem,LUA_TSTRING,lua_gettop(pctx->L)-1);
		}
		else if(lua_type(pctx->L,-1-idx)==LUA_TNUMBER){
		    dItem=(double)lua_tonumber(pctx->L,-1-idx);
		    pctx->pfunCallback(*pctx,lua_tostring(pctx->L,-2-idx),(void*)&dItem,LUA_TNUMBER,lua_gettop(pctx->L)-1);
		}
		else if(lua_type(pctx->L,-1-idx)==LUA_TTABLE){
		    pctx->pfunCallback(*pctx,lua_tostring(pctx->L,-2-idx),NULL,LUA_TTABLE,lua_gettop(pctx->L)-1);
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
static void populateAttrs(Context ctx,const char *key,void *value,int type,int elems){
    double *pd;
    if(ctx.ncid<=0)return;
    if(LUA_TNUMBER==type){
	pd=(double*)value;
	setAttrDouble(ctx.ncid,ctx.attrscope,key,pd);
    }else if(LUA_TSTRING==type)
	setAttrText(ctx.ncid,ctx.attrscope,key,(const char*)value);
}
static int query1DVarType(lua_State *L,const char* pstrTableName,const char* pstrKey){
    int type=-1;
    if(!L||!lua_istable(L,-1))return -1;
    if(pstrTableName){
        lua_pushstring(L,pstrTableName);
        lua_gettable(L,-2);
    }
    if(lua_istable(L,-1)){
	lua_pushstring(L,pstrKey);
	lua_gettable(L,-2);
	if(lua_isnumber(L,-1))
	    type=LUA_TNUMBER;
	else if(lua_isstring(L,-1))
	    type=LUA_TSTRING;
	lua_pop(L,1);
    }
    if(pstrTableName)
        lua_pop(L,1);
    return type;
}
static void* queryTableValue(lua_State *L,const char* pstrTableName,const char* pstrSubTableName,const char* pstrKey){
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
	if(pstrSubTableName){
	    lua_pushstring(L,pstrSubTableName);
            lua_gettable(L,-2);
	}
	if(!pstrSubTableName||lua_istable(L,-1)){
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
    }
    if(pstrSubTableName)
	lua_pop(L,1);
    if(pstrTableName)
        lua_pop(L,1);
    return pval;
}
static BOOL populateArray(lua_State *L,const char *varName,const char *parrName,int type,void *ptrVals,size_t len){
    double *pdVal=NULL;
    char *pstrVal=NULL;
    BOOL ret=FALSE;
    int i;
    if(!L)return ret;
    if(varName){
	lua_pushstring(L,varName);
        lua_gettable(L,-2);
    }
    if(lua_istable(L,-1)){
        if(parrName){
            lua_pushstring(L,parrName);
            lua_gettable(L,-2);
        }
        if(lua_istable(L,-1)){
	    if(type==LUA_TNUMBER)pdVal=(double*)ptrVals;
	    else if(type==LUA_TSTRING)pstrVal=(char*)ptrVals;
	    for(i=0;i<len;++i){
	        lua_rawgeti(L,-1,i);
	        if(type==LUA_TNUMBER&&lua_isnumber(L,-1)&&pdVal)
		    pdVal[i]=lua_tonumber(L,-1);
	        //else if(type==LUA_TSTRING&&lua_isstring(L,-1)&&pstrVal)
		    //pstrVal[i]=lua_tostring(L,-1);
	    }
	    ret=TRUE;
        }
    }
    if(parrName)
        lua_pop(L,1);
    if(varName)
	lua_pop(L,1);
    return ret;
}
static BOOL populateTableKey(lua_State *L,const char *varName,const char *parrName,int type,void *ptrVals,size_t len){
}
static void defineVars(Context ctx,const char *var,void *value,int type,int elems){
    int ret,t,varid=-1;
    char *pstrFt=NULL;
    DimRef *pdr=NULL;
    if(ctx.ncid<=0||!ctx.param||!var)return;
    pdr=(DimRef*)ctx.param;
    if(0==strncmp(VAR_TIME,var,strlen(VAR_TIME))){
	if(NC_NOERR!=(ret=nc_def_var(ctx.ncid,VAR_TIME,NC_DOUBLE,1,pdr->dim1t,&varid)))
	    nc_error(ret,__LINE__);
    }else if(0==strncmp(VAR_OBS,var,strlen(VAR_OBS))){
	pstrFt=(char*)queryTableValue(ctx.L,NULL,NULL,FEATURE_TYPE);
	if(pstrFt){
	    if(0==strncmp(TIMESERIES_PROFILE,pstrFt,strlen(TIMESERIES_PROFILE))){
		if(NC_NOERR!=(ret=nc_def_var(ctx.ncid,VAR_OBS,NC_DOUBLE,3,pdr->dim3,&varid)))
	    	    nc_error(ret,__LINE__);
	    }
	    else if(0==strncmp(TIMESERIES,pstrFt,strlen(TIMESERIES))){

		if(NC_NOERR!=(ret=nc_def_var(ctx.ncid,VAR_OBS,NC_DOUBLE,2,pdr->dim2,&varid)))
	    	    nc_error(ret,__LINE__);
	    }
	    free(pstrFt);
	}
    }else if(0==strncmp(VAR_Z,var,strlen(VAR_Z))){
	if(NC_NOERR!=(ret=nc_def_var(ctx.ncid,VAR_Z,NC_DOUBLE,1,pdr->dim1z,&varid)))
	    nc_error(ret,__LINE__);
    }else{
	t=query1DVarType(ctx.L,NULL,VALUE);
	if(t==LUA_TNUMBER)t=NC_DOUBLE;
	else if(t==LUA_TSTRING)t=NC_STRING;

	if(t==NC_DOUBLE||t==NC_STRING){
	    if(NC_NOERR!=(ret=nc_def_var(ctx.ncid,var,t,1,pdr->dim1,&varid)))
	        nc_error(ret,__LINE__);
 	}
    }
    if(varid!=-1){
	ctx.pfunCallback=populateAttrs;
	ctx.attrscope=varid;
	loopTable(&ctx,VAR_ATTRIBUTES,0);
    }
}
size_t start1[1]={0};
size_t count1[1];
size_t start2[2]={0,0};
size_t count2[2];
size_t start3[3]={0,0,0};
size_t count3[3];
static void putVars(Context ctx,const char *var,void *value,int type,int elems){
    int ret,t,varid=-1;
    double *pdVal=NULL;
    char *pstrVal=NULL;
    DimRef *pdr=NULL;
    size_t dlen=1;
    if(ctx.ncid<=0||!ctx.param||!var)return;
    pdr=(DimRef*)ctx.param;
    if(0==strncmp(VAR_TIME,var,strlen(VAR_TIME))&&ctx.time){
	if(NC_NOERR!=(ret=nc_inq_varid(ctx.ncid,VAR_TIME,&varid)))
	   nc_error(ret,__LINE__);
	count1[0]=ctx.len;
	nc_put_vara_double(ctx.ncid,varid,start1,count1,ctx.time);
    }else if(0==strncmp(VAR_OBS,var,strlen(VAR_OBS))&&ctx.obs){
	if(NC_NOERR!=(ret=nc_inq_dimlen(ctx.ncid,pdr->dim1z[0],&dlen)))
	    nc_error(ret,__LINE__);
	if(NC_NOERR!=(ret=nc_inq_varid(ctx.ncid,VAR_OBS,&varid)))
	    nc_error(ret,__LINE__);
	if(1==dlen){
	    count2[0]=1;count2[1]=ctx.len;
	    nc_put_vara_double(ctx.ncid,varid,start2,count2,ctx.obs);
	}else{
	    count3[0]=1;count3[1]=ctx.len;count3[2]=dlen;
	    nc_put_vara_double(ctx.ncid,varid,start3,count3,ctx.obs);
	}
    }else if(0==strncmp(VAR_Z,var,strlen(VAR_Z))){
	if(NC_NOERR!=(ret=nc_inq_varid(ctx.ncid,VAR_Z,&varid)))
	    nc_error(ret,__LINE__);
	if(NC_NOERR!=(ret=nc_inq_dimlen(ctx.ncid,pdr->dim1z[0],&dlen)))
	    nc_error(ret,__LINE__);
	if(1==dlen){
	    pdVal=(double*)queryTableValue(ctx.L,NULL,NULL,VALUE);
	    if(pdVal){
		nc_put_var_double(ctx.ncid,varid,pdVal);
		free(pdVal);
	    }
	}else if(ctx.height){
	    nc_put_var_double(ctx.ncid,varid,ctx.height);
	    free(pdVal);
	}
    }else{
	t=query1DVarType(ctx.L,NULL,VALUE);
	if(t==LUA_TSTRING){
	    pstrVal=(char*)queryTableValue(ctx.L,NULL,NULL,VALUE);
	    if(pstrVal){
		if(NC_NOERR!=(ret=nc_inq_varid(ctx.ncid,var,&varid)))
	   	    nc_error(ret,__LINE__);
		nc_put_var_string(ctx.ncid,varid,(const char**)&pstrVal);
		free(pstrVal);
	    }
	}
	else if(t==LUA_TNUMBER){
	    pdVal=(double*)queryTableValue(ctx.L,NULL,NULL,VALUE);
	    if(pdVal){
		if(NC_NOERR!=(ret=nc_inq_varid(ctx.ncid,var,&varid)))
	   	    nc_error(ret,__LINE__);
		nc_put_var_double(ctx.ncid,varid,pdVal);
		free(pdVal);
	    }
	}
    }
}
#define Abs(x)    ((x) < 0 ? -(x) : (x))
#define Max(a, b) ((a) > (b) ? (a) : (b))
#define TOLERANCE 0.000001
static double RelDif(double a,double b){
	double c = Abs(a);
	double d = Abs(b);
	d = Max(c, d);
	return d == 0.0 ? 0.0 : Abs(a - b) / d;
}
static int compareDoubles(const void *v1,const void *v2){
    double d1=(double)*v1;
    double d2=(double)*v2;
    if(d1<d2)return -1;
    else if(d1>d2)return 1;
    else if(RedDif(d1,d2)<=TOLERANCE)return 0;
}
static void populateVars(Context ctx,const char *key,void *value,int type,int elems){
    int ret,ncgid,cnt;
    DimRef dr;
    double *pdDepth=NULL;
    char *pstrSource=NULL;
    size_t dep,*basei=NULL,*endi=NULL;
    size_t i=0;
    if(ctx.ncid<=0)return;
    if(type==LUA_TTABLE){//any other type at this level will be disgarded
	if(NC_NOERR!=(ret=nc_def_grp(ctx.ncid,key,&ncgid)))
	    nc_error(ret,__LINE__);
	ctx.ncid=ncgid;
	ctx.pfunCallback=populateAttrs;
	loopTable(&ctx,NULL,0);
	if(NC_NOERR!=(ret=nc_def_dim(ctx.ncid,VAR_FEATURE_TYPE_INSTANCE,1,&(dr.dim1[0]))))
	    nc_error(ret,__LINE__);
	if(NC_NOERR!=(ret=nc_def_dim(ctx.ncid,VAR_TIME,NC_UNLIMITED,&(dr.dim2[1]))))
	    nc_error(ret,__LINE__);
	pdDepth=(double*)queryTableValue(ctx.L,LUA_VARS_ROOT,VAR_Z,DEPTH_LEVEL);
	if(pdDepth){
	    dep=(size_t)*pdDepth;
            if(NC_NOERR!=(ret=nc_def_dim(ctx.ncid,VAR_Z,dep,&(dr.dim3[2]))))
	        nc_error(ret,__LINE__);
	    free(pdDepth);
	}
	dr.dim2[0]=dr.dim3[0]=dr.dim1[0];
	dr.dim1t[0]=dr.dim3[1]=dr.dim2[1];
	dr.dim1z[0]=dr.dim3[2];
	ctx.pfunCallback=defineVars;
	ctx.param=(void*)&dr;
	loopTable(&ctx,LUA_VARS_ROOT,0);
	pstrSource=(char*)queryTableValue(ctx.L,LUA_VARS_ROOT,NULL,DATA_SOURCE);
	if(pstrSource&&dep>0){
	    pdDepth=(double*)queryTableValue(ctx.L,LUA_VARS_ROOT,VAR_META,OBS_COUNT);
	    if(dep>1){
		basei=(size_t*)queryTableValue(ctx.L,LUA_VARS_ROOT,VAR_META,TS_MIN);
		endi=(size_t*)queryTableValue(ctx.L,LUA_VARS_ROOT,VAR_META,TS_MAX);
	    }
	    if(pdDepth){//dpDepth represents tscount here
		cnt=(int)(*pdDepth)*dep;
		free(pdDepth);
		ctx.obs=(double*)malloc(sizeof(double)*cnt);
		ctx.time=(double*)malloc(sizeof(double)*cnt);
		FILE *f=fopen(pstrSource,"r");
    		if(f){
		    double f1,f2,f3;
		    int ret=2;
		    if(1==dep){
		        while(!feof(f)&&ret==2&&cnt--){
			    ret=fscanf(f,"%lf,%lf",&f1,&f2);
	    		    ctx.time[i]=f1;
			    ctx.obs[i++]=f2;
			}
		    }else{
			if(basei&&endi&&*endi>=*basei){
			    double *pdKey=(double*)malloc(sizeof(double)*(*pdDepth));
			    ctx.height=(double*)malloc(sizeof(double)*cnt);
			    if(populateArray(ctx.L,VAR_Z,DEPTHS,LUA_TNUMBER,ctx.height,cnt)){
				qsort(ctx.height,dep,sizeof(double),compareDoubles);
				for(;i<cnt;++i)
				    *(ctx.obs+i)=ctx.missingval;
			        while(!feof(f)&&ret==2&&cnt--){
			            ret=fscanf(f,"%lf,%lf,%lf",&f1,&f2,&f3);
				    bsearch(f3,ctx.height,dep,sizeof(double),compareDoubles);
	    		            ctx.time[i]=f1;
			            ctx.obs[i++]=f2;
			        }
			    }
			    else{
				free(ctx.height,dep,sizeof(double),);
			  	ctx.height=NULL;
			    }
			    if(pdKey)free(pdKey);
			}
			else{
			    free(ctx.obs);
			    free(ctx.time);
			    ctx.obs=ctx.time=NULL;
			}
			
		    }
		    fclose(f);
		    ctx.pfunCallback=putVars;
		    ctx.len=i;
	            loopTable(&ctx,LUA_VARS_ROOT,0);
    		}
		if(basei)free(basei);
		if(endi)free(endi);
		if(ctx.height)free(ctx.height);
		if(ctx.time)free(ctx.time);
		if(ctx.obs)free(ctx.obs);
		free(pstrSource);
	    }
	}
    }
}
int main(int argc,char **argv){
    if(argc<3)return -1;
    Context ctx={-1,-1,0,-9999.0,NULL,NULL,NULL,NULL,NULL,NULL};
    int ret;
    
    ctx.L=lua_open();
    luaL_openlibs(ctx.L);
    luaL_dofile(ctx.L,LUA_COMMON_FILE);
    lua_getglobal(ctx.L,LUA_COMMON_FUN);
    luaL_dofile(ctx.L,argv[1]);
    lua_getglobal(ctx.L,"Platform");
    if(!lua_istable(ctx.L,-1))return LUA_ERRCODE;
    lua_pcall(ctx.L,1,1,0);
    
    if(NC_NOERR!=(ret=nc_create(argv[2],NC_NOCLOBBER|NC_CLASSIC_MODEL,&(ctx.ncid))))
	nc_error(ret,__LINE__);
    ctx.pfunCallback=populateAttrs;
    ctx.attrscope=NC_GLOBAL;
    loopTable(&ctx,pstrGlobalAttributes,0);
    if(NC_NOERR!=(nc_get_att_double(ctx.ncid,ctx.attrscope,MISSING_VALUE,&(ctx.missingval))))
	ctx.missingval=-9999.0;
    ctx.pfunCallback=populateVars;
    loopTable(&ctx,LUA_NC_ROOT,0);
    lua_close(ctx.L);
    if(NC_NOERR!=(ret=nc_close(ctx.ncid)))
    	nc_error(ret,__LINE__);
    return 0;
}

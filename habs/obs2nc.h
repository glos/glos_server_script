#include <stdio.h>
#include <string.h>
#include <stdlib.h>
#include "lua.h"
#include "lualib.h"
#include "lauxlib.h"
#include "netcdf.h"

#define BINARY_NAME "habs2nc"
#define BINARY_NAME_LEN 6
#define LUA_COMMON_FILE "common.lua"
#define LUA_COMMON_FUN "initPlatformInfo"
#define LUA_NC_ROOT "NCs"
#define LUA_VARS_ROOT "vars"
#define VAR_TIME "time"
#define VAR_OBS "obs"
#define VAR_NAME "varname"
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
#define FEATURE_TYPE "featureType"

#define TIME_SERIES "timeSeries"
#define TIME_SERIES_PROFILE "timeSeriesProfile"

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


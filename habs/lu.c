#include "lua.h"
#include "lualib.h"
#include "lauxlib.h"
#include <stdio.h>


int main(int argc,char **argv)
{
    if(argc<2)return -1;
    lua_State *L=lua_open();
    luaL_openlibs(L);
    luaL_dofile(L,argv[1]);
    lua_getglobal(L,"foobar");
    lua_pushnumber(L,1);
    lua_pushnumber(L,2);
    lua_call(L,2,1);
    printf("%d\n",(int)lua_tointeger(L,-1));
    lua_settop(L,0);
    lua_getglobal(L,"mytable");
    if(lua_istable(L,-1))
    {
        lua_pushstring(L,"r");
	printf("%d elem on stack\n",lua_gettop(L));
	lua_gettable(L,-2);
	printf("%d elem on stack\n",lua_gettop(L));
	if(lua_isnumber(L,-1))
	    printf("r=%f\n",(double)lua_tonumber(L,-1));
	printf("%d elem on stack\n",lua_gettop(L));
	lua_pop(L,1);
	lua_pushnil(L);
	while(lua_next(L,-2)){
	    printf("%s--%s\n",lua_typename(L,lua_type(L,-2)),lua_typename(L,lua_type(L,-1)));
	    lua_pop(L,1);
	}
    }
    lua_settop(L,0);
    lua_getglobal(L,"myarray");
    if(lua_istable(L,-1))
    {
	printf("size of myarray is:%d\n",luaL_getn(L,-1));
    }
    lua_close(L);
    return 0;
}

objs=obs2nc.o
CFLAGS=-g -Wall
INCLUDE=-I/usr/local/include
LDPATH=-L/usr/local/lib64
LDFLAGS=-llua -lnetcdf

obs2nc:$(objs)
	gcc $(objs) $(CFLAGS) $(LDPATH) $(LDFLAGS) -o $@
.c.o:
	gcc -c $< $(CFLAGS) 



#obs2nc:obs2nc.c
#	gcc $< $(CFLAGS) $(INCLUDE) $(LDPATH) $(LDFLAGS) -o $@

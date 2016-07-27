import sys
import os
import datetime
import importlib
import copy

_program="/usr/local/glos/habs/habs2nc"
_output="/usr/local/glos/habs/tmp"
_remotedir="/var/local/glos/storage/HABS/lakes"
#/var/local/glos/storage/HABS/lakes/sea_surface_water_temperature/tollsps/2015
_scp="/usr/bin/scp {0}/{1} $USER@192.168.76.104:{2}/{3}/{4}/{5}/{6}.nc"
_connStr="host='192.168.76.104' dbname='glos_obs' user='glos_obs_reader' password='U285gcM'"
_parameters={
        "sea_surface_water_temperature":"",
        "ph":"",
        "water_conductivity":"",
        "ysi_chlorophyll":"",
        "ysi_blue_green_algae":"",
        "ysi_turbidity":"",
	"dissolved_oxygen_saturation":"",
	"dissolved_oxygen":""
        }
def main(argv):
    sys.path.append(argv[1])
    #make sure trailling slash is added
    output=os.path.join(_output,"")
    dir=[di for di in os.listdir(argv[1]) if os.path.isdir(os.path.join(argv[1],di))]
    now=datetime.datetime.now()
    if 4 == len(argv):
	now=datetime.datetime.strptime(argv[3],"%Y-%m-%d")
    year=now.year
    doy=now.strftime("%j")
    now=now.strftime("%Y-%m-%d")
    for d in dir:
        try:
            m=importlib.import_module("{0}.{1}".format(d,d))#("{0}.{1}.{2}".format(argv[1],d,d))
            print("Fetch data for:{0} at {1}".format(d,now))
        except:
            print("Failed to load module at {0}".format(os.path.join(argv[1],d)))
            raise
        ncs=m.fetch(_connStr,now,copy.deepcopy(_parameters))
        conf=os.path.join(argv[2],d)
        if os.path.isfile(conf):
            os.system("{0} {1}".format(_program,conf))
            for nc in _parameters:
                if ncs[nc]:
                    #print _scp.format(_output,ncs[nc],_remotedir,nc,d,year,"{0}_{1}{2}".format(os.path.splitext(ncs[nc])[0],year,doy))
                    print os.system(_scp.format(_output,ncs[nc],_remotedir,nc,d,year,"{0}_{1}{2}".format(os.path.splitext(ncs[nc])[0],year,doy)))
                print("done")
if __name__=="__main__":
    if len(sys.argv)>2:
        main(sys.argv)
    else:
        print("habs.py: A dir for platform scripts and a dir for platform-config files are required!")
    sys.exit(-1)

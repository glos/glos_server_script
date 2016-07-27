#!/usr/bin/env python
"""
USAGE:

hecwfs.py [nowcast|forecast] [filename]
"""

import os
import sys
import pika
import datetime
sys.path.insert(0, '../pyutils/')
import myftp
import mydate
import mylogging

__hecwfs_admin="gwang@glc.org"
ftp_site='ftp.glerl.noaa.gov'
ftp_user='anonymous'
ftp_pass="gwang@glc.org"
ftp_timeout=10
nowcast_path='EMF/hecwfs/NCAST'
forecast_path='EMF/hecwfs/FCAST'
nc_path_fmt="/tmp/%s"
nowcast_length=3
forecast_length=48
hecwfs_cmd_fmt="/usr/local/glos/bin/hecwfs -l -v -y {0} -m {1} -d {2} -h {3} -s {4} -e {5} -f {6} -o {7}"
nowcast_shp="/var/local/glos/hecwfs/nowcast/shp/"
forecast_shp='/var/local/glos/hecwfs/forecast/shp/'
nowcast_netcdf_fmt="/var/local/glos/hecwfs/nowcast/{0}"
forecast_netcdf_fmt="/var/local/glos/hecwfs/forecast/{0}"
shp_list="/tmp/hecwfs_shp_lst"
nowcast_selinux="/usr/bin/chcon -t httpd_sys_content_t -R %s"
nowcast_ftp_timestamp_file='hecwfs_nowcast_newest'
forecast_ftp_timestamp_file='hecwfs_forecast_newest'
pg_server="db1.glos.us"
pg_db_name="hecwfs"
pg_user="hecwfs_writer"
hecwfs_sql="/tmp/hecwfs.sql"
hecwfs_log_file='log/hecwfs.log'
_notify_error="glos_notify_data_status"
_mq_host="mon.glos.us"
_mq_routing_key="hecwfs"

def run_external_script(cmd):
    return 0 == os.system(cmd)

def hecwfs_process(logger,castType,fileName=None):
    file=None
    ffile=None
    ret=1
    ncPathFmt=nowcast_netcdf_fmt if castType == "nowcast" else forecast_netcdf_fmt
    ftp=None
    try:	
	logger.info(("Begin HECWFS {0} Data Processing...").format(castType.capitalize()))
        ftp=myftp.FTP(ftp_site,ftp_user,ftp_pass,ftp_timeout,logger)
        ftp.changeDir(nowcast_path if castType == "nowcast" else forecast_path)
        logger.debug("Connected with ftp server at %s"%ftp_site)
        if fileName is None:
            file=ftp.getNewestFile(nowcast_path if castType == "nowcast" else forecast_path)
	    ffile=file.split('.')[0]+'.hec_forcing.nc.gz'
	    file=file.split('.')[0]+'.nc.gz'
	else:
	    file=fileName
	tsfile=nowcast_ftp_timestamp_file if castType == "nowcast" else forecast_ftp_timestamp_file
        if not fileName and True==os.path.exists(tsfile):
	    with open(tsfile,'r') as newestF:
            	latestFile=newestF.readline()
                if file==latestFile:
		    logger.info("No new files on the ftp")
                    return 3
	if file:
	    logger.info("Found the newest file:{0} and {1}".format(file,ffile))
	    logger.debug("Begin to download...")
	    if True==ftp.downloadTo(file,ncPathFmt.format(file)) and True==ftp.downloadTo(ffile,ncPathFmt.format(ffile)):
		logger.info("Successfully download!")
                with open(tsfile,'w') as newestF:
                    newestF.write("{0}".format(file))
                    logger.debug("Update ftp timestamp file at: {0}".format(tsfile))       
	    else:
		logger.error("Download failed!")
		return ret
    except:
	raise
    finally:
	if ftp:
            ftp.dispose()
	    logger.debug("Close connection with FTP Server")
    logger.debug("Decompress file:{0}".format(file))
    if False == run_external_script("gzip -f -q -d {0}".format(ncPathFmt.format(file))):
	logger.error("Failed to unzip {0}".format(file))
	return ret
    logger.debug("Decompress file:{0}".format(ffile))
    if False == run_external_script("gzip -f -q -d {0}".format(ncPathFmt.format(ffile))):
	logger.error("Failed to unzip {0}".format(ffile))
        return ret
    nctxt=os.path.splitext(file)[0]
    year=int(nctxt[:4])
    dayofyear=int(nctxt[4:7])
    hour=int(nctxt[7:9])
    file=ncPathFmt.format(nctxt)
    ffile=ncPathFmt.format(os.path.splitext(ffile)[0])
    ymd=mydate.getYearMonthDay(dayofyear,year)
    if ymd is None:
	raise Exception("Can not parse date and time")
    ts=datetime.datetime(ymd[0],ymd[1],ymd[2],hour)
    tstart=ts+datetime.timedelta(hours=1)
    tsend=ts+datetime.timedelta(hours=3)
    castLen=nowcast_length if castType == "nowcast" else forecast_length
    logger.info("Begin to parse data at: {0}-{1}-{2}-{3}:00:00 -- {4}:00:00".format(ymd[0],ymd[1],ymd[2],hour+1,hour+castLen))
    shp_path=nowcast_shp if castType == "nowcast" else forecast_shp
    if False == run_external_script(hecwfs_cmd_fmt.format(ymd[0],ymd[1],ymd[2],hour,0,castLen,file,shp_path)):
	logger.error("Failed to convert netcdf to shp: {0}".format(file))
        return ret
    logger.debug("Parse data end")
    if os.path.exists(shp_list):
        with open(shp_list,"r") as shpList:
	     shps=shpList.readlines()
	     shp_stem=None
	     redirect='>'
	     for shp in shps:
                shp=shp[:-1]
                if False == run_external_script("shp2pgsql -s 26990 -a {0} public.{1} {2} {3}".format(shp,castType,redirect,hecwfs_sql)):
		    logger.error("Failed to shp2pgsql {0}".format(shp))
        	    return ret
		redirect='>>'
                #tar the shp files in the nowcast repos
                shp_stem=os.path.splitext(os.path.basename(shp))[0]
                if False == run_external_script("tar -czf {0}{1}_{2}.tar.gz -C {0} {1}.shp {1}.shx {1}.dbf {1}.prj".format(shp_path,shp_stem,castType)):
		    logger.error("Failed to tar {0}".format(shp_stem))
        	    return ret
                if False == run_external_script("rm {0}{1}.*".format(shp_path,shp_stem)):
		    logger.error("Failed to remove {0}".format(shp_stem))
		    ret=4
	logger.info("Create sql file")
    	run_external_script("psql -h {0} -d {1} -U {2} -f {3}".format(pg_server,pg_db_name,pg_user,hecwfs_sql));
    	logger.info("Import sql file to the db")
    	run_external_script("rm {0}".format(hecwfs_sql))
    	if 1 == ret and True == run_external_script('psql -h {0} -d {1} -U {2} -c "delete from {3} where current_timestamp-interval \'48 hours\'>timestamp;"'.format(pg_server,pg_db_name,pg_user,castType)):
    	    ret=0
	logger.debug("Delete records with timestamp more than 48 hours")
    	#in order to do vacuum, hecwfs_writer must be the owner of the hecwfs_nowcast
    	run_external_script('psql -h {0} -d {1} -U {2} -c "vacuum analyze {3}"'.format(pg_server,pg_db_name,pg_user,castType))
    	logger.debug("Vacuum table to rebuild index")
    	logger.info("End HECWFS Data Processing")
    else:
	run_external_script("rm {0}{1}.*".format(shp_path,shp_stem))
       	raise Exception("Can not read from shp list file")
    return ret

def enqueue(message,logger):
    connection=None
    try:
        connection=pika.BlockingConnection(pika.ConnectionParameters(host=_mq_host))
        channel=connection.channel()
        channel.exchange_declare(exchange=_notify_error,type='direct')
        channel.basic_publish(exchange=_notify_error,routing_key=_mq_routing_key,body=message)
        logger.info("Send message to MQ:{0}\n".format(message))
    except Exception as e:
        if connection is not None:
            connection.close()
        logger.error("Error during MQ:{0}\n".format(e))

def hecwfs_main():
    file=None;
    ret=1
    if len(sys.argv)>1:
	param=sys.argv[1].lower()
	file=None
        if len(sys.argv)>2:
            file=sys.argv[2]
	if param not in ["nowcast","forecast"]:
	    print __doc__
	    return ret
	with mylogging.Logger('harvesting.hecwfs','debug',hecwfs_log_file) as logger:
	    try:
               	logger.info('*'*32)
		ret=hecwfs_process(logger,param,file)
	        if 4 == ret:
		    enqueue("HECWFS[{0}]: {1}".format(param,"shp files were not cleaned up properly! Manual removal is required..."),logger);
	    except Exception as e:
                logger.error("Exception: {0}".format(e))
		enqueue("HECWFS[{0}]: {1}".format(param,str(e)),logger);
  	    finally:
        	logger.info('*'*32)
    else:
	print __doc__
    return ret
if __name__=="__main__":
    sys.exit(hecwfs_main())

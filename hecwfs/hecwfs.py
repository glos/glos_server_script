#!/usr/bin/env python
"""
USAGE:

hecwfs.py [nowcast|forecast] [filename]
"""

import os
import sys
import urllib2
import socket
import pika
import datetime
sys.path.insert(0, '../pyutils/')
import mydate
import mylogging

_hecwfs_admin = "dmac@glos.us"
_http_site = 'http://www.glerl.noaa.gov/emf/hecwfs'
_timeout = 10
_nowcast_path = 'NCAST'
_forecast_path = 'FCAST'
_nowcast_length = 3
_forecast_length = 48
_hecwfs_cmd_fmt = "/usr/local/glos/bin/hecwfs -l -v -y {0} -m {1} -d {2} -h {3} -s {4} -e {5} -f {6} -o {7}"
_nowcast_shp = "/var/local/glos/hecwfs/nowcast/shp/"
_forecast_shp = '/var/local/glos/hecwfs/forecast/shp/'
_nowcast_netcdf_fmt = "/var/local/glos/hecwfs/nowcast"
_forecast_netcdf_fmt = "/var/local/glos/hecwfs/forecast"
_shp_list = "/tmp/hecwfs_shp_lst"
_nowcast_selinux = "/usr/bin/chcon -t httpd_sys_content_t -R %s"
_nowcast_timestamp_file = 'hecwfs_nowcast_newest'
_forecast_timestamp_file = 'hecwfs_forecast_newest'
_pg_server = "db1.glos.us"
_pg_db_name = "hecwfs"
_pg_user = "hecwfs_writer"
_hecwfs_sql = "/tmp/hecwfs.sql"
_hecwfs_log_file = 'log/hecwfs.log'
_notify_error = "glos_notify_data_status"
_mq_host = "mon.glos.us"
_mq_routing_key = "hecwfs"

def run_external_script(cmd):
    return 0 == os.system(cmd)

def hecwfs_process(logger, castType, hourMask):
    file = None
    ffile = None
    lfile = ""
    ret = 1
    ncPathFmt = _nowcast_netcdf_fmt if castType == "nowcast" else _forecast_netcdf_fmt
    rpath = _nowcast_path if castType == "nowcast" else _forecast_path
    ncf = None
    today = datetime.datetime.utcnow()
    doy = int(today.strftime('%j'))
    # This is ugly! DOY actually changed for the last harvest
    if hourMask == "21":
        doy -= 1
    ts = '{0}{1}{2}'.format(today.strftime('%Y'), doy, hourMask)
    try:	
        logger.info(("Begin HECWFS {0} Data Processing...").format(castType.capitalize()))
        tsfile = _nowcast_timestamp_file if castType == "nowcast" else _forecast_timestamp_file
        if True == os.path.exists(tsfile):
            with open(tsfile, 'r') as newestF:
                latestTS = newestF.readline()
                if long(ts) <= long(latestTS):
                    logger.info("This file with time stamp {0} might be downloaded already.".format(ts))
                    return 3
        ffile = '{0}.hec_forcing.nc.gz'.format(ts)
        file = '{0}.nc.gz'.format(ts)
        ncf = urllib2.urlopen("{0}/{1}/{2}".format(_http_site, rpath, ffile), timeout = _timeout)
        logger.debug("Connected with HTTP server at {0}".format(_http_site))
        lfile = "{0}/{1}".format(ncPathFmt, ffile)
        with open(lfile, "wb") as local_file:
           local_file.write(ncf.read())
           logger.debug("Download of {0} is done!".format(lfile))
        ncf.close()

        nfc = None
        ncf = urllib2.urlopen("{0}/{1}/{2}".format(_http_site, rpath, file), timeout = _timeout)
        logger.debug("Connected with HTTP server at {0}".format(_http_site))
        lfile = "{0}/{1}".format(ncPathFmt, file)
        with open(lfile, "wb") as local_file:
           local_file.write(ncf.read())
           logger.debug("Download of {0} is done!".format(lfile))
        with open(tsfile, 'w') as newestF:
            newestF.write("{0}".format(ts))
            logger.debug("Update timestamp file at: {0}".format(tsfile))
    except urllib2.HTTPError as e:
        logger.error("HTTP status code: {0}. If 404, {1} may not be posted yet".format(e.code, lfile))
        return 2
    except socket.timeout as te:
        logger.error("Timeout on download {0}".format(lfile))
        return 3
    finally:
        if ncf:
            ncf.close()
    logger.debug("Decompress file:{0}".format(file))
    if False == run_external_script("gzip -f -q -d {0}/{1}".format(ncPathFmt, file)):
        logger.error("Failed to unzip {0}".format(file))
        return ret
    logger.debug("Decompress file:{0}".format(ffile))
    if False == run_external_script("gzip -f -q -d {0}/{1}".format(ncPathFmt, ffile)):
        logger.error("Failed to unzip {0}".format(ffile))
        return ret

    year = int(ts[:4])
    dayofyear = int(ts[4:7])
    hour = int(ts[7:9])
    file = "{0}/{1}".format(ncPathFmt, file[: -3])
    ymd = mydate.getYearMonthDay(dayofyear, year)
    if ymd is None:
        raise Exception("Can not parse date and time")
    ts = datetime.datetime(ymd[0], ymd[1], ymd[2], hour)
    castLen = _nowcast_length if castType == "nowcast" else _forecast_length
    logger.info("Begin to parse data at: {0}-{1}-{2}-{3}:00:00 -- {4}:00:00".format(ymd[0], ymd[1], ymd[2], hour + 1, hour + castLen))
    shp_path = _nowcast_shp if castType == "nowcast" else _forecast_shp
    if False == run_external_script(_hecwfs_cmd_fmt.format(ymd[0], ymd[1], ymd[2], hour, 0, castLen, file, shp_path)):
        logger.error("Failed to convert netcdf to shp: {0}".format(file))
        return ret
    logger.debug("Parse data end")
    n = datetime.datetime.now()
    dt = mydate.getDateTimePlus(ymd[0], ymd[1], ymd[2], hour, 0, 0, castLen)
    os.system(
        "echo 'HECWFS16988|%s|%d-%02d-%02d %02d:00:00|%d-%02d-%02d %02d:00:00|%d-%02d-%02d %02d:%02d:%02d' | nc 192.168.76.104 16988" % (castType.lower(), ymd[0], ymd[1], ymd[2], hour + 1, dt.year, dt.month, dt.day, dt.hour, n.year, n.month, n.day, n.hour, n.minute, n.second))
    if os.path.exists(_shp_list):
        with open(_shp_list,"r") as shpList:
            shps = shpList.readlines()
            shp_stem = None
            redirect = '>'
            for shp in shps:
                shp = shp[: -1]
                if False == run_external_script("shp2pgsql -s 26990 -a {0} public.{1} {2} {3}".format(shp, castType, redirect, _hecwfs_sql)):
                    logger.error("Failed to shp2pgsql {0}".format(shp))
                    return ret
                redirect='>>'
                #tar the shp files in the nowcast repos
                shp_stem = os.path.splitext(os.path.basename(shp))[0]
                if False == run_external_script("tar -czf {0}{1}_{2}.tar.gz -C {0} {1}.shp {1}.shx {1}.dbf {1}.prj".format(shp_path, shp_stem, castType)):
                    logger.error("Failed to tar {0}".format(shp_stem))
                    return ret
                if False == run_external_script("rm {0}{1}.*".format(shp_path, shp_stem)):
                    logger.error("Failed to remove {0}".format(shp_stem))
                    ret=4
        logger.info("Create sql file")
        run_external_script("psql -h {0} -d {1} -U {2} -f {3}".format(_pg_server, _pg_db_name, _pg_user, _hecwfs_sql))
        logger.info("Import sql file to the db")
        run_external_script("rm {0}".format(_hecwfs_sql))
        if 1 == ret and True == run_external_script('psql -h {0} -d {1} -U {2} -c "delete from {3} where current_timestamp-interval \'48 hours\'>timestamp;"'.format(_pg_server, _pg_db_name, _pg_user,castType)):
            ret=0
        logger.debug("Delete records with timestamp more than 48 hours")
        #in order to do vacuum, hecwfs_writer must be the owner of the hecwfs_nowcast
    	run_external_script('psql -h {0} -d {1} -U {2} -c "vacuum analyze {3}"'.format(_pg_server, _pg_db_name, _pg_user,castType))
        logger.debug("Vacuum table to rebuild index")
        logger.info("End HECWFS Data Processing")
    else:
        #run_external_script("rm {0}{1}.*".format(shp_path, shp_stem))
        raise Exception("Can not read from shp list file")
    return ret

def enqueue(message,logger):
    connection=None
    try:
        connection = pika.BlockingConnection(pika.ConnectionParameters(host = _mq_host))
        channel = connection.channel()
        channel.exchange_declare(exchange = _notify_error, type = 'direct')
        channel.basic_publish(exchange = _notify_error, routing_key = _mq_routing_key, body = message)
        logger.info("Send message to MQ:{0}\n".format(message))
    except Exception as e:
        if connection is not None:
            connection.close()
        logger.error("Error during MQ:{0}\n".format(e))

def hecwfs_main():
    file = None
    ret = 1
    if len(sys.argv) > 1:
        param = sys.argv[1].lower()
        file = None
        if len(sys.argv) > 2:
            file = sys.argv[2]
        if param not in ["nowcast", "forecast"]:
            print __doc__
            return ret
        with mylogging.Logger('harvesting.hecwfs', 'debug', _hecwfs_log_file) as logger:
            try:
                logger.info('*' * 32)
                ret = hecwfs_process(logger, param, file)
                if 4 == ret:
                    enqueue("HECWFS[{0}]: {1}".format(param, "shp files were not cleaned up properly! Manual removal is required..."), logger)
            except Exception as e:
                logger.error("Exception: {0}".format(e))
                enqueue("HECWFS[{0}]: {1}".format(param, str(e)), logger)
            finally:
                logger.info('*' * 32)
    else:
        print __doc__
    return ret

if __name__=="__main__":
    sys.exit(hecwfs_main())

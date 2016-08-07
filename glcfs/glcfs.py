#!/usr/bin/env python
"""
USAGE:

python glcfs.py local_repos [nowcast|forecast] [timestamp]
"""
import os
import sys
import urllib2
import socket
import pika
sys.path.insert(0, '../pyutils/')
import mylogging
import mydate
from datetime import datetime

_glcfs_admin="dmac@glos.org"
_http_site="http://www.glerl.noaa.gov/emf/glcfs/netcdf"
_timeout=10
_nowcast_path='NCAST'
_forecast_path='FCAST'
_nowcast_ftp_timestamp_file="glcfs_nowcast_newest"
_forecast_ftp_timestamp_file="glcfs_forecast_newest"
_glcfs_log_file="log/glcfs.log"
_glcfs_nowcast_path_fmt='/tmp/{0}'#"/var/local/glos/glcfs/nowcast/{0}"
_glcfs_forecast_path_fmt='/tmp/{0}'#"/var/local/glos/glcfs/forecast/{0}"
_storage_nowcast_path=None#"/var/local/glos/storage/temp"
_storage_forecast_path=None#"/var/local/glos/storage/temp"
#_web_status_path="/var/www/html/data.glos/status/wtf.sqlite3"
_notify_error="glos_notify_data_status"
_mq_host="mon.glos.us"
_mq_routing_key="glcfs"


def enqueue(message,logger):
    connection = None
    try:
        connection = pika.BlockingConnection(pika.ConnectionParameters(host=_mq_host))
        channel = connection.channel()
        channel.exchange_declare(exchange=_notify_error,type='direct')
        channel.basic_publish(exchange=_notify_error,routing_key=_mq_routing_key,body=message)
        logger.info("Send message to MQ:{0}\n".format(message))
    except Exception as e:
        if connection is not None:
            connection.close()
        logger.error("Error during MQ:{0}\n".format(e))

def glcfs_common(fpath, rpath, timestamp, logger):
    logger.info("Try newest files with the timestamp:{0}".format(timestamp))
    logger.debug("Connecting with HTTP server:{0}".format(_http_site))
    #for fmt in ['m%s.in1.nc','m%s.out1.nc','m%s.out3.nc','e%s.in1.nc','e%s.out1.nc','e%s.out3.nc','h%s.in1.nc','h%s.out1.nc','h%s.out3.nc','o%s.in1.nc','o%s.out1.nc','o%s.out3.nc','s%s.in1.nc','s%s.out1.nc','s%s.out3.nc']:
    ret = 0
    for fmt in ['m{0}.in1.nc.gz','m{0}.out1.nc.gz','m{0}.out3.nc.gz','e{0}.in1.nc.gz','e{0}.out1.nc.gz','e{0}.out3.nc.gz','h{0}.in1.nc.gz','h{0}.out1.nc.gz','h{0}.out3.nc.gz','o{0}.in1.nc.gz','o{0}.out1.nc.gz','o{0}.out3.nc.gz','s{0}.in1.nc.gz','s{0}.out1.nc.gz','s{0}.out3.nc.gz','c{0}.in1.nc.gz','c{0}.out1.nc.gz']:
        filename = fmt.format(timestamp)
        logger.debug("Begin to download {0}".format(filename))
        lfile = fpath.format(filename)
        ncf = None
        try:
            ncf = urllib2.urlopen("{0}/{1}/{2}".format(_http_site, rpath, filename), timeout = _timeout)
            with open(lfile, "wb") as local_file:
                local_file.write(ncf.read())
            logger.debug("Download of {0} is done!".format(filename))
        except urllib2.HTTPError as e:
            ret = 2
            logger.error("HTTP status code: {0}. If 404, {1} may not be posted yet".format(e.code, filename))
        except socket.timeout as te:
            ret = 1
            logger.error("Timeout on download {0}".format(filename))
        finally:
            if ncf:
                ncf.close()
    return ret

def glcfs(castType, logger, ts):
    if castType is None or logger is None or ts is None:
        return 1
    ret = 0
    try:
        if castType.lower() == 'nowcast':
            logger.info("Begin GLCFS Nowcast Data Processing...")
            ldir = _glcfs_nowcast_path_fmt
            rdir = _nowcast_path
            tfile = _nowcast_ftp_timestamp_file
        else:
            logger.info("Begin GLCFS Forecast Data Processing...")
            rdir = _forecast_path
            ldir = _glcfs_forecast_path_fmt
            tfile = _forecast_ftp_timestamp_file
        today = datetime.utcnow()
        newts = "{0}{1}{2}".format(today.strftime('%Y'), today.strftime('%j'), ts)
        if os.path.exists(tfile):
            with open(tfile, 'r') as f:
                t = f.readline()[0 : -1]
                if t and long(t) >= long(newts):
                    logger.info("This file with time stamp {0} might be downloaded already.".format(newts))
                    return 2
        ret = glcfs_common(ldir, rdir, newts, logger)
        #logger.debug('Return from glcfs_coommon is: {0}'.format(ret))
        if 0 != ret:
            return ret
        with open(tfile,'w') as f:
            f.write('{0}\n'.format(newts))
            logger.debug('Update ftp timestamp file at:{0}'.format(tfile))
        #ugly posting for status page
        year = int(newts[:4])
        dayofyear = int(newts[4:7])
        hour = int(newts[-2:])
        ymd = mydate.getYearMonthDay(dayofyear, year)
        mhour = 6 if castType.lower() == 'nowcast' else 12
        dt = mydate.getDateTimePlus(ymd[0], ymd[1], ymd[2], hour, 0, 0, mhour)
        n=datetime.now()
        os.system("echo 'GLCFS16989|%s|%d-%02d-%02d %02d:00:00|%d-%02d-%02d %02d:00:00|%d-%02d-%02d %02d:%02d:%02d' | nc 192.168.76.104 16989"%(castType.lower(),ymd[0],ymd[1],ymd[2],hour+1,dt.year,dt.month,dt.day,dt.hour,n.year,n.month,n.day,n.hour,n.minute,n.second))
    except:
        raise
    finally:
        pass
    return ret

if __name__ == "__main__":
    ncpkg = None
    if len(sys.argv) < 4:
        print __doc__
        sys.exit(1)
    lrepos = sys.argv[1]
    if lrepos[-1] == '/':
        lrepos = lrepos[:-1]
    _glcfs_nowcast_path_fmt = "{0}/{1}".format(lrepos,'{0}')
    _glcfs_forecast_path_fmt = _glcfs_nowcast_path_fmt
    if sys.argv[2].lower() not in ["nowcast","forecast"]:
        print __doc__
        sys.exit(1)
    ncpkg = sys.argv[3]
    ret = 1
    with mylogging.Logger('harvesting.glcfs','debug',_glcfs_log_file) as logger:
        try:
            logger.info('*' * 32)
            ret = glcfs(sys.argv[2].lower(),logger,ncpkg)
            if ret not in (0, 2):#-2 means no new file, 0 means ok
                enqueue("GLCFS[{0}]: Possible download error from HTTP server.".format(sys.argv[2].lower()),logger)
                #enqueue("GLCFS[{0}]: No new file was found on FTP. Potential delay on data provider end.".format(sys.argv[2].lower()),logger)
        except Exception as e:
            logger.error(str(e))
            enqueue("GLCFS[{0}]: {1}".format(sys.argv[2].lower(),str(e)),logger)
        finally:
            logger.info('*' * 32)
    sys.exit(ret)

#!/usr/bin/env python
"""
USAGE:

"""
import errno
from socket import error as socket_error
from socket import timeout as socket_timeout
import ftplib
import time

FTP_MAX_SLEEP=64

class FTP:
	def __init__(self,host,user,passwd,timeout=0,logger=None):
		if host is None or user is None or passwd is None:
			raise Exception('FTP:parameters can  not be null');
		sec=1
		while sec<=FTP_MAX_SLEEP:
		    try:
			self.__ftp=ftplib.FTP(host,user,passwd,timeout)
			break;
		    except (ftplib.error_temp,socket_error,socket_timeout) as err:
			if err is ftplib.error_temp or err.errno in (errno.ECONNREFUSED,errno.ETIMEDOUT,errno.ENETRESET,errno.ECONNABORTED,errno.ECONNRESET):
			    if logger:
				logger.error("FTP connection failed, may reconnect")
			    if sec<FTP_MAX_SLEEP/2:
				time.sleep(sec);
			    sec<<=1
			else:
			    raise err
		if self.__ftp is None:
			if logger:
			    logger.error('Failed all FTP connection attempts')
			raise Exception('FTP:can not create ftp instance')
	def dispose(self):
		if self.__ftp is not None:
			self.__ftp.quit()
	def changeDir(self,dir):
		if not self.__ftp is None and not dir is None:
			self.__ftp.cwd(dir)
			self.__dir=dir
	def getNewestFile(self,dir):
		timestamp=-1
                candidate=None
		if not self.__ftp is None and not dir is None:
			files=self.getFileList(dir)
			for f in files:
				temp=int(self.__ftp.sendcmd('mdtm %s'%f).split(None,2)[-1])
				if timestamp<temp:
					timestamp=temp
					candidate=f
		return candidate
	def downloadTo(self,file,path):
		if not self.__ftp is None and not path is None and not file is None:
			#self.__ftp.set_pasv(False)
			with open(path,'wb') as f:
			    self.__ftp.retrbinary('RETR %s'%file,f.write)
			    return True
		return False
	def getFileList(self,dir):
		if not self.__ftp is None and not dir is None:
			if dir!=self.__dir:
				self.__ftp.cwd(dir)
			fList=[]
			self.__ftp.dir('.',fList.append)
			if dir!=self.__dir:
				self.__ftp.cwd(self.__dir)
			files=[f.split(None,8)[-1] for f in fList if f.startswith('-')]
			return tuple(files)
	__dir='.'

if __name__=='__main__':
	f=FTP('127.0.0.1','anonymous','')
        foo='depot/netcdf/hecwfs/'
	result=f.getNewestFile(foo)
        print result
	f.downloadTo(result,'./%s'%result)
	f.dispose()

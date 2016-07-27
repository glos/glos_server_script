import logging
import os

_myLogLevels={}
_myLogLevels['debug']=logging.DEBUG
_myLogLevels['critical']=logging.CRITICAL
_myLogLevels['error']=logging.ERROR
_myLogLevels['warning']=logging.WARNING
_myLogLevels['info']=logging.INFO
class Logger(object):
    def _init(self,name,level,filePath,logFormat=None):
	self.logger=logging.getLogger(name)
	if level and _myLogLevels.get(level.lower()):
            self.logger.setLevel(_myLogLevels[level.lower()])
	else:
	    self.logger.setLevel(logging.INFO)
        if logFormat:
	    formatter=logging.Formatter(logFormat)
	else:
            formatter=logging.Formatter('%(asctime)s - %(name)s - %(levelname)s - %(message)s')
        if filePath:
            dirName=os.path.dirname(filePath)
            if os.path.isdir(dirName):
                fh=logging.FileHandler(filePath)
                if formatter:
                    fh.setFormatter(formatter)
                self.logger.addHandler(fh)

    def close(self):
	handlers=self.logger.handlers[:]#[:] will create a copy of the original list, so you can then modify the list in the loop
	for handler in handlers:
    	    handler.close()
    	    self.logger.removeHandler(handler)

    def __init__(self,name,level,filePath,logFormat=None):
	self._init(name,level,filePath,logFormat)
    
    def __enter__(self):
	return self

    def __exit__(self, type, value, traceback):
	self.close()

    def debug(self,msg):
	self.logger.debug(msg)

    def info(self,msg):
	self.logger.info(msg)

    def warn(self,msg):
	self.logger.warn(msg)

    def error(self,msg): 
	self.logger.error(msg)

    def critical(self,msg):
	self.logger.critical(msg)    



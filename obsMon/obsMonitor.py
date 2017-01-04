import urllib2
#import sched
import time
import json
import datetime
import pytz
import pika

PRECISION=1e-3
MAX_SLEEP=128
RUN_EVERY_5MIN=5*60
CUTOFF_SEC=3600*2
TOLERENCE_COUNT=3
_notify_error="glos_notify_data_status"
_mq_host="localhost"
_mq_routing_key="glos_obs"

class ObsContext:
    def __init__(self,id,name,lastUpdate,now):
        self.id=id;
        self.name=name
        self.previousUpdate=lastUpdate
        self.startAt=now
	self.lastNow=now
        self.interval=0
        self.count=0
        self.scount=TOLERENCE_COUNT-1

    def isStabled(self):
        return self.scount==0

    def isOffline(self,now):
        if self.count>TOLERENCE_COUNT:
	    return True
	elif self.interval==0 and (now-self.startAt).total_seconds()>CUTOFF_SEC:
            return True
	else:
            return False

    def lastUpdate(self):
        return self.previousUpdate

    def getName(self):
	return self.name

    def update(self,lastUpdate,now):
        diff=(lastUpdate-self.previousUpdate).total_seconds()
        if diff<0:
            return
        elif realDiff(diff,0.0)<PRECISION:
	    #if interval extracted from report time is set and stable
	    #compare it with the sample collecting interval: now-self.lastNow
	    if self.interval>0 and self.scount==0 and (now-self.lastNow).total_seconds()>self.interval:
                self.count+=1
	    elif self.interval==0 and (now-self.lastNow).total_seconds()>CUTOFF_SEC:
                self.count=TOLERENCE_COUNT+1
        else:
	    self.previousUpdate=lastUpdate
	    self.lastNow=now
            if realDiff(self.interval,0.0)<PRECISION:
                self.interval=diff
            else:
                if self.count>0:
                    self.count-=1
                if realDiff(self.interval,diff)<PRECISION:
                    if self.scount>0:
                        self.scount-=1
                else:
                    self.interval=diff

def realDiff(a,b):
    c=abs(a)
    d=abs(b)
    d=max(c,d)
    return 0.0 if d==0.0 else abs(a-b)/d;

def tryRead(url,logger):
    sec=1
    while sec<=MAX_SLEEP:
        try:
            response=urllib2.urlopen(url)
            break
        except Exception as e:
            logger.write("Error during connection:{0}\n".format(e))
            if sec<=MAX_SLEEP/2:
                time.sleep(sec);
            sec<<=1
    if sec<=MAX_SLEEP:
        content=response.read()
        obs=json.loads(content)
        response.close()
	return obs
    else:
	return None

def enqueue(message,logger):
    connection=None
    try:
        connection=pika.BlockingConnection(pika.ConnectionParameters(host=_mq_host))
        channel=connection.channel()
        channel.exchange_declare(exchange=_notify_error,type='direct')
        channel.basic_publish(exchange=_notify_error,routing_key=_mq_routing_key,body=message)
	logger.write("Send message to MQ:{0}\n".format(message))
    except Exception as e:
	if connection is not None:
	    connection.close()
	logger.write("Error during MQ:{0}\n".format(e))

template=[
"*****THIS IS AN AUTOMATIC EMAIL, PLEASE DO NOT REPLY*****",
"",
"**********"
]
gmt=pytz.timezone("GMT")
et=pytz.timezone("US/Eastern")
def compose(name,lastupdate,freq):
    #ts=datetime.datetime.strptime(lastupdate,"%Y-%m-%d %H:%M:%S")
    ts=gmt.localize(lastupdate)
    now=datetime.datetime.utcnow()
    timediff=(now-lastupdate).total_seconds()/60
    #make sure freq is positive
    template[1]="Buoy {0} might be offline, last report was at: {1} ({2} minutes ago) and has missed {3} reports at frequency of {4} minutes".format(name,ts.astimezone(et), round(timediff,2), round(timediff/10,0),freq/60)
    return "\n".join(template)

obsList={}
#def task(scheduler,delay,priority,callback):
def main(f):
    #with open('log/debug.log','a',0) as f:
        while True:
	    obs=tryRead("http://data.glos.us/glos_obs/platform.glos?tid=15",f)
	    if obs is None:
		continue
            now=datetime.datetime.utcnow()
	    f.write("Wake up at:{0}\n".format(now))
            for o in obs:
                lastupdate=datetime.datetime.strptime(o['lastDataUpdate'],'%Y/%m/%d %H:%M:%S')
                if obsList.get(o['id']) is not None:
		    #f.write("Update:{0}:{1}\n".format(o['id'],lastupdate))
                    obsList[o['id']].update(lastupdate,now)
                else:
                    if CUTOFF_SEC>(now-lastupdate).total_seconds():
                        obsList[o['id']]=ObsContext(o['id'],o['shortName'],lastupdate,now)
		        f.write("Add:{0}:{1}\n".format(o['id'],lastupdate))
	    cdt=[]
            for d in obsList:
                if obsList[d].isOffline(now):
                    f.write("Report:{0}:{1}:{2}:{3}\n".format(d,obsList[d].lastUpdate(),obsList[d].interval,obsList[d].lastNow))
                    cdt.append(d)
		elif not obsList[d].isStabled():
	            f.write("Not Stable:{0}:{1},{2}\n".format(d,obsList[d].lastUpdate(),obsList[d].interval))
	    for d in cdt:
		if obsList[d].isStabled():#only stablized record can trigger notification
		    enqueue(compose(obsList[d].getName(),obsList[d].lastUpdate(),obsList[d].interval),f)
                del obsList[d]
    	    time.sleep(RUN_EVERY_5MIN)
    #scheduler.enter(delay,priority,callback,(scheduler,delay,priority,callback))
    #scheduler.run()    

#scheduler = sched.scheduler(time.time, time.sleep)
#scheduler.enter(5,1,task,(scheduler,5,1,task))
#scheduler.run()

if __name__ == '__main__':
    with open('log/debug.log','a',0) as f:
        try:
            main(f)
        except Exception as e:
	    f.write("Error:{0}\n".format(e))

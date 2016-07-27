package org.glc.glos.coastwatch;

import java.util.logging.Logger;
import java.util.logging.LogManager;
import java.util.logging.Level;
import java.util.logging.FileHandler;
import java.util.logging.Formatter;
import java.util.logging.LogRecord;
import java.util.Date;
import java.util.Calendar;
import java.util.TimeZone;
import java.util.ArrayList;
import java.util.HashMap;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;

import org.glc.glos.coastwatch.domain.Station;
import org.glc.glos.coastwatch.logredirect.*;
import org.glc.glos.coastwatch.SqlHelper.Record;

public class PlatformUpdater {
	private static final int MAX_SLEEP_SEC=128;
	private static final int[] Format_Offset=new int[]{1,9,13,16,36,40,44,45,49,52,61};
	private static Logger log;
	
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		try
		{
		    ConfigManager.initConfig();
		    LogManager.getLogManager().reset();
		    log=Logger.getLogger("org.glc.glos.coastwatch.PlatformUpdater");
		    log.setLevel(Level.parse(ConfigManager.getLogLevel()));
		    log.setUseParentHandlers(false);
//		    //Writing log to the file
			FileHandler logHandler=new FileHandler(ConfigManager.getPlatformLogFile(),40960,4,true);
			//Reformat the log message
			logHandler.setFormatter(new Formatter(){
				public String format(LogRecord record)
				{
					return String.format("%s: %s -- %s%s", record.getLevel(),new Date(record.getMillis()).toString(),record.getMessage(),ConfigManager.NEWLINE);
				}
			});
			log.addHandler(logHandler);
			log.info("Start to update platform location...");
			LogMessage("Start to read the remote file at: "+ConfigManager.getPlatformURL(),Level.FINE,null);
		    
			URL pltURL=null;
			URLConnection conn=null;
			
			InputStream in=null;
			BufferedReader reader=null;
			String temp=null;
			ArrayList<String> rawData=new ArrayList<String>();
			HashMap<String,Station> fStations=null;
			ArrayList<Station> dStations=null;
			ArrayList<Record> results=null;
			HashMap<String,ArrayList<Station>> rawHistory;
			HashMap<Long,ArrayList<Station>> history;
			int sleepSec=1;
			while(true)
			{
				try
				{
				    pltURL=new URL(ConfigManager.getPlatformURL());
				    conn=pltURL.openConnection();
				    conn.setConnectTimeout(5000);
					conn.setReadTimeout(5000);
					in=conn.getInputStream();
				    if(in!=null)
					    break;
				}
				catch(java.net.SocketTimeoutException ste)
				{
					//Exponential backoff algorithm from APUE to handle transient connect errors
					if(sleepSec<=MAX_SLEEP_SEC)
					{
					    if(sleepSec<=MAX_SLEEP_SEC/2)
					    {
						    Thread.sleep(sleepSec*1000);
						    sleepSec*=2;
					    }
					}
					else
					{
						LogMessage("Connection timeout when connect to the remote file.",Level.SEVERE,null);
						throw ste;
					}
				}
			}
			reader=new BufferedReader(new InputStreamReader(in));
			while((temp=reader.readLine())!=null)
				rawData.add(temp);
			reader.close();
			LogMessage("Reading is done.",Level.FINE,null);
			reader=null;
			if(rawData.size()==0)
			{
				LogMessage("No data available!",Level.WARNING,null);
			}
			rawHistory=new HashMap<String,ArrayList<Station>>();
			fStations=ParseRawData(rawData,rawHistory);
			LogMessage("Parsing is done.",Level.FINE,null);
			LogMessage("Read stations from database.",Level.FINE,null);
			dStations=getPlatformsFromDB();
			LogMessage("Reading is done.",Level.FINE,null);
			history=new HashMap<Long,ArrayList<Station>>();
			results=FilterPolicy(fStations,dStations,rawHistory,history);
			if(args!=null&&args.length==1&&args[0].toLowerCase().equals("-t"))
			{
				LogMessage("Print test result on the terminal.",Level.FINE,null);
				System.out.println(String.format("%d platforms need to be updated", results.size()));
				java.util.Collections.sort(results, new java.util.Comparator<Record>(){

					public int compare(Record o1, Record o2) {
						// TODO Auto-generated method stub
						return (int)(o1.pid-o2.pid);
					}
					
				});
				for(Record r:results)
				{
					System.out.println(String.format("Row ID: %d, Old Latlon:(%f,%f); Old Z: %f; OLD A: %f; -- New Latlon:(%f,%f); New Z: %f; New A: %f;", 
							r.pid,r.oldLat,r.oldLon,r.oldZ,r.oldAnemo,
							fStations.get(r.handle).getLatitude(),
							fStations.get(r.handle).getLongitude(),
							fStations.get(r.handle).getAltitude(),
							fStations.get(r.handle).getAnemoHeight()));
				}
			}
			else
			{
				SqlHelper sql=new SqlHelper(ConfigManager.getJDBCDriver(),
		                ConfigManager.getConnectionString(),
		                ConfigManager.getDBAccount(),
		                ConfigManager.getDBPassword(),
		                log);
		        sql.setTimeZone(ConfigManager.getObservationTimeZone());
		        LogMessage("Update platform_location table.",Level.FINE,null);
		        int count=sql.insertPlatformLoc(history);
		        LogMessage(String.format("%d records were inserted", count),Level.FINE,null);
				
		        LogMessage("Update platform_loc_history table.",Level.FINE,null);
				if(!results.isEmpty())
				{
			        if(sql.updateStationLoc(results,fStations))
			            LogMessage(String.format("%d records are updated", results.size()),Level.FINE,null);
			        else
			        	LogMessage("Failed to update platform_loc_history table",Level.SEVERE,null);
				}
			    else
			    	LogMessage("No need to update platform_loc_history table",Level.FINE,null);
			}
			log.info("End parsing.");
			log.info("************************************************************");
		}
		catch(Exception e)
		{
			log.severe(e.getMessage());
		}
	}
	private static ArrayList<Record> FilterPolicy(HashMap<String,Station> f,ArrayList<Station> d,HashMap<String,ArrayList<Station>> rh,HashMap<Long,ArrayList<Station>> h)
	{
		if(f==null||d==null||f.isEmpty()||d.isEmpty()||rh==null||h==null)return null;
		long id=-1;
		String handle=null;
		Station fs=null;
		ArrayList<Record> records=new ArrayList<Record>();
		for(Station plt:d)
		{
			if(f.containsKey(plt.getCoop_handle()))
			{
				id=plt.getId();
				handle=plt.getCoop_handle();
			}
			else if(f.containsKey(plt.getIcao_handle()))
			{
				id=plt.getId();
				handle=plt.getIcao_handle();
			}
			else if(f.containsKey(plt.getNdbc_handle()))
			{
				id=plt.getId();
				handle=plt.getNdbc_handle();
			}
			else if(f.containsKey(plt.getNos_handle()))
			{
				id=plt.getId();
				handle=plt.getNos_handle();
			}
			else if(f.containsKey(plt.getNos_handle()))
			{
				id=plt.getId();
				handle=plt.getNos_handle();
			}
			else if(f.containsKey(plt.getNws_handle()))
			{
				id=plt.getId();
				handle=plt.getNws_handle();
			}
			else if(f.containsKey(plt.getRadio_call_sign()))
			{
				id=plt.getId();
				handle=plt.getRadio_call_sign();
			}
			else if(f.containsKey(plt.getWmo_handle()))
			{
				id=plt.getId();
				handle=plt.getWmo_handle();
			}
			if(handle!=null)
			{
				if(rh.get(handle)!=null)
					h.put(id, rh.get(handle));
				fs=f.get(handle);
				if(fs!=null&&id>0)
				{
					Record r=new Record();
					r.pid=id;
					r.handle=handle;
					r.oldLat=plt.getLatitude();
					r.oldLon=plt.getLongitude();
					r.oldZ=plt.getAltitude();
					r.oldAnemo=plt.getAnemoHeight();
					if(!Float.isNaN(fs.getLongitude())&&Math.abs(fs.getLongitude()-plt.getLongitude())>0.0049f)
					{
						r.bLatlonChanged=true;
						//r.handle=fs.getHandle();
						//r.oldLon=plt.getLongitude();
						//r.oldLat=plt.getLatitude();
					}
					if(!Float.isNaN(fs.getLatitude())&&Math.abs(fs.getLatitude()-plt.getLatitude())>0.0049f)
					{
						r.bLatlonChanged=true;
						//r.handle=fs.getHandle();
						//r.oldLon=plt.getLongitude();
						//r.oldLat=plt.getLatitude();
					}
					if(!Float.isNaN(fs.getAltitude())&&Math.abs(fs.getAltitude()-plt.getAltitude())>0.49f)
					{
						r.bZChanged=true;
						//r.handle=fs.getHandle();
						//r.oldZ=plt.getAltitude();
					}
					if(!Float.isNaN(fs.getAnemoHeight())&&Math.abs(fs.getAnemoHeight()-plt.getAnemoHeight())>0.49f)
					{
						r.bAnemoChanged=true;
						//r.handle=fs.getHandle();
						//r.oldAnemo=plt.getAnemoHeight();
					}
					if(r.bAnemoChanged||r.bZChanged||r.bLatlonChanged)
						records.add(r);
				}
			}
			id=0;
			handle=null;
			
		}
		return records;
	}
	private static ArrayList<Station> getPlatformsFromDB() throws ClassNotFoundException
	{
		SqlHelper sql=new SqlHelper(ConfigManager.getJDBCDriver(),
                ConfigManager.getConnectionString(),
                ConfigManager.getDBAccount(),
                ConfigManager.getDBPassword(),
                log);
        sql.setTimeZone(ConfigManager.getObservationTimeZone());
		return sql.getAllStations();
	}
	private static HashMap<String,Station> ParseRawData(ArrayList<String> raws,HashMap<String,ArrayList<Station>> history)
	{
		int len=0;
		int year=0;
		int dayofyear=0;
		String sid=null;
		String temp=null;
		Calendar cal=Calendar.getInstance(TimeZone.getTimeZone(ConfigManager.getObservationTimeZone()));
		if(raws!=null&&!raws.isEmpty()&&history!=null)
		{
			HashMap<String,Station> stations=new HashMap<String,Station>();
			for(String str:raws)
			{
				if(str!=null&&!str.equals(""))
				{
					year=0;
					dayofyear=0;
					len=str.length();
					Station plt=new Station();
					if(len>=Format_Offset[1])
					{
						temp=str.substring(Format_Offset[0],Format_Offset[1]).trim();
						if(temp!=null&&!temp.equals(""))
						    plt.setHandle(temp);
					}
					if(len>=Format_Offset[2])
					{
						temp=str.substring(Format_Offset[1],Format_Offset[2]).trim();
						if(temp!=null&&!temp.equals(""))
						    year=Integer.parseInt(temp);
					}
					if(len>=Format_Offset[3])
					{
						temp=str.substring(Format_Offset[2],Format_Offset[3]).trim();
						if(temp!=null&&!temp.equals(""))
						    dayofyear=Integer.parseInt(temp);
					}
					if(year>0&&dayofyear>0)
					{
					    cal.set(Calendar.YEAR, year);
					    cal.set(Calendar.DAY_OF_YEAR, dayofyear);
					    cal.set(Calendar.HOUR_OF_DAY,0);
					    cal.set(Calendar.MINUTE,0);
					    cal.set(Calendar.MINUTE, 0);
					    cal.set(Calendar.SECOND,0);
					    cal.set(Calendar.MILLISECOND, 0);
					    plt.setUpdateMillSec(cal.getTimeInMillis());
					}
					if(len>=Format_Offset[5])
					{
						temp=str.substring(Format_Offset[4],Format_Offset[5]).trim();
						if(temp!=null&&!temp.equals(""))
						    plt.setLatitude(Float.parseFloat(temp)/100.0f);
					}
					if(len>=Format_Offset[6])
					{
						temp=str.substring(Format_Offset[5],Format_Offset[6]).trim();
						if(temp!=null&&!temp.equals(""))
						    plt.setLongitude(Float.parseFloat(temp)/-100.0f);
					}
					if(len>=Format_Offset[8])
					{
						temp=str.substring(Format_Offset[7],Format_Offset[8]).trim();
						if(temp!=null&&!temp.equals(""))
						    plt.setAltitude(Float.parseFloat(temp));
					}
					if(len>=Format_Offset[9])
					{
						temp=str.substring(Format_Offset[8],Format_Offset[9]).trim();
						if(temp!=null&&!temp.equals(""))
						    plt.setAnemoHeight(Float.parseFloat(temp));
					}
					if(plt.getHandle()!=null)
					{
					    if(stations.get(plt.getHandle())!=null)
					    {
					        if(stations.get(plt.getHandle()).getUpdateMillSec()>plt.getUpdateMillSec())
					    	    continue;	
					    }
					    
						stations.put(plt.getHandle(), plt);
						
						if(history.get(plt.getHandle())==null)
							history.put(plt.getHandle(), new ArrayList<Station>());
						history.get(plt.getHandle()).add(plt);
					}
				}
				
			}
			return stations;
		}
		return null;
	}
	private synchronized static void LogMessage(String message,Level level,Throwable exception )
	{
		if(log!=null)
			log.log(level, message,exception);
		
	}
}

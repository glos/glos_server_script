package org.glc.glos.coastwatch;

import java.util.logging.Logger;
import java.util.logging.LogManager;
import java.util.logging.Level;
import java.util.logging.FileHandler;
import java.util.logging.Formatter;
import java.util.logging.LogRecord;
import java.util.Date;
import java.util.Calendar;
import java.util.Properties;
import java.util.TimeZone;
import java.util.ArrayList;
import java.util.HashMap;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;

import org.glc.glos.coastwatch.domain.Station;
import org.glc.glos.coastwatch.logredirect.*;
import org.glc.glos.coastwatch.SqlHelper.Record;

public class PlatformDataAvail {
	private static String YEAR="YEAR";
	private static String GLOS_BUOY_TYPE_ID="GLOS_BUOY_TYPE_ID";
	private static String NOAAPORT_TABLES="NOAAPORT_TABLES";
	private static String GLOS_TABLES="GLOS_TABLES";
	private static Logger log;
	private static Properties config;
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		try
		{
			if(args.length==0)
			{
				System.err.println("config file is required to run the script!");
				return;
			}
			
			ConfigManager.initConfig();
			LogManager.getLogManager().reset();
			log=Logger.getLogger("org.glc.glos.coastwatch.PlatformDataAvail");
			log.setLevel(Level.parse(ConfigManager.getLogLevel()));
			log.setUseParentHandlers(false);
			//	    //Writing log to the file
			FileHandler logHandler=new FileHandler(ConfigManager.getDataAvailLogFile(),40960,4,true);
			//Reformat the log message
			logHandler.setFormatter(new Formatter(){
				public String format(LogRecord record)
				{
					return String.format("%s: %s -- %s%s", record.getLevel(),new Date(record.getMillis()).toString(),record.getMessage(),ConfigManager.NEWLINE);
				}
			});
			log.addHandler(logHandler);
			log.info("******************************************");
			log.info("Start platform data availability script...");
			//LogMessage("Start to read the remote file at: "+ConfigManager.getPlatformURL(),Level.FINE,null);
			
			//for debug env
			//InputStream is=ConfigManager.class.getClassLoader().getResourceAsStream(args[0]);
			//for production env
			InputStream is=new FileInputStream(args[0]);
			if(is!=null)
			{
			    config=new Properties();
			    config.load(is);
			    is.close();
			    if(ConfigManager.getDataAvailTableName()!=null&&config.getProperty(YEAR)!=null&&!config.getProperty(YEAR).equals(""))
			    {
			    	int year=Integer.parseInt(config.getProperty(YEAR));
			    	int glosid=-1;
			    	String noaatables[]=null,glostables[]=null,glosids[]=null;
			    	if(config.getProperty(GLOS_BUOY_TYPE_ID)!=null&&!config.getProperty(GLOS_BUOY_TYPE_ID).equals(""))
			    		glosid=Integer.parseInt(config.getProperty(GLOS_BUOY_TYPE_ID));
			    	if(config.getProperty(NOAAPORT_TABLES)!=null&&!config.getProperty(NOAAPORT_TABLES).equals(""))
			    		noaatables=config.getProperty(NOAAPORT_TABLES).split(ConfigManager.OFFSET_STR_DELIMITER);
			    	if(config.getProperty(GLOS_TABLES)!=null&&!config.getProperty(GLOS_TABLES).equals(""))
			    	{
			    		String tempa[]=config.getProperty(GLOS_TABLES).split(ConfigManager.OFFSET_STR_DELIMITER);
			    		if(tempa!=null&&tempa.length>0)
			    		{
			    			glostables=new String[tempa.length];
			    			glosids=new String[tempa.length];
			    			for(int i=0;i<tempa.length;++i)
			    			{
			    				String ta[]=tempa[i].split(":");
			    				if(ta==null||ta.length!=2)
			    				{
			    					glostables=null;
			    					glosids=null;
			    					break;
			    				}
			    				glosids[i]=ta[0];
			    				glostables[i]=ta[1];
			    			}
			    		}
			    	}
			    	SqlHelper sql=new SqlHelper(ConfigManager.getJDBCDriver(),
			    			ConfigManager.getConnectionString(),
			    			ConfigManager.getDBAccount(),
			    			ConfigManager.getDBPassword(),
			    			log);
			    	sql.setTimeZone(ConfigManager.getObservationTimeZone());
			    	ArrayList<Station> stations=sql.getAllStations();
			    	HashMap<Integer,String> glosmap=null;
			    	if(glosids!=null&&glostables!=null)
			    	{
			    		glosmap=new HashMap<Integer,String>();
			    		for(int i=0;i<glosids.length;++i)
			    		{
			    			
			    			glosmap.put(Integer.parseInt(glosids[i]), glostables[i]);
			    		}
			    	}
			    	log.info("Start searching database against the config file...");
			    	sql.checkDataAvailability(ConfigManager.getDataAvailTableName(), 
			    			stations, noaatables, glosmap, glosid, year);
			    	log.info("Finish update platform data availability.");
			    	log.info("******************************************");
			    }
			}
			else
			{
				log.severe(String.format("Can not open %s", args[0]));
				throw new IOException();
			}
		}
		catch(Exception e)
		{
			log.severe(e.getMessage());
		}
	}
	
	private synchronized static void LogMessage(String message,Level level,Throwable exception )
	{
		if(log!=null)
			log.log(level, message,exception);
		
	}

}

/* Great Lakes Observing System Regional Association 
 * @Author Guan Wang
 * @Organization Great Lakes Commission
 * @Contact Pete Giencke
 *           pgiencke@glc.org
 *           734-971-9135
 *           Eisenhower Corporate Park
 *           2805 S. Industrial Hwy, Suite 100
 */
package org.glc.glos.coastwatch;

import java.util.Calendar;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.regex.Pattern;
import java.util.regex.MatchResult;
import java.util.logging.Logger;
import java.util.logging.Level;
import java.net.URL;
import java.net.URLConnection;
import java.net.MalformedURLException;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.IOException;

import org.apache.http.HttpResponse;
import org.apache.http.HttpEntity;
import org.apache.http.client.HttpClient;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.client.methods.HttpGet;

import org.glc.glos.coastwatch.domain.Observation;

public class CoastTodayParser {
	private static final int MAX_SLEEP_SEC=128;
	private String url;
	private Logger log;
	private int[] offsets;
	private String regex=null;
	private static Calendar cal;
	{
		cal=Calendar.getInstance();
	}
	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}
	public static void setTimeZone(String id)
	{
		cal=Calendar.getInstance(java.util.TimeZone.getTimeZone(id));
	}
	public CoastTodayParser()
	{
		url="";
		log=null;
		offsets=null;
		
	}
	public CoastTodayParser(String todayURL,int[] dataOffset, String regx,Logger logger)
	{
		url=todayURL;
		log=logger;
		offsets=dataOffset;
		regex=regx;
		
	}
	private synchronized void logMessage(String message,Level level,Throwable exception )
	{
		if(log!=null)
			log.log(level, message,exception);
		
	}
	public static ArrayList<Observation> ParseByRegularExpression(String regx,ArrayList<String> source,Logger log)
	{
		if(regx==null||source==null)
			return null;
		ArrayList<Observation> list=null;
		Observation observ=null;
		Scanner rowScan=null;
		MatchResult matResult=null;
		String mobileRowStr=null;
		String[] mobileLoc=null;
		short year=1971,dayofyear=0,hourofday=0,minute=0;
		Pattern pattern=Pattern.compile(regx);
		for(String row:source)
		{
			if(row==null||row.equals(""))continue;
			//System.out.println(row);
			try
			{
				observ=new Observation();
				rowScan=new Scanner(row);
				rowScan.findInLine(pattern);
				matResult=rowScan.match();
				if(matResult==null||matResult.groupCount()<20)
				{
					return null;
				}
				else
				{
					if(list==null)
						list=new ArrayList<Observation>();
					year=Short.parseShort(matResult.group(1));
					dayofyear=Short.parseShort(matResult.group(2));
					hourofday=Short.parseShort(matResult.group(3));
					minute=Short.parseShort(matResult.group(4));
					//Calendar cal=Calendar.getInstance();
					cal.set(Calendar.YEAR, year);
					cal.set(Calendar.DAY_OF_YEAR, dayofyear);
					cal.set(Calendar.HOUR_OF_DAY, hourofday);
					cal.set(Calendar.MINUTE, minute);
					cal.set(Calendar.SECOND, 0);
					cal.set(Calendar.MILLISECOND, 0);
					observ.setDate(cal.getTimeInMillis());
					if(matResult.group(5).trim().equals(""))
						observ.setStationType((short)999);
					else
					    observ.setStationType(Short.parseShort(matResult.group(5)));
					observ.setHandle(matResult.group(6));
					if(matResult.group(7).trim().equals(""))
						observ.setLakeNo((short)999);
					else
						observ.setLakeNo(Short.parseShort(matResult.group(7)));
					observ.setDataFormat(Short.parseShort(matResult.group(8)));
					observ.setAirTemperature(Float.parseFloat(matResult.group(9)));
					observ.setDewPoint(Float.parseFloat(matResult.group(10)));
					observ.setWindDirection(Short.parseShort(matResult.group(11)));
					observ.setWindSpeed(Float.parseFloat(matResult.group(12)));
					observ.setMaxWindGust(Float.parseFloat(matResult.group(13)));
					observ.setCloudCover(Float.parseFloat(matResult.group(14)));
					observ.setSolarRadiation(Float.parseFloat(matResult.group(15)));
					observ.setBarometricPressure(Float.parseFloat(matResult.group(16)));
					observ.setWaterTemperature(Float.parseFloat(matResult.group(17)));
					observ.setSigWaveHeight(Float.parseFloat(matResult.group(18)));
					observ.setWavePeroid(Float.parseFloat(matResult.group(19)));
					if(!(mobileRowStr=matResult.group(20)).equals(""))
					{
						mobileLoc=mobileRowStr.trim().split(" ");
						if(mobileLoc.length==2)
						{
							observ.setNorthLatitude(Float.parseFloat(mobileLoc[0]));
							observ.setWestLongitude(Float.parseFloat(mobileLoc[1]));
						}
					}
					
					list.add(observ);
				}
				if(rowScan!=null)
					rowScan.close();
			}
			catch(IllegalStateException ie)
			{
				log.info("Number Format Exception in the row: "+row);
				log.severe(ie.getMessage());
			}
		}
		return list;
	}
	public static ArrayList<Observation> ParseBySubstring(int[] offsets,ArrayList<String> source,Logger log)
	{
		if(offsets==null||offsets.length<Observation.NumberOfFields||source==null)
			return null;
		ArrayList<Observation> list=null;
		Observation observ=null;
		String mobileRowStr=null;
		String[] mobileLoc=null;
		String temp=null;
		int strLen=0;
		short year=1971,dayofyear=0,hourofday=0,minute=0;
		for(String row:source)
		{
			if(list==null)
				list=new ArrayList<Observation>();
			if(row==null||row.equals(""))continue;
			//System.out.println(row);
			strLen=row.length();
			observ=new Observation();
			try
			{
				if(strLen>=offsets[0])
					year=Short.parseShort(row.substring(0,offsets[0]).trim());
					
				if(strLen>=offsets[1])
					dayofyear=Short.parseShort(row.substring(offsets[0],offsets[1]).trim());
					
				if(strLen>=offsets[2])
					hourofday=Short.parseShort(row.substring(offsets[1],offsets[2]).trim());
					
				if(strLen>=offsets[3])
					minute=Short.parseShort(row.substring(offsets[2],offsets[3]).trim());
					
				//Calendar cal=Calendar.getInstance();
				cal.set(Calendar.YEAR, year);
				cal.set(Calendar.DAY_OF_YEAR, dayofyear);
				cal.set(Calendar.HOUR_OF_DAY, hourofday);
				cal.set(Calendar.MINUTE, minute);
				cal.set(Calendar.SECOND, 0);
				cal.set(Calendar.MILLISECOND, 0);
				observ.setDate(cal.getTimeInMillis());
				if(strLen>=offsets[4])
				{
					temp=row.substring(offsets[3],offsets[4]);
					if(temp!=null)
					{
						if(temp.trim().equals(""))
							observ.setStationType((short)999);
						else
							observ.setStationType(Short.parseShort(temp.trim()));
					}
					//observ.setStationType(Short.parseShort(row.substring(offsets[3],offsets[4]).trim()));
				}
				if(strLen>=offsets[5])
				    observ.setHandle(row.substring(offsets[4],offsets[5]).trim());
				if(strLen>=offsets[6])
				{
					temp=row.substring(offsets[5],offsets[6]);
					if(temp!=null)
					{
						if(temp.trim().equals(""))
							observ.setLakeNo((short)999);
						else
							observ.setLakeNo(Short.parseShort(temp.trim()));
					}
					//observ.setLakeNo(Short.parseShort(row.substring(offsets[5],offsets[6]).trim()));
				}
				if(strLen>=offsets[7])
					observ.setDataFormat(Short.parseShort(row.substring(offsets[6],offsets[7]).trim()));
				if(strLen>=offsets[8])
					observ.setAirTemperature(Float.parseFloat(row.substring(offsets[7],offsets[8]).trim()));
				if(strLen>=offsets[9])
					observ.setDewPoint(Float.parseFloat(row.substring(offsets[8],offsets[9]).trim()));
				if(strLen>=offsets[10])
					observ.setWindDirection(Short.parseShort(row.substring(offsets[9],offsets[10]).trim()));
				if(strLen>=offsets[11])
					observ.setWindSpeed(Float.parseFloat(row.substring(offsets[10],offsets[11]).trim()));
				if(strLen>=offsets[12])
					observ.setMaxWindGust(Float.parseFloat(row.substring(offsets[11],offsets[12]).trim()));
				if(strLen>=offsets[13])
					observ.setCloudCover(Float.parseFloat(row.substring(offsets[12],offsets[13]).trim()));
				if(strLen>=offsets[14])
					observ.setSolarRadiation(Float.parseFloat(row.substring(offsets[13],offsets[14]).trim()));
				if(strLen>=offsets[15])
					observ.setBarometricPressure(Float.parseFloat(row.substring(offsets[14],offsets[15]).trim()));
				if(strLen>=offsets[16])
					observ.setWaterTemperature(Float.parseFloat(row.substring(offsets[15],offsets[16]).trim()));
				if(strLen>=offsets[17])
					observ.setSigWaveHeight(Float.parseFloat(row.substring(offsets[16],offsets[17]).trim()));
				if(strLen>=offsets[18])
					observ.setWavePeroid(Float.parseFloat(row.substring(offsets[17],offsets[18]).trim()));
				if(strLen>=offsets[20])
				{
					mobileRowStr=row.substring(offsets[18],offsets[20]);
					mobileLoc=mobileRowStr.trim().split(" ");
					if(mobileLoc.length==2)
					{
						observ.setNorthLatitude(Float.parseFloat(mobileLoc[0].trim()));
						observ.setWestLongitude(Float.parseFloat(mobileLoc[1].trim()));
					}
				}
				
				list.add(observ);
			}
			catch(NumberFormatException ne)
			{
				if(log!=null)
				{
					log.info("Number Format Exception in the row: "+row);
					log.severe(ne.getMessage());
				}
			}
			catch(Exception e)
			{
				if(log!=null)
				{
					log.info("Unexpected Exception occurred at the row: "+row);
					log.severe(e.getMessage());
				}
			}
		}
		return list;
	}
	public synchronized ArrayList<Observation> TryParse(boolean isRegex)
	{
		if(offsets==null||offsets.length<Observation.NumberOfFields)
			return null;
		
		ArrayList<Observation> list=null;
		BufferedReader reader=null;
		String errMsg=null;
		String temp=null;
		ArrayList<String> rawData=new ArrayList<String>();
		HttpClient client=null;
		try
		{
			client=new DefaultHttpClient();
			client.getParams().setParameter("http.socket.timeout", 3000);
			client.getParams().setParameter("http.connection.timeout", 3000);
			HttpGet hget=new HttpGet(url);
			/*URL todayURL=new URL(url);
			logMessage("Begin to read from the remote file.",Level.FINE,null);
			URLConnection conn=todayURL.openConnection();
			conn.setConnectTimeout(5000);
			conn.setReadTimeout(5000);*/
			InputStream in=null;
			int sleepSec=1;
			while(true)
			{
				try
				{
				    /*in=conn.getInputStream();
				    if(in!=null)
				        break;*/
					HttpResponse response=client.execute(hget);
					HttpEntity entity=response.getEntity();
					if(entity!=null)
					{
				        in=entity.getContent();
						if(in!=null)
					        break;
					}
				}
				catch(Exception e)//(java.net.SocketTimeoutException ste)
				{
					logMessage("Possible socket timeout.",Level.SEVERE,null);
					//Exponential backoff algorithm from APUE to handle transient connect errors
					if(sleepSec<=MAX_SLEEP_SEC)
					{
					    if(sleepSec<=MAX_SLEEP_SEC/2)
					    { 
						    Thread.sleep(sleepSec*1000);
						    sleepSec*=2;
						    logMessage("Try to reconnect.",Level.SEVERE,null);
					    }
					    else
					    	sleepSec*=2;
					}
					else
					{
						logMessage("Connection timeout when connect to the remote file.",Level.SEVERE,null);
						throw e;
					}
				}
			}
			reader=new BufferedReader(new InputStreamReader(in));
			while((temp=reader.readLine())!=null)
				rawData.add(temp);
			reader.close();
			reader=null;
			logMessage("Reading is done.",Level.FINE,null);
			
			if(rawData.size()==0)
			{
				logMessage("No data available!",Level.WARNING,null);
			}
			else
				if(isRegex)
				{
					logMessage("Parse raw data by regular expression.",Level.FINE,null);
					list=ParseByRegularExpression(this.regex,rawData,log);
				}
				else
				{
					logMessage("Parse raw data by string's substring function.",Level.FINE,null);
					logMessage(String.format("%d records needed to be parsed.", rawData.size()),Level.FINE,null);
					list=ParseBySubstring(this.offsets,rawData,log);
				}
			return list;
		}
		catch(MalformedURLException me)
		{
			errMsg=String.format("Can not connect with the url: %s", url);
			System.err.printf(errMsg);
			System.err.println();
			me.printStackTrace();
			logMessage(errMsg,Level.SEVERE,me);
			return list;
		}
		catch(IOException ie)
		{
			errMsg=String.format("Can not read from the url: %s",url);
			System.err.printf(errMsg);
			System.err.println();
			ie.printStackTrace();
			logMessage(errMsg,Level.SEVERE,ie);
			return list;
		}
		catch(Exception e)
		{
			e.printStackTrace();
			return null;
		}
		finally
		{
			try
			{
			    if(reader!=null)
				    reader.close();
			    if(client!=null)
			    	client.getConnectionManager().shutdown();
			}
			catch(Exception e){}
		}
	}
}

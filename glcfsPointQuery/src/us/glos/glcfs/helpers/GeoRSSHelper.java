package us.glos.glcfs.helpers;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import javax.servlet.http.HttpServletResponse;

import com.sun.syndication.io.FeedException;
import com.sun.syndication.io.SyndFeedOutput;
import com.sun.syndication.feed.synd.*;
import com.sun.syndication.feed.module.georss.*;
import com.sun.syndication.feed.module.georss.geometries.*;

import ucar.ma2.Array;

import org.glc.Logger;
import org.glc.utils.TimeZoneOffset;
import org.glc.xmlconfig.LogLevel;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import us.glos.glcfs.domain.GLCFSRecord;

public class GeoRSSHelper {
	private static final String DATA_NOT_AVAILABLE="N/A";
	public static boolean GetGeoRSS(HttpServletResponse response,
			ArrayList<Long> timestamps, ArrayList<String> varNames,
			ArrayList<Array> records, DateTimeZone defaultZone, 
			double lon,double lat,int hourOffset, double d3d,boolean is3D,double rqDepth,
			boolean isEnglish,boolean isNowcast,boolean isInFile, boolean isLatest,boolean isAsc,boolean isDOY,String lake) throws IOException
	{
		boolean result=false;
		if(response!=null&&timestamps!=null&&records!=null&&varNames!=null&&varNames.size()>0&&defaultZone!=null&&varNames.size()==records.size())
		{
			response.setContentType("application/xml;charset=UTF-8");
		    SyndFeed feed=new SyndFeedImpl();
		    feed.setFeedType("rss_2.0");
		    feed.setTitle("Great Lakes Observing System");
			feed.setLink("http://www.glos.us");
			feed.setDescription("Great Lakes Coastal Forecast System Channel");
			
			ArrayList<SyndEntry> entries=new ArrayList<SyndEntry>();
			int tlen=(int)timestamps.size();
			SyndEntry entry=null;
			DateTime dt=null;
			Array record=null;
			String vName=null;
			int vLen=varNames.size();
			int len=records.size();
			int times=len/vLen;
			//GLCFSRecord.reSortVarsByVelocityPair(varNames);
			DateTimeFormatter DateTimeFormatter=DateTimeFormat.forPattern("YYYY-MM-dd HH:mm:ss Z");
			GLCFSRecord.Velocity velocity=null;
			int maxLen=0;
			for(int i=tlen-1,k=times;i>=0&&k>=0;--i)
			{
				StringBuilder sb=new StringBuilder("");
				entry=new SyndEntryImpl();
				if(isInFile)
					entry.setTitle(isNowcast?"GLCFS Nowcast Forcing Data":"GLCFS Forecast Forcing Data");
				else
					entry.setTitle(isNowcast?String.format("GLCFS Nowcast %s",is3D?"3D":"2D"):"GLCFS Forecast");
				entry.setLink("");
				//if(isNowcast||isInFile)
				if(isNowcast)
				{
					dt=new DateTime(timestamps.get(i)*1000,defaultZone);
				}
				else
				{
					dt=new DateTime(new DateTime().getYear(),1,1,0,0,0,0,defaultZone);
					dt=dt.plusHours(timestamps.get(i).intValue());
				}
				entry.setPublishedDate(new java.util.Date());
				SyndContent desc=new SyndContentImpl();
				desc.setType("text/html");
				
				sb.append("<div><b>");
				sb.append("Datetime");
				sb.append("</b>:");
				sb.append("<span>");
				sb.append(DateTimeFormatter.withZone(DateTimeZone.forOffsetHours(hourOffset)).print(dt));
				/*if(hourOffset==0)
				    sb.append(" GMT");
				else
					sb.append(TimeZoneOffset.GetOffsetString(hourOffset));*/
				if(isDOY)
				{
					sb.append("<div><b>");
					sb.append("Day of Year");
					sb.append("</b>:");
					sb.append("<span>");
					sb.append(String.format("%s", dt.dayOfYear().getAsText()));
					sb.append("</span></div>");
				}
				sb.append("</span></div>");
				sb.append("<div><b>");
				sb.append("Requested Latitude");
				sb.append("</b>:");
				sb.append("<span>");
				sb.append(String.format("%.4f", lat));
				sb.append(" degree</span></div>");
				sb.append("<div><b>");
				sb.append("Requested Longitude");
				sb.append("</b>:");
				sb.append("<span>");
				sb.append(String.format("%.4f", lon));
				sb.append(" degree</span></div>");
				if(lake!=null)
				{
					sb.append(String.format("<div><b>Lake</b>:<span>%s</span></div>", Character.toUpperCase(lake.charAt(0))+lake.substring(1)));
				}
				if(is3D)
				{
					if(rqDepth>=0)
					{
						sb.append("<div><b>");
						sb.append("Requested Depth");
						sb.append("</b>:");
						sb.append("<span>");
						sb.append(String.format("%.4f", isEnglish?Unit2English.Convert2En(rqDepth, GLCFSRecord.getVariableUnitByAbbrev("depth")):rqDepth));
						sb.append(String.format(" %s</span></div>",isEnglish?Unit2English.Convert2EnUnit(GLCFSRecord.getVariableUnitByAbbrev("depth")):GLCFSRecord.getVariableUnitByAbbrev("depth")));
					}
					else
					{
						sb.append("<div><b>");
						sb.append("Depth at Node");
						sb.append("</b>:");
						if(GLCFSRecord.isValidData("d3d", d3d))
						{
							sb.append("<span>");
							sb.append(String.format("%.4f", isEnglish?Unit2English.Convert2En(d3d, GLCFSRecord.getVariableUnitByAbbrev("depth")):d3d));
							sb.append(String.format(" %s</span></div>",isEnglish?Unit2English.Convert2EnUnit(GLCFSRecord.getVariableUnitByAbbrev("depth")):GLCFSRecord.getVariableUnitByAbbrev("depth")));
						}
						else
							sb.append(String.format("<span>%s</span></div>", DATA_NOT_AVAILABLE));
					}
				}
				//all variable with time as one of the dimensions should have the same length
				if(maxLen==0)
				{
					--k;
					for(int m=0;m<vLen;++m)
						if(records.get(m+k*vLen).getSize()-1>maxLen)
							maxLen=(int)records.get(m+k*vLen).getSize()-1;
				}
				else
					--maxLen;
				boolean isValid=true;
				for(int j=0;j<vLen;++j)
				{
					record=records.get(j+k*vLen);
					vName=varNames.get(j);
					sb.append("<div>");
                	sb.append("<b>");
                	sb.append(GLCFSRecord.getVariableNameByAbbrev(vName));
                	sb.append("</b>: ");
                	sb.append("<span>");
                	if(record.getSize()>maxLen)
                	{
                		if(GLCFSRecord.isValidData(vName, record.getDouble(maxLen)))
                		{
                			isValid=true;
                			sb.append(String.format("%.4f",isEnglish?Unit2English.Convert2En(record.getDouble(maxLen), GLCFSRecord.getVariableUnitByAbbrev(vName)):record.getDouble(maxLen)));
                		}
                	   	else
                		{
                			sb.append(DATA_NOT_AVAILABLE);
                			isValid=false;
                		}
                	}
                	else
                	{
                		if(GLCFSRecord.isValidData(vName, record.getDouble(0)))
                		{
                			isValid=true;
                			sb.append(String.format("%.4f",isEnglish?Unit2English.Convert2En(record.getDouble(0), GLCFSRecord.getVariableUnitByAbbrev(vName)):record.getDouble(0)));//variable not has time dimension, etc. depth
                		}
                		else
                		{
                			sb.append(DATA_NOT_AVAILABLE);
                			isValid=false;
                		}
                	}
                	if(isValid)
                	{
                		sb.append(" ");
                		sb.append(isEnglish?Unit2English.Convert2EnUnit(GLCFSRecord.getVariableUnitByAbbrev(vName)):GLCFSRecord.getVariableUnitByAbbrev(vName));
                	}
                	sb.append("</span>");
                	sb.append("</div>");
                	if(j>0&&records.get(j+k*vLen).getSize()>maxLen&&records.get(j+k*vLen-1).getSize()>maxLen)
                	{
                		//Logger.writeLog(String.format("%d", maxLen), LogLevel.INFO);
                		velocity=GLCFSRecord.VelocityPair(varNames.get(j-1), varNames.get(j), records.get(j+k*vLen-1).getDouble(maxLen), records.get(j+k*vLen).getDouble(maxLen));
                		if(velocity!=null)
                		{
                			
                			sb.append("<div>");
                			sb.append("<b>");
                			sb.append(velocity.speedName);
                			sb.append("</b>: ");
                			sb.append("<span>");
                			if(velocity.isMissingData==false)
                			{
                				sb.append(String.format("%.4f", isEnglish?Unit2English.Convert2En(velocity.speed, GLCFSRecord.Velocity.speedUnit):velocity.speed));
                				sb.append(" ");
                				sb.append(isEnglish?Unit2English.Convert2EnUnit(GLCFSRecord.Velocity.speedUnit):GLCFSRecord.Velocity.speedUnit);
                			}
                			else
                				sb.append(DATA_NOT_AVAILABLE);
                			sb.append("</span>");
                			sb.append("</div>");

                			sb.append("<div>");
                			sb.append("<b>");
                			sb.append(velocity.degreeName);
                			sb.append("</b>: ");
                			sb.append("<span>");
                			if(velocity.isMissingData==false)
                			{
                				sb.append(String.format("%.4f", velocity.degree));
                				sb.append(" ");
                				sb.append(GLCFSRecord.isDirection2North(varNames.get(j-1), varNames.get(j))?GLCFSRecord.Velocity.degreeUnit1:GLCFSRecord.Velocity.degreeUnit);
                			}
                			else
                				sb.append(DATA_NOT_AVAILABLE);
                			sb.append("</span>");
                			sb.append("</div>");
                			
                		}
                	}
                	
				}
				desc.setValue(sb.toString());
				entry.setDescription(desc);
				
				GeoRSSModule geo=new SimpleModuleImpl();//W3CGeoModuleImpl();

				geo.setPosition(new Position(lat,lon));
				entry.getModules().add(geo);
				entries.add(entry);
			}
			if(isAsc)//if(!isNowcast&&!isLatest)//people intends to see the most recent first for forecast
				java.util.Collections.reverse(entries);
			feed.setEntries(entries);
			SyndFeedOutput output=new SyndFeedOutput();
			try
			{
				output.output(feed, response.getWriter());
				result=true;
			}
			catch(FeedException ex)
			{
				Logger.writeLog(ex.getMessage(), LogLevel.SEVERE);
				result=false;
			}
		}
		return result;
	}
	
	public static boolean GetGeoRSS(HttpServletResponse response,
			ArrayList<String> varNames,
			ArrayList<Array> records, DateTimeZone defaultZone, 
			double lon,double lat,int hourOffset, double d3d,boolean is3D,double rqDepth,
			boolean isEnglish,boolean isNowcast,boolean isInFile, boolean isAsc,String lake) throws IOException
	{
		boolean result=false;
		if(response!=null&&records!=null&&varNames!=null&&varNames.size()>0&&varNames.size()==records.size()&&defaultZone!=null)
		{
			response.setContentType("application/xml;charset=UTF-8");
		    SyndFeed feed=new SyndFeedImpl();
		    feed.setFeedType("rss_2.0");
		    feed.setTitle("Great Lakes Observing System");
			feed.setLink("http://www.glos.us");
			feed.setDescription("Great Lakes Coastal Forecast System Channel");
			
			ArrayList<SyndEntry> entries=new ArrayList<SyndEntry>();
			
			SyndEntry entry=null;
			//DateTime dt=null;
			Array record=null;
			String vName=null;
			Long millSec=null;
			int vLen=varNames.size();
			DateTimeFormatter DateTimeFormatter=DateTimeFormat.forPattern("YYYY-MM-dd HH:mm:ss Z");
						
			StringBuilder sb=new StringBuilder("");
			
			entry=new SyndEntryImpl();
			if(isInFile)
				entry.setTitle(isNowcast?"GLCFS Nowcast Forcing Data":"GLCFS Forecast Forcing Data");
			else
				entry.setTitle(isNowcast?"GLCFS Nowcast":"GLCFS Forecast");
			entry.setLink("");
			//dt=new DateTime(millSec,defaultZone);
			entry.setPublishedDate(new java.util.Date());
			SyndContent desc=new SyndContentImpl();
			desc.setType("text/html");

			sb.append("<div><b>");
			sb.append("Datetime");
			sb.append("</b>:");
			sb.append("<span>");
			//sb.append(DateTimeFormatter.withZone(DateTimeZone.forOffsetHours(hourOffset)).print(dt));
			sb.append(DATA_NOT_AVAILABLE);
			sb.append("</span></div>");
			sb.append("<div><b>");
			sb.append("Requested Latitude");
			sb.append("</b>:");
			sb.append("<span>");
			sb.append(String.format("%.4f", lat));
			sb.append(" degree</span></div>");
			sb.append("<div><b>");
			sb.append("Requested Longitude");
			sb.append("</b>:");
			sb.append("<span>");
			sb.append(String.format("%.4f", lon));
			sb.append(" degree</span></div>");
			if(lake!=null)
			{
				sb.append(String.format("<div><b>Lake</b>:<span>%s</span></div>", Character.toUpperCase(lake.charAt(0))+lake.substring(1)));
			}
			if(is3D)
			{
				if(rqDepth>=0)
				{
					sb.append("<div><b>");
					sb.append("Requested Depth");
					sb.append("</b>:");
					sb.append("<span>");
					sb.append(String.format("%.4f", isEnglish?Unit2English.Convert2En(rqDepth, GLCFSRecord.getVariableUnitByAbbrev("depth")):rqDepth));
					sb.append(String.format(" %s</span></div>",isEnglish?Unit2English.Convert2EnUnit(GLCFSRecord.getVariableUnitByAbbrev("depth")):GLCFSRecord.getVariableUnitByAbbrev("depth")));
				}
				else
				{
					if(GLCFSRecord.isValidData("d3d", d3d))
					{
						sb.append("<div><b>");
						sb.append("Depth at Node");
						sb.append("</b>:");
						sb.append("<span>");
						sb.append(String.format("%.4f", isEnglish?Unit2English.Convert2En(d3d, GLCFSRecord.getVariableUnitByAbbrev("depth")):d3d));
						sb.append(String.format(" %s</span></div>",isEnglish?Unit2English.Convert2EnUnit(GLCFSRecord.getVariableUnitByAbbrev("depth")):GLCFSRecord.getVariableUnitByAbbrev("depth")));
					}
					else
						sb.append(String.format("<span>%s</span></div>", DATA_NOT_AVAILABLE));
				}
			}
			for(int j=0;j<vLen;++j)
			{
				record=records.get(j);
				vName=varNames.get(j);
				sb.append("<div>");
				sb.append("<b>");
				sb.append(GLCFSRecord.getVariableNameByAbbrev(vName));
				sb.append("</b>: ");
				sb.append("<span>");
				if(GLCFSRecord.isValidData(vName, record.getDouble(0)))
				{
					sb.append(String.format("%.4f",isEnglish?Unit2English.Convert2En(record.getDouble(0), GLCFSRecord.getVariableUnitByAbbrev(vName)):record.getDouble(0)));//variable not has time dimension, etc. depth
					sb.append(" ");
					sb.append(isEnglish?Unit2English.Convert2EnUnit(GLCFSRecord.getVariableUnitByAbbrev(vName)):GLCFSRecord.getVariableUnitByAbbrev(vName));
				}
				else
					sb.append(DATA_NOT_AVAILABLE);
				sb.append("</span>");
				sb.append("</div>");
				
			}
			desc.setValue(sb.toString());
			entry.setDescription(desc);

			GeoRSSModule geo=new SimpleModuleImpl();//W3CGeoModuleImpl();

			geo.setPosition(new Position(lat,lon));
			entry.getModules().add(geo);
			entries.add(entry);
			
			feed.setEntries(entries);
			SyndFeedOutput output=new SyndFeedOutput();
			try
			{
				output.output(feed, response.getWriter());
				result=true;
			}
			catch(FeedException ex)
			{
				Logger.writeLog(ex.getMessage(), LogLevel.SEVERE);
				result=false;
			}
		}
		return result;
	}

}

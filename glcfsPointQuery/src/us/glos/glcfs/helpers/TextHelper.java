package us.glos.glcfs.helpers;

import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.ArrayList;

import javax.servlet.http.HttpServletResponse;

import org.glc.utils.TimeZoneOffset;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import ucar.ma2.Array;
import us.glos.glcfs.domain.GLCFSRecord;
import us.glos.glcfs.domain.IndexPair;

public class TextHelper {
	private static final char CSVDelimiter=',';
	private static final char TABDelimiter='\t';
	private static final char RETURN='\n';
	private static final String DATA_NOT_AVAILABLE="N/A";
	
	public static boolean GetTextFile(HttpServletResponse response,
			ArrayList<Long> timestamps, ArrayList<String> varNames,
			ArrayList<Array> records, DateTimeZone defaultZone, 
			double lon,double lat,int hourOffset, double d3d,boolean is3D,double rqDepth,
			boolean isEnglish,boolean isNowcast,boolean isInFile, boolean isCSV,boolean isPreview,boolean isAsc,ArrayList<IndexPair> indexArray,boolean isDOY,String lake) throws IOException
	{
		boolean result=false;
		if(indexArray!=null&&((timestamps!=null&&indexArray.size()!=timestamps.size())))
			return result;
		
		if(response!=null&&records!=null&&varNames!=null&&varNames.size()>0&&defaultZone!=null)//&&varNames.size()==records.size())
		{
			Array record=null;
			String vName=null;
			DateTime dt=null;
			int vLen=varNames.size();
			int len=records.size();
			int times=len/vLen;
			int maxLen=0;
			//GLCFSRecord.reSortVarsByVelocityPair(varNames);
			char sep=isCSV?CSVDelimiter:TABDelimiter;
			DateTimeFormatter DateTimeFormatter=DateTimeFormat.forPattern("YYYY-MM-dd HH:mm:ss Z");
			GLCFSRecord.Velocity velocity=null;
			String fname=null;
			if(isInFile)
			{
				if(isNowcast)
					fname="GLCFS_NOWCAST_FORCING_Data_Point";
				else
					fname="GLCFS_FORECAST_FORCING_Data_Point";
			}
			else
			{
				if(isNowcast)
					fname="GLCFS_Nowcast_Point";
				else
					fname="GLCFS_Forecast_Point";
			}
			StringBuilder sb=new StringBuilder();
			if(isInFile)
				sb.append(String.format("Great Lakes Coastal Forecasting System (%s)\n",isNowcast?"Nowcast Forcing Data":"Forecast Forcing Data"));
			else
				sb.append(String.format("Great Lakes Coastal Forecasting System (%s)\n",isNowcast?"Nowcast":"Forecast"));
			sb.append(String.format("Requested Longitude: %.4f degree\n", lon));
			sb.append(String.format("Requested Latitude: %.4f degree\n", lat));
			if(lake!=null)
				sb.append(String.format("Lake:%s\n", Character.toUpperCase(lake.charAt(0))+lake.substring(1)));
			if(is3D)
			{
				if(rqDepth>=0)
					sb.append(String.format("Requested Depth: %.4f %s\n",isEnglish?Unit2English.Convert2En(rqDepth, GLCFSRecord.getVariableUnitByAbbrev("depth")):rqDepth,
							isEnglish?Unit2English.Convert2EnUnit(GLCFSRecord.getVariableUnitByAbbrev("depth")):GLCFSRecord.getVariableUnitByAbbrev("depth")));
				else
				{
					if(GLCFSRecord.isValidData("d3d", d3d))
						sb.append(String.format("Depth at Node: %.4f %s\n",isEnglish?Unit2English.Convert2En(d3d, GLCFSRecord.getVariableUnitByAbbrev("depth")):d3d,
									isEnglish?Unit2English.Convert2EnUnit(GLCFSRecord.getVariableUnitByAbbrev("depth")):GLCFSRecord.getVariableUnitByAbbrev("depth")));
					else
						sb.append(String.format("Depth at Node: %s\n",DATA_NOT_AVAILABLE));
				}
			}
			String temp[]=null;
			if(timestamps!=null)
			{
				sb.append(String.format("Date/Time(GMT%s)%s", TimeZoneOffset.GetOffsetString(hourOffset),sep));
			    if(isDOY)
				    sb.append(String.format("Day of Year%s", sep));
			    if(indexArray!=null)
				    sb.append(String.format("Grid I%sGrid J%s", sep, sep));
			}
			for(int i=0;i<vLen;++i)
			{
				vName=varNames.get(i);
				sb.append(String.format("%s(%s)",
						GLCFSRecord.getVariableNameByAbbrev(vName),
						isEnglish?Unit2English.Convert2EnUnit(GLCFSRecord.getVariableUnitByAbbrev(vName)):GLCFSRecord.getVariableUnitByAbbrev(vName)));
				sb.append(sep);
				if(i>0)
				{
					temp=GLCFSRecord.VelocityPairExist(varNames.get(i-1), varNames.get(i));
					if(temp!=null)
					{
						sb.append(String.format("%s(%s)%s%s(%s)%s", temp[0],
								isEnglish?Unit2English.Convert2EnUnit(GLCFSRecord.getVariableUnitByAbbrev(vName)):GLCFSRecord.getVariableUnitByAbbrev(vName),
								sep,
								temp[1],
								GLCFSRecord.isDirection2North(varNames.get(i-1), varNames.get(i))?GLCFSRecord.Velocity.degreeUnit1:GLCFSRecord.Velocity.degreeUnit,
								sep));
					}
				}
				
			}
			sb.append(RETURN);
			if(timestamps!=null)
			{
				int tlen=(int)timestamps.size();
				int i=0;
				int k=0;
				int n=-1;
				for(i=isAsc?0:tlen-1,k=isAsc?-1:times;isAsc?i<tlen&&k<=times:i>=0&&k>=0;)
				{
					//if(isNowcast||isInFile)
					if(isNowcast)
						dt=new DateTime(timestamps.get(i)*1000,defaultZone);
					else
					{
						dt=new DateTime(new DateTime().getYear(),1,1,0,0,0,0,defaultZone);
						dt=dt.plusHours(timestamps.get(i).intValue());
					}
					sb.append(DateTimeFormatter.withZone(DateTimeZone.forOffsetHours(hourOffset)).print(dt));
					sb.append(sep);
					if(isDOY)
					{
						sb.append( dt.dayOfYear().getAsText());
						sb.append(sep);
					}
					if(indexArray!=null)
					{
						sb.append(String.format("%d%s%d%s", indexArray.get(i).I,sep,indexArray.get(i).J,sep));
					}
					if((n>=maxLen-1&&isAsc)||(n<=0&&!isAsc))
					{
						if(isAsc)
							++k;
						else
							--k;
						maxLen=0;
						for(int m=0;m<vLen;++m)
							if(records.get(m+k*vLen).getSize()>maxLen)
								maxLen=(int)records.get(m+k*vLen).getSize();
						if(isAsc)n=0;
						else n=maxLen-1;
					}
					else
					{
						if(isAsc)
							++n;
						else
							--n;
						//--maxLen;
					}
					for(int j=0;j<vLen;++j)
					{
						record=records.get(j+k*vLen);
						vName=varNames.get(j);
						if(record.getSize()>n)
						{
	                		if(GLCFSRecord.isValidData(vName, record.getDouble(n)))
	                			sb.append(String.format("%.4f",isEnglish?Unit2English.Convert2En(record.getDouble(n), GLCFSRecord.getVariableUnitByAbbrev(vName)):record.getDouble(n)));
	                		else
	                			sb.append(DATA_NOT_AVAILABLE);
						}
	                	else
	                	{
	                		if(GLCFSRecord.isValidData(vName, record.getDouble(0)))
	                			sb.append(String.format("%.4f",isEnglish?Unit2English.Convert2En(record.getDouble(0), GLCFSRecord.getVariableUnitByAbbrev(vName)):record.getDouble(0)));//variable not has time dimension, etc. depth
	                		else
	                			sb.append(DATA_NOT_AVAILABLE);
	                	}
	                	sb.append(sep);
						if(j>0&&records.get(j+k*vLen).getSize()>n&&records.get(j+k*vLen-1).getSize()>n)
	                	{
	                		velocity=GLCFSRecord.VelocityPair(varNames.get(j-1), varNames.get(j), records.get(j+k*vLen-1).getDouble(n), records.get(j+k*vLen).getDouble(n));
	                		if(velocity!=null)
	                		{
	                			if(velocity.isMissingData)
	                			{
	                				sb.append(DATA_NOT_AVAILABLE);
	                				sb.append(sep);
	                				sb.append(DATA_NOT_AVAILABLE);
	                				sb.append(sep);
	                			}
	                			else
	                			{
	                				sb.append(String.format("%.4f", isEnglish?Unit2English.Convert2En(velocity.speed, GLCFSRecord.Velocity.speedUnit):velocity.speed));
	                				sb.append(sep);
	                				sb.append(String.format("%.4f", velocity.degree));
	                				sb.append(sep);
	                			}
	                        }
	                	}
					}
					sb.append(RETURN);
					if(isAsc)++i;
					else --i;
				}
				
			}
			else
			{
				for(int j=0;j<vLen;++j)
				{
					record=records.get(j);
					vName=varNames.get(j);
					if(false==GLCFSRecord.isValidData(vName, record.getDouble(0)))
						sb.append(DATA_NOT_AVAILABLE);
					else
						sb.append(String.format("%.4f",isEnglish?Unit2English.Convert2En(record.getDouble(0), GLCFSRecord.getVariableUnitByAbbrev(vName)):record.getDouble(0)));//variable not has time dimension, etc. depth
					sb.append(sep);
					
				}
				sb.append(RETURN);
			}
			String headp="";
			if(isPreview)
			{
				response.getWriter().println(sb.toString());
				response.getWriter().flush();
				result=true;
			}
			else
			{
				if(isCSV)
				{
					response.setContentType("application/octet-stream");
					headp=String.format("attachment;filename=%s.csv",fname);
				}
				else
				{
					response.setContentType("text/plain");
					headp=String.format("attachment;filename=%s.txt",fname);
				}

				headp=headp.replace(' ', '_');
				response.setHeader("Content-Disposition",headp);

				if(sb.length()>0)
				{
					byte[] data=sb.toString().getBytes();
					response.setContentLength(data.length);
					ByteArrayInputStream bis=new ByteArrayInputStream(data);
					BufferedOutputStream bos=new BufferedOutputStream(response.getOutputStream());
					byte[] buffer=new byte[2048];
					while(bis.read(buffer, 0, 2048)>0)
						bos.write(buffer, 0, 2048);
					bos.flush();
					bos.close();
					bis.close();
					result=true;
				}
			}
		}
		return result;
	}
	
	public static boolean GetJSONString(HttpServletResponse response,
			ArrayList<Long> timestamps, ArrayList<String> varNames,
			ArrayList<Array> records, DateTimeZone defaultZone, 
			double lon,double lat,int hourOffset, double d3d,boolean is3D,
			boolean isEnglish,boolean isNowcast,boolean isInFile, boolean isAsc) throws IOException
	{
		StringBuilder sb=null;
		boolean result=false;
		if(response!=null&&records!=null&&varNames!=null&&varNames.size()>0&&defaultZone!=null)
		{
			Array record=null;
			String vName=null;
			DateTime dt=null;
			int vLen=varNames.size();
			int len=records.size();
			int times=len/vLen;
			int maxLen=0;
			DateTimeFormatter DateTimeFormatter=DateTimeFormat.forPattern("YYYY-MM-dd HH:mm:ss Z");
			String NUL="null";
			sb=new StringBuilder();
			sb.append('{');
			sb.append(String.format("\"lon\":%.4f,", lon));
			sb.append(String.format("\"lat\":%.4f,", lat));
			
			if(is3D)
			{
				if(GLCFSRecord.isValidData("d3d", d3d))
				{
					sb.append(String.format("\"d3d\":%.4f,", isEnglish?Unit2English.Convert2En(d3d, GLCFSRecord.getVariableUnitByAbbrev("depth")):d3d));
					sb.append(String.format("\"d3dunit\":\"%s\",", isEnglish?Unit2English.Convert2EnUnit(GLCFSRecord.getVariableUnitByAbbrev("depth")):GLCFSRecord.getVariableUnitByAbbrev("depth")));
				}
				else
					sb.append(String.format("\"d3d\":\"%s\",", NUL));
			}
			sb.append("\"data\":[");
			if(timestamps!=null)
			{
				int tlen=(int)timestamps.size();
				int i=0;
				int k=0;
				int n=-1;
				for(i=isAsc?0:tlen-1,k=isAsc?-1:times;isAsc?i<tlen&&k<=times:i>=0&&k>=0;)
				{
					sb.append('{');
					//if(isNowcast||isInFile)
					if(isNowcast)
						dt=new DateTime(timestamps.get(i)*1000,defaultZone);
					else
					{
						dt=new DateTime(new DateTime().getYear(),1,1,0,0,0,0,defaultZone);
						dt=dt.plusHours(timestamps.get(i).intValue());
					}
					sb.append(String.format("\"datetime\":\"%s\",", DateTimeFormatter.withZone(DateTimeZone.forOffsetHours(hourOffset)).print(dt)));
					if((n>=maxLen-1&&isAsc)||(n<=0&&!isAsc))
					{
						if(isAsc)
							++k;
						else
							--k;
						maxLen=0;
						for(int m=0;m<vLen;++m)
							if(records.get(m+k*vLen).getSize()>maxLen)
								maxLen=(int)records.get(m+k*vLen).getSize();
						if(isAsc)n=0;
						else n=maxLen-1;
					}
					else
					{
						if(isAsc)
							++n;
						else
							--n;
					}
					for(int j=0;j<vLen;++j)
					{
						record=records.get(j+k*vLen);
						vName=varNames.get(j);
						if(record.getSize()>n)
						{
	                		if(GLCFSRecord.isValidData(vName, record.getDouble(n)))
	                		{
	                			sb.append(String.format("\"%s\":%.4f,", vName, isEnglish?Unit2English.Convert2En(record.getDouble(n), GLCFSRecord.getVariableUnitByAbbrev(vName)):record.getDouble(n)));
	                			sb.append(String.format("\"%sunit\":\"%s\",", vName, isEnglish?Unit2English.Convert2EnUnit(GLCFSRecord.getVariableUnitByAbbrev(vName)):GLCFSRecord.getVariableUnitByAbbrev(vName)));
	                		}
	                		else
	                			sb.append(String.format("\"%s\":\"%s\",", vName,NUL));
						}
						else
						{
							if(GLCFSRecord.isValidData(vName, record.getDouble(0)))
							{
	                			sb.append(String.format("\"%s\":%.4f,", vName, isEnglish?Unit2English.Convert2En(record.getDouble(0), GLCFSRecord.getVariableUnitByAbbrev(vName)):record.getDouble(0)));
	                			sb.append(String.format("\"%sunit\":\"%s\",", vName, isEnglish?Unit2English.Convert2EnUnit(GLCFSRecord.getVariableUnitByAbbrev(vName)):GLCFSRecord.getVariableUnitByAbbrev(vName)));
							}
	                		else
	                			sb.append(String.format("\"%s\":\"%s\",", vName,NUL));
						}
					}
					if(sb.charAt(sb.length()-1)==',')
						sb.deleteCharAt(sb.length()-1);
					sb.append("},");
					
					if(isAsc)++i;
					else --i;

				}
				if(sb.charAt(sb.length()-1)==',')
					sb.deleteCharAt(sb.length()-1);
							
			}
			else
			{
				sb.append('{');
				for(int j=0;j<vLen;++j)
				{
					record=records.get(j);
					vName=varNames.get(j);
					if(GLCFSRecord.isValidData(vName, record.getDouble(0)))
					{
						sb.append(String.format("\"%s\":%.4f,", vName, isEnglish?Unit2English.Convert2En(record.getDouble(0), GLCFSRecord.getVariableUnitByAbbrev(vName)):record.getDouble(0)));
						sb.append(String.format("\"%sunit\":\"%s\",", vName, isEnglish?Unit2English.Convert2EnUnit(GLCFSRecord.getVariableUnitByAbbrev(vName)):GLCFSRecord.getVariableUnitByAbbrev(vName)));
					}
					else
						sb.append(String.format("\"%s\":\"%s\",", vName,NUL));
				}
				if(sb.charAt(sb.length()-1)==',')
					sb.deleteCharAt(sb.length()-1);
				sb.append('}');
			}
			
			sb.append("]}");
			//response.setContentType("application/json");
			if(sb==null||sb.length()==0)
				response.getWriter().print("{}");
			else
				response.getWriter().print(sb.toString());
			response.getWriter().flush();
		}
		return result;
	}
}

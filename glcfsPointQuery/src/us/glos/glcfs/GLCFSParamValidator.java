package us.glos.glcfs;

import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import java.util.ArrayList;
import java.util.HashMap;

import org.joda.time.DateTimeZone;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import org.glc.IValidParam;
import org.glc.Logger;
import org.glc.xmlconfig.ConfigManager;
import org.glc.xmlconfig.LogLevel;
import org.glc.utils.Validation;
import org.glc.utils.GreatLakes;
import org.glc.utils.TimeZoneOffset;

import us.glos.glcfs.domain.DataQueryParam;
import us.glos.glcfs.domain.StartEndDateTimes;
import us.glos.obs.domain.ObsDataFormat;
import us.glos.obs.domain.UnitOfMeasure;

public class GLCFSParamValidator implements IValidParam {

	private static DateTimeFormatter DateTimeFormatter=null;
	private static final String PEROID_ID_FMT="nowcast:%d-%s";
	private static final String PEROID_ID_FMT_C="nowcast:current-%s";
	private static final String PEROID_ID_FMT_F="forecast:%d-%s";
	private static final String PEROID_ID_FMT_F_C="forecast:current-%s";
	private static final String PEROID_ID_FMT_FORCE="force:%d-%s";
	private static final String PEROID_ID_FMT_FORCE_C="force:current-%s";
	
	
	private static boolean isReady=false;
	private static final String TIME_ZONE_ID="TIME_ZONE";
	private static DateTimeZone TimeZone=null;
	private static final String TIME_ZONE_OFFSET_ID="TIME_ZONE_OFFSET_PARAM";
	private static String TimeZoneOffsetP=null;
	private static final String MAX_QUERY_DAYS_ID="MAX_QUERY_DAYS";
	private static int MaxDays=31;
	private static final String MAX_LATEST_HOURS_ID="MAX_LATEST_HOURS";
	private static int MaxLatestHour=1;
	private static final String START_DATE_PARAM_ID="START_DATE_PARAM";
	private static String StartDateP=null;
	private static final String END_DATE_PARAM_ID="END_DATE_PARAM";
	private static String EndDateP=null;
	private static final String VAR_PARAM_ID="VAR_PARAM";
	private static String VarP=null;
	private static final String CELL_X_PARAM_ID="CELL_X_PARAM";
	private static String XP=null;
	private static final String CELL_Y_PARAM_ID="CELL_Y_PARAM";
	private static String YP=null;
	private static final String SIGMA_PARAM_ID="SIGMA_PARAM";
	private static String SigmaP=null;
	private static final String LAKE_PARAM_ID="LAKE_PARAM";
	private static String LakeP=null;
	private static final String DELIMITER_ID="DELIMITER";
	private static String Delimiter=",";
	private static final String LATEST_PARAM_ID="LATEST_PARAM";
	private static String LatestP=null;
	private static final String HOUR_PARAM_ID="HOUR_PARAM";
	private static String HourP=null;
	private static final String UNIT_OF_MEASURE_PARAM_ID="UNIT_OF_MEASURE_PARAM";
	private static String UnitOfMeasureP=null;
	private static final String FORMAT_PARAM_ID="FORMAT_PARAM";
	private static String FormatP=null;
	private static final String CAST_TYPE_PARAM_ID="CAST_TYPE_PARAM";
	private static String CastTypeP=null;
	private static final String Preview_PARAM_ID="Preview_PARAM";
	private static String PreviewP=null;
	private static final String ORDER_PARAM_ID="ORDER_PARAM";
	private static String OrderP=null;
	private static final String REQUESTED_DEPTH_PARAM_ID="REQUESTED_DEPTH_PARAM";
	private static String RDepthP=null;
	private static final String FORCING_FILE_PARAM_ID="FORCING_FILE_PARAM";
	private static String ForcingFileP=null;
	static
	{
		try
		{
			if(null!=ConfigManager.getAppSetting(TIME_ZONE_ID))
				TimeZone=DateTimeZone.forID(ConfigManager.getAppSetting(TIME_ZONE_ID));
			else
				TimeZone=DateTimeZone.UTC;
			DateTimeFormatter=DateTimeFormat.forPattern("YYYY-MM-dd:HH:mm:ss Z");
			if(null!=ConfigManager.getAppSetting(TIME_ZONE_OFFSET_ID)
				&&!ConfigManager.getAppSetting(TIME_ZONE_OFFSET_ID).equals(""))
				TimeZoneOffsetP=ConfigManager.getAppSetting(TIME_ZONE_OFFSET_ID);
			else
				throw new IllegalArgumentException("TIME_ZONE_OFFSET_PARAM is not set.");
			
			if(null!=ConfigManager.getAppSetting(DELIMITER_ID)
					&&!ConfigManager.getAppSetting(DELIMITER_ID).equals(""))
				Delimiter=ConfigManager.getAppSetting(DELIMITER_ID);
			
			if(null!=ConfigManager.getAppSetting(MAX_QUERY_DAYS_ID)
					&&!ConfigManager.getAppSetting(MAX_QUERY_DAYS_ID).equals(""))
				MaxDays=Integer.parseInt(ConfigManager.getAppSetting(MAX_QUERY_DAYS_ID));
			if(MaxDays<=0)
				MaxDays=31;
			
			if(null!=ConfigManager.getAppSetting(MAX_LATEST_HOURS_ID)
					&&!ConfigManager.getAppSetting(MAX_LATEST_HOURS_ID).equals(""))
				MaxLatestHour=Integer.parseInt(ConfigManager.getAppSetting(MAX_LATEST_HOURS_ID));
			if(MaxLatestHour<=0&&MaxLatestHour>=8760)
				MaxLatestHour=1;
			
			if(null!=ConfigManager.getAppSetting(START_DATE_PARAM_ID)
					&&!ConfigManager.getAppSetting(START_DATE_PARAM_ID).equals(""))
				StartDateP=ConfigManager.getAppSetting(START_DATE_PARAM_ID);
			else
				throw new IllegalArgumentException("START_DATE_PARAM_ID is not set.");
			
			if(null!=ConfigManager.getAppSetting(END_DATE_PARAM_ID)
					&&!ConfigManager.getAppSetting(END_DATE_PARAM_ID).equals(""))
				EndDateP=ConfigManager.getAppSetting(END_DATE_PARAM_ID);
			else
				throw new IllegalArgumentException("END_DATE_PARAM_ID is not set.");
			
			if(null!=ConfigManager.getAppSetting(VAR_PARAM_ID)
					&&!ConfigManager.getAppSetting(VAR_PARAM_ID).equals(""))
				VarP=ConfigManager.getAppSetting(VAR_PARAM_ID);
			else
				throw new IllegalArgumentException("VAR_PARAM_ID is not set.");
			
			if(null!=ConfigManager.getAppSetting(LATEST_PARAM_ID)
					&&!ConfigManager.getAppSetting(LATEST_PARAM_ID).equals(""))
				LatestP=ConfigManager.getAppSetting(LATEST_PARAM_ID);
			else
				throw new IllegalArgumentException("LATEST_PARAM is not set.");
			
			if(null!=ConfigManager.getAppSetting(HOUR_PARAM_ID)
					&&!ConfigManager.getAppSetting(HOUR_PARAM_ID).equals(""))
				HourP=ConfigManager.getAppSetting(HOUR_PARAM_ID);
			else
				throw new IllegalArgumentException("HOUR_PARAM is not set.");
			
			if(null!=ConfigManager.getAppSetting(CELL_X_PARAM_ID)
					&&!ConfigManager.getAppSetting(CELL_X_PARAM_ID).equals(""))
				XP=ConfigManager.getAppSetting(CELL_X_PARAM_ID);
			else
				throw new IllegalArgumentException("CELL_X_PARAM is not set.");
			
			if(null!=ConfigManager.getAppSetting(CELL_Y_PARAM_ID)
					&&!ConfigManager.getAppSetting(CELL_Y_PARAM_ID).equals(""))
				YP=ConfigManager.getAppSetting(CELL_Y_PARAM_ID);
			else
				throw new IllegalArgumentException("CELL_Y_PARAM is not set.");
			
			if(null!=ConfigManager.getAppSetting(SIGMA_PARAM_ID)
					&&!ConfigManager.getAppSetting(SIGMA_PARAM_ID).equals(""))
				SigmaP=ConfigManager.getAppSetting(SIGMA_PARAM_ID);
			else
				throw new IllegalArgumentException("SIGMA_PARAM is not set.");
			
			if(null!=ConfigManager.getAppSetting(LAKE_PARAM_ID)
					&&!ConfigManager.getAppSetting(LAKE_PARAM_ID).equals(""))
				LakeP=ConfigManager.getAppSetting(LAKE_PARAM_ID);
			else
				throw new IllegalArgumentException("LAKE_PARAM is not set.");
			
			if(null!=ConfigManager.getAppSetting(UNIT_OF_MEASURE_PARAM_ID)
					&&!ConfigManager.getAppSetting(UNIT_OF_MEASURE_PARAM_ID).equals(""))
				UnitOfMeasureP=ConfigManager.getAppSetting(UNIT_OF_MEASURE_PARAM_ID);
			else
				throw new IllegalArgumentException("UNIT_OF_MEASURE_PARAM is not set.");
			
			if(null!=ConfigManager.getAppSetting(FORMAT_PARAM_ID)
					&&!ConfigManager.getAppSetting(FORMAT_PARAM_ID).equals(""))
				FormatP=ConfigManager.getAppSetting(FORMAT_PARAM_ID);
			else
				throw new IllegalArgumentException("FORMAT_PARAM is not set.");
			
			if(null!=ConfigManager.getAppSetting(CAST_TYPE_PARAM_ID)
					&&!ConfigManager.getAppSetting(CAST_TYPE_PARAM_ID).equals(""))
				CastTypeP=ConfigManager.getAppSetting(CAST_TYPE_PARAM_ID);
			else
				throw new IllegalArgumentException("CAST_TYPE_PARAM is not set.");
			
			if(null!=ConfigManager.getAppSetting(Preview_PARAM_ID)
					&&!ConfigManager.getAppSetting(Preview_PARAM_ID).equals(""))
				PreviewP=ConfigManager.getAppSetting(Preview_PARAM_ID);
			
			if(null!=ConfigManager.getAppSetting(ORDER_PARAM_ID)
					&&!ConfigManager.getAppSetting(ORDER_PARAM_ID).equals(""))
				OrderP=ConfigManager.getAppSetting(ORDER_PARAM_ID);
			
			if(null!=ConfigManager.getAppSetting(REQUESTED_DEPTH_PARAM_ID)
					&&!ConfigManager.getAppSetting(REQUESTED_DEPTH_PARAM_ID).equals(""))
				RDepthP=ConfigManager.getAppSetting(REQUESTED_DEPTH_PARAM_ID);
			else
				throw new IllegalArgumentException("REQUESTED_DEPTH_PARAM is not set.");
			
			if(null!=ConfigManager.getAppSetting(FORCING_FILE_PARAM_ID)
					&&!ConfigManager.getAppSetting(FORCING_FILE_PARAM_ID).equals(""))
				ForcingFileP=ConfigManager.getAppSetting(FORCING_FILE_PARAM_ID);
			else
				throw new IllegalArgumentException("FORCING_FILE_PARAM_ID is not set.");
			
			isReady=true;
		}
		catch(IllegalArgumentException iae)
		{
			Logger.writeLog(GLCFSParamValidator.class.getName()+" init failed: "+iae.getMessage(), LogLevel.SEVERE);
			isReady=false;
		}
		
	}
	@Override
	public boolean IsParamValid(ServletRequest request, ServletResponse response) {
		// TODO Auto-generated method stub
		if(isReady)
		{
			HttpServletRequest req=(HttpServletRequest)request;
			HttpServletResponse rsp=(HttpServletResponse)response;
			String sc=req.getServletPath();
			if(sc.equals("/glcfsps.glos"))
			{
				return isGLCFSParamValid(req,rsp);
			}
			else
				return true;
		}
		else
			return true;
	}
	private boolean isGLCFSParamValid(HttpServletRequest request,HttpServletResponse response)
	{
		String lake=null;
		ArrayList<String> urls=null;
		ArrayList<StartEndDateTimes> periods=null;
		ArrayList<Integer> hours=null;
		long interval=0;
		double x=0.,y=0.;
		int sigma=-1;
		double rdepth=-1.;
		int tOffset=0;
		ArrayList<String> mVars=null;
		String currentCastFmt=PEROID_ID_FMT_C;
		String archiveCastFmt=PEROID_ID_FMT;
		boolean isNowcast=true;
		boolean isInFile=false;
		boolean isLatest=false;
		if(Validation.basicValidation(request, LakeP)
			&&Validation.basicValidation(request, VarP)
			&&Validation.basicValidation(request, XP)
			&&Validation.basicValidation(request, YP))
		{
			try
			{
				x=Double.parseDouble(request.getParameter(XP).trim());//Integer.parseInt(request.getParameter(XP).trim());
				y=Double.parseDouble(request.getParameter(YP).trim());//Integer.parseInt(request.getParameter(YP).trim());
			}
			catch(NumberFormatException nfe)
			{
				return false;
			}
			//if(x<0||y<0)
			if(y<0)
				return false;
			String lt=request.getParameter(LakeP);
			
			for(String l:GreatLakes.Lakes)
				if(lt.trim().equalsIgnoreCase(l))
				{
					lake=l;
					break;
				}
			if(lake!=null)
			{
				if(Validation.basicValidation(request, CastTypeP))
				{
					if(request.getParameter(CastTypeP).trim().equalsIgnoreCase("forecast"))
					{
						currentCastFmt=PEROID_ID_FMT_F_C;
						archiveCastFmt=PEROID_ID_FMT_F;
						isNowcast=false;
					}
					
				}
				if(Validation.basicValidation(request, ForcingFileP))
				{
					currentCastFmt=PEROID_ID_FMT_FORCE_C;
					archiveCastFmt=PEROID_ID_FMT_FORCE;
					isInFile=true;
				}
				String[] vtemp=request.getParameter(VarP).split(Delimiter);
				if(vtemp!=null&&vtemp.length>0)
				{
					
					if(Validation.basicValidation(request, SigmaP))
					{
						try
						{
							sigma=Integer.parseInt(request.getParameter(SigmaP));
						}
						catch(NumberFormatException nef)
						{
							return false;
						}
					}
					else if(Validation.basicValidation(request, RDepthP))
					{
						try
						{
							rdepth=Double.parseDouble(request.getParameter(RDepthP));
						}
						catch(NumberFormatException nef)
						{
							return false;
						}
					}
					String varKey="2d";
					if(isInFile)
					{
						if(isNowcast)
							varKey="force-nowcast";
						else
							varKey="force-forecast";
					}
					else if(sigma>=0||rdepth>=0)
						varKey="3d";
					ArrayList<String> vars=ConfigManager.getThreddsDatasetVariablesSorted(lake, varKey);
					if(vars!=null&&vars.size()>0)
					{
						//ArrayList<String> vList=null;
						/*if(vtemp.length>1)
						{
							vList=new ArrayList<String>(vars.size());
							java.util.Arrays.sort(vtemp);
							int i=0;
							for(;i<vtemp.length-1;++i)
							{
								if(false==vtemp[i].equals(vtemp[i+1]))
									vList.add(vtemp[i]);
							}
							vList.add(vtemp[i]);
						}
						else
						{
							vList=new ArrayList<String>(1);
							vList.add(vtemp[0]);
						}*/
						//for(String v:vList)
						
						HashMap<String,String> existVarMap=new HashMap<String,String>();
						for(String v:vtemp)
						{
							if(0<=java.util.Collections.binarySearch(vars, v)&&!existVarMap.containsKey(v))
							{
								if(mVars==null)
									//mVars=new ArrayList<String>(vList.size());
									mVars=new ArrayList<String>();
								mVars.add(v);
								existVarMap.put(v, "");
							}
						}
						if(mVars!=null&&mVars.size()>0)
						{
							int th=0;
							DateTime endD=null;
							DateTime startD=null;
							if(Validation.basicValidation(request, TimeZoneOffsetP))
							{
								try
								{
									tOffset=Integer.parseInt(request.getParameter(TimeZoneOffsetP).trim());
								}
								catch(NumberFormatException nfe)
								{
									return false;
								}
								if(tOffset>0||tOffset<-10)
									tOffset=0;
							}
							if(mVars.size()>0&&Validation.basicValidation(request, LatestP))
							{
								if(Validation.basicValidation(request, HourP))
								{
									th=Integer.parseInt(request.getParameter(HourP).trim());
									if(th<=0||th>MaxLatestHour)
										th=1;
									isLatest=true;
								}
							}
							else if(mVars.size()>0&&Validation.basicValidation(request, StartDateP)
									&&Validation.basicValidation(request, EndDateP))
							{
								String sD=String.format("%s %s", request.getParameter(StartDateP).trim(),TimeZoneOffset.GetOffsetString(tOffset));
								String eD=String.format("%s %s",request.getParameter(EndDateP).trim(),TimeZoneOffset.GetOffsetString(tOffset));
								//startD=DateTimeFormatter.withZone(DateTimeZone.forOffsetHours(tOffset)).parseDateTime(sD);
								//endD=DateTimeFormatter.withZone(DateTimeZone.forOffsetHours(tOffset)).parseDateTime(eD);
								try
								{
									startD=DateTimeFormatter.withZone(TimeZone).parseDateTime(sD);
									//make sure we have time right on XX:00:00
									if(startD!=null)
										startD=new DateTime(startD.getYear(),startD.getMonthOfYear(),startD.getDayOfMonth(),startD.getHourOfDay(),0,0,0,TimeZone);
									endD=DateTimeFormatter.withZone(TimeZone).parseDateTime(eD);
									if(endD!=null)
										endD=new DateTime(endD.getYear(),endD.getMonthOfYear(),endD.getDayOfMonth(),endD.getHourOfDay(),0,0,0,TimeZone);
								}
								catch(IllegalArgumentException e)
								{
									startD=null;
									endD=null;
								}
							}
							if(th>0||(startD!=null&&endD!=null&&(endD.isAfter(startD)||endD.isEqual(startD))))
							{
								if(th>0)
								{
									endD=new DateTime(TimeZone);
									if(isNowcast)
									{
										endD=new DateTime(endD.getYear(),endD.getMonthOfYear(),endD.getDayOfMonth(),endD.getHourOfDay(),0,0,0,TimeZone);
										startD=endD.minusHours(th-1);
									}
									else
									{
										startD=new DateTime(endD.getYear(),endD.getMonthOfYear(),endD.getDayOfMonth(),endD.getHourOfDay(),0,0,0,TimeZone);
										endD=startD.plusHours(th-1);
									}
								}
								//GLCFS specific 2011 January 1 00:00:00 belongs to 2010 archive
								DateTime nowD=new DateTime(TimeZone);
								DateTime startBaseD=new DateTime(startD.getYear(),1,1,0,0,0,0,TimeZone);
								int startY=startD.getYear();
								int endY=endD.getYear();
								int nowY=nowD.getYear();
								urls=new ArrayList<String>(endY-startY+1);
								periods=new ArrayList<StartEndDateTimes>(endY-startY+2);
								
								if(startD.getMillis()==startBaseD.getMillis())
								{
									periods.add(new StartEndDateTimes(startD,startD));//(startD.minusHours(1),startD));
									startD=startD.plusHours(1);
									if(isInFile)
										urls.add(ConfigManager.getThreddsDatasetURL(lake, String.format(archiveCastFmt,startY-1,isNowcast?"nowcast":"forecast")));
									else
										urls.add(ConfigManager.getThreddsDatasetURL(lake, String.format(archiveCastFmt,startY-1,varKey)));
								}
								do
								{
									if(endY-startY>0)
									{
										DateTime temp=new DateTime(startY+1,1,1,0,0,0,0,TimeZone);
										periods.add(new StartEndDateTimes(startD,temp));
										startD=temp.plusHours(1);
										if(isInFile)
											urls.add(ConfigManager.getThreddsDatasetURL(lake, String.format(archiveCastFmt,startY,isNowcast?"nowcast":"forecast")));
										else
											urls.add(ConfigManager.getThreddsDatasetURL(lake, String.format(archiveCastFmt,startY,varKey)));
									}
									else
									{
										if(endD.getMillis()>=startD.getMillis())
										{
											periods.add(new StartEndDateTimes(startD,endD));
											if(endY==nowY)
											{
												if(isInFile)
													urls.add(ConfigManager.getThreddsDatasetURL(lake, String.format(currentCastFmt,isNowcast?"nowcast":"forecast")));
												else
													urls.add(ConfigManager.getThreddsDatasetURL(lake, String.format(currentCastFmt,varKey)));
											}
											else
											{
												if(isInFile)
													urls.add(ConfigManager.getThreddsDatasetURL(lake, String.format(archiveCastFmt,endY,isNowcast?"nowcast":"forecast")));
												else
													urls.add(ConfigManager.getThreddsDatasetURL(lake, String.format(archiveCastFmt,endY,varKey)));
											}
										}
									}
									++startY;
									
								}while(endY-startY>=0);
								UnitOfMeasure uom=UnitOfMeasure.Metric;
								ObsDataFormat format=ObsDataFormat.JSON;
								String temp=null;
								if(Validation.basicValidation(request, UnitOfMeasureP))
								{
									temp=request.getParameter(UnitOfMeasureP).trim();
									if(temp.equalsIgnoreCase("e"))
										uom=UnitOfMeasure.English;
								}
								if(Validation.basicValidation(request, FormatP))
								{
									temp=request.getParameter(FormatP).trim();
									if(temp.equalsIgnoreCase("csv"))
										format=ObsDataFormat.CSV;
									else if(temp.equalsIgnoreCase("tab"))
										format=ObsDataFormat.TAB;
									else if(temp.equalsIgnoreCase("image"))
										format=ObsDataFormat.IMAGE;
									else if(temp.equalsIgnoreCase("rss"))
										format=ObsDataFormat.GeoRSS;
								}
																
								interval=ConfigManager.getThreddsDatasetReportInterval(lake, varKey);
								DataQueryParam dqp=new DataQueryParam(isNowcast,isInFile, urls, periods,lake, interval, tOffset, x, y, sigma, rdepth, uom,format,isLatest);
								dqp.addVariables(mVars);
								if(PreviewP!=null&&Validation.basicValidation(request, PreviewP))
									dqp.setPreview(true);
								if(OrderP!=null&&Validation.basicValidation(request, OrderP))
									if(request.getParameter(OrderP).equalsIgnoreCase("asc"))
										dqp.setAsc(true);
								if(Validation.basicValidation(request, "gi"))
									dqp.setDisplayIndex(true);
								if(Validation.basicValidation(request, "doy"))
									dqp.setDisplayDOY(true);
								request.setAttribute(DataQueryParam.GetName(), dqp);
								return true;
							}
						}
					}
				}
			}
		}
		return false;
	}
	@Override
	public void dispose() {
		// TODO Auto-generated method stub

	}

}

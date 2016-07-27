package us.glos.glcfs.serlvet;

import java.io.IOException;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import java.util.ArrayList;
import java.util.HashMap;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import ucar.ma2.Array;
import ucar.ma2.InvalidRangeException;
import ucar.ma2.Range;
import ucar.nc2.Dimension;
import ucar.nc2.Variable;
import ucar.nc2.dataset.NetcdfDataset;
import ucar.nc2.dataset.NetcdfDataset.Enhance;

import org.glc.xmlconfig.ConfigManager;
import org.glc.xmlconfig.LogLevel;
import org.glc.Logger;
import org.glc.domain.Coordinate;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;


import us.glos.glcfs.domain.DataQueryParam;
import us.glos.glcfs.domain.IndexPair;
import us.glos.glcfs.domain.StartEndDateTimes;
import us.glos.glcfs.domain.GLCFSRecord;
import us.glos.glcfs.helpers.GeoRSSHelper;
import us.glos.glcfs.helpers.TextHelper;
import us.glos.glcfs.helpers.GLCFSGrid;
import us.glos.obs.domain.*;

/**
 * Servlet implementation class PointSourceServlet
 */
public final class GLCFSPointSourceServlet extends HttpServlet {
	
    /**
	 * 
	 */
	private static final long serialVersionUID = -1115901643495872356L;
	private static final String DIM_X_NAME="nx";
	private static final String DIM_Y_NAME="nx";
	private static final String DIM_TIME_NAME="time";
	private static final String DIM_SIGMA_NAME="nsigma";
	private static final String VAR_LON_NAME="lon";
	private static final String VAR_LAT_NAME="lat";
	private static final String VAR_TIME_NAME="time";
	private static final String VAR_SIGMA_NAME="sigma";
	private static final String VAR_DEPTH_NAME="depth";
	private static final String VAR_DEPTH_AT_NODE_NAME="d3d";
	private static final String TIME_ZONE_ID="TIME_ZONE";
	private static final double PLACE_HOLDER=-99999.0;
	private Set<Enhance> DATASET_ENHANCEMENTS =EnumSet.of(Enhance.ScaleMissingDefer);//, Enhance.CoordSystems);
	private static DateTimeZone TimeZone=null;
	/**
     * @see HttpServlet#HttpServlet()
     */
    public GLCFSPointSourceServlet() {
        super();
        // TODO Auto-generated constructor stub
    }

	/**
	 * @see Servlet#init(ServletConfig)
	 */
	public void init(ServletConfig config) throws ServletException {
		// TODO Auto-generated method stub
		if(null!=ConfigManager.getAppSetting(TIME_ZONE_ID))
			TimeZone=DateTimeZone.forID(ConfigManager.getAppSetting(TIME_ZONE_ID));
		else
			TimeZone=DateTimeZone.UTC;
	}

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
		if(request.getAttribute(DataQueryParam.GetName())!=null
			&&request.getAttribute(DataQueryParam.GetName()) instanceof DataQueryParam)
		{
			DataQueryParam dqp=(DataQueryParam)request.getAttribute(DataQueryParam.GetName());
			NetcdfDataset nc=null;
			ArrayList<Variable> targetVars=new ArrayList<Variable>(dqp.getVariables().size());
			ucar.ma2.Array placeholders=null;
			if(dqp.getUrl()!=null&&dqp.getPeriods()!=null&&dqp.getUrl().size()==dqp.getPeriods().size())
			{
				int pcount=0;
				ArrayList<Array> dataList=new ArrayList<Array>();
				double d3d=0;
				boolean isD3dSet=false;
				double lon=0;
				boolean isLonSet=false;
				double lat=0;
				boolean isLatSet=false;
				ArrayList<String> varNameList=null;
				Range sigmaRange=null;
				ArrayList<Long> timeArray=null;
				ArrayList<IndexPair> indexArray=null;
				IndexPair indexPair=null;
				double maxBathy=0.;
				boolean isDepthExceedAll=true;
				if(dqp.isDisplayIndex()&&(dqp.getFormat()==ObsDataFormat.CSV||dqp.getFormat()==ObsDataFormat.TAB))
					indexArray=new ArrayList<IndexPair>(dqp.getUrl().size());
				
				for(String url:dqp.getUrl())
				{
					if(url==null)
					{
						++pcount;
						continue;
					}
					if(targetVars.size()>0)targetVars.clear();
					try{
					nc=NetcdfDataset.openDataset(url,				
							DATASET_ENHANCEMENTS,
							-1, // use default buffer size
							null, // no CancelTask
							null // no iospMessage
					);
					}catch(Exception e){
						Logger.writeLog("Requested URL: "+url, LogLevel.SEVERE);
					}
					if(nc!=null)
					{
						StartEndDateTimes period=dqp.getPeriods().get(pcount++);
						List<Variable> vl=nc.getVariables();
						HashMap<String,Variable> varMap=null;
						Variable lonVar=null;
						Variable latVar=null;
						Variable timeVar=null;
						Variable sigmaVar=null;
						Variable depthVar=null;
						Variable depthAtNodeVar=null;
						Array lonArray=null;
						Array latArray=null;
						//Array sigmaArray=null;
						Array depthArray=null;
						Range timeRange=null;
						Range yRange=null;
						Range xRange=null;
						List<Range> ranges=null;
						
						
						
						
						boolean isClosed=false;
						if(vl!=null&&vl.size()>0)
						{
							varMap=new HashMap<String,Variable>(vl.size());
							
							for(Variable v:vl)
							{
								//get 4 basic variables first
								if(v.getName().equalsIgnoreCase(VAR_LON_NAME))
									lonVar=v;
								else if(v.getName().equalsIgnoreCase(VAR_LAT_NAME))
									latVar=v;
								else if(v.getName().equalsIgnoreCase(VAR_TIME_NAME))
									timeVar=v;
								else if(v.getName().equalsIgnoreCase(VAR_SIGMA_NAME))
									sigmaVar=v;
								else if(v.getName().equalsIgnoreCase(VAR_DEPTH_NAME))
									depthVar=v;
								else if(v.getName().equalsIgnoreCase(VAR_DEPTH_AT_NODE_NAME))
									depthAtNodeVar=v;
								varMap.put(v.getName(), v);
							}
							if(lonVar!=null&&latVar!=null&&timeVar!=null&&sigmaVar!=null)
							{
								placeholders=null;
								boolean hasMissingVars=false;
								HashMap<String,ucar.ma2.Array> varsData=new HashMap<String,ucar.ma2.Array>();
								for(String sv:dqp.getVariables())
								{
									if(varMap.containsKey(sv))
										targetVars.add(varMap.get(sv));
									varsData.put(sv, null);
								}
								hasMissingVars=(dqp.getVariables().size()!=targetVars.size());
								if(targetVars.size()>0)
								{
									int counts=(int)(1+(period.endDateTime.getMillis()-period.startDateTime.getMillis())/dqp.getInterval());

									try
									{
										ranges=new ArrayList<Range>();
										varNameList=new ArrayList<String>(targetVars.size());
										Coordinate coord=GLCFSGrid.GetCoordInXY(true, dqp.getCellI(), dqp.getCellJ(), String.format("%s",period.startDateTime.getYear()), dqp.getId());
										if(coord==null||coord.getLat()==-1.||coord.getLon()==-1.)
											continue;
										int y=(int)Math.floor(coord.getLat());
										int x=(int)Math.floor(coord.getLon());
										yRange=new Range(y,y);
										xRange=new Range(x,x);
										if(indexArray!=null)
										{
											indexPair=new IndexPair();
											indexPair.I=x;
											indexPair.J=y;
										}
										for(Variable v:targetVars)
										{
											if(ranges.size()>0)
												ranges.clear();
											List<Dimension> dims=v.getDimensions();
											//fill in time and nsigma dimension first
											//if this variable doesn't have time dimension, it's OK
											for(Dimension d:dims)
											{
												if(d.getName().equalsIgnoreCase(DIM_TIME_NAME))
												{
													if(timeRange==null)
													{
														List<Range> tPeekRange=new ArrayList<Range>(1);
														int ulmtDimCnt=d.getLength();
														if(ulmtDimCnt==0)
														{
															nc.close();
															return;
														}
														tPeekRange.add(new Range(ulmtDimCnt-1,ulmtDimCnt-1));
														Array tArr=timeVar.read(tPeekRange);
														if(tArr==null||tArr.getSize()==0)
														{	
															nc.close();
															return;
														}
														int it=0;
														DateTime endD=null;
														if(dqp.isNowcast())
															endD=new DateTime(tArr.getLong(0)*1000,TimeZone);
														else//forecast
														{
															//if(dqp.isInFile())
															//	endD=new DateTime(tArr.getLong(0)*1000,TimeZone);
															//else
															{
																endD=new DateTime(new DateTime().getYear(),1,1,0,0,0,0,TimeZone);
																endD=endD.plusHours((int)tArr.getLong(0));
															}
														}
														if(dqp.isLatest())
														{
															it=0;
															int hourDiff=(int)((period.endDateTime.getMillis()-period.startDateTime.getMillis())/3600000);
															if(dqp.isNowcast())
															{
																period.endDateTime=endD;
																period.startDateTime=endD.minusHours(hourDiff);
															}
														}
														else
															it=(int)((endD.getMillis()-period.endDateTime.getMillis())/dqp.getInterval());
														//in case the current date is later than the most recent model output
														if(it<0)
															it=0;
														int startIdx=ulmtDimCnt-it-counts;
														int endIdx=ulmtDimCnt-1-it;
														//in case the model has gaps on timestamps
														if(startIdx<0||endIdx<0)
														{
															/*if(startIdx<0)startIdx=0;
															if(endIdx<0)endIdx=counts-1;*/
															startIdx=0;
															endIdx=counts>=ulmtDimCnt-1?ulmtDimCnt-1:counts;
														}
														else
														{
															tPeekRange.clear();
															tPeekRange.add(new Range(startIdx,startIdx));
															tArr=timeVar.read(tPeekRange);
															if(tArr==null||tArr.getSize()==0)
															{	
																nc.close();
																return;
															}
															DateTime sd=null;
															if(dqp.isNowcast())
																sd=new DateTime(tArr.getLong(0)*1000,TimeZone);
															else//forecast
															{
																//if(dqp.isInFile())
																//{
																//	sd=new DateTime(tArr.getLong(0)*1000,TimeZone);
																//}
																//else
																{
																	sd=new DateTime(new DateTime().getYear(),1,1,0,0,0,0,TimeZone);
																	sd=sd.plusHours((int)tArr.getLong(0));
																}
															}
																
															it=(int)((period.startDateTime.getMillis()-sd.getMillis())/dqp.getInterval());
															startIdx+=it;
															if(startIdx>ulmtDimCnt-1)
																startIdx=ulmtDimCnt-counts;
															tPeekRange.clear();
															tPeekRange.add(new Range(endIdx,endIdx));
															tArr=timeVar.read(tPeekRange);
															if(tArr==null||tArr.getSize()==0)
															{	
																nc.close();
																return;
															}
															if(dqp.isNowcast())
																sd=new DateTime(tArr.getLong(0)*1000,TimeZone);
															else//forecast
															{
																//if(dqp.isInFile())
																//{
																//	sd=new DateTime(tArr.getLong(0)*1000,TimeZone);
																//}
																//else
																{
																	sd=new DateTime(new DateTime().getYear(),1,1,0,0,0,0,TimeZone);
																	sd=sd.plusHours((int)tArr.getLong(0));
																}
															}
															it=(int)((period.endDateTime.getMillis()-sd.getMillis())/dqp.getInterval());
															endIdx+=it;
															if(endIdx<startIdx)
																endIdx=startIdx+counts>ulmtDimCnt?ulmtDimCnt-1:startIdx+counts-1;
															else if(endIdx>ulmtDimCnt-1)
																endIdx=ulmtDimCnt-1;
														}
														timeRange=new Range(startIdx,endIdx);
														//estimate the number of placeholders we have to put for nonexist vars
														//only if there are existing vars in the same query set.
													}
													ranges.add(timeRange);
												}
												else if(dqp.is3D()&&d.getName().equalsIgnoreCase(DIM_SIGMA_NAME))
												{
													if(sigmaRange==null)
													{
														if(dqp.isQueryByDepth())
														{
															/*ArrayList<Range> d3dRanges=new ArrayList<Range>();
															d3dRanges.add(new Range(0,d.getLength()-1));
															d3dRanges.add(yRange);
															d3dRanges.add(xRange);
															Array d3da=depthAtNodeVar.read(d3dRanges);
															if(d3da==null||d3da.getSize()==0)
															{
																nc.close();
																return;
															}
															double[] d3dArr=new double[(int)d3da.getSize()];
															int i=0;
															for(i=0;i<d3da.getSize();++i)
																d3dArr[i]=d3da.getDouble(i)*(-1.);
															java.util.Arrays.sort(d3dArr);
															for(i=0;i<d3dArr.length;++i)
																if(dqp.getRequestedDepth()<d3dArr[i])
																	break;
															if(i==d3dArr.length)--i;
															sigmaRange=new Range(i,i);*/
															ArrayList<Range> bathyRanges=new ArrayList<Range>();
															bathyRanges.add(yRange);
															bathyRanges.add(xRange);
															Array depthA=depthVar.read(bathyRanges);
															if(depthA==null||depthA.getSize()==0)
															{
																nc.close();
																return;
															}
															int i=GLCFSRecord.getSigmaLevel(dqp.getId(), dqp.getRequestedDepth(), depthA.getDouble(0));
															
															if(i>=d.getLength())
															{
																i=d.getLength()-1;
																if(depthA.getDouble(0)>maxBathy)maxBathy=depthA.getDouble(0);
																//dqp.setRequestedDepth(maxBathy);
																
															}
															else
																isDepthExceedAll=false;
															sigmaRange=new Range(i,i);
														}
														else
														{
															if(dqp.getSigmaLevel()>=0&&dqp.getSigmaLevel()<d.getLength())
																sigmaRange=new Range(dqp.getSigmaLevel(),dqp.getSigmaLevel());
															else
															{
																if(dqp.getSigmaLevel()<0)
																	sigmaRange=new Range(0,0);
																else
																	sigmaRange=new Range(d.getLength()-1,d.getLength()-1);
															}
														}
													}
													ranges.add(sigmaRange);
												}
											}
											
											//if(yRange==null)
											//yRange=new Range(y,y);
																//(dqp.getCellJ(),dqp.getCellJ());
											ranges.add(yRange);
											//if(xRange==null)
											//xRange=new Range(x,x);
													//(dqp.getCellI(),dqp.getCellI());
											ranges.add(xRange);
											
											varNameList.add(v.getName());
											if(true == hasMissingVars && null==placeholders && null != timeRange)
											{
													//we made two assumptions here
													//1. All data have the identical type: double...
													//2. All placeholders have same length, so we only populate it once per url (per year)
													double[] tmpA=new double[timeRange.last()-timeRange.first()+1];
													java.util.Arrays.fill(tmpA, PLACE_HOLDER);
													placeholders=ucar.ma2.Array.factory(tmpA);
											}
											if(true == hasMissingVars)
												varsData.put(v.getName(), v.read(ranges));
											else
											    dataList.add(v.read(ranges));
										}
										if(true == hasMissingVars)
										{
										    for(String sv:dqp.getVariables())
										    {
											    if(null!=varsData.get(sv))
												    dataList.add(varsData.get(sv));
											    else
												    dataList.add(placeholders);
										    }
										}
										if(dataList.size()>0)
										{
											if(ranges.size()>0)
												ranges.clear();
											
											if(sigmaRange!=null)
											{
												/*double sigmaVal=0;
												ranges.add(sigmaRange);
												sigmaArray=sigmaVar.read(ranges);
												if(sigmaArray!=null&&sigmaArray.getSize()>0)
												{
													sigmaVal=sigmaArray.getDouble(0);
													ranges.clear();
													ranges.add(yRange);
													ranges.add(xRange);
													depthArray=depthVar.read(ranges);
													if(depthArray!=null&&depthArray.getSize()>0)
														depth=depthArray.getDouble(0)*sigmaVal;
												}*/
												ranges.add(sigmaRange);
												ranges.add(yRange);
												ranges.add(xRange);
												depthArray=depthAtNodeVar.read(ranges);
												if(depthArray!=null&&depthArray.getSize()>0)
													d3d=depthArray.getDouble(0);
												
											}
											if(!isLonSet||!isLatSet)
											{
												if(ranges.size()==0)
												{
													ranges.add(yRange);
													ranges.add(xRange);
												}
												else
													ranges.remove(0);
											}
											if(!isLonSet)
											{
												lonArray=lonVar.read(ranges);
												if(lonArray!=null&&lonArray.getSize()>0)
												{
													lon=lonArray.getDouble(0);
													isLonSet=true;
												}
											}
											if(!isLatSet)
											{
												latArray=latVar.read(ranges);
												if(latArray!=null&&latArray.getSize()>0)
													lat=latArray.getDouble(0);
											}
											ranges.clear();
											if(timeRange!=null)
											{
												ranges.add(timeRange);
												Array temp=timeVar.read(ranges);
												if(temp!=null&&temp.getSize()>0)
												{
													if(timeArray==null)	
														timeArray=new ArrayList<Long>();
													for(int i=0;i<temp.getSize();++i)
													{
														timeArray.add(temp.getLong(i));
														if(indexArray!=null)
															indexArray.add(indexPair);
													}
												}
											}
										}
									}
									catch (InvalidRangeException e) {
										// TODO Auto-generated catch block
										Logger.writeLog(this.getClass().getName()+": "+e.getMessage(), LogLevel.SEVERE);
										Logger.writeLog(this.getClass().getName()+": Query String -- "+request.getQueryString(), LogLevel.SEVERE);
									}
									finally
									{
										nc.close();
										isClosed=true;
									}
								}
							}
						}
						if(!isClosed)
							nc.close();
						
						if(dqp.isQueryByDepth())
							sigmaRange=null;
						
					}
					
				}//next url
				//if requested depth exceeds the bathy value of the cell, pick the largest bathy value as requested depth
				//especially query is across years. Bathy value may vary due to grid change.
				if(dqp.isQueryByDepth()&&maxBathy>0.&&isDepthExceedAll)
					dqp.setRequestedDepth(maxBathy);
				ObsDataFormat format=dqp.getFormat();
				//UnitOfMeasure unit=dqp.getUnitOfMeasure();
				if(timeArray!=null&&timeArray.size()>0)
				{
					//this occurs if we only query against one or multiple years' data that happen
					//not to contain certain variables that were defined in later years.
					//For example: utm,vtm for Superior was added in 2015, if we query
					//these two along with other vars, say uc/vc, for 2014 only, due to the logic
					//of this piece of shit, we will end up have 2 vars in varNameList but 4 arrays
					//in dataList with two of them are garbage...
					if(0 != dataList.size()%varNameList.size() && 0 == dataList.size()%dqp.getVariables().size())
					{
						varNameList.clear();
						for(String sv:dqp.getVariables())
							varNameList.add(sv);
					}
					//time aggregation
					//HashMap<Long,GLCFSRecord> records=GLCFSRecord.CreateGLCFSRecord(dataList, varNameList, timeArray, lon, lat, d3d);
					if(format==ObsDataFormat.CSV||format==ObsDataFormat.TAB)
					{
						TextHelper.GetTextFile(response, timeArray, 
								varNameList, dataList, 
								TimeZone, dqp.getCellI(), dqp.getCellJ(), dqp.getTimeZoneOffset(), 
								d3d, 
								dqp.is3D(), dqp.getRequestedDepth(),
								dqp.getUnitOfMeasure()==UnitOfMeasure.English, 
								dqp.isNowcast(),dqp.isInFile(),format==ObsDataFormat.CSV,
								dqp.isPreview(),dqp.isAsc(),indexArray,dqp.isDisplayDOY(),dqp.getId());
					}
					else if(format==ObsDataFormat.JSON)
					{
						TextHelper.GetJSONString(response, timeArray, 
								varNameList, dataList, 
								TimeZone, lon, lat, dqp.getTimeZoneOffset(), 
								dqp.getRequestedDepth()>=0?dqp.getRequestedDepth():d3d, 
								dqp.is3D(), 
								dqp.getUnitOfMeasure()==UnitOfMeasure.English, 
								dqp.isNowcast(),dqp.isInFile(),dqp.isAsc());
					}
					else if(format==ObsDataFormat.IMAGE)
					{
						
					}
					else if(format==ObsDataFormat.GeoRSS)
					{
						GeoRSSHelper.GetGeoRSS(response, timeArray, 
								varNameList, dataList, 
								TimeZone, dqp.getCellI(), dqp.getCellJ(), dqp.getTimeZoneOffset(), 
								d3d, 
								dqp.is3D(),dqp.getRequestedDepth(), 
								dqp.getUnitOfMeasure()==UnitOfMeasure.English, 
								dqp.isNowcast(),dqp.isInFile(), dqp.isLatest(),dqp.isAsc(),dqp.isDisplayDOY(),dqp.getId());
					}
					return;
				}
				else
				{
					//query depth or d3d only
					if(format==ObsDataFormat.CSV||format==ObsDataFormat.TAB)
					{
						TextHelper.GetTextFile(response, timeArray,
								varNameList, dataList, 
								TimeZone, dqp.getCellI(), dqp.getCellJ(), dqp.getTimeZoneOffset(), 
								d3d, 
								dqp.is3D(), dqp.getRequestedDepth(),
								dqp.getUnitOfMeasure()==UnitOfMeasure.English, 
								dqp.isNowcast(),dqp.isInFile(),format==ObsDataFormat.CSV,
								dqp.isPreview(),dqp.isAsc(),indexArray,dqp.isDisplayDOY(),dqp.getId());
					}
					else if(format==ObsDataFormat.JSON)
					{
						TextHelper.GetJSONString(response, timeArray, 
								varNameList, dataList, 
								TimeZone, lon, lat, dqp.getTimeZoneOffset(), 
								dqp.getRequestedDepth()>=0?dqp.getRequestedDepth():d3d, 
								dqp.is3D(), 
								dqp.getUnitOfMeasure()==UnitOfMeasure.English, 
								dqp.isNowcast(),dqp.isInFile(),dqp.isAsc());
					}
					else if(format==ObsDataFormat.GeoRSS)
					{
						GeoRSSHelper.GetGeoRSS(response, 
								varNameList, dataList, 
								TimeZone, dqp.getCellI(), dqp.getCellJ(), dqp.getTimeZoneOffset(), 
								d3d, 
								dqp.is3D(), dqp.getRequestedDepth(),
								dqp.getUnitOfMeasure()==UnitOfMeasure.English, 
								dqp.isNowcast(),dqp.isInFile(), dqp.isAsc(),dqp.getId());
					}
					return;
				}
			}
		}
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
	}

}

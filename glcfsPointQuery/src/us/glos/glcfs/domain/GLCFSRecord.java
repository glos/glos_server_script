package us.glos.glcfs.domain;

import java.util.ArrayList;
import java.util.HashMap;

import ucar.ma2.Array;

public class GLCFSRecord {

	public static class Velocity
	{
		public double speed;
		public String speedName;
		public static final String speedUnit="m/s";
		public double degree;
		public String degreeName;
		public static final String degreeUnit="Degrees 0=toward North";
		public static final String degreeUnit1="Degrees 0=from North";
		public boolean isMissingData=false;
		private Velocity(){}
	}
	private double lon;
	private static final String lonName="Longitude";
	private double lat;
	private static final String latName="Latitude";
	private double depth;
	private static final String depthName="Bathymetry";
	private static final String depthUnit="meter";
	private double depthAtNode;
	private static final String depthAtNodeName="3D Depth at Nodes";
	private static final String depthAtNodeUnit="meter";
	private long utcDateTime;
	private double sigmaLevel;
	private double waterTemp;
	private static final String waterTempName="Water Temperature";
	private static final String waterTempUnit="Celsius";
	private double waterLevel;
	private static final String waterLevelName="Model Water Level";
	private static final String waterLevelUnit="meter";
	private double surfaceVelocityEast;
	private static final String surfaceVelocityEastName="Eastward Water Velocity at Surface";
	private static final String surfaceVelocityEastUnit="m/s";
	private double surfaceVelocityNorth;
	private static final String surfaceVelocityNorthName="Northward Water Velocity at Surface";
	private static final String surfaceVelocityNorthUnit="m/s";
	private double velocityEast;
	private static final String velocityEastName="Eastward Water Velocity";
	private static final String velocityEastUnit="m/s";
	private double velocityNorth;
	private static final String velocityNorthName="Northward Water Velocity";
	private static final String velocityNorthUnit="m/s";
	private double velocityAverageEast;
	private static final String velocityAverageEastName="Depth-Averaged Eastward Water Velocity";
	private static final String velocityAverageEastUnit="m/s";
	private double velocityAverageNorth;
	private static final String velocityAverageNorthName="Depth-Averaged Northward Waer Velocity";
	private static final String velocityAverageNorthUnit="m/s";
	private double waveHeight;
	private static final String waveHeightName="Significant Wave Height";
	private static final String waveHeightUnit="meter";
	private double waveDirection;
	private static final String waveDirectionName="Wave Direction";
	private static final String waveDirectionUnit="Degrees 0=toward North";
	private double wavePeriod;
	private static final String wavePeriodName="Wave Period";
	private static final String wavePeriodUnit="second";
	private double airVelocityEast;
	private static final String airVelocityEastName="Eastward Air Velocity";
	private static final String airVelocityEastUnit="m/s";
	private double airVelocityNorth;
	private static final String airVelocityNorthName="Northward Air Velocity";
	private static final String airVelocityNorthUnit="m/s";
	private double airTemp;
	private static final String airTempName="Air Temperature";
	private static final String airTempUnit="Celsius";
	private double cloudCover;
	private static final String cloudCoverName="Cloud Cover";
	private static final String cloudCoverUnit="fraction";
	private double dewPoint;
	private static final String dewPointName="Dew Point";
	private static final String dewPointUnit="Celsius";
	private double iceConcentration;
	private static final String iceConcentrationName="Ice Concentration";
	private static final String iceConcentrationUnit="fraction";
	private double iceThickness;
	private static final String iceThicknessName="Ice Thickness";
	private static final String iceThicknessUnit="meter";
	private static HashMap<String,String> VariableUnits=null;
	private static HashMap<String,String> VariableNames=null;
	
	private static final String[] UCVC={"Water Velocity at Surface","Water Velocity at Surface Direction"};
	private static final String[] UV={"Water Velocity","Water Velocity Direction"};
	private static final String[] UTMVTM={"Depth-Averaged Water Velocity","Depth-Averaged Water Velocity Direction"};
	private static final String[] AIRUV={"Air Velocity","Air Velocity Direction"};
	private static final double MISSING_VALUE=-99999.0;
	
	private static final double[] DepthFrac4L={.0227,.0454,.0681,.0908,.1135,.1362,.1589,.1816,
		.2043,.2270,.2724,.3405,.4313,.5448,.6810,.7945,.8853,.9534,1};
	private static final double[] DepthFracErie={.05,.1,.15,.2,.25,.3,.35,.4,.45,.5,
		.55,.6,.65,.7,.75,.8,.85,.9,.95,1};
	static
	{
		VariableUnits=new HashMap<String,String>();
		VariableNames=new HashMap<String,String>();
		VariableUnits.put("depth", depthUnit);
		VariableNames.put("depth", depthName);
		VariableUnits.put("d3d", depthAtNodeUnit);
		VariableNames.put("d3d", depthAtNodeName);
		VariableUnits.put("eta", waterLevelUnit);
		VariableNames.put("eta", waterLevelName);
		VariableUnits.put("uc", surfaceVelocityEastUnit);
		VariableNames.put("uc", surfaceVelocityEastName);
		VariableUnits.put("vc", surfaceVelocityNorthUnit);
		VariableNames.put("vc", surfaceVelocityNorthName);
		VariableUnits.put("u", velocityEastUnit);
		VariableNames.put("u", velocityEastName);
		VariableUnits.put("v", velocityNorthUnit);
		VariableNames.put("v", velocityNorthName);
		VariableUnits.put("utm", velocityAverageEastUnit);
		VariableNames.put("utm", velocityAverageEastName);
		VariableUnits.put("vtm", velocityAverageNorthUnit);
		VariableNames.put("vtm", velocityAverageNorthName);
		VariableUnits.put("temp", waterTempUnit);
		VariableNames.put("temp", waterTempName);
		VariableUnits.put("wvh", waveHeightUnit);
		VariableNames.put("wvh", waveHeightName);
		VariableUnits.put("wvd", waveDirectionUnit);
		VariableNames.put("wvd", waveDirectionName);
		VariableUnits.put("wvp", wavePeriodUnit);
		VariableNames.put("wvp", wavePeriodName);
		VariableNames.put("air_u", airVelocityEastName);
		VariableUnits.put("air_u", airVelocityEastUnit);
		VariableNames.put("air_v", airVelocityNorthName);
		VariableUnits.put("air_v", airVelocityNorthUnit);
		VariableNames.put("at", airTempName);
		VariableUnits.put("at", airTempUnit);
		VariableNames.put("cl", cloudCoverName);
		VariableUnits.put("cl", cloudCoverUnit);
		VariableNames.put("dp", dewPointName);
		VariableUnits.put("dp", dewPointUnit);
		VariableNames.put("ci", iceConcentrationName);
		VariableUnits.put("ci", iceConcentrationUnit);
		VariableNames.put("hi", iceThicknessName);
		VariableUnits.put("hi", iceThicknessUnit);
	}
	private GLCFSRecord()
	{
		
	}
	public static int getSigmaLevel(String lake, double depth,double bathy)
	{
		int s=0;
		if(bathy>0.)
		{
			double ratio=depth/bathy;
			double[] fracA=null;
			if(lake.equals("erie"))
				fracA=DepthFracErie;
			else
				fracA=DepthFrac4L;
			for(;s<fracA.length;++s)
				if(ratio<=fracA[s])
					break;
		}
		return s;
	}
	public static boolean isValidData(String vName,double value)
	{
		boolean result=true;
		if(vName==null)
			result=false;
		else if((vName.equals("d3d")||vName.equals("depth"))&&value==0.0)
			result=false;
		else if(value==MISSING_VALUE)
			result=false;
		return result;
	}
	private static Velocity _GetVelocity(double x,double y,String speedName,String degreeName,boolean is2North)
	{
		Velocity vel=new Velocity();
		vel.speedName=speedName;
		vel.degreeName=degreeName;
		if(x==MISSING_VALUE||y==MISSING_VALUE)
		{
			vel.isMissingData=true;
		}
		else
		{
			vel.speed=Math.sqrt(x*x+y*y);
			
			//degree to North
			//double t=Math.atan2(x,y)*180.0/Math.PI+0.5;
			//if(t<0)t+=360.0;
			if(is2North)
				vel.degree=Math.atan2(-x,-y)*180.0/Math.PI;
			else
				vel.degree=Math.atan2(x,y)*180.0/Math.PI;//(int)(t);
			if(vel.degree<0)vel.degree+=360.0;
			
		}
		return vel;
	}
	public static String[] VelocityPairExist(String vn1,String vn2)
	{
		
		if(vn1!=null&&vn2!=null)
		{
			if(vn1.equalsIgnoreCase("uc")&&vn2.equalsIgnoreCase("vc"))
				return UCVC;
			else if(vn1.equalsIgnoreCase("vc")&&vn2.equalsIgnoreCase("uc"))
				return UCVC;
			else if(vn1.equalsIgnoreCase("u")&&vn2.equalsIgnoreCase("v"))
				return UV;
			else if(vn1.equalsIgnoreCase("v")&&vn2.equalsIgnoreCase("u"))
				return UV;
			else if(vn1.equalsIgnoreCase("utm")&&vn2.equalsIgnoreCase("vtm"))
				return UTMVTM;
			else if(vn1.equalsIgnoreCase("vtm")&&vn2.equalsIgnoreCase("utm"))
				return UTMVTM;
			else if(vn1.equalsIgnoreCase("air_u")&&vn2.equalsIgnoreCase("air_v"))
				return AIRUV;
			else if(vn1.equalsIgnoreCase("air_v")&&vn2.equalsIgnoreCase("air_u"))
				return AIRUV;
		}
		return null;
	}
	public static boolean isDirection2North(String v1,String v2)
	{
		if(v1!=null&&v2!=null)
			return (v1.equalsIgnoreCase("air_u")&&v2.equalsIgnoreCase("air_v"))||(v1.equalsIgnoreCase("air_v")&&v2.equalsIgnoreCase("air_u"));
		return false;
	}
	public static Velocity VelocityPair(String vn1,String vn2,double v1,double v2)
	{
		Velocity vel=null;
		
		if(vn1!=null&&vn2!=null)
		{
			if(vn1.equalsIgnoreCase("uc")&&vn2.equalsIgnoreCase("vc"))
				vel=_GetVelocity(v1,v2,UCVC[0],UCVC[1],false);
			else if(vn1.equalsIgnoreCase("vc")&&vn2.equalsIgnoreCase("uc"))
				vel=_GetVelocity(v2,v1,UCVC[0],UCVC[1],false);
			else if(vn1.equalsIgnoreCase("u")&&vn2.equalsIgnoreCase("v"))
				vel=_GetVelocity(v1,v2,UV[0],UV[1],false);
			else if(vn1.equalsIgnoreCase("v")&&vn2.equalsIgnoreCase("u"))
				vel=_GetVelocity(v2,v1,UV[0],UV[1],false);
			else if(vn1.equalsIgnoreCase("utm")&&vn2.equalsIgnoreCase("vtm"))
				vel=_GetVelocity(v1,v2,UTMVTM[0],UTMVTM[1],false);
			else if(vn1.equalsIgnoreCase("vtm")&&vn2.equalsIgnoreCase("utm"))
				vel=_GetVelocity(v2,v1,UTMVTM[0],UTMVTM[1],false);
			else if(vn1.equalsIgnoreCase("air_u")&&vn2.equalsIgnoreCase("air_v"))
				vel=_GetVelocity(v1,v2,AIRUV[0],AIRUV[1],true);
			else if(vn1.equalsIgnoreCase("air_v")&&vn2.equalsIgnoreCase("air_u"))
				vel=_GetVelocity(v2,v1,AIRUV[0],AIRUV[1],true);
		}
		return vel;
	}
	public static void reSortVarsByVelocityPair(ArrayList<String> varNames)
	{
		if(varNames!=null)
		{
			int len=varNames.size();
			int idx=-1;
			String temp=null;
			if(len>0)
				for(int i=0;i<len-1;++i)
				{
					if(varNames.get(i).equalsIgnoreCase("uc"))
					{
						idx=java.util.Collections.binarySearch(varNames, "vc");
						if(idx>=0)
						{
							temp=varNames.get(i+1);
							varNames.set(i+1, "vc");
							varNames.set(idx,temp);
							++i;
						}
					}
					else if(varNames.get(i).equalsIgnoreCase("vc"))
					{
						idx=java.util.Collections.binarySearch(varNames, "uc");
						if(idx>=0)
						{
							temp=varNames.get(i+1);
							varNames.set(i+1, "uc");
							varNames.set(idx,temp);
							++i;
						}
					}
					else if(varNames.get(i).equalsIgnoreCase("u"))
					{
						idx=java.util.Collections.binarySearch(varNames, "v");
						if(idx>=0)
						{
							temp=varNames.get(i+1);
							varNames.set(i+1, "v");
							varNames.set(idx,temp);
							++i;
						}
					}
					else if(varNames.get(i).equalsIgnoreCase("v"))
					{
						idx=java.util.Collections.binarySearch(varNames, "u");
						if(idx>=0)
						{
							temp=varNames.get(i+1);
							varNames.set(i+1, "u");
							varNames.set(idx,temp);
							++i;
						}
					}
					else if(varNames.get(i).equalsIgnoreCase("utm"))
					{
						idx=java.util.Collections.binarySearch(varNames, "vtm");
						if(idx>=0)
						{
							temp=varNames.get(i+1);
							varNames.set(i+1, "vtm");
							varNames.set(idx,temp);
							++i;
						}
					}
					else if(varNames.get(i).equalsIgnoreCase("vtm"))
					{
						idx=java.util.Collections.binarySearch(varNames, "utm");
						if(idx>=0)
						{
							temp=varNames.get(i+1);
							varNames.set(i+1, "utm");
							varNames.set(idx,temp);
							++i;
						}
					}
					else if(varNames.get(i).equalsIgnoreCase("air_u"))
					{
						idx=java.util.Collections.binarySearch(varNames, "air_v");
						if(idx>=0)
						{
							temp=varNames.get(i+1);
							varNames.set(i+1, "air_v");
							varNames.set(idx,temp);
							++i;
						}
					}
					else if(varNames.get(i).equalsIgnoreCase("air_v"))
					{
						idx=java.util.Collections.binarySearch(varNames, "air_u");
						if(idx>=0)
						{
							temp=varNames.get(i+1);
							varNames.set(i+1, "air_u");
							varNames.set(idx,temp);
							++i;
						}
					}
				}
		}
	}
	public static HashMap<Long,GLCFSRecord> CreateGLCFSRecord(ArrayList<Array> data,ArrayList<String> varNames,Array dt,double lon,double lat,double depthAtNode)
	{
		HashMap<Long,GLCFSRecord> records=null;
		if(data!=null&&data.size()>0&&varNames!=null&&varNames.size()>0&&dt!=null&&dt.getSize()>0&&varNames.size()==data.size())
		{
			int dlen=(int)dt.getSize();
			int vlen=varNames.size();
			GLCFSRecord record=null;
			Array arr=null;
			for(int i=0;i<dlen;++i)
			{
				record=new GLCFSRecord();
				record.depthAtNode=depthAtNode;
				record.lat=lat;
				record.lon=lon;
				record.utcDateTime=dt.getLong(i);
				
				for(int j=0;j<vlen;++j)
				{
					if(varNames.get(j).equalsIgnoreCase("depth"))
					{
						arr=data.get(j);
						if(arr!=null&&arr.getSize()>0)
							record.depth=arr.getDouble(0);
					}
					else if(varNames.get(j).equalsIgnoreCase("eta"))
					{
						arr=data.get(j);
						if(arr!=null&&arr.getSize()>i)
							record.waterLevel=arr.getDouble(i);
					}
					else if(varNames.get(j).equalsIgnoreCase("uc"))
					{
						arr=data.get(j);
						if(arr!=null&&arr.getSize()>i)
							record.surfaceVelocityEast=arr.getDouble(i);
					}
					else if(varNames.get(j).equalsIgnoreCase("vc"))
					{
						arr=data.get(j);
						if(arr!=null&&arr.getSize()>i)
							record.surfaceVelocityNorth=arr.getDouble(i);
					}
					else if(varNames.get(j).equalsIgnoreCase("u"))
					{
						arr=data.get(j);
						if(arr!=null&&arr.getSize()>i)
							record.velocityEast=arr.getDouble(i);
					}
					else if(varNames.get(j).equalsIgnoreCase("v"))
					{
						arr=data.get(j);
						if(arr!=null&&arr.getSize()>i)
							record.velocityNorth=arr.getDouble(i);
					}
					else if(varNames.get(j).equalsIgnoreCase("utm"))
					{
						arr=data.get(j);
						if(arr!=null&&arr.getSize()>i)
							record.velocityAverageEast=arr.getDouble(i);
					}
					else if(varNames.get(j).equalsIgnoreCase("vtm"))
					{
						arr=data.get(j);
						if(arr!=null&&arr.getSize()>i)
							record.velocityAverageNorth=arr.getDouble(i);
					}
					else if(varNames.get(j).equalsIgnoreCase("temp"))
					{
						arr=data.get(j);
						if(arr!=null&&arr.getSize()>i)
							record.waterTemp=arr.getDouble(i);
					}
					else if(varNames.get(j).equalsIgnoreCase("wvh"))
					{
						arr=data.get(j);
						if(arr!=null&&arr.getSize()>i)
							record.waveHeight=arr.getDouble(i);
					}
					else if(varNames.get(j).equalsIgnoreCase("wvd"))
					{
						arr=data.get(j);
						if(arr!=null&&arr.getSize()>i)
							record.waveDirection=arr.getDouble(i);
					}
					else if(varNames.get(j).equalsIgnoreCase("wvp"))
					{
						arr=data.get(j);
						if(arr!=null&&arr.getSize()>i)
							record.wavePeriod=arr.getDouble(i);
					}
					else if(varNames.get(j).equalsIgnoreCase("air_u"))
					{
						arr=data.get(j);
						if(arr!=null&&arr.getSize()>i)
							record.airVelocityEast=arr.getDouble(i);
					}
					else if(varNames.get(j).equalsIgnoreCase("air_v"))
					{
						arr=data.get(j);
						if(arr!=null&&arr.getSize()>i)
							record.airVelocityNorth=arr.getDouble(i);
					}
					else if(varNames.get(j).equalsIgnoreCase("at"))
					{
						arr=data.get(j);
						if(arr!=null&&arr.getSize()>i)
							record.airTemp=arr.getDouble(i);
					}
					else if(varNames.get(j).equalsIgnoreCase("cl"))
					{
						arr=data.get(j);
						if(arr!=null&&arr.getSize()>i)
							record.cloudCover=arr.getDouble(i);
					}
					else if(varNames.get(j).equalsIgnoreCase("dp"))
					{
						arr=data.get(j);
						if(arr!=null&&arr.getSize()>i)
							record.dewPoint=arr.getDouble(i);
					}
					else if(varNames.get(j).equalsIgnoreCase("ci"))
					{
						arr=data.get(j);
						if(arr!=null&&arr.getSize()>i)
							record.iceConcentration=arr.getDouble(i);
					}
					else if(varNames.get(j).equalsIgnoreCase("hi"))
					{
						arr=data.get(j);
						if(arr!=null&&arr.getSize()>i)
							record.iceThickness=arr.getDouble(i);
					}
				}
				if(records==null)
					records=new HashMap<Long,GLCFSRecord>(dlen);
				records.put(record.utcDateTime, record);
			}
		}
		return records;
	}
	
	public static final String getVariableUnitByAbbrev(String abbrev)
	{
		return VariableUnits.get(abbrev);
	}
	public static final String getVariableNameByAbbrev(String abbrev)
	{
		return VariableNames.get(abbrev);
		
	}
	/*public static String getLonName() {
		return lonName;
	}
	
	public static String getLatname() {
		return latName;
	}
	public static String getDepthname() {
		return depthName;
	}
	public static String getDepthatnodename() {
		return depthAtNodeName;
	}
	public static String getWatertempname() {
		return waterTempName;
	}
	public static String getWaterlevelname() {
		return waterLevelName;
	}
	public static String getSurfacevelocityeastname() {
		return surfaceVelocityEastName;
	}
	public static String getSurfacevelocitynorthname() {
		return surfaceVelocityNorthName;
	}
	public static String getVelocityeastname() {
		return velocityEastName;
	}
	public static String getVelocitynorthname() {
		return velocityNorthName;
	}
	public static String getVelocityaverageeastname() {
		return velocityAverageEastName;
	}
	public static String getVelocityaveragenorthname() {
		return velocityAverageNorthName;
	}
	public static String getWaveheightname() {
		return waveHeightName;
	}
	public static String getWavedirectionname() {
		return waveDirectionName;
	}
	public static String getWaveperiodname() {
		return wavePeriodName;
	}*/
	/*public double getLon() {
		return lon;
	}
	public double getLat() {
		return lat;
	}
	public double getDepth() {
		return depth;
	}
	public String getDepthUnit() {
		return depthUnit;
	}
	public double getDepthAtNode() {
		return depthAtNode;
	}
	public static String getDepthAtNodeUnit() {
		return depthAtNodeUnit;
	}
	public long getUtcDateTime() {
		return utcDateTime;
	}
	public double getSigmaLevel() {
		return sigmaLevel;
	}
	public double getWaterTemp() {
		return waterTemp;
	}
	public static String getWaterTempUnit() {
		return waterTempUnit;
	}
	public double getWaterLevel() {
		return waterLevel;
	}
	public static String getWaterLevelUnit() {
		return waterLevelUnit;
	}
	public double getVelocityEast() {
		return velocityEast;
	}
	public static String getVelocityEastUnit() {
		return velocityEastUnit;
	}
	public double getVelocityNorth() {
		return velocityNorth;
	}
	public static String getVelocityNorthUnit() {
		return velocityNorthUnit;
	}
	
	public double getSurfaceVelocityEast() {
		return surfaceVelocityEast;
	}
	public static String getSurfaceVelocityEastUnit() {
		return surfaceVelocityEastUnit;
	}
	public double getSurfaceVelocityNorth() {
		return surfaceVelocityNorth;
	}
	public static String getSurfaceVelocityNorthUnit() {
		return surfaceVelocityNorthUnit;
	}
	public double getVelocityAverageEast() {
		return velocityAverageEast;
	}
	public static String getVelocityAverageEastUnit() {
		return velocityAverageEastUnit;
	}
	public double getVelocityAverageNorth() {
		return velocityAverageNorth;
	}
	public static String getVelocityAverageNorthUnit() {
		return velocityAverageNorthUnit;
	}
	public double getWaveHeight() {
		return waveHeight;
	}
	public static String getWaveHeightUnit() {
		return waveHeightUnit;
	}
	public double getWaveDirection() {
		return waveDirection;
	}
	public static String getWaveDirectionUnit() {
		return waveDirectionUnit;
	}
	public double getWavePeriod() {
		return wavePeriod;
	}
	public static String getWavePeriodUnit() {
		return wavePeriodUnit;
	}*/
	
	
}

package us.glos.glcfs.domain;

import java.util.ArrayList;

import org.joda.time.DateTime;

import us.glos.glcfs.domain.StartEndDateTimes;
import us.glos.obs.domain.ObsDataFormat;
import us.glos.obs.domain.UnitOfMeasure;

public class DataQueryParam {
	private String lake;
	private ArrayList<String> urls;
	private ArrayList<StartEndDateTimes> periods;
	private long interval;
	private boolean isLatest=false;
	private boolean isInFile=false;
	private boolean is3D=false;
	private int sigmaLevel=-1;
	private double requestedDepth=-1.;
	private int hours;
	private DateTime startDateTime;
	private DateTime endDateTime;
	private int timeZoneOffset;
	private double cellI;
	private double cellJ;
	private UnitOfMeasure unitOfMeasure;
	private ObsDataFormat format;
	private ArrayList<String> variables;
	private boolean isNowcast;
	private boolean isPreview=false;
	private boolean isAsc=false;
	private boolean isDisplayIndex=false;
	private boolean isDisplayDOY=false;
	private boolean isQueryByDepth=false;

	public static String GetName()
	{
		return DataQueryParam.class.getName();
	}
	
	public DataQueryParam(boolean isNowcast,boolean isInFile, ArrayList<String> url,ArrayList<StartEndDateTimes> period,String lakeName,long interval,int tOffset,double i,double j,int sigma,double rdepth,UnitOfMeasure um,ObsDataFormat format,boolean isLatest)
	{
		this.isNowcast=isNowcast;
		this.isInFile=isInFile;
		this.urls=url;
		this.periods=period;
		this.interval=interval;
		this.isLatest=isLatest;
		this.lake=lakeName;
		this.cellI=i;
		this.cellJ=j;
		this.sigmaLevel=sigma;
		this.requestedDepth=rdepth;
		this.timeZoneOffset=tOffset;
		this.unitOfMeasure=um;
		this.format=format;
		if(this.sigmaLevel>=0||rdepth>=0)
			is3D=true;
		if(rdepth>=0)
			isQueryByDepth=true;
		
	}
	
	public boolean isDisplayDOY() {
		return isDisplayDOY;
	}

	public void setDisplayDOY(boolean isDisplayDOY) {
		this.isDisplayDOY = isDisplayDOY;
	}

	public boolean isDisplayIndex() {
		return isDisplayIndex;
	}

	public void setDisplayIndex(boolean isDisplayIndex) {
		this.isDisplayIndex = isDisplayIndex;
	}

	public boolean isAsc() {
		return isAsc;
	}

	public void setAsc(boolean isAsc) {
		this.isAsc = isAsc;
	}

	public boolean isPreview() {
		return isPreview;
	}

	public void setPreview(boolean isPreview) {
		this.isPreview = isPreview;
	}

	public boolean isNowcast() {
		return isNowcast;
	}

	public boolean isInFile() {
		return isInFile;
	}

	public UnitOfMeasure getUnitOfMeasure() {
		return unitOfMeasure;
	}

	public ObsDataFormat getFormat() {
		return format;
	}

	public ArrayList<String> getUrl() {
		return urls;
	}

	public ArrayList<StartEndDateTimes> getPeriods() {
		return periods;
	}

	public String getId() {
		return lake;
	}

	public long getInterval() {
		return interval;
	}

	public boolean isLatest() {
		return isLatest;
	}

	public int getTimeZoneOffset() {
		return timeZoneOffset;
	}

	public double getCellI() {
		return cellI;
	}

	public double getCellJ() {
		return cellJ;
	}

	public boolean is3D() {
		return is3D;
	}

	public int getSigmaLevel() {
		return sigmaLevel;
	}

	public double getRequestedDepth() {
		return requestedDepth;
	}

	public void setRequestedDepth(double requestedDepth) {
		this.requestedDepth = requestedDepth;
	}

	public boolean isQueryByDepth() {
		return isQueryByDepth;
	}

	public ArrayList<String> getVariables() {
		return variables;
	}
	public void addVariables(ArrayList<String> vlist)
	{
		this.variables=vlist;
	}
	public void addVariable(String var)
	{
		if(var!=null)
		{
			if(this.variables==null)
				this.variables=new ArrayList<String>();
			this.variables.add(var);
		}
	}
}

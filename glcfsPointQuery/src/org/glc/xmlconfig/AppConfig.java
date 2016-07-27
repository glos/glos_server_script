package org.glc.xmlconfig;

import java.util.HashMap;

public class AppConfig {
	
	public HashMap<String, Action> actions=null;
	public HashMap<String,SimpleDataSource> dss=null;
	public HashMap<String,AppSetting> appsettings=null;
	public HashMap<Long,String> alterTables=null;
	public HashMap<String,UnitMap> unitmapping=null;
	public HashMap<String,Provider> providers=null;
	public HashMap<String,WmsService> wms=null;
	public HashMap<String,ThreddsDataset> thredds=null;
	private LogLevel _level=LogLevel.NONE;
	private String curAppKey=null;
	//private String level="NONE";
	public AppConfig()
	{
		actions=new HashMap<String, Action>();
		dss=new HashMap<String, SimpleDataSource>();
		appsettings=new HashMap<String,AppSetting>();
		alterTables=new HashMap<Long,String>();
		unitmapping=new HashMap<String,UnitMap>();
		providers=new HashMap<String,Provider>();
		wms=new HashMap<String,WmsService>();
		thredds=new HashMap<String,ThreddsDataset>();
	}
	public void Clear()
	{
		actions.clear();
		dss.clear();
	}
	public void addDataSource(SimpleDataSource sds)
	{
	    dss.put(sds.getName(), sds);	
	}
	public void addAction(Action ac)
	{
		actions.put(ac.getType(), ac);
		actions.put(ac.getPath(),ac);
	}
	public void addUnitMap(UnitMap um)
	{
		unitmapping.put(um.getAbbre(), um);
	}
	public LogLevel getLogLevel() {
		return _level;
	}
	public void setLevel(String l) {
		try
		{
		    this._level =LogLevel.valueOf(l);
		}
		catch(Exception e)
		{
			
		}
		
	}
	public void addAppSetting(AppSetting as)
	{
		appsettings.put(as.getKey(), as);
	}
	public void addProvider(Provider p)
	{
		if(p.getName()!=null)
			providers.put(p.getName(), p);
		else if(p.getType()!=null)
		    providers.put(p.getType(), p);
	}
	public void addWMS(WmsService p)
	{
		wms.put(p.getName(), p);
	}
	public void addThreddsDataset(ThreddsDataset td)
	{
		if(td!=null)
			thredds.put(td.getId(), td);
	}
	public void setTable(String t){
	    if(t!=null)
	    {
	    	String[] temp=t.split(",");
	    	if(temp!=null&&temp.length>0)
	    	{
	    		Long l=null;
	    		for(String tt:temp)
	    		{
	    		    try
	    		    {
	    			    l=Long.parseLong(tt);
	    			    if(l!=null&&l>0)
	    			    {
	    			    	this.alterTables.put(l, "1");
	    			    }
	    		    }
	    		    catch(NumberFormatException nef)
	    		    {
	    		    	
	    		    }
	    		}
	    	}
	    }
	}
	public boolean isCheckAlternativeTable(Long pltid)
	{
		return this.alterTables.get(pltid)!=null;
	}
}

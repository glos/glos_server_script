package org.glc.xmlconfig;

import java.util.HashMap;
import java.util.ArrayList;
public class ThreddsDataset {
	private String id;
	private HashMap<String,String> map;
	private HashMap<String, ArrayList<String>> variables;
	private HashMap<String,Long> intervals;
	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}
	public String getURL(String key)
	{
		if(map!=null)
			return map.get(key);
		else 
			return null;
	}
	public ArrayList<String> getVariables(String key)
	{
		if(variables!=null)
			return variables.get(key);
		else
			return null;
	}
	public long getInterval(String key)
	{
		return intervals!=null?intervals.get(key):null;
	}
	public void addPeriod(AppSetting as)
	{
		if(as!=null)
		{
			if(map==null)
				map=new HashMap<String,String>();
			map.put(as.getKey(), as.getValue());
		}
	}
	public void addVariables(AppSetting as)
	{
		if(as!=null&&as.getKey()!=null&&as.getValue()!=null)
		{
			if(variables==null)
				variables=new HashMap<String, ArrayList<String>>();
			String[] temp=as.getValue().split(",");
			if(temp!=null&&temp.length>0)
			{
				java.util.Arrays.sort(temp);
				ArrayList<String> vars=new ArrayList<String>(temp.length);
				for(int i=0;i<temp.length;++i)
					vars.add(temp[i]);
				variables.put(as.getKey(), vars);					
			}
		}
	}
	public void addInterval(AppSetting as)
	{
		if(as!=null)
		{
			if(intervals==null)
				intervals=new HashMap<String,Long>();
			Long temp=Long.parseLong(as.getValue());
			if(temp>0)
				intervals.put(as.getKey(), temp);
		}
	}
}

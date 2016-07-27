package org.glc.xmlconfig;

import java.util.HashMap;

import org.glc.xmlconfig.AppSetting;
public class WmsService {
	private String name=null;
	private String label=null;
	private String provider=null;
	private String url=null;
	private HashMap<String,String> map;
	
	public WmsService()
	{
		
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getLabel() {
		return label;
	}

	public void setLabel(String label) {
		this.label = label;
	}

	public String getProvider() {
		return provider;
	}

	public void setProvider(String provider) {
		this.provider = provider;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}
	public void addParam(AppSetting as)
    {
    	if(as!=null)
    	{
    		if(as.getKey()!=null)
    		{
    			if(map==null)
    				map=new HashMap<String,String>();
    			map.put(as.getKey(), as.getValue());
    		}
    	}
    }
	
}

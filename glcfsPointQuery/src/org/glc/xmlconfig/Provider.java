package org.glc.xmlconfig;

import java.util.HashMap;
import java.util.Set;
import org.glc.xmlconfig.AppSetting;
public class Provider {
    private String type;
    private String name;
    ;
    private HashMap<String,String> map;
    
    public Provider()
    {
    	type=null;
    	map=new HashMap<String,String>();
    }

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}
	
    public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public void addParam(AppSetting as)
    {
    	if(as!=null)
    	{
    		if(as.getKey()!=null)
    			map.put(as.getKey(), as.getValue());
    	}
    }
	
	public String getValueByKey(String key)
	{
		return map.get(key);
	}
    
}

package org.glc.xmlconfig;

public class AppSetting {
    private String key;
    private String value;
    public AppSetting()
    {
    	key=null;
    	value=null;
    }
	public String getKey() {
		return key;
	}
	public void setKey(String key) {
		this.key = key;
	}
	public String getValue() {
		return value;
	}
	public void setValue(String value) {
		this.value = value;
	}
    
}

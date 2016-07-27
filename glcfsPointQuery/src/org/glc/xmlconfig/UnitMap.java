package org.glc.xmlconfig;

public class UnitMap {
    private String abbre;
    private String metric;
    private String english;
    
    public UnitMap()
    {
    	abbre=null;
    	metric=null;
    	english=null;
    }

	public String getAbbre() {
		return abbre;
	}

	public void setAbbre(String abbre) {
		this.abbre = abbre;
	}

	public String getMetric() {
		return metric;
	}

	public void setMetric(String metric) {
		this.metric = metric;
	}

	public String getEnglish() {
		return english;
	}

	public void setEnglish(String english) {
		this.english = english;
	}

	
    
}

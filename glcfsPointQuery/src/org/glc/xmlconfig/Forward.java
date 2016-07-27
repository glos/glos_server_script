package org.glc.xmlconfig;

public class Forward {
	private String name;
	private String path;
	private boolean isRedirect;
	public Forward()
	{
		name=null;
		path=null;
		isRedirect=false;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getPath() {
		return path;
	}
	public void setPath(String path) {
		this.path = path;
	}
	public boolean isRedirect() {
		return isRedirect;
	}
	public void setRedirect(boolean isRedirect) {
		this.isRedirect = isRedirect;
	}
	
}

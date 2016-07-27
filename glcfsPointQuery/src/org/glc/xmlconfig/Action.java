package org.glc.xmlconfig;

import java.util.HashMap;

public class Action {
	public static enum Scope
	{
		REQUEST,
		SESSION,
		GLOBAL,
		NONE
	};
	private static class ForwardItem
	{
		public String path;
		public boolean isRedirect;
		public ForwardItem(String p,boolean red)
		{
			this.path=p;
			this.isRedirect=red;
		}
	}
	private String path;
	private String type;
	private Scope _scope;
	private String range;
	private HashMap<String,ForwardItem> forwarding;
	
	public Action()
	{
		path=null;
		type=null;
		range=null;
		_scope=Scope.NONE;
		forwarding=null;
		
	}
	public String getPath() {
		return path;
	}
	public void setPath(String path) {
		this.path = path;
	}
	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
	}
	public Scope getScope() {
		return _scope;
	}
	/*public String getValue()
	{
		return this.value;
	}*/
	public void setRange(String scope) {
		
		try
		{
		    this._scope = Scope.valueOf(scope.toUpperCase());
		}
		catch(Exception e){}
	}
	public void setForward(Forward fw)
	{
		if(this.forwarding==null)
			this.forwarding=new HashMap<String,ForwardItem>();
		this.forwarding.put(fw.getName(),new ForwardItem(fw.getPath(),fw.isRedirect()));
		
	}
	public String getForward(String name)
	{
		//return this.forwarding==null?null:this.forwarding.get(name).path;
		if(this.forwarding!=null&&this.forwarding.get(name)!=null)
			return this.forwarding.get(name).path;
		else
			return null;
	}
	public boolean isRedirect(String name)
	{
		return this.forwarding==null?null:this.forwarding.get(name).isRedirect;
	}
}

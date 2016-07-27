package org.glc.domain;

import java.util.Hashtable;

public class ErrorMessage {
	private Hashtable<String,String> fldList;
	private StringBuilder global=null;
	//private ArrayList<String> msg;
	private boolean isAjax;
	private boolean isForward;
	private String sessionName;
	private String forwardURL;
	private boolean isAuthorized;
	public static String SEPARATOR=",";
	public static String getClassName()
	{
		return ErrorMessage.class.getName();
	}
	public ErrorMessage()
	{
		this.fldList=new Hashtable<String,String>();
		global=new StringBuilder();
		//this.msg=new ArrayList<String>();
		this.isAjax=false;
		this.isAuthorized=true;
		this.isForward=false;
		this.sessionName=null;
		this.forwardURL=null;
	}
	public void appendGlobalMessage(String err)
	{
		global.append(err);
		global.append(SEPARATOR);
	}
	public String getHTMLFriendlyGlobalMessage(String startTag,String endTag)
	{
		if(startTag==null)startTag="";
		if(endTag==null)endTag="<br/>";
		if(this.global.length()>0)
		{
			String temp=this.global.toString();
			String tmp[]=temp.split(SEPARATOR);
			if(tmp!=null&&tmp.length>0)
			{
				StringBuilder sb=new StringBuilder();
				for(String t:tmp)
				{
					if(t!=null&&!t.equals(""))
					{
						sb.append(startTag);
						sb.append(t);
						sb.append(endTag);
					}
				}
				return sb.toString();
			}
		}
		return "";
	}
	/*public String getErrorFields()
	{
		if(!this.fldList.isEmpty())
		{
			StringBuilder sb=new StringBuilder();
			for(int i=0;i<fldList.size();++i)
			{
				if(sb.length()>0)
					sb.append(SEPARATOR);
				sb.append(this.fldList.get(i));
			}
			return sb.toString();
		}
		return null;
	}*/
	public String[] getAllErrors()
	{
		String[] errs=null;
		if(!this.fldList.isEmpty())
		{
			errs=new String[this.fldList.size()];
			int i=0;
			for(String key:this.fldList.keySet())
			{
				errs[i++]=this.fldList.get(key);
			}
		}
		return errs;
	}
	public boolean isValidated(String fld)
	{
		return this.fldList.containsKey(fld);
	}
	public String getErrorMsg(String fld)
	{
		return this.fldList.get(fld);
	}
	public String getErrorJSON()
	{
		StringBuilder sb=new StringBuilder();
		sb.append("[");
		for(String fld:this.fldList.keySet())
		{
			sb.append("{");
			sb.append("\"");
			sb.append("id");
			sb.append("\"");
			sb.append(":");
			sb.append("\"");
			sb.append(fld);
			sb.append("\"");
			sb.append(SEPARATOR);
			sb.append("\"");
			sb.append("err");
			sb.append("\"");
			sb.append(":");
			sb.append("\"");
			sb.append(this.fldList.get(fld));
			sb.append("\"");
			sb.append("}");
			sb.append(SEPARATOR);
		}
		if(sb.length()>1)
			sb.deleteCharAt(sb.length()-1);
		sb.append("]");
		return sb.toString();
	}
	public void addError(String fld,String message)
	{
		if(fld!=null&&message!=null)
		    this.fldList.put(fld, message);
	}
	
	public boolean isAjax() {
		return isAjax;
	}

	public void setAjax(boolean isAjax) {
		this.isAjax = isAjax;
	}
	public String getSessionName() {
		return sessionName;
	}
	public void setSessionName(String sessionName) {
		this.sessionName = sessionName;
	}
	public boolean isAuthorized() {
		return isAuthorized;
	}
	public void setAuthorized(boolean isAuthorized) {
		this.isAuthorized = isAuthorized;
	}
	public String getRedirectURL() {
		return forwardURL;
	}
	public void setRedirectURL(String forwardURL) {
		this.forwardURL = forwardURL;
	}
	public int getErrorCount()
	{
		return this.fldList.keySet().size();
	}
	public boolean isForward() {
		return isForward;
	}
	public void setForward(boolean isForward) {
		this.isForward = isForward;
	}
	public String getForwardURL() {
		return forwardURL;
	}
	public void setForwardURL(String forwardURL) {
		this.forwardURL = forwardURL;
	}
	
}

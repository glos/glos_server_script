package org.glc.tags;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.tagext.TagSupport;
import javax.servlet.jsp.JspWriter;

import org.glc.Logger;
import org.glc.xmlconfig.LogLevel;

public class DateDisplayTag extends TagSupport {
	
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -2457271978048633741L;
	private String datetime;
	//private String format="%s";
	private boolean isLocal=true;
	private String jsPath="";
	//private SimpleDateFormat dateFormat;
	
	public DateDisplayTag()
	{
		//dateFormat= new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
		//dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
	}
	@Override
	public int doStartTag() throws JspException {
		// TODO Auto-generated method stub
		try
		{
			JspWriter jw=this.pageContext.getOut();
			jw.print("<span id='orgglctagsdatedisplaytag'>");
			//jw.print(String.format(this.format, this.datetime));
			jw.print("</span>");
			if(this.isLocal)
			{
			    jw.print("<script>");
			//String temp=this.datetime==null?"null":this.dateFormat.format(this.datetime);
			    if(this.datetime!=null)
			    	jw.print(String.format("ddtag_dateStr='%s';",this.datetime));
			    else
			    	jw.print(String.format("ddtag_dateStr='%s';","null"));
			    jw.print("</script>");
			    jw.print(String.format("<script src='%s/date.js'>",this.jsPath));
			    jw.print("</script>");
			    jw.print(String.format("<script src='%s/datedisplay.js'>",this.jsPath));
			    jw.print("</script>");
			}
		}
		catch(java.io.IOException ie)
		{
			Logger.writeLog("org.glc.tags.DateDisplayTag: "+ie.getMessage(),LogLevel.SEVERE);
		}
		//return super.doStartTag();
		return (SKIP_BODY);
	}
	public void setDatetime(String dt)
	{
		if(dt!=null)
		    this.datetime=dt;
		
	}
	public void setLocal(String l)
	{
		this.isLocal=Boolean.parseBoolean(l);
	}
	/*public void setFormat(String fmt)
	{
		if(fmt!=null)
		{
			dateFormat= new SimpleDateFormat(fmt);
			dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
			this.format=fmt;
		}
		
	}*/
	public void setJspath(String jp)
	{
		if(jp!=null&&jp.length()>0)
		{
		    if(jp.charAt(jp.length()-1)!='/')
		    	jp=jp+"/";
			this.jsPath=jp;
		}
	}
}

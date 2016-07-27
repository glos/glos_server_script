package org.glc.tags;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspWriter;
import javax.servlet.jsp.tagext.TagSupport;

import org.glc.utils.FIPSCodes;
import org.glc.Logger;
import org.glc.xmlconfig.LogLevel;

public class StatesSelectTag extends TagSupport {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -7167112351564726900L;
	static final String SELECTED="selected='selected'";
	private String selected="";
	private String id="";
	private String name="";
	private String cssClass="";
	private boolean isHide=false;
	private String country=FIPSCodes.CountryCode.US;
	@Override
	public int doStartTag() throws JspException {
		// TODO Auto-generated method stub
		try
		{
			JspWriter jw=this.pageContext.getOut();
			String temp="";
			if(isHide)
				temp="display:none;";
			jw.print(String.format("<select id='%s' name='%s' class='%s' style='%s'>", 
					               this.id,this.name,this.cssClass,temp));
			//jw.print(String.format(this.format, this.datetime));
			temp="";
			if(this.selected.equals(temp))
			    jw.print(String.format("<option value='' %s>----</option>",SELECTED));
			else
				jw.print("<option value=''>----</option>");
			if(this.country.equalsIgnoreCase(FIPSCodes.CountryCode.US))
			{
				for(String key:FIPSCodes.US_State_FIPS_Sorted_Key)
				{
					if(this.selected.equalsIgnoreCase(key))
						temp=SELECTED;
					else
						temp="";
					jw.print(String.format("<option value='%s' %s>%s</option>", key,temp,FIPSCodes.US_State_FIPS.get(key)));
				}
			}
			else if(this.country.equalsIgnoreCase(FIPSCodes.CountryCode.CA))
			{
				for(String key:FIPSCodes.CA_Province_FIPS_Sorted_Key)
				{
					if(this.selected.equalsIgnoreCase(key))
						temp=SELECTED;
					else
						temp="";
					jw.print(String.format("<option value='%s' %s>%s</option>", key,temp,FIPSCodes.CA_Province_FIPS.get(key)));
				}
			}
			else
				jw.print("<option value=''>Other Country</option>");
			jw.print("</select>");
			
		}
		catch(java.io.IOException ie)
		{
			Logger.writeLog("org.glc.tags.StatesSelectTag: "+ie.getMessage(),LogLevel.SEVERE);
		}
		//return super.doStartTag();
		return (SKIP_BODY);
	}
	
	public void setSelected(String s)
	{
		if(s!=null)
		    this.selected=s;
	}
	public void setId(String id)
	{
		if(id!=null)
		    this.id=id;
	}
	public void setName(String name)
	{
		if(name!=null)
		    this.name=name;
	}
	public void setCssClass(String css)
	{
		if(css!=null)
		    this.cssClass=css;
	}
	public void setCountry(String c)
	{
		if(c!=null&&!c.equals(""))
		    this.country=c;
	}
	public void setHide(String h)
	{
		if(h!=null)
		{
			this.isHide=Boolean.parseBoolean(h);
		}
	}
	public void setHide(boolean b)
	{
		this.isHide=b;
	}
}

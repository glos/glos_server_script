package org.glc.tags;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspWriter;
import javax.servlet.jsp.tagext.TagSupport;

import org.glc.Logger;
import org.glc.domain.ErrorMessage;
import org.glc.xmlconfig.LogLevel;

public class ErrorDisplayTag extends TagSupport {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1013679897938608633L;
	private String cssStyle="";
	private ErrorMessage errMsg;
	private String name;
	private String id;
	@Override
	public int doStartTag() throws JspException {
		// TODO Auto-generated method stub
		JspWriter jw=this.pageContext.getOut();
		if(this.errMsg!=null)
		{
			String[] errs=errMsg.getAllErrors();
			if(errs!=null&&errs.length>0)
			{
				try
				{
					
					int len=errs.length;
					for(int i=0;i<len;++i)
					{
						jw.print(String.format("<div id='%s' name='%s' style='%s'>",this.id,this.name,this.cssStyle));
						jw.print(errs[i]);
						jw.print("</div>");
					}
				}
				catch(java.io.IOException ie)
				{
					Logger.writeLog("org.glc.tags.ErrorDisplayTag: "+ie.getMessage(),LogLevel.SEVERE);
				}
			}
		}
		else
		{
			try
			{
				jw.print(String.format("<div id='%s' name='%s' style='%s'></div>",this.id,this.name,this.cssStyle));
			}
			catch(java.io.IOException ie)
			{
				Logger.writeLog("org.glc.tags.ErrorDisplayTag: "+ie.getMessage(),LogLevel.SEVERE);
			}
		}
		return (SKIP_BODY);
	}
	public void setCssStyle(String cssStyle) {
		if(cssStyle!=null)
			this.cssStyle = cssStyle;
	}
	public void setErrMsg(Object errMsg) {
		if(errMsg!=null&&errMsg instanceof ErrorMessage)
			this.errMsg = (ErrorMessage)errMsg;
		else
			this.errMsg=null;
	}
	public void setName(String name) {
		if(name!=null)
			this.name = name;
	}
	public void setId(String id) {
		if(id!=null)
			this.id = id;
	}
	
	
}

package org.glc.utils;

import java.text.SimpleDateFormat;
import java.text.ParseException;
import java.util.TimeZone;
import java.util.Date;
import java.sql.Timestamp;

import org.glc.IDateFormater;

public class UTCDateFormatter implements IDateFormater{
	private TimeZone TIME_ZONE;
	private SimpleDateFormat dateFormat;
	
	private void init(TimeZone tz,String fmt)
	{
		dateFormat= new SimpleDateFormat(fmt);
		
		dateFormat.setTimeZone(TIME_ZONE);
	}
	public UTCDateFormatter()
	{
		TIME_ZONE=TimeZone.getTimeZone("UTC");
		this.init(this.TIME_ZONE,"yyyy-MM-dd HH:mm:ss");
	}
	public UTCDateFormatter(TimeZone tz,String fmt)
	{
		if(tz!=null)
    	    TIME_ZONE=tz;
		else
			TIME_ZONE=TimeZone.getTimeZone("UTC");
    	if(fmt==null||fmt.length()==0)
    	    this.init(this.TIME_ZONE,"yyyy-MM-dd HH:mm:ss");
    	else
    	  	this.init(this.TIME_ZONE,fmt);
    	
	}
	public String format(Timestamp ts)
	{
		return ts==null?null:this.dateFormat.format(ts);
	}
	public String format(Date ts)
	{
		return ts==null?null:this.dateFormat.format(ts);
	}
	public Date parse(String str)
	{
		try
		{
		    return str==null?null:dateFormat.parse(str);
		}
		catch(ParseException pe)
		{
			return null;
		}
	}
}

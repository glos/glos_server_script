package org.glc.glos.coastwatch;

import java.text.SimpleDateFormat;
import java.text.ParseException;
import java.util.TimeZone;
import java.util.Date;
import java.sql.Timestamp;

public class UTCDateFormatter {
	private TimeZone TIME_ZONE;
	private SimpleDateFormat dateFormat;
	{
		dateFormat= new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		TIME_ZONE=TimeZone.getTimeZone("UTC");
		dateFormat.setTimeZone(TIME_ZONE);
	}
	public UTCDateFormatter()
	{
		
	}
	public UTCDateFormatter(TimeZone tz)
	{
		if(tz!=null&&tz.hasSameRules(TIME_ZONE)==false)
    	{
    	    TIME_ZONE=tz;
    	    dateFormat.setTimeZone(TIME_ZONE);
    	}
	}
	public String format(Timestamp ts)
	{
		return this.dateFormat.format(ts);
	}
	public Date parse(String str)
	{
		try
		{
		    return dateFormat.parse(str);
		}
		catch(ParseException pe)
		{
			return null;
		}
	}
}

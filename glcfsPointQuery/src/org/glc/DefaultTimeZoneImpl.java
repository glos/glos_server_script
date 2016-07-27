package org.glc;

import java.util.TimeZone;
import java.util.Calendar;

public class DefaultTimeZoneImpl implements ITimeZoneAware {

	protected TimeZone timeZone;
	protected Calendar cal;
	public DefaultTimeZoneImpl()
	{
		this.timeZone=TimeZone.getTimeZone("UTC");
		cal=Calendar.getInstance(this.timeZone);
	}
	@Override
	public TimeZone getTimeZone() {
		// TODO Auto-generated method stub
		return this.timeZone;
	}

	@Override
	public void setTimeZone(TimeZone tz) {
		// TODO Auto-generated method stub
		if(tz!=null&&false==tz.hasSameRules(this.timeZone))
		{
		    this.timeZone=tz;
		    cal=Calendar.getInstance(this.timeZone);
		}
	}

}

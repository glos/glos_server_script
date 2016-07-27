package us.glos.glcfs.domain;

import org.joda.time.DateTime;

public class StartEndDateTimes {
	public DateTime startDateTime;
	public DateTime endDateTime;
	
	public StartEndDateTimes(DateTime start,DateTime end)
	{
		if(end.isAfter(start))
		{
			this.startDateTime=start;
			this.endDateTime=end;
		}
		else
		{
			this.startDateTime=end;
			this.endDateTime=start;
		}
	}
}

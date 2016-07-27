package org.glc.utils;

public class TimeZoneOffset {
	private static final String[] US_TimeZoneOffset={"+0000","-0100","-0200","-0300","-0400","-0500","-0600","-0700","-0800","-0900","-1000"};
	
	public static String GetOffsetString(int hourOffset)
	{
		if(hourOffset>0||hourOffset<-10)
			return null;
		else
			return US_TimeZoneOffset[hourOffset*-1];
	}
}

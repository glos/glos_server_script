package org.glc;

import java.util.TimeZone;

public interface ITimeZoneAware {
	void setTimeZone(TimeZone tz);
	TimeZone getTimeZone();
}

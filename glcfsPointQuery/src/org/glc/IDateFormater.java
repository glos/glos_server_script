package org.glc;

import java.sql.Timestamp;
import java.util.Date;

public interface IDateFormater {
	public String format(Timestamp ts);
	
	public String format(Date ts);
	
	public Date parse(String str);
	
}

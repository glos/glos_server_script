package org.glc;

import java.util.Date;
import java.text.DateFormat;
import java.text.SimpleDateFormat;


import org.glc.xmlconfig.ConfigManager;
import org.glc.xmlconfig.LogLevel;

public class Logger {
    static LogLevel level;
    static DateFormat dateFormat;
    static String LineSeparator;
    static
    {
    	level=ConfigManager.getLogLevel();
    	dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
    	LineSeparator=System.getProperty("line.separator");
    }
    public static void writeLog(String msg,LogLevel l)
    {
    	
        Date date = new Date();
        
        if(l.compareTo(level)>=0)
        {
    	    System.err.printf("${GLC Logger} %s: %s.%s",dateFormat.format(date),msg,LineSeparator);
    	    System.err.flush();
        }
    }
}

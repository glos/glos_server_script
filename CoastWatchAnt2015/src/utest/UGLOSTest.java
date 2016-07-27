package utest;

import java.io.IOException;
import java.io.PrintStream;
import java.util.Date;
import java.util.logging.FileHandler;
import java.util.logging.Formatter;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

import org.glc.glos.uglos.*;
import org.glc.glos.coastwatch.ConfigManager;
import org.glc.glos.coastwatch.logredirect.LoggingOutputStream;
import org.glc.glos.coastwatch.logredirect.StdOutErrLevel;
public class UGLOSTest {

	/**
	 * @param args
	 */
	public static void main(String[] args) throws IOException{
		// TODO Auto-generated method stub
		ConfigManager.initConfig();
		LogManager.getLogManager().reset();
		Logger log=Logger.getLogger("utest.UGLOSTest");
		log.setLevel(Level.parse(ConfigManager.getLogLevel()));
		log.setUseParentHandlers(false);
		FileHandler logHandler=new FileHandler(ConfigManager.getLogFile(),40960,4,true);
		//Reformat the log message
		logHandler.setFormatter(new Formatter(){
			public String format(LogRecord record)
			{
				return String.format("%s: %s -- %s%s", record.getLevel(),new Date(record.getMillis()).toString(),record.getMessage(),ConfigManager.NEWLINE);
			}
		});
		log.addHandler(logHandler);
		//Redirect stdout/stderr to log, make sure on output to terminal
		System.setOut(new PrintStream(new LoggingOutputStream(Logger.getLogger("utest.UGLOSTest.stdout"),
				                                              StdOutErrLevel.STDOUT),
				                      true));
		System.setErr(new PrintStream(new LoggingOutputStream(Logger.getLogger("utest.UGLOSTest.stderr"),
                StdOutErrLevel.STDERR),
                true));
        UGLOSAnt ant=new UGLOSAnt();
        ant.March(log);
	}

}

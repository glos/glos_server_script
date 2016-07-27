package org.glc.glos.coastwatch.logredirect;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class LoggingOutputStream extends ByteArrayOutputStream {
	static private String lineSeparator;
	{
		lineSeparator=System.getProperty("line.separator");
	}
	private Logger logger;
	private Level level;
	/*
	 * Constructor
	 * @param logger Logger to write to
	 * @param level Level at which to write the log message
	 */
	public LoggingOutputStream(Logger logger,Level level){
		super();
		this.logger=logger;
		this.level=level;
		
	}
	/*
	 * upon flush() write the existing contents of the OutputStream to
	 * the logger as a log record
	 * @throws java.io.IOException in case of error
	 */
	public void flush() throws IOException{
		String record;
		record=this.toString();
		super.reset();
		if(record.length()==0||record.equals(lineSeparator)){
			return;
		}
		logger.logp(level, "", "", record);
	}
}

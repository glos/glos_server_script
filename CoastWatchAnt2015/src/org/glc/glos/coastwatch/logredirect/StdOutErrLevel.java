package org.glc.glos.coastwatch.logredirect;

import java.util.logging.Level;
import java.io.InvalidClassException;
import java.io.ObjectStreamException;

public class StdOutErrLevel extends Level {
	/*
	 * Private constructor
	 */
    private StdOutErrLevel(String name,int value){
    	super(name,value);
    }
    public static final long serialVersionUID=10000000L;
    /**
     * Level for STDOUT activity
     */
    public static Level STDOUT=new StdOutErrLevel("STDOUT",Level.INFO.intValue()+53);
    /*
     * Level for STDERR activity
     */
    public static Level STDERR=new StdOutErrLevel("STDERR",Level.INFO.intValue()+54);
    /*
     * Method to avoid creating duplicate instance when deserializing the object.
     * @return the singleton instance of this <code>Level</code> value in this classloader
     * @throws ObjectStreamException if unable to deserialize
     */
    protected Object ReadResolve() throws ObjectStreamException{
    	if(this.intValue()==STDOUT.intValue())
    		return STDOUT;
    	if(this.intValue()==STDERR.intValue())
    		return STDERR;
    	throw new InvalidClassException("Unknown instance:"+this);
    }
}

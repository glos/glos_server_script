package org.glc;

import java.util.HashMap;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

import org.glc.xmlconfig.ConfigManager;
import org.glc.Logger;
import org.glc.xmlconfig.LogLevel;

public class DBConnFactory {
	//private static Context initCtx=null;
	//private static Context envCtx=null;
	//private static DataSource dSrc=null;
	private static HashMap<String,DataSource> sources;
	static
	{
		sources=new HashMap<String,DataSource>();
	}
	public synchronized static boolean init(String name)
	{
		try
		{
			if(sources.get(name)!=null)return true;
			Context initCtx=new InitialContext();
			//Context envCtx=(Context)initCtx.lookup("java:comp/env");
			DataSource dSrc=(DataSource)initCtx.lookup("java:comp/env/"+ConfigManager.getSimpleDataSourceValue(name));
		    if(dSrc!=null)
		    {
		    	sources.put(name, dSrc);
		    	//initCtx.close();
		    	//envCtx.close();
		    	return true;
		    }
		    else
		    	Logger.writeLog("DBConnFactory::init: Can not locate the JNDI DB Resource: "+name,LogLevel.SEVERE);
		}
		catch(NamingException ne)
		{
			Logger.writeLog("DBConnFactory::init: "+ne.getMessage(),LogLevel.SEVERE);
		}
		return false;
	}
	public static Connection getConnection(String name) throws SQLException
	{
		DataSource ds=sources.get(name);
		return ds==null?null:ds.getConnection();
	}
}

package org.glc.glos.dbman;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.logging.Logger;

import org.glc.glos.coastwatch.IAnt;
import org.glc.glos.coastwatch.ConfigManager;

public class CleanerAnt implements IAnt{

	public void March(Logger log) {
		// TODO Auto-generated method stub
		Connection conn=null;
		Statement st=null;
		long result=0;
		try
		{
			conn=DriverManager.getConnection(ConfigManager.getConnectionString(),
					                    	 ConfigManager.getDBAccount(),
					                    	 ConfigManager.getDBPassword());
			if(conn!=null)
			{
				conn.setAutoCommit(false);
				st=conn.createStatement();
				if(st!=null)
				{
					result=st.executeUpdate("delete from multi_obs where m_date < current_timestamp  - interval '72 hours';");
					if(log!=null)
						log.info(String.format("%d records were deleted from multi_obs table", result));
					
					conn.commit();
				}
				
			}
		}
		catch(SQLException se)
		{
			try
			{
				conn.rollback();
			}
			catch(Exception ie){}
			if(log!=null)
			{
				log.info("An SQL exception occured when CleanerAnt tried to delete data from table multi_obss that are older than 72 hours from now.");
				log.severe(se.getMessage());
			}
		}
		finally
		{
			try
			{
				if(st!=null)
					st.close();
				if(conn!=null)
					conn.close();
			}
			catch(Exception ie){}
		}
	}
	
}

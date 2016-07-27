package org.glc.glos.dbman;

import java.util.logging.Logger;
import java.sql.Connection;
import java.sql.Statement;
import java.sql.DriverManager;
import java.sql.SQLException;
import org.glc.glos.coastwatch.IAnt;
import org.glc.glos.coastwatch.ConfigManager;

public class DumpObs implements IAnt{

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
					result=st.executeUpdate("insert into multi_obs_repos select * from multi_obs where m_date < current_timestamp  - interval '2 weeks';");
					if(result>0)
						st.execute("delete from multi_obs where m_date < current_timestamp  - interval '2 weeks';");
					if(log!=null)
						log.info(String.format("%d records were dumped to multi_obs_repos table", result));
					
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
				log.info("An SQL exception occured when DumpObs tried to dump table multi_obs to table multi_obs_repos with data older than 2 weeks from now.");
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
	/*public static void deleteme()
	{
		Connection conn=null;
		Statement st=null;
		Statement st1=null;
		ResultSet rs=null;
		long result=0;
		String nws=null;
		String radio=null;
		java.util.ArrayList<String> list=new java.util.ArrayList<String>();
		try
		{
			conn=DriverManager.getConnection(ConfigManager.getConnectionString(),
					                    	 ConfigManager.getDBAccount(),
					                    	 ConfigManager.getDBPassword());
			if(conn!=null)
			{
				
				st=conn.createStatement();
				st1=conn.createStatement();
				if(st!=null)
				{
					rs=st.executeQuery("select nws_handle,radio_call_sign from platform;");
					while(rs.next())
					{
						nws=rs.getString(1);
						if(nws!=null&&!nws.equals(""))
							st1.execute("update platform set station_url='http://www.glerl.noaa.gov/marobs/php/data.php?sta="+nws+"' where nws_handle='"+nws+"'");
						else
						{
						    radio=rs.getString(2);
						    if(radio!=null&&!radio.equals(""))
						    	st1.execute("update platform set station_url='http://www.glerl.noaa.gov/marobs/php/data.php?sta="+radio+"' where radio_call_sign='"+radio+"'");
						}
					}
					//if(log!=null)
					//	log.info(String.format("%d records was dumped to multi_obs_repos table", result));
					
					
				}
				
			}
		}
		catch(SQLException se)
		{
			se.printStackTrace();
		}
		finally
		{
			try
			{
				if(st!=null)
					st.close();
				if(st1!=null)
					st1.close();
				if(rs!=null)
					rs.close();
				if(conn!=null)
					conn.close();
			}
			catch(Exception ie){}
		}
	}*/

}

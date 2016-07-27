/* Great Lakes Observing System Regional Association 
 * @Author Guan Wang
 * @Organization Great Lakes Commission
 * @Contact Pete Giencke
 *           pgiencke@glc.org
 *           734-971-9135
 *           Eisenhower Corporate Park
 *           2805 S. Industrial Hwy, Suite 100
 */
package org.glc.glos.coastwatch;

import java.sql.Array;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.sql.PreparedStatement;
import java.sql.DriverManager;
import java.sql.Timestamp;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.HashMap;
import java.util.Set;
import java.util.Calendar;
import java.util.TimeZone;
import java.util.logging.Logger;
import java.util.Properties;

import org.glc.glos.coastwatch.domain.ObsZ;
import org.glc.glos.coastwatch.domain.Observation;
import org.glc.glos.coastwatch.domain.Station;


public final class SqlHelper {
	private Logger log=null;
	private String jdbc;
	private String url;
	private String user;
	private String password;
	private HashMap<String,Station> lookup=null;
	private IDateFilter dateFilter=null;
	private long sensorCount=0;
	private long observCount=0;
	private long hourlyCachedCount=0;
	private long latestCachedCount=0;
	private String cacheSqlStm=null;
	private Calendar obsCalendar=Calendar.getInstance();
	private IConverter<Float> converter=null;
	private static float FLAGGED=-998.0f; 
	private static String QueryFields="platform_handle,sensor_id,m_type_id,m_date,m_lon,m_lat,m_z,m_value,row_entry_date,row_update_date";
	
	public final static class Record
	{
		public boolean bLatlonChanged;
		public boolean bZChanged;
		public boolean bAnemoChanged;
		public long pid;
		public String handle;
		public float oldLon;
		public float oldLat;
		public float oldZ;
		public float oldAnemo;
		public Record()
		{
			bLatlonChanged=false;
			bZChanged=false;
			bAnemoChanged=false;
			pid=0;
			handle=null;
			oldLon=-9999.0f;
			oldLat=-9999.0f;
			oldZ=-9999.0f;
			oldAnemo=-9999.0f;
		}
	}
	
	public String getJdbc() {
		return jdbc;
	}
	public String getUrl() {
		return url;
	}
	public void setUrl(String url) {
		this.url = url;
	}
	public String getUser() {
		return user;
	}
	public void setUser(String user) {
		this.user = user;
	}
	public String getPassword() {
		return password;
	}
	public void setPassword(String password) {
		this.password = password;
	}
	
	public long getSensorCount() {
		return sensorCount;
	}
	
	public long getObservCount() {
		return observCount;
	}
	
	public long getHourlyCachedCount() {
		return hourlyCachedCount;
	}
	
	public long getLatestCachedCount() {
		return latestCachedCount;
	}
	public String getCacheSqlStm() {
		return cacheSqlStm;
	}
	public void setCacheSqlStm(String cacheSqlStm) {
		this.cacheSqlStm = cacheSqlStm;
	}
	public void setTimeZone(String id)
	{
		obsCalendar=Calendar.getInstance(TimeZone.getTimeZone(id));
	}
	
	
	public IConverter<Float> getConverter() {
		return converter;
	}
	public void setConverter(IConverter<Float> converter) {
		this.converter = converter;
	}
	public SqlHelper(String jdbc,String url,String user,String passwd,Logger log) throws ClassNotFoundException
	{
		this.jdbc=jdbc;
		this.url=url;
		this.user=user;
		this.password=passwd;
		this.log=log;
		this.lookup=new HashMap<String,Station>();
		try
		{
			Class.forName(jdbc);
		}
		catch(ClassNotFoundException ce)
		{
			if(log!=null)
			{
				log.info("Can not find the JDBC Driver Jar defined in configuration file!");
				log.severe(ce.getMessage());
			}
			throw ce;
		}
	}
	public void setDateFilter(IDateFilter filter)
	{
		this.dateFilter=filter;
	}
	public boolean checkDataAvailability(String tableName,ArrayList<Station> stations,String[] noaatables,HashMap<Integer,String> glostables,int glostype,int year)
	{
		if(tableName==null||stations==null||(noaatables==null&&glostables==null))
			return false;
		Connection conn=null;
		ResultSet rs=null;
		PreparedStatement ps1=null;
		PreparedStatement psu=null;
		PreparedStatement psi=null;
		boolean bCont=false;
		String startDateTime=String.format("%d-01-01 00:00:00",year);
		String endDateTime=String.format("%d-12-31 23:59:59",year);
		
		try
		{
			conn=DriverManager.getConnection(url,user,password);
			psu=conn.prepareStatement(String.format("update %s set y%d=? where id=?", tableName,year));
			psi=conn.prepareStatement(String.format("insert into %s(id,y%d) values(?,?)", tableName,year));
			ps1=conn.prepareStatement("SELECT count(*) FROM pg_attribute WHERE attrelid = (SELECT oid FROM pg_class WHERE relname = ?) AND attname = ?;");
			ps1.setString(1, tableName);
			ps1.setString(2, String.format("y%d", year));
			rs=ps1.executeQuery();
			if(rs.next())
			{
				if(rs.getInt(1)==1)
				{
					rs.close();
					ps1.close();
					bCont=true;
					log.info(String.format("Column y%d exists in table '%s'", year,tableName));
				}
				else
				{
					rs.close();
					ps1.close();
					ps1=conn.prepareStatement(String.format("alter table %s add column y%d text",tableName,year));
					ps1.execute();
					/*bCont=rs.next()&&rs.getInt(1)==1;
					if(bCont)
						log.info(String.format("Add column y%d in table '%s'", year,tableName));
					else
						log.severe(String.format("Failed to add column y%d in table '%s'", year,tableName));*/
					bCont=true;
					log.info(String.format("Add column y%d in table '%s'", year,tableName));
					rs.close();
					ps1.close();
				}
				if(bCont)
				{
					UTCDateFormatter dateFormater=new UTCDateFormatter();
					int count=0;
					log.info(String.format("%d stations will be searched", stations.size()));
					for(Station p:stations)
					{
						if(p==null)continue;
						Timestamp min=null,max=null;
						log.info(String.format("Search for station: %d",p.getId()));
						if(glostables!=null&&glostype==p.getTypeId()&&glostables.get((int)p.getId())!=null)
						{
							ps1=conn.prepareStatement(String.format("select max(m_date),min(m_date) from %s where m_date>='%s' and m_date<='%s'",glostables.get((int)p.getId()),startDateTime,endDateTime));
							rs=ps1.executeQuery();
							if(rs.next())
							{
								max=rs.getTimestamp(1);
								min=rs.getTimestamp(2);
							}
							rs.close();
							ps1.close();
								
						}
						else if(noaatables!=null&&glostype!=p.getTypeId()&&p.getTypeId()!=13)
						{
							Timestamp t1=null,t2=null;
							for(String t:noaatables)
							{
								if(t==null)continue;
								ps1=conn.prepareStatement(String.format("select max(m_date),min(m_date) from %s where platform_handle=? and m_date>='%s' and m_date<='%s'",t,startDateTime,endDateTime));
								ps1.setInt(1, (int)p.getId());
								rs=ps1.executeQuery();
								if(rs.next())
								{
									t1=rs.getTimestamp(1);
									t2=rs.getTimestamp(2);
									
									if(max==null||(t1!=null&&max.before(t1)))
										max=rs.getTimestamp(1);
									if(min==null||(t2!=null&&min.after(t2)))
										min=rs.getTimestamp(2);
								}
								rs.close();
								ps1.close();
							}
						}
						if(min!=null&&max!=null)
						{
							log.info(String.format("Found records for station: %d",p.getId()));
							ps1=conn.prepareStatement(String.format("select count(*) from %s where id=?",tableName));
							ps1.setInt(1, (int)p.getId());
							rs=ps1.executeQuery();
							if(rs.next())
							{
								if(rs.getInt(1)==1)
								{
									psu.setString(1, String.format("%s--%s", dateFormater.format(min),dateFormater.format(max)));
									psu.setInt(2, (int)p.getId());
									if(1==psu.executeUpdate())
									    ++count;
									log.info(String.format("Station: %d updated",p.getId()));
								}
								else
								{
									psi.setInt(1, (int)p.getId());
									psi.setString(2, String.format("%s--%s", dateFormater.format(min),dateFormater.format(max)));
									if(1==psi.executeUpdate())
									    ++count;
									log.info(String.format("Station: %d insertd",p.getId()));
								}
							}
							rs.close();
							ps1.close();
							
						}
						
					}
					log.info(String.format("%d stations were updated", count));
				}
			}
			
		}
		catch(SQLException se)
		{
			log.info(String.format("checkDataAvailability failed!"));
			log.severe(se.getMessage());
			se.printStackTrace();
		}
		finally
		{
			try
			{
				if(rs!=null)
					rs.close();
				if(ps1!=null)
					ps1.close();
				if(psi!=null)
					psi.close();
				if(psu!=null)
					psu.close();
				if(conn!=null)
					conn.close();
			}
			catch(Exception e){}
		}
		return false;
	}
	public int insertPlatformLoc(HashMap<Long,ArrayList<Station>> his)
	{
		Connection conn=null;
		PreparedStatement pstSel=null;
		PreparedStatement pstInst=null;
		ArrayList<Station> slist=null;
		int count=0;
		ResultSet rs=null;
		if(his!=null&&!his.isEmpty())
		{
			try
			{
				conn=DriverManager.getConnection(url,user,password);
				pstSel=conn.prepareStatement("select count(*) from platform_location where platform_id=? and start_date=?;");
			    pstInst=conn.prepareStatement("insert into platform_location(start_date,insert_date,platform_id,lat,lon,elev,anemometer_height) values(?,?,?,?,?,?,?);");
			    for(Long l:his.keySet())
			    {
			    	slist=his.get(l);
			    	if(slist!=null&&!slist.isEmpty())
			    	{
			    		for(Station s:slist)
			    		{
			    			if(s!=null)
			    			{
			    				pstSel.setLong(1, l);
			    				pstSel.setTimestamp(2, new Timestamp(s.getUpdateMillSec()),this.obsCalendar);
			    				rs=pstSel.executeQuery();
			    				if(rs.next())
			    				{
			    					if(rs.getInt(1)==0)
			    					{
			    						rs.close();
			    						pstInst.setTimestamp(1, new Timestamp(s.getUpdateMillSec()),this.obsCalendar);
			    						pstInst.setTimestamp(2, new Timestamp(System.currentTimeMillis()),this.obsCalendar);
			    						pstInst.setLong(3, l);
			    						if(Float.isNaN(s.getLatitude()))
			    							pstInst.setFloat(4, -9999.0f);
			    						else
			    						    pstInst.setFloat(4, s.getLatitude());
			    						
			    						if(Float.isNaN(s.getLongitude()))
			    						    pstInst.setFloat(5, -9999.0f);
			    						else
			    							pstInst.setFloat(5, s.getLongitude());
			    						
			    						if(Float.isNaN(s.getAltitude()))
			    							pstInst.setFloat(6, -9999.0f);
			    						else
			    							pstInst.setFloat(6, s.getAltitude());
			    						
			    						if(Float.isNaN(s.getAnemoHeight()))
			    							pstInst.setFloat(7, -9999.0f);
			    						else
			    							pstInst.setFloat(7,s.getAnemoHeight());
			    						
			    						pstInst.executeUpdate();
			    						++count;
			    						
			    					}
			    					else
			    						rs.close();
			    						
			    				}
			    			}
			    		}
			    	}
			    	
			    }
			}
			catch(SQLException se)
			{
				log.info(String.format("Insert failed!"));
				log.severe(se.getMessage());
				
			}
			finally
			{
				try
				{
					if(rs!=null)
						rs.close();
					if(pstSel!=null)
						pstSel.close();
					if(pstInst!=null)
						pstInst.close();
					if(conn!=null)
						conn.close();
				}
				catch(Exception e){}
			}
		}
		return count;
	}
	public boolean updateStationLoc(ArrayList<Record> records,HashMap<String,Station> files)
	{
		Connection conn=null;
		PreparedStatement pstPlt=null;
		PreparedStatement pstPltHis=null;
		
		if(records!=null&&!records.isEmpty()&&!files.isEmpty())
		{
			try
			{
			    conn=DriverManager.getConnection(url,user,password);
			    conn.setAutoCommit(false);
			    pstPlt=conn.prepareStatement("update platform set fixed_longitude=?,fixed_latitude=?,fixed_z=?,anemometer_height=?,row_update_date=? where row_id=?;");
			    pstPltHis=conn.prepareStatement("insert into platform_loc_history(lat,lon,elev,anemo_height,end_date,platform_id,insert_date) values(?,?,?,?,?,?,?);");
			    for(Record r:records)
			    {
			        if(r!=null)
			        {
			    	    if(r.bLatlonChanged)
			    	    {
			    	    	pstPlt.setFloat(1, files.get(r.handle).getLongitude());
			    	    	pstPlt.setFloat(2, files.get(r.handle).getLatitude());
			    	    	pstPltHis.setFloat(1, r.oldLat);
			    	    	pstPltHis.setFloat(2, r.oldLon);
			    	    }
			    	    else
			    	    {
			    	    	pstPlt.setFloat(1, r.oldLon);
			    	    	pstPlt.setFloat(2, r.oldLat);
			    	    	pstPltHis.setFloat(1, -9999.0f);
			    	    	pstPltHis.setFloat(2, -9999.0f);
			    	    }
			    	    if(r.bZChanged)
			    	    {
			    	    	pstPlt.setFloat(3, files.get(r.handle).getAltitude());
			    	    	pstPltHis.setFloat(3, r.oldZ);
			    	    }
			    	    else
			    	    {
			    	    	pstPlt.setFloat(3, r.oldZ);
			    	    	pstPltHis.setFloat(3, -9999.0f);
			    	    }
			    	    if(r.bAnemoChanged)
			    	    {
			    	    	pstPlt.setFloat(4, files.get(r.handle).getAnemoHeight());
			    	    	pstPltHis.setFloat(4, r.oldAnemo);
			    	    }
			    	    else
			    	    {
			    	    	pstPlt.setFloat(4, r.oldAnemo);
			    	    	pstPltHis.setFloat(4, -9999.0f);
			    	    }
			    	    long misec=System.currentTimeMillis();
			    	    pstPlt.setTimestamp(5, new Timestamp(misec),this.obsCalendar);
			    	    pstPlt.setLong(6, r.pid);
			    	    pstPltHis.setTimestamp(5, new Timestamp(files.get(r.handle).getUpdateMillSec()),this.obsCalendar);
			    	    pstPltHis.setLong(6, r.pid);
			    	    pstPltHis.setTimestamp(7, new Timestamp(misec),this.obsCalendar);
			    	    pstPlt.executeUpdate();
			    	    pstPltHis.executeUpdate();
			    	    conn.commit();
			        }
			    }
				//conn.setAutoCommit(true);
			    return true;
			}
			catch(SQLException se)
			{
				try
				{
				    conn.rollback();
				}
				catch(SQLException see)
				{
					
					if(log!=null)
					{
						log.info(String.format("Rollback failed!"));
						log.severe(se.getMessage());
					}
				}
				if(log!=null)
				{
					log.info(String.format("Transaction is being Rollback!"));
					log.severe(se.getMessage());
				}
			}
			finally
			{
				try
				{
					if(pstPlt!=null)
						pstPlt.close();
					if(pstPltHis!=null)
						pstPltHis.close();
					if(conn!=null)
						conn.close();
				}
				catch(Exception e){}
			}
		}
		return false;
	}
	public ArrayList<Station> getAllStations()
	{
		Connection conn=null;
		PreparedStatement pst=null;
		ResultSet rs=null;
		ArrayList<Station> stations=null;
		Station plt=null;
		try
		{
			conn=DriverManager.getConnection(url,user,password);
			if(conn!=null)
			{
				pst=conn.prepareStatement("select row_id,fixed_longitude,fixed_latitude,fixed_z,anemometer_height,nws_handle,nos_handle,icao_handle,coop_handle,wmo_handle,radio_call_sign,ndbc_handle,other_id,type_id from platform");
				rs=pst.executeQuery();
				if(rs!=null)
				{
					stations=new ArrayList<Station>();
					while(rs.next())
					{
					    plt=new Station();
					    plt.setId(rs.getLong(1));
					    plt.setLongitude(rs.getFloat(2));
					    plt.setLatitude(rs.getFloat(3));
					    plt.setAltitude(rs.getFloat(4));
					    plt.setAnemoHeight(rs.getFloat(5));
					    plt.setNws_handle(rs.getString(6));
					    plt.setNos_handle(rs.getString(7));
					    plt.setIcao_handle(rs.getString(8));
					    plt.setCoop_handle(rs.getString(9));
					    plt.setWmo_handle(rs.getString(10));
					    plt.setRadio_call_sign(rs.getString(11));
					    plt.setNdbc_handle(rs.getString(12));
					    plt.setOther_handle(rs.getString(13));
					    plt.setTypeId(rs.getInt(14));
					    stations.add(plt);
					}
					return stations;
				}
			}
		}
		catch(SQLException se)
		{
			if(log!=null)
			{
				log.info("An SQL Exception was occured when checking the existance of records!");
				log.severe(se.getMessage());
			}
		}
		finally
		{
			try
			{
				if(rs!=null)
					rs.close();
				if(pst!=null)
					pst.close();
				if(conn!=null)
					conn.close();
			}
			catch(Exception e){}
		}
		return null;
	}
	public boolean updateLastObsDate(Properties stations)
	{
		//HashMap<Long,Station> stations=getStationLookup();
		Connection conn=null;
		PreparedStatement pst=null;
		ResultSet rs=null;
		long id=-1;
		Timestamp timestamp=null;
		long time=0;
		if(stations!=null&&!stations.isEmpty()&&this.lookup!=null&&!this.lookup.isEmpty())
		{
			try
			{
				log.info("Begin to save timestamp in db.");
				conn=DriverManager.getConnection(url,user,password);
				pst=conn.prepareStatement("select row_id,last_update_date from obs_last_update;");
				rs=pst.executeQuery();
				//lookup from db
				HashMap<Long,Long> tempLookup=new HashMap<Long,Long>();
				while(rs.next())
				{
					id=rs.getLong(1);
					timestamp=rs.getTimestamp(2);
					if(timestamp!=null)
						time=timestamp.getTime();
					else
						time=0;
					if(!tempLookup.containsKey(id))
						tempLookup.put(id, time);
				}
				rs.close();
				pst.close();
				pst=conn.prepareStatement("update obs_last_update set last_update_date=? where row_id=?;");
				long temp=0;
				Station st=null;
				int count=0;
				if(!tempLookup.isEmpty())
				{
					Set<String> sSet=this.lookup.keySet();
					log.info("Begin to update obs_last_update table.");
					for(String handle:sSet)
					{
						st=this.lookup.get(handle);
						if(st!=null)
						{
						    temp=Long.parseLong(stations.getProperty(handle));
						    if(tempLookup.get(st.getId())<temp)//new timestamp
						    {
						    	pst.setTimestamp(1, new Timestamp(temp), this.obsCalendar);
						    	pst.setLong(2, st.getId());
						    	pst.executeUpdate();
						    	++count;
						    }
						}
					}
					log.info(String.format("%d records were updated.", count));
					return true;
				}
			}
			catch(SQLException se)
			{
				if(log!=null)
				{
					log.info("An SQL Exception was occured when updating obs_last_update table!");
					log.severe(se.getMessage());
				}
			}
			finally
			{
				try
				{
					if(rs!=null)
						rs.close();
					if(pst!=null)
						pst.close();
					if(conn!=null)
						conn.close();
					
				}
				catch(Exception e){}
				
			}
		}
		return false;
	}
	//sorry, only works after you call the function FilterNonExistance() otherwise lookup is empty
	private HashMap<Long,Station> getStationLookup()
	{
		if(this.lookup!=null&&!this.lookup.isEmpty())
		{
			HashMap<Long,Station> stations=new HashMap<Long,Station>();
			java.util.Collection<Station> cs=this.lookup.values();
			for(Station s:cs)
			{
				if(s!=null)
				{
					if(!stations.containsKey(s.getId()))
					{
						stations.put(s.getId(), s);
					}
				}
			}
			return stations;
		}
		return null;
	}
	public ArrayList<Observation> FilterNonExistance(ArrayList<Observation> source,HashSet<String> newStations)
	{
		if(source==null||newStations==null)return null;
		ArrayList<Observation> data=new ArrayList<Observation>();
		Connection conn=null;
		PreparedStatement pst=null;
		ResultSet rs=null;
		boolean isNew=false;
		try
		{
			conn=DriverManager.getConnection(url,user,password);
			if(conn!=null)
			{
				pst=conn.prepareStatement("select row_id,fixed_longitude,fixed_latitude,fixed_z from platform where nws_handle=? or nos_handle=? or icao_handle=? or coop_handle=? or wmo_handle=? or radio_call_sign=? or ndbc_handle=? or other_id=?");
				for(Observation ob:source)
				{
					if(ob==null)continue;
					//if already confirm it's a new data, no need to check again, move to the next
					if(newStations.contains(ob.getHandle()))
						continue;
					if(dateFilter!=null)
						if(!dateFilter.IsNewData(ob.getHandle(), ob.getDate()))
							continue;
					pst.setString(1, ob.getHandle());
					pst.setString(2, ob.getHandle());
					pst.setString(3, ob.getHandle());
					pst.setString(4, ob.getHandle());
					pst.setString(5, ob.getHandle());
					pst.setString(6, ob.getHandle());
					pst.setString(7, ob.getHandle());
					pst.setString(8, ob.getHandle());
					//try to by pass database query
					if(false==this.lookup.containsKey(ob.getHandle()))
					{    
						rs=pst.executeQuery();
						if(rs==null)continue;
						if(!rs.next())//new platform/station
						{
							if(newStations!=null)
								isNew=newStations.add(ob.getHandle());
							if(log!=null&&isNew)
								log.info(String.format("Find new station with the handle: %s", ob.getHandle()));
							rs.close();
							rs=null;
							continue;
						}
						this.lookup.put(ob.getHandle(),new Station(rs.getLong(1),rs.getFloat(2),rs.getFloat(3),rs.getFloat(4)));
						rs.close();
					    rs=null;
						
					}
					data.add(ob);
					
				}
				
			}
		}
		catch(SQLException se)
		{
			if(log!=null)
			{
				log.info("An SQL Exception was occured when checking the existance of records!");
				log.severe(se.getMessage());
			}
		}
		finally
		{
			try
			{
				if(rs!=null)
					rs.close();
				if(pst!=null)
					pst.close();
				if(conn!=null)
					conn.close();
			}
			catch(Exception e){}
		}
		return data;
	}
	public long[] getSensor(PreparedStatement sel,PreparedStatement ins,long platId,long sTypeId,long mTypeId,String shortName,float alt) throws SQLException
	{
		long[] Ids=new long[2];
		Ids[0]=Ids[1]=-1;
		if(sel==null||ins==null)
			return Ids;
		ResultSet rs=null;
		
		try
		{
			sel.setLong(1, platId);
			sel.setLong(2, sTypeId);
			rs=sel.executeQuery();
			if(rs.next())
			{
				Ids[0]=rs.getLong(1);
				Ids[1]=rs.getLong(2);
			}
			else
			{
				rs.close();
				ins.setLong(1, platId);
				ins.setLong(2, sTypeId);
				ins.setString(3, shortName);
				ins.setLong(4,mTypeId);
				ins.setFloat(5, alt);
				ins.setTimestamp(6, new Timestamp(System.currentTimeMillis()),this.obsCalendar);
				ins.execute();
				sel.setLong(1, platId);
				sel.setLong(2, sTypeId);
				rs=sel.executeQuery();
				if(rs.next())
				{
					Ids[0]=rs.getLong(1);
					Ids[1]=rs.getLong(2);
				}
				++this.sensorCount;
			}
			if(rs!=null)
				rs.close();
			//sel.clearParameters();
			return Ids;
		}
		catch(SQLException se)
		{
			throw se;
		}
	}
	public void InsertResult2D(PreparedStatement pst,long platId,long sensorId,long typeId,long date,float lon,float lat,float alt,float result) throws SQLException
	{
		_insertResult(pst,platId,sensorId,typeId,date,lon,lat,alt,result,-9999,-9999);
	}
	public void InsertResult3D(PreparedStatement pst,long platId,long sensorId,long typeId,long date,float lon,float lat,float alt,float result,float depth,float order) throws SQLException
	{
		_insertResult(pst,platId,sensorId,typeId,date,lon,lat,alt,result,depth,order);
	}
	private void _insertResult(PreparedStatement pst,long platId,long sensorId,long typeId,long date,float lon,float lat,float alt,float result,float depth,float order) throws SQLException
	{
		if(pst==null)return;
		Float value2=null;
		try
		{
			Timestamp tims=new Timestamp(date);
			tims.setNanos(0);
			pst.setLong(1, platId);
			pst.setLong(2, sensorId);
			pst.setLong(3, typeId);
			pst.setTimestamp(4, tims,this.obsCalendar);
			pst.setFloat(5, lon);
			pst.setFloat(6, lat);
			pst.setFloat(7, alt);
			pst.setFloat(8, result);
			//never used for coverter
			if(converter!=null)//the 9th parm needed
			{
				value2=converter.getResult(typeId, 0, 0,result);
				if(value2!=null)
					pst.setFloat(9, value2);
				else
					pst.setNull(9, java.sql.Types.FLOAT);
			}
			if(depth!=-9999)
				pst.setFloat(9, depth);
			else
				pst.setNull(9, java.sql.Types.FLOAT);
			if(order>=0)
				pst.setFloat(10, order);
			else
				pst.setNull(10, java.sql.Types.FLOAT);
			pst.execute();
			//pst.clearParameters();
			++this.observCount;
		}
		catch(SQLException se)
		{
			throw se;
		}
		
	}
	public void InsertObservation(ArrayList<Observation> obs,ILookupID ConfigManager,String table)
	{
		Connection conn=null;
		PreparedStatement pstSelSensor=null;
		PreparedStatement pstInsSensor=null;
		PreparedStatement pstInsObs=null;
		
		ResultSet rs=null;
		long platformId=-1;
		long[] Ids=null;
		float alt=-9999;
		float lon=-9999;
		float lat=-9999;
		float result=Float.NaN;
		Station platform=null;
		long lastSensorCount=0;
		long lastObservCount=0;
		if(obs==null||obs.size()==0)
			return;
		if(table==null||table.equals(""))
			return;
		try
		{
			conn=DriverManager.getConnection(url,user,password);
			pstSelSensor=conn.prepareStatement("select row_id, m_type_id from sensor where platform_id=? and type_id=?;");
			pstInsSensor=conn.prepareStatement("insert into sensor(platform_id,type_id,short_name,m_type_id,fixed_z, begin_date) values(?,?,?,?,?,?);");
			//if(converter==null)
			//	pstInsObs=conn.prepareStatement("insert into "+table+"(platform_handle,sensor_id,m_type_id,m_date,m_lon,m_lat,m_z,m_value) values(?,?,?,?,?,?,?,?);");
			//else
				pstInsObs=conn.prepareStatement("insert into "+table+"(platform_handle,sensor_id,m_type_id,m_date,m_lon,m_lat,m_z,m_value,m_value_2,m_value_3) values(?,?,?,?,?,?,?,?,?,?);");
			if(conn!=null)
			{
				for(Observation ob:obs)
				{
					if(lookup!=null)	
						platform=lookup.get(ob.getHandle());
					if(platform==null)
						continue;
					if(dateFilter!=null)
					{
						if(!dateFilter.IsNewData(ob.getHandle(),ob.getDate()))
							continue;
					}
					platformId=platform.getId();
					if(platformId==-1)
						continue;
								
					try
					{
						conn.setAutoCommit(false);
						//Add your logic here
						lastSensorCount=this.sensorCount;
						lastObservCount=this.observCount;
						if(Float.isNaN(lon=ob.getWestLongitude()*-1.0f))//try ship first
						    if(Float.isNaN(lon=platform.getLongitude()))//then try fixed station
						    	lon=-9999;
						if(Float.isNaN(lat=ob.getNorthLatitude()))
							if(Float.isNaN(lat=platform.getLatitude()))
								lat=-9999;
						if(Float.isNaN(alt=platform.getAltitude()))
							alt=-9999;
						if(!Float.isNaN(result=ob.getAirTemperature()))
						{
							if(!(result>999.89&&result<999.91))
							{
								Ids=getSensor(pstSelSensor,pstInsSensor,
										      platformId,ConfigManager.getAirTempSensorId(),
										      ConfigManager.getAirTempMeasureId(),
										      "ATMP",
										      alt);
								if(result>50.0)
									result=FLAGGED;
								InsertResult2D(pstInsObs,platformId,Ids[0],ConfigManager.getAirTempMeasureId(),ob.getDate(),lon,lat,alt,result);
								
							}
						}
						if(!Float.isNaN(result=ob.getDewPoint()))
						{
							if(!(result>999.89&&result<999.91))
							{
								Ids=getSensor(pstSelSensor,pstInsSensor,
									      platformId,ConfigManager.getDewPntSensorId(),
									      ConfigManager.getDewPntMeasureId(),
									      "DEWP",
									      alt);
								if(result>50.0 || result<-30.0)
									result = FLAGGED;
								InsertResult2D(pstInsObs,platformId,Ids[0],ConfigManager.getDewPntMeasureId(),ob.getDate(),lon,lat,alt,result);
								
							}
						}
						if(!Float.isNaN(result=ob.getWindDirection()))
						{
							if(!(result>998.89&&result<999.01))
							{
								Ids=getSensor(pstSelSensor,pstInsSensor,
									      platformId,ConfigManager.getWndDirectSensorId(),
									      ConfigManager.getWndDirectMeasureId(),
									      "WDIR",
									      alt);
								if(result>360.0||result<0.0)
									result = FLAGGED;
								InsertResult2D(pstInsObs,platformId,Ids[0],ConfigManager.getWndDirectMeasureId(),ob.getDate(),lon,lat,alt,result);
								
							}
						}
						if(!Float.isNaN(result=ob.getWindSpeed()))
						{
							if(!(result>99.89&&result<99.91))
							{
								Ids=getSensor(pstSelSensor,pstInsSensor,
									      platformId,ConfigManager.getWndSpeedSensorId(),
									      ConfigManager.getWndSpeedMeasureId(),
									      "WSPD",
									      alt);
								if(result>50.0||result<0.0)
									result = FLAGGED;
								InsertResult2D(pstInsObs,platformId,Ids[0],ConfigManager.getWndSpeedMeasureId(),ob.getDate(),lon,lat,alt,result);
								
							}
						}
						if(!Float.isNaN(result=ob.getMaxWindGust()))
						{
							if(!(result>99.89&&result<99.91))
							{
								Ids=getSensor(pstSelSensor,pstInsSensor,
									      platformId,ConfigManager.getMaxWndGustSensorId(),
									      ConfigManager.getMaxWndGustMeasureId(),
									      "GST",
									      alt);
								if(result>50.0||result<0.0)
									result = FLAGGED;
								InsertResult2D(pstInsObs,platformId,Ids[0],ConfigManager.getMaxWndGustMeasureId(),ob.getDate(),lon,lat,alt,result);
								
							}
						}
						if(!Float.isNaN(result=ob.getCloudCover()))
						{
							if(result!=999)
							{
								Ids=getSensor(pstSelSensor,pstInsSensor,
									      platformId,ConfigManager.getCloudCoverSensorId(),
									      ConfigManager.getCloudCoverMeasureId(),
									      "CCVR",
									      alt);
								//if(result>1.0||result<0.0)
								//	result = FLAGGED;
								InsertResult2D(pstInsObs,platformId,Ids[0],ConfigManager.getCloudCoverMeasureId(),ob.getDate(),lon,lat,alt,result);
								
							}
						}
						if(!Float.isNaN(result=ob.getSolarRadiation()))
						{
							if(!(result>9999.89&&result<9999.91))
							{
								Ids=getSensor(pstSelSensor,pstInsSensor,
									      platformId,ConfigManager.getSolarRadiationSensorId(),
									      ConfigManager.getSolarRadiationMeasureId(),
									      "SRAD",
									      alt);
								InsertResult2D(pstInsObs,platformId,Ids[0],ConfigManager.getSolarRadiationMeasureId(),ob.getDate(),lon,lat,alt,result);
								
							}
						}
						if(!Float.isNaN(result=ob.getBarometricPressure()))
						{
							if(!(result>9999.89&&result<9999.91))
							{
								Ids=getSensor(pstSelSensor,pstInsSensor,
									      platformId,ConfigManager.getBarPressureSensorId(),
									      ConfigManager.getBarPressureMeasureId(),
									      "PRES",
									      alt);
								if(result>1600.0||result<933.0)
									result = FLAGGED;
								InsertResult2D(pstInsObs,platformId,Ids[0],ConfigManager.getBarPressureMeasureId(),ob.getDate(),lon,lat,alt,result);
								
							}
						}
						if(!Float.isNaN(result=ob.getWaterTemperature()))
						{
							if(!(result>99.89&&result<99.91))
							{
								Ids=getSensor(pstSelSensor,pstInsSensor,
									      platformId,ConfigManager.getWaterTempSensorId(),
									      ConfigManager.getWaterTempMeasureId(),
									      "WTMP",
									      alt);
								if(result>40.0||result<0.0)
									result = FLAGGED;
								InsertResult2D(pstInsObs,platformId,Ids[0],ConfigManager.getWaterTempMeasureId(),ob.getDate(),lon,lat,alt,result);
								
							}
						}
						if(!Float.isNaN(result=ob.getSigWaveHeight()))
						{
							if(!(result>99.89&&result<99.91))
							{
								Ids=getSensor(pstSelSensor,pstInsSensor,
									      platformId,ConfigManager.getWaveHeightSensorId(),
									      ConfigManager.getWaveHeightMeasureId(),
									      "WVHT",
									      alt);
								if(result>10.0||result<0.0)
									result = FLAGGED;
								InsertResult2D(pstInsObs,platformId,Ids[0],ConfigManager.getWaveHeightMeasureId(),ob.getDate(),lon,lat,alt,result);
								
							}
						}
						if(!Float.isNaN(result=ob.getWavePeroid()))
						{
							if(!(result>99.89&&result<99.91))
							{
								Ids=getSensor(pstSelSensor,pstInsSensor,
									      platformId,ConfigManager.getWavePeriodSensorId(),
									      ConfigManager.getWavePeriodMeasureId(),
									      "WPRD",
									      alt);
								if(result>15.0||result<0.0)
									result = FLAGGED;
								InsertResult2D(pstInsObs,platformId,Ids[0],ConfigManager.getWavePeriodMeasureId(),ob.getDate(),lon,lat,alt,result);
								
							}
						}
						if(!Float.isNaN(result=ob.getRelativeHumidity()))
						{
							if(!(result>99.89&&result<99.91))
							{
								Ids=getSensor(pstSelSensor,pstInsSensor,
									      platformId,ConfigManager.getRelativeHumiditySensorId(),
									      ConfigManager.getRelativeHumidity2MeasureId(),
									      "RH1",
									      alt);
								if(result>100.0||result<0.0)
									result = FLAGGED;
								InsertResult2D(pstInsObs,platformId,Ids[0],ConfigManager.getRelativeHumidity2MeasureId(),ob.getDate(),lon,lat,alt,result);
								
							}
						}
						if(!Float.isNaN(result=ob.getWaterConductivity()))
						{
							//if(!(result>99.89&&result<99.91))
							{
								Ids=getSensor(pstSelSensor,pstInsSensor,
									      platformId,ConfigManager.getWaterConductivitySensorId(),
									      ConfigManager.getWaterConductivityMeasureId(),
									      "SPCOND",
									      alt);
								if(result>700.0||result<100.0)
									result = FLAGGED;
								InsertResult2D(pstInsObs,platformId,Ids[0],ConfigManager.getWaterConductivityMeasureId(),ob.getDate(),lon,lat,alt,result);
								
							}
						}
						if(!Float.isNaN(result=ob.getPh()))
						{
							//if(!(result>99.89&&result<99.91))
							{
								Ids=getSensor(pstSelSensor,pstInsSensor,
									      platformId,ConfigManager.getPHSensorId(),
									      ConfigManager.getPHMeasureId(),
									      "PH",
									      alt);
								if(result>10.0||result<6.0)
									result = FLAGGED;
								InsertResult2D(pstInsObs,platformId,Ids[0],ConfigManager.getPHMeasureId(),ob.getDate(),lon,lat,alt,result);
								
							}
						}
						if(!Float.isNaN(result=ob.getYsiTurbidity()))
						{
							//if(!(result>99.89&&result<99.91))
							{
								Ids=getSensor(pstSelSensor,pstInsSensor,
									      platformId,ConfigManager.getYSITurbiditySensorId(),
									      ConfigManager.getYSITurbidityMeasureId(),
									      "YTURBI",
									      alt);
								if(result>1000.0||result<0.0)
									result = FLAGGED;
								InsertResult2D(pstInsObs,platformId,Ids[0],ConfigManager.getYSITurbidityMeasureId(),ob.getDate(),lon,lat,alt,result);
								
							}
						}
						if(!Float.isNaN(result=ob.getYsiChlorophyll()))
						{
							//if(!(result>99.89&&result<99.91))
							{
								Ids=getSensor(pstSelSensor,pstInsSensor,
									      platformId,ConfigManager.getYSIChlorophyllSensorId(),
									      ConfigManager.getYSIChlorophyllMeasureId(),
									      "YCHLOR",
									      alt);
								if(result>120.0||result<-5.0)
									result = FLAGGED;
								InsertResult2D(pstInsObs,platformId,Ids[0],ConfigManager.getYSIChlorophyllMeasureId(),ob.getDate(),lon,lat,alt,result);
								
							}
						}
						if(!Float.isNaN(result=ob.getYsiBlueGreenAlgae()))
						{
							//if(!(result>99.89&&result<99.91))
							{
								Ids=getSensor(pstSelSensor,pstInsSensor,
									      platformId,ConfigManager.getYSIBlueGreenAlgaeSensorId(),
									      ConfigManager.getYSIBlueGreenAlgaeMeasureId(),
									      "YBGALG",
									      alt);
								if(result>120.0||result<-5.0)
									result = FLAGGED;
								InsertResult2D(pstInsObs,platformId,Ids[0],ConfigManager.getYSIBlueGreenAlgaeMeasureId(),ob.getDate(),lon,lat,alt,result);
								
							}
						}
						if(!Float.isNaN(result=ob.getDissovledOxygen()))
						{
							//if(!(result>99.89&&result<99.91))
							{
								Ids=getSensor(pstSelSensor,pstInsSensor,
									      platformId,ConfigManager.getDissolvedOxygenSensorId(),
									      ConfigManager.getDissolvedOxygenMeasureId(),
									      "DISOXY",
									      alt);
								if(result>25.0||result<-1.0)
									result = FLAGGED;
								InsertResult2D(pstInsObs,platformId,Ids[0],ConfigManager.getDissolvedOxygenMeasureId(),ob.getDate(),lon,lat,alt,result);
								
							}
						}
						if(!Float.isNaN(result=ob.getDissovledOxygenSaturation()))
						{
							//if(!(result>99.89&&result<99.91))
							{
								Ids=getSensor(pstSelSensor,pstInsSensor,
									      platformId,ConfigManager.getDissolvedOxygenSaturationSensorId(),
									      ConfigManager.getDissolvedOxygenSaturationMeasureId(),
									      "DIOSAT",
									      alt);
								if(result>150.0||result<0.0)
									result = FLAGGED;
								InsertResult2D(pstInsObs,platformId,Ids[0],ConfigManager.getDissolvedOxygenSaturationMeasureId(),ob.getDate(),lon,lat,alt,result);
								
							}
						}
						if(ob.getThermalString()!=null&&ob.getThermalString().size()>0)
						{
							ArrayList<ObsZ> thermals=ob.getThermalString();
							for(int i=0;i<thermals.size();++i)
							{
								Ids=getSensor(pstSelSensor,pstInsSensor,
									      platformId,ConfigManager.getThermalStringSensorId(),
									      ConfigManager.getThermalStringMeasureId(),
									      "TTAD",
									      -9999);
								InsertResult3D(pstInsObs,platformId,Ids[0],ConfigManager.getThermalStringMeasureId(),ob.getDate(),lon,lat,alt,
												(float)thermals.get(i).value,(float)thermals.get(i).depth,i+1);
							}
							//use one extra row as the place to store the number of nodes in m_value
							InsertResult3D(pstInsObs,platformId,Ids[0],ConfigManager.getThermalStringMeasureId(),ob.getDate(),lon,lat,alt,
									(float)thermals.size(),-9999,-9999);
						}
						/*if(ob.getChlorophy2Concentration()!=null&&ob.getChlorophy2Concentration().size()>0)
						{
							ArrayList<ObsZ> chlorophy=ob.getChlorophy2Concentration();
							for(int i=0;i<chlorophy.size();++i)
							{
								Ids=getSensor(pstSelSensor,pstInsSensor,
									      platformId,ConfigManager.getChlorophy2SensorId(),
									      ConfigManager.getChlorophy2MeasureId(),
									      "CLCON",
									      -9999);
								InsertResult3D(pstInsObs,platformId,Ids[0],ConfigManager.getChlorophy2MeasureId(),ob.getDate(),lon,lat,alt,
												(float)chlorophy.get(i).value,(float)chlorophy.get(i).depth,i+1);
							}
							//use one extra row as the place to store the number of nodes in m_value
							InsertResult3D(pstInsObs,platformId,Ids[0],ConfigManager.getChlorophy2MeasureId(),ob.getDate(),lon,lat,alt,
									(float)chlorophy.size(),-9999,-9999);
						}*/
						
						conn.commit();
						conn.setAutoCommit(true);
						
						dateFilter.updateDate(ob.getHandle(), ob.getDate());
					}
					catch(Exception inne)
					{
						conn.rollback();
						this.sensorCount=lastSensorCount;
						this.observCount=lastObservCount;
						if(log!=null)
						{
							log.info(String.format("Transaction is being Rollback on the record %s with the date %d!",ob.getHandle(),ob.getDate()));
							log.severe(inne.getMessage());
						}
					}
				}
				
			}
			
		}
		catch(SQLException se)
		{
			if(log!=null)
			{
				log.info("An SQL Exception was occured when inserting records!");
				log.severe(se.getMessage());
			}
		}
		finally
		{
			try
			{
				if(rs!=null)
					rs.close();
				if(pstSelSensor!=null)
					pstSelSensor.close();
				if(pstInsSensor!=null)
					pstInsSensor.close();
				if(conn!=null)
					conn.close();
			}
			catch(Exception e){}
		}
	}
	public void TryCreateObsTableCache(String cacheTable)
	{
		if(cacheTable==null||cacheTable.equals(""))
		{
			if(log!=null)
				log.severe("Enable_Hourly_Cache is set, but OBS_Cached_Table_Name is not valid!");
			return;
		}
		if(this.cacheSqlStm==null||this.cacheSqlStm.equals(""))
		{
			if(log!=null)
				log.severe("Enable_Hourly_Cache, OBS_Cached_Table_Name are set,but OBS_Cached_Sql is not valid!");
			return;
		}
		else
			cacheSqlStm=cacheSqlStm.replaceAll("\\*",QueryFields);
		Connection conn=null;
		ResultSet rs=null;
		Statement st=null;
		PreparedStatement pst=null;
		/*ArrayList<ObsSnapShot> obs=null;
		ObsSnapShot ob=null;*/
		try
		{
			conn=DriverManager.getConnection(url,user,password);
			if(conn!=null)
			{
				st=conn.createStatement();
				if(st!=null)
				{
					st.execute("delete from multi_obs_hourly;");
					hourlyCachedCount=st.executeUpdate("insert into multi_obs_hourly(platform_handle,sensor_id,m_type_id,m_date,m_lon,m_lat,m_z,m_value,row_entry_date,row_update_date)"+cacheSqlStm);
					//rs=st.executeQuery(cacheSqlStm);
					/*if(rs!=null)
					{//platform_handle,sensor_id,m_type_id,m_date,m_lon,m_lat,m_z,m_value
						obs=new ArrayList<ObsSnapShot>();
						while(rs.next())
						{
							ob=new ObsSnapShot();
							ob.setPlatformHandler(rs.getString(1));
							ob.setSensorId(rs.getLong(2));
							ob.setMeasureId(rs.getLong(3));
							ob.setDate(rs.getTimestamp(4,obsCalendar).getTime());
							ob.setLongitude(rs.getFloat(5));
							ob.setLatitude(rs.getFloat(6));
							ob.setAltitude(rs.getFloat(7));
							ob.setMeasureValue(rs.getFloat(8));
							ob.setRowEntryDate(rs.getTimestamp(9,obsCalendar).getTime());
							ob.setRowUpdateDate(rs.getTimestamp(10,obsCalendar).getTime());
							obs.add(ob);
						}
						ob=null;
						rs.close();
						
						st.execute("delete from multi_obs_hourly;");
						{
							st.close();
							pst=conn.prepareStatement("insert into multi_obs_hourly(platform_handle,sensor_id,m_type_id,m_date,m_lon,m_lat,m_z,m_value,row_entry_date,row_update_date) values(?,?,?,?,?,?,?,?,?,?);");
							for(ObsSnapShot oss:obs)
							{
								pst.setString(1, oss.getPlatformHandler());
								pst.setLong(2, oss.getSensorId());
								pst.setLong(3, oss.getMeasureId());
								pst.setTimestamp(4, new Timestamp(oss.getDate()),obsCalendar);
								pst.setFloat(5, oss.getLongitude());
								pst.setFloat(6, oss.getLatitude());
								pst.setFloat(7, oss.getAltitude());
								pst.setFloat(8, oss.getMeasureValue());
								pst.setTimestamp(9, new Timestamp(oss.getRowEntryDate()),obsCalendar);
								pst.setTimestamp(10, new Timestamp(oss.getRowUpdateDate()),obsCalendar);
								pst.execute();
								cachedCount++;
							}
						}
					}*/
				}
			}
		}
		catch(SQLException se)
		{
			if(log!=null)
			{
				log.info("An SQL Exception occured when creating the hourly cache!");
				log.severe(se.getMessage());
			}
		}
		finally
		{
			try
			{
				if(rs!=null)
					rs.close();
				if(st!=null)
					st.close();
				if(pst!=null)
					pst.close();
				if(conn!=null)
					conn.close();
			}
			catch(Exception e){}
		}
	}
	public void TryCreateLatestObsCache(String cacheTable,Set<Object> stations)
	{
		if(cacheTable==null||cacheTable.equals(""))
		{
			if(log!=null)
				log.severe("Enable_Latest_Cache is set, but OBS_Cache_Latest_Table_Name is not valid!");
			return;
		}
		if(stations==null||stations.size()==0)
		{
			if(log!=null)
				log.severe("Can not retrieve any staions info from the stations.dat file!");
			return;
		}
		Connection conn=null;
		ResultSet rs=null;
		Statement st=null;
		PreparedStatement pst=null;
		String station=null;
		String sql="insert into multi_obs_latest (platform_handle,sensor_id,m_type_id,m_date,m_lon,m_lat,m_z,m_value,row_entry_date,row_update_date,obsName,stdUnitName,obs_abbreviation,org_shortname) SELECT mo.platform_handle,mo.sensor_id,mo.m_type_id,mo.m_date,mo.m_lon,mo.m_lat,mo.m_z,mo.m_value,mo.row_entry_date,mo.row_update_date,ot.standard_name ,ut.display,pt.obs_abbreviation,o.short_name   FROM multi_obs mo inner join m_type mt on (mo.m_type_id=mt.row_id) inner join m_scalar_type mst on (mst.row_id=mt.m_scalar_type_id) inner join uom_type ut on (ut.row_id=mst.uom_type_id) inner join obs_type ot on (mst.obs_type_id=ot.row_id) inner join platform p on (p.row_id=mo.platform_handle) inner join platform_type pt on(pt.row_id=p.type_id) inner join organization o on(o.row_id=p.organization_id) where mo.m_date=(select max(multi_obs.m_date) from multi_obs where platform_handle=(select row_id from platform where nws_handle=? or radio_call_sign=?)) and platform_handle=(select row_id from platform where nws_handle=? or radio_call_sign=?);";//"insert into %s (platform_handle,sensor_id,m_type_id,m_date,m_lon,m_lat,m_z,m_value,row_entry_date,row_update_date) SELECT platform_handle,sensor_id,m_type_id,m_date,m_lon,m_lat,m_z,m_value,row_entry_date,row_update_date  FROM multi_obs where multi_obs.m_date=(select max(multi_obs.m_date) from multi_obs where platform_handle=(select row_id from platform where nws_handle=? or radio_call_sign=?)) and platform_handle=(select row_id from platform where nws_handle=? or radio_call_sign=?);";//where (m_date,platform_handle) IN (select max(multi_obs.m_date),platform_handle from multi_obs where platform_handle=(select row_id from platform where nws_handle=? or radio_call_sign=?) group by platform_handle);";
		try
		{
			conn=DriverManager.getConnection(url,user,password);
			if(conn!=null)
			{
				st=conn.createStatement();
				if(st!=null)
				{
					st.execute("delete from multi_obs_latest;");
					st.close();
					pst=conn.prepareStatement(String.format(sql,cacheTable ));
					for(Object obj:stations)
					{
						if(obj==null)
							continue;
						station=obj.toString();
						pst.setString(1, station);
						pst.setString(2, station);
						pst.setString(3, station);
						pst.setString(4, station);
						latestCachedCount+=pst.executeUpdate();
					}
				}
			}
		}
		catch(SQLException se)
		{
			if(log!=null)
			{
				log.info("An SQL Exception occured when creating the latest cache!");
				log.severe(se.getMessage());
			}
		}
		finally
		{
			try
			{
				if(rs!=null)
					rs.close();
				if(st!=null)
					st.close();
				if(pst!=null)
					pst.close();
				if(conn!=null)
					conn.close();
			}
			catch(Exception e){}
		}
	}
	public void createUpdateMgr(java.util.Properties props)
	{
		if(props==null)return;
		String sql_plt="select row_id,nws_handle,radio_call_sign from platform;";
		String sql_obs="SELECT max(m_date) FROM multi_obs where platform_handle=?;";
		Connection conn=null;
		ResultSet rs=null;
		Statement st=null;
		PreparedStatement pst=null;
		HashMap<Long,String> ptInfo=new HashMap<Long,String>();
		long plt_id=-1;
		String nws=null;
		String radio=null;
		Timestamp ts=null;
		try
		{
			conn=DriverManager.getConnection(url,user,password);
			if(conn!=null)
			{
				st=conn.createStatement();
				if(st!=null)
				{
					rs=st.executeQuery(sql_plt);
					if(rs!=null)
					{
						while(rs.next())
						{
							plt_id=rs.getLong(1);
							if(plt_id>=0)
							{
								nws=rs.getString(2);
								radio=rs.getString(3);
								if(nws!=null&&!nws.equals(""))
									ptInfo.put(plt_id, nws);
								else if(radio!=null&&!radio.equals(""))
									ptInfo.put(plt_id, radio);
							}
						}
						rs.close();
					}
				}
				st.close();
				if(ptInfo.size()>0)
				{
					pst=conn.prepareStatement(sql_obs);
					for(Long id:ptInfo.keySet())
					{
						pst.setString(1, id.toString());
						rs=pst.executeQuery();
						if(rs!=null)
						{
							if(rs.next())
							{
								ts=rs.getTimestamp(1);
								if(ts!=null)
									props.setProperty(ptInfo.get(id), ((Long)ts.getTime()).toString());
							}
							rs.close();
						}
						pst.clearParameters();
					}
				}
			}
		}
		catch(SQLException se)
		{
			if(log!=null)
			{
				log.info("An SQL Exception occured when creating the station.dat!");
				log.severe(se.getMessage());
			}
		}
		finally
		{
			try
			{
				if(rs!=null)
					rs.close();
				if(st!=null)
					st.close();
				if(pst!=null)
					pst.close();
				if(conn!=null)
					conn.close();
			}
			catch(Exception e){}
		}
	}
	public void createUpdateMgr1(java.util.Properties props,String tableName)
	{
		if(props==null)return;
		String sql="SELECT distinct p.nws_handle,p.radio_call_sign,m.m_date,p.nos_handle,p.icao_handle,p.coop_handle,p.wmo_handle,p.ndbc_handle,p.other_id FROM "+tableName+" m inner join platform p on(p.row_id=m.platform_handle) where (m.m_date,platform_handle) in(select max(m_date),platform_handle from multi_obs group by platform_handle);";
		String sql1="select distinct p.nws_handle,p.radio_call_sign,p.nos_handle,p.icao_handle,p.coop_handle,p.wmo_handle,p.ndbc_handle,p.other_id FROM platform p where p.row_id not in (select distinct platform_handle from "+tableName+");";
		Connection conn=null;
		ResultSet rs=null;
		Statement st=null;
		String nos=null;
		String nws=null;
		String radio=null;
		String icao=null;
		String coop=null;
		String wmo=null;
		String ndbc=null;
		String otherid=null;
		Timestamp ts=null;
		try
		{
			conn=DriverManager.getConnection(url,user,password);
			if(conn!=null)
			{
				st=conn.createStatement();
				if(st!=null)
				{
					rs=st.executeQuery(sql1);
					if(rs!=null)
					{   //sync the platforms not having data in the obs table,
						//this will include new platforms
						while(rs.next())
						{
							nws=rs.getString(1);
							radio=rs.getString(2);
							
							nos=rs.getString(3);
							icao=rs.getString(4);
							coop=rs.getString(5);
							wmo=rs.getString(6);
							ndbc=rs.getString(7);
							otherid=rs.getString(8);
							//there are duplicated entries in the stations.dat
							if(nws!=null&&!nws.equals(""))
								props.setProperty(nws,"0");
							if(icao!=null&&!icao.equals(""))
								props.setProperty(icao,"0");
							if(coop!=null&&!coop.equals(""))
								props.setProperty(coop,"0");
							if(nos!=null&&!nos.equals(""))
								props.setProperty(nos,"0");
							if(radio!=null&&!radio.equals(""))
								props.setProperty(radio, "0");
							if(wmo!=null&&!wmo.equals(""))
								props.setProperty(wmo,"0");
							if(ndbc!=null&&!ndbc.equals(""))
								props.setProperty(ndbc,"0");
							if(otherid!=null&&!otherid.equals(""))
								props.setProperty(otherid, "0");
							
						}
					}
					rs.close();
					rs=null;
					rs=st.executeQuery(sql);
					if(rs!=null)
					{   //sync regular platforms that are currently active
						while(rs.next())
						{
							nws=rs.getString(1);
							radio=rs.getString(2);
							ts=rs.getTimestamp(3);
							nos=rs.getString(4);
							icao=rs.getString(5);
							coop=rs.getString(6);
							wmo=rs.getString(7);
							ndbc=rs.getString(8);
							otherid=rs.getString(9);
							//there are duplicated entries in the stations.dat
							if(nws!=null&&!nws.equals(""))
								props.setProperty(nws,((Long)ts.getTime()).toString());
							if(icao!=null&&!icao.equals(""))
								props.setProperty(icao,((Long)ts.getTime()).toString());
							if(coop!=null&&!coop.equals(""))
								props.setProperty(coop,((Long)ts.getTime()).toString());
							if(nos!=null&&!nos.equals(""))
								props.setProperty(nos,((Long)ts.getTime()).toString());
							if(radio!=null&&!radio.equals(""))
								props.setProperty(radio, ((Long)ts.getTime()).toString());
							if(wmo!=null&&!wmo.equals(""))
								props.setProperty(wmo,((Long)ts.getTime()).toString());
							if(ndbc!=null&&!ndbc.equals(""))
								props.setProperty(ndbc,((Long)ts.getTime()).toString());
							if(otherid!=null&&!otherid.equals(""))
								props.setProperty(otherid,((Long)ts.getTime()).toString());
							
						}
						rs.close();
					}
				}
				st.close();
				
			}
		}
		catch(SQLException se)
		{
			if(log!=null)
			{
				log.info("An SQL Exception occured when sync the station.dat!");
				log.severe(se.getMessage());
			}
		}
		finally
		{
			try
			{
				if(rs!=null)
					rs.close();
				if(st!=null)
					st.close();
				if(conn!=null)
					conn.close();
			}
			catch(Exception e){}
		}
	}
	public void initStations(java.util.Properties props)
	{
		if(props==null)return;
		String sql="SELECT distinct on(row_id) nos_handle,nws_handle,icao_handle,coop_handle,wmo_handle,radio_call_sign,ndbc_handle from platform;";
		Connection conn=null;
		ResultSet rs=null;
		Statement st=null;
		String nos=null;
		String nws=null;
		String radio=null;
		String icao=null;
		String coop=null;
		String wmo=null;
		String ndbc=null;
		try
		{
			conn=DriverManager.getConnection(url,user,password);
			if(conn!=null)
			{
				st=conn.createStatement();
				if(st!=null)
				{
					rs=st.executeQuery(sql);
					if(rs!=null)
					{
						while(rs.next())
						{
							nos=rs.getString(1);
							nws=rs.getString(2);
							icao=rs.getString(3);
							coop=rs.getString(4);
							wmo=rs.getString(5);
							radio=rs.getString(6);
							ndbc=rs.getString(7);
							//there are duplicated entries in the stations.dat
							if(nws!=null&&!nws.equals(""))
								props.setProperty(nws,"0");
							if(icao!=null&&!icao.equals(""))
								props.setProperty(icao, "0");
							if(coop!=null&&!coop.equals(""))
								props.setProperty(coop, "0");
							if(nos!=null&&!nos.equals(""))
							    props.setProperty(nos, "0");
							if(radio!=null&&!radio.equals(""))
								props.setProperty(radio, "0");
							if(wmo!=null&&!wmo.equals(""))
								props.setProperty(wmo, "0");
							if(ndbc!=null&&!ndbc.equals(""))
								props.setProperty(ndbc, "0");
						}
						rs.close();
					}
				}
				st.close();
				
			}
		}
		catch(SQLException se)
		{
			if(log!=null)
			{
				log.info("An SQL Exception occured when creating the station.dat!");
				log.severe(se.getMessage());
			}
		}
		finally
		{
			try
			{
				if(rs!=null)
					rs.close();
				if(st!=null)
					st.close();
				if(conn!=null)
					conn.close();
			}
			catch(Exception e){}
		}
	}
	
}

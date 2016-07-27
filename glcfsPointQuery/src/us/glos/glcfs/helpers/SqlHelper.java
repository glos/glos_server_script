package us.glos.glcfs.helpers;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;

import org.glc.DBConnFactory;
import org.glc.Logger;
import org.glc.domain.Coordinate;
import org.glc.xmlconfig.LogLevel;

import us.glos.glcfs.domain.NearestCoordinate;

public class SqlHelper {
	static String DB_SOURCE_R="GLCFSDB";
	static boolean isActive=false;
	static HashMap<String,String> LakeTableMap=new HashMap<String,String>(5);
	static HashMap<String,String> LakeBoundTableMap=new HashMap<String,String>(5);
	static
	{
		isActive=DBConnFactory.init(DB_SOURCE_R);
		LakeTableMap.put("erie", "glcfs_grid_erie");
		LakeTableMap.put("huron", "glcfs_grid_huron");
		LakeTableMap.put("michigan", "glcfs_grid_michigan");
		LakeTableMap.put("ontario", "glcfs_grid_ontario");
		LakeTableMap.put("superior", "glcfs_grid_superior");
		
		LakeBoundTableMap.put("erie", "glcfs_boundary_erie");
		LakeBoundTableMap.put("huron", "glcfs_boundary_huron");
		LakeBoundTableMap.put("michigan", "glcfs_boundary_michigan");
		LakeBoundTableMap.put("ontario", "glcfs_boundary_ontario");
		LakeBoundTableMap.put("superior", "glcfs_boundary_superior");
	}
	//static String SQL_NEAREST="select st_astext(st_centroid(the_geom)),st_distance(st_transform(st_geomfromtext('POINT(%f %f)',4326),4269),the_geom) as dist from glcfs_grid_huron where the_geom&& st_buffer(st_transform(st_geomfromtext('POINT(%f %f)',4326),4269),st_distance(st_transform(st_geomfromtext('POINT(%f %f)',4326),4269),(select the_geom from glcfs_boundary_huron))) order by dist asc limit 1;";
	static String SQL_NEAREST="select st_astext(st_centroid(the_geom)),st_distance(st_transform(st_geomfromtext('POINT(%f %f)',4326),93786),st_transform(the_geom,93786)) as dist from %s where the_geom&& st_buffer(st_transform(st_geomfromtext('POINT(%f %f)',4326),4269),st_distance(st_transform(st_geomfromtext('POINT(%f %f)',4326),4269),(select the_geom from %s))) order by dist asc limit %d;";
	static String SQL_CONTAIN="select st_contains(the_geom, st_transform(st_geomfromtext('POINT(%f %f)',4326),4269)) from %s;";
	public static ArrayList<Coordinate> getNearestCell(Coordinate coord,String lake,int count)
	{
		ArrayList<Coordinate> result=null;
		if(isActive&&coord!=null&&lake!=null&&LakeTableMap.get(lake)!=null&&LakeBoundTableMap.get(lake)!=null)
		{
			Connection conn=null;
			PreparedStatement st=null;
			ResultSet rs=null;
			try
			{
				conn=DBConnFactory.getConnection(DB_SOURCE_R);
				st=conn.prepareStatement(String.format(SQL_NEAREST,coord.getLon(),coord.getLat(),LakeTableMap.get(lake),coord.getLon(),coord.getLat(),coord.getLon(),coord.getLat(),LakeBoundTableMap.get(lake),count));
				rs=st.executeQuery();
				String pnt=null;
				String[] tmp=null;
				NearestCoordinate lonlat=null;
			    while(rs.next())
			    {
			    	pnt=rs.getString(1);
			    	if(pnt!=null&&pnt.length()>6)
			    	{
			    		tmp=pnt.substring(6, pnt.length()-1).split(" ");
			    		if(tmp!=null&&tmp.length==2)
			    		{
			    			try
			    			{
			    				lonlat=new NearestCoordinate();
			    				lonlat.setLon(Double.parseDouble(tmp[0]));
			    				lonlat.setLat(Double.parseDouble(tmp[1]));
			    				lonlat.setDistance(rs.getDouble(2));
			    				if(result==null)
			    					result=new ArrayList<Coordinate>();
			    				result.add(lonlat);
			    			}
			    			catch(NumberFormatException nfe)
			    			{
			    				
			    			}
			    		}
			    	}
			    }
			}
			catch(SQLException se)
			{
				Logger.writeLog(SqlHelper.class.getName()+":getNearestCell:"+se.getMessage(), LogLevel.SEVERE);
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
					rs=null;
					st=null;
					conn=null;
				}
				catch(Exception e)
				{
					Logger.writeLog(e.getMessage(), LogLevel.SEVERE);
				}
			}
		}
		return result;
	}
	
	public static boolean isInLake(Coordinate coord,String lake)
	{
		boolean result=false;
		if(isActive&&coord!=null&&lake!=null&&LakeBoundTableMap.get(lake)!=null)
		{
			Connection conn=null;
			PreparedStatement st=null;
			ResultSet rs=null;
			try
			{
				conn=DBConnFactory.getConnection(DB_SOURCE_R);
				st=conn.prepareStatement(String.format(SQL_CONTAIN,coord.getLon(),coord.getLat(),LakeBoundTableMap.get(lake)));
				rs=st.executeQuery();
				if(rs.next())
					result=rs.getBoolean(1);
			}
			catch(SQLException se)
			{
				Logger.writeLog(SqlHelper.class.getName()+":isInLake:"+se.getMessage(), LogLevel.SEVERE);
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
					rs=null;
					st=null;
					conn=null;
				}
				catch(Exception e)
				{
					Logger.writeLog(e.getMessage(), LogLevel.SEVERE);
				}
			}
		}
		return result;
	}
}

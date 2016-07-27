package org.glc.glos;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.logging.Logger;
import java.io.IOException;
import org.glc.glos.coastwatch.ConfigManager;
import org.glc.glos.coastwatch.IDateFilter;
import org.glc.glos.coastwatch.LookupIDImpl;
import org.glc.glos.coastwatch.SqlHelper;
import org.glc.glos.coastwatch.UnitConverter;
import org.glc.glos.coastwatch.UpdateManager;
import org.glc.glos.coastwatch.domain.Observation;

public class Eagle {
    public Eagle()
    {
    	
    }
    public void hover(Logger log,ArrayList<Observation> list,String id,String tableName,String stationFile) throws IOException, ClassNotFoundException
    {
    	if(list==null||id==null||tableName==null||stationFile==null)
    		return;
    	HashSet<String> newStations=null;
    	//try
    	{
    		final UpdateManager updateMgr=new UpdateManager(stationFile,log);

    		SqlHelper sql=new SqlHelper(ConfigManager.getJDBCDriver(),
    				ConfigManager.getConnectionString(),
    				ConfigManager.getDBAccount(),
    				ConfigManager.getDBPassword(),
    				log);
    		sql.setTimeZone(ConfigManager.getObservationTimeZone());
    		if(ConfigManager.allowUnitConversion())
    			sql.setConverter(new UnitConverter());
    		//Set the filter type of sql to IDateFilter
    		//Filter the record by updatemgr 
    		sql.setDateFilter(new IDateFilter(){
    			public boolean IsNewData(String handle,long time)
    			{
    				boolean result=false;
    				if(updateMgr!=null)
    				{
    					if(updateMgr.isEntryExist(handle))
    						result=updateMgr.IsNewData(time, handle);
    					else //not exist at all
    						result=true;
    				}
    				return result;
    			}
    			public void updateDate(String handle,long time)
    			{
    				if(updateMgr!=null)
    				{
    					if(updateMgr.isEntryExist(handle))
    						updateMgr.updateEntry(handle, time);
    					else
    						updateMgr.setNewEntry(handle, time);
    				}
    			}
    		});
    		newStations=new HashSet<String>();
    		//Return two collections, one is for new records, one is for new stations
    		if(log!=null)
    		    log.info("Begin to filter old data.");
    		ArrayList<Observation> data=sql.FilterNonExistance(list, newStations);
    		list.clear();
    		if(data!=null&&data.size()>0)
    		{
    			if(log!=null)
    			    log.fine("Begin to insert new observations.");
    			//Insert new records to DB and update latest update timestamp in the updatemgr
    			sql.InsertObservation(data,new LookupIDImpl(),tableName);
    			if(log!=null)
    			{
    			    log.info(String.format("%d new observation records inserted from the %s into %s.", sql.getObservCount(),id,tableName));
    			    log.info(String.format("%d new sensors added for existing platforms.", sql.getSensorCount()));
    			}
    			//Save new latest update timestamp to file
    			updateMgr.StoreRecords(sql);
    		}
    		else
    			if(log!=null)
    			    log.info(String.format("No new observation record available on the %s site.",id));
        }
       	/*catch(Exception e)
    	{
    		log.severe("Exception occured. Abort execution.");
    		log.severe("***********************************************************");
    	}*/
    }
    
}

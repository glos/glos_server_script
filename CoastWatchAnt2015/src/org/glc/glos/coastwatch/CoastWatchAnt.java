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

import java.util.logging.Logger;
import java.util.logging.LogManager;
import java.util.logging.Level;
import java.util.logging.FileHandler;
import java.util.logging.Formatter;
import java.util.logging.LogRecord;
import java.util.Date;
import java.util.ArrayList;
import java.util.HashSet;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.nio.channels.OverlappingFileLockException;

import org.glc.glos.coastwatch.domain.Observation;
import org.glc.glos.coastwatch.logredirect.*;

public class CoastWatchAnt {

	private static Logger log;
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		ArrayList<Observation> list=null;
		HashSet<String> newStations=null;
		RandomAccessFile lock=null;
		FileOutputStream fos=null;
		FileLock fLock=null;
		//preserve old stdout/stderr streams in case they might be useful
		PrintStream stdout=System.out;
		PrintStream stderr=System.err;
		final UpdateManager updateMgr;
		try
		{
			ConfigManager.initConfig();
			LogManager.getLogManager().reset();
			log=Logger.getLogger("org.glc.glos.coastwatch.coastwatchant");
			log.setLevel(Level.parse(ConfigManager.getLogLevel()));
			log.setUseParentHandlers(false);
			//Create file lock to prevent from multiple write-access to files
			//and make sure only one instance is allowed in the system
			if(ConfigManager.TEMP_FOLDER_PATH.endsWith(ConfigManager.FILE_SEPARATOR))
				lock=new RandomAccessFile(ConfigManager.TEMP_FOLDER_PATH+"cwAnt.lck","rw");//.createTempFile("cwAnt", "lck");
			else
				lock=new RandomAccessFile(ConfigManager.TEMP_FOLDER_PATH+ConfigManager.FILE_SEPARATOR+"cwAnt.lck","rw");
			//fos=new FileOutputStream(lock);
			FileChannel fChannel=lock.getChannel();//fos.getChannel();
			fLock=fChannel.tryLock();
			if(fLock==null)
			{
				System.err.println("An instance of CoastWatchAnt has already been running!");
				return;
			}
			//Writing log to the file
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
			System.setOut(new PrintStream(new LoggingOutputStream(Logger.getLogger("org.glc.glos.coastwatch.coastwatchant.stdout"),
					                                              StdOutErrLevel.STDOUT),
					                      true));
			System.setErr(new PrintStream(new LoggingOutputStream(Logger.getLogger("org.glc.glos.coastwatch.coastwatchant.stderr"),
                    StdOutErrLevel.STDERR),
                    true));
			
			//update station.dat file
			if(args!=null&&args.length==1)
			{
				if(args[0].toLowerCase().equals("-sync"))
				{
					log.info("Begin to update station.dat...");
					SyncUpdate su=new SyncUpdate(ConfigManager.getUpdateRecordFile(),log);
					su.Sync(ConfigManager.getJDBCDriver(),
						    ConfigManager.getConnectionString(),
						    ConfigManager.getDBAccount(),
						    ConfigManager.getDBPassword(),
						    ConfigManager.getTableNameByDate()
						    );
					log.info(String.format("%d entries updated.",su.SaveFile()));
					log.info("End updating.");
					log.info("************************************************************");
					return;
				}
				else if(args[0].toLowerCase().equals("-init"))
				{
					log.info("Begin to initialize station.dat...");
					SyncUpdate su=new SyncUpdate(ConfigManager.getUpdateRecordFile(),log);
					su.InitStations(ConfigManager.getJDBCDriver(),
						    ConfigManager.getConnectionString(),
						    ConfigManager.getDBAccount(),
						    ConfigManager.getDBPassword());
					log.info(String.format("%d entries created.",su.SaveFile()));
					log.info("End initialization.");
					log.info("************************************************************");
					return;
				}
			}
			log.info("Begin to parse data from "+ConfigManager.getTodayURL());
			//your logic here
			//Create this mgr to get all handlers with the latest update timestamp
			updateMgr=new UpdateManager(ConfigManager.getUpdateRecordFile(),log);
			//analyze the online input file  			
			CoastTodayParser parser=new CoastTodayParser(ConfigManager.getTodayURL(),ConfigManager.getDataOffset(), ConfigManager.getDataFormatRegx(), log);
			CoastTodayParser.setTimeZone(ConfigManager.getObservationTimeZone());
			list=parser.TryParse(false);
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
			ArrayList<Observation> data=null;
			if(list!=null)
			{
			    log.info("Begin to filter old data.");
			    data=sql.FilterNonExistance(list, newStations);
			    list.clear();
			}
			if(data!=null&&data.size()>0)
			{
				log.fine("Begin to insert new observations.");
				//Insert new records to DB and update latest update timestamp in the updatemgr
				sql.InsertObservation(data,new LookupIDImpl(),ConfigManager.getTableNameByDate());
				log.info(String.format("%d new observation records inserted from the CoastWatch site.", sql.getObservCount()));
				log.info(String.format("%d new sensors added for existing platforms.", sql.getSensorCount()));
				//Save new latest update timestamp to file and db
				updateMgr.StoreRecords(sql);
			}
			else
				log.info("No new observation record available on the CoastWatch site.");
			//Ant Army here:
			log.info("Start to invoke ant army to march...");
			AntCommandor.March(AntCommandor.FindAnts(log),log);
			log.info("Cease ant army.");
			if(ConfigManager.IsDBCacheEnabled())
			{
				sql.setCacheSqlStm(ConfigManager.getObservationCacheSql());
				log.info("Try to create the hourly cache table for observations");
				sql.TryCreateObsTableCache(ConfigManager.getObservationCacheTableName());
				log.info(String.format("%d observations records were cached",sql.getHourlyCachedCount()));
				log.info("Caching end.");
			}
			if(ConfigManager.IsDBLatestCacheEnabled())
			{
				log.info("Try to create the latest cache table for observations");
				sql.TryCreateLatestObsCache(ConfigManager.getLatestObsCacheTableName(),updateMgr.getStationHandles());
				log.info(String.format("%d observations records were cached",sql.getLatestCachedCount()));
				log.info("Caching end.");
			}
			//Send email if notification is enabled
			if(newStations.size()>0)
			{
				String[] msgs=new String[newStations.size()];
				if(ConfigManager.IsMailEnabled())
				{
					MailSender.Send(newStations.toArray(msgs),
							        ConfigManager.getSMTPMailServer(),
							        ConfigManager.getMailFromAccount(),
							        ConfigManager.getMailToAccounts(),
							        log);
					log.info("Mail notification enabled. Sending message to admins.");
				}
			}
			
			log.info("End parsing.");
			log.info("************************************************************");
		}
		catch(OverlappingFileLockException oe)
		{
			System.err.println("An instance of CoastWatchAnt has already been running!");
			
		}
		catch(Exception e)
		{
			e.printStackTrace();
			log.severe("Exception occured. Abort execution.");
			log.severe("***********************************************************");
		}
		finally
		{
			try
			{
				if(fLock!=null)//release the file lock
					fLock.release();
				if(fos!=null)
					fos.close();
				if(lock!=null)
				{
					//lock.delete();
					lock.close();
				}
			}
			catch(IOException e)
			{
				
			}
			
		}
	}

}

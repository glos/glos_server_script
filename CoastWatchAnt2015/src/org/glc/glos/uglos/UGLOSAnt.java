package org.glc.glos.uglos;

import java.util.logging.Logger;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileFilter;
import java.util.ArrayList;
import java.util.Properties;
import java.util.Collections;
import java.util.Comparator;

import org.glc.glos.Eagle;
import org.glc.glos.coastwatch.IAnt;
import org.glc.glos.coastwatch.ConfigManager;
import org.glc.glos.coastwatch.domain.Observation;

public class UGLOSAnt implements IAnt {

	private static final String FTP_LOCATION_KEY="UGLOS_Folders";
	private static final String UGLOS_TABLE_KEY="uglos_tables.dat";
	private static final String UGLOS_STATION_KEY="uglos_stations.dat";
	public void March(Logger log) {
		// TODO Auto-generated method stub
		if(log!=null)
		    log.info("Begin to parse UGLOS data");
					
		File file=new File(UGLOS_TABLE_KEY);
		if(file.exists()==false)
		{
			if(log!=null)
			{
				log.severe(String.format("UGLOSAnt: Can not fild %s",UGLOS_TABLE_KEY));
			}
			else
				System.err.println(String.format("UGLOSAnt: Can not fild %s",UGLOS_TABLE_KEY));
		}
		try
		{
		FileInputStream fis=new FileInputStream(file);
		Properties table=new Properties();
		table.load(fis);
		fis.close();
        String temp=ConfigManager.getCustomProperty(FTP_LOCATION_KEY);
        if(temp!=null)
        {
            String[] dirs=temp.split(ConfigManager.OFFSET_STR_DELIMITER);
            if(dirs!=null)
            {
            	FileFilter filter=new FileFilter(){
            		public boolean accept(File file)
            		{
            			return !file.isDirectory();
            		}
            		
            	};
            	File dir=null;
            	File[] files=null;
            	UGLOSXMLParser parser=null;
            	ArrayList<Observation> obs=null;
            	Eagle eagle=new Eagle();
            	
            	for(String d:dirs)
            	{
            	    dir=new File(d);
            	    if(dir==null)
            	    {
            	    	if(log!=null)
            	    	    log.info(String.format("UGLOSAnt: No directory named %s", d));
            	    	continue;
            	    }
            	    files=dir.listFiles(filter);
            	    if(files!=null)
            	    {
            	    	parser=new UGLOSXMLParser();
            	    	obs=parser.parse(files, log);
            	    	
            	    	if(obs.size()>0)
            	    	{
            	    	    Collections.sort(obs, new Comparator<Observation>(){

								public int compare(Observation o1, Observation o2) {
									// TODO Auto-generated method stub
									if(o1.getDate()>o2.getDate())
									    return 1;
									else if(o1.getDate()==o2.getDate())
										return 0;
									else
										return -1;
								}
                                              
                             });
            	    	    
            	    	    
            	    	    if(obs.get(0).getHandle().equals(obs.get(obs.size()-1).getHandle())&&
            	    	    		obs.get(obs.size()-1).getHandle().equals(obs.get(obs.size()/2).getHandle()))
            	    	    {
            	    	    	if(log!=null)
                	    	    	log.info(String.format("Ready to write to table %s",table.getProperty(obs.get(0).getHandle())));
            	    	    	eagle.hover(log, obs, "UGLOS",table.getProperty(obs.get(0).getHandle()), UGLOS_STATION_KEY);
            	    	    	
            	    	    }
            	    	    else
            	    	    	if(log!=null)
                	    	    	log.severe(String.format("Station ids are not identical in %s", d));
            	    	    	
            	    	}
            	    	
            	    }
            	    else
            	    {
            	    	if(log!=null)
            	    	    log.info(String.format("UGLOSAnt: No file in %s", d));
            	    	continue;
            	    }
            	    Process asa=null;
            	    for(File f:files)
            	    {
            	    	asa=Runtime.getRuntime().exec("cp "+f.getAbsolutePath()+" /var/ftp/asa/");
            	    	asa.waitFor();
            	    	f.delete();
            	    }
            	    dir=null;
            	    files=null;
            	    
            	}
            	
            }
            else
            {
            	if(log!=null)
            	    log.severe(String.format("UGLOSAnt: %s is not using the standard delimiter: %s", FTP_LOCATION_KEY,ConfigManager.OFFSET_STR_DELIMITER));
            }
        }
        else
        {
        	if(log!=null)
        	    log.severe(String.format("UGLOSAnt: Can not find %s in the setting file!",FTP_LOCATION_KEY));
        }
		}
		catch(Exception e)
		{
			if(log!=null)
				log.severe(String.format("UGLOSAnt: unexpected exception: %s", e.getMessage()));
		}
	}

}

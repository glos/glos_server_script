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
import java.util.ArrayList;
import java.lang.reflect.InvocationTargetException;

public class AntCommandor {
	public static ArrayList<IAnt> FindAnts(Logger log)
	{
		ArrayList<IAnt> result=null;
		String temp=null;
		String[] classNames=null;
		Class antClass=null;
		IAnt ant=null;
		if((temp=ConfigManager.getAntSoldiersClassNames())!=null)
		{
			if(true==temp.equals(""))return result;
			classNames=temp.split(ConfigManager.OFFSET_STR_DELIMITER);
			if(classNames!=null&&classNames.length>0)
			{
				
				for(String className:classNames)
				{
					if(log!=null)
						log.fine(String.format("Try to load %s",className));
					try
					{
						antClass=Class.forName(className);
						if(antClass!=null)
						{
							ant=(IAnt)antClass.getConstructor().newInstance(new Object[0]);
							if(result==null)
								result=new ArrayList<IAnt>();
							result.add(ant);
						}
					}
					catch(ClassNotFoundException cnfe)
					{
						if(log!=null)
						{
							log.info(String.format("Load class %s failed",className));
							log.severe(cnfe.getMessage());
						}
					}
					catch(InstantiationException ie)
			    	{
						if(log!=null)
						{
							log.info("Can not initialize the class specified. Is ant an abstract class or interface?");
							log.severe(ie.getMessage());
						}
			    		
			    	}
			    	catch(NoSuchMethodException ne)
			    	{
			    		if(log!=null)
						{
							log.info("Exception occured when creating the ant. No such method in the ant.");
							log.severe(ne.getMessage());
						}
			    	}
			    	catch(IllegalAccessException ile)
			    	{
			    		if(log!=null)
						{
							log.info("Exception occured when creating the ant. Access Denied.");
							log.severe(ile.getMessage());
						}
			    	}
					catch(InvocationTargetException ite)
					{
						if(log!=null)
						{
							log.info("Exception occured when creating the ant. Does ant have a non-parameter constructor?");
							log.severe(ite.getMessage());
						}
					}
				}
				
				
			}
		}
		return result;
	}
	public static void March(ArrayList<IAnt> soldiers,Logger log)
	{
		if(soldiers==null)return;
		for(IAnt ant:soldiers)
			ant.March(log);
	}
}

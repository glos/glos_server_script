package org.glc.glos.uglos;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.logging.Logger;
import java.util.Date;
//import java.util.Calendar;
import java.util.TimeZone;
import java.text.SimpleDateFormat;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import org.glc.glos.coastwatch.domain.Observation;
import org.glc.glos.coastwatch.domain.ObsZ;

public class UGLOSXMLParser {
	
    static TimeZone timeZone=null;
	static SimpleDateFormat dateFormat=null;
	//static Calendar cal=null;
	static DocumentBuilderFactory factory=null;
    {
    	factory=DocumentBuilderFactory.newInstance();
    	factory.setIgnoringComments(true);
    	factory.setIgnoringElementContentWhitespace(true);
    	//factory.setValidating(true);
    	
    	dateFormat=new SimpleDateFormat("MM/dd/yyyy HH:mm:ss");
    	timeZone=TimeZone.getTimeZone("UTC");
    	dateFormat.setTimeZone(timeZone);
    	//cal=Calendar.getInstance(timeZone);
    }
	public UGLOSXMLParser()
    {
    	
    }
    public ArrayList<Observation> parse(File[] files,Logger log)
    {
    	ArrayList<Observation> obs=null;
    	if(files!=null&&factory!=null)
    	{
    		try
    		{
    			Document doc=null;
    			//Element root=null;
    			NodeList nlist=null;
    			NodeList messageList=null;
    			Element message=null;
    			Node node=null;
    			Date date=null;
    			int msgLen=0;
    			obs=new ArrayList<Observation>();
    			Observation o=null;
    			DocumentBuilder builder=factory.newDocumentBuilder();
    			float missingVal=-9999.0f;
    			float val=missingVal;
    			boolean isGVSU=false;
    			for(File f:files)
    		    {
    				doc=builder.parse(f);
    			    //root=doc.getDocumentElement();
    			    messageList=doc.getElementsByTagName("message");
    			    if(messageList!=null&&messageList.getLength()>0)
    			    {
    			    	msgLen=messageList.getLength();
    			    	for(int i=0;i<msgLen;++i)
    			    	{
    			    		if(messageList.item(i) instanceof Element)
    			    		{
    			    			message=(Element)messageList.item(i);
    			    			nlist=message.getElementsByTagName("station");
    		    			    if(nlist!=null)
    		    			    {
    		    			    	o=new Observation();
    		    			    	node=nlist.item(0);
    		    			        o.setHandle(node.getTextContent());
    		    			        //if(node.getTextContent().equalsIgnoreCase("GVSU1"))
    		    			        //	isGVSU=true;
    		    			    }
    		    			    else
    		    			    {
    		    			    	if(log!=null)
    		    	    				log.severe("UGLOSXMLParser: Can not find tag 'station'");
    		    			    	continue;
    		    			    }
    		    			    nlist=message.getElementsByTagName("date");
    		    			    if(nlist!=null)
    		    			    {
    		    			    	node=nlist.item(0);
    		    			    	date=dateFormat.parse(node.getTextContent());
    		    			    	o.setDate(date.getTime());
    		    			    	
    		    			    }
    		    			    else
    		    			    {
    		    			    	if(log!=null)
    		    	    				log.severe("UGLOSXMLParser: Can not find tag 'date'");
    		    			    	continue;
    		    			    }
    		    			    nlist=message.getElementsByTagName("missing");
    		    			    if(nlist!=null&&nlist.getLength()>0)
    		    			    {
    		    			    	try
    		    			    	{
    		    			    		missingVal=Float.parseFloat(nlist.item(0).getTextContent());
    		    			    	}
    		    			    	catch(NumberFormatException nfe)
    		    			    	{
    		    			    		
    		    			    	}
    		    			    }
    		    			    nlist=message.getElementsByTagName("met");
    		    			    if(nlist!=null)
    		    			    {
    		    			    	nlist=null;
    		    			    	nlist=message.getElementsByTagName("wdir1");
    		    			    	if(nlist!=null&&nlist.getLength()>0)
    		    			    	{
    		    			    	    try
    		    			    	    {
    		    			    	    	val=Float.parseFloat(nlist.item(0).getTextContent());
    		    			    	    	if(val!=missingVal)
    		    			    	    		o.setWindDirection(val);
    		    			    	    	else
    		    			    	    		o.setWindDirection(Float.NaN);
    		    			    	    }
    		    			    	    catch(NumberFormatException e)
    		    			    	    {
    		    			    	    	o.setWindDirection(Float.NaN);
    		    			    	    }
    		    			    	}
    		    			    	nlist=null;
    		    			    	nlist=message.getElementsByTagName("wspd1");	
    		    			    	if(nlist!=null&&nlist.getLength()>0)
    		    			    	{
    		    			    		try
    		    			    		{
    		    			    			val=Float.parseFloat(nlist.item(0).getTextContent());
    		    			    			if(val!=missingVal)
    		    			    				o.setWindSpeed(val);
    		    			    			else
    		    			    				o.setWindSpeed(Float.NaN);
    		    			    		}
    		    			    		catch(NumberFormatException e)
    		    			    	    {
    		    			    	    	o.setWindSpeed(Float.NaN);
    		    			    	    }
    		    			    	}
    		    			    	nlist=null;
    		    			    	nlist=message.getElementsByTagName("gust1");
    		    			    	if(nlist!=null&&nlist.getLength()>0)
    		    			    	{
    		    			    	    try
    		    			    	    {
    		    			    		    val=Float.parseFloat(nlist.item(0).getTextContent());
    		    			    		    if(val!=missingVal)
    		    			    		    	o.setMaxWindGust(val);
    		    			    		    else
    		    			    		    	o.setMaxWindGust(Float.NaN);
    		    			    	    }
    		    			    	    catch(NumberFormatException e)
    		    			    	    {
    		    			    	    	o.setMaxWindGust(Float.NaN);
    		    			    	    }
    		    			    	
    		    			    	}
    		    			    	nlist=null;
    		    			    	nlist=message.getElementsByTagName("atmp1");
    		    			    	if(nlist!=null&&nlist.getLength()>0)
    		    			    	{
    		    			    	    try
    		    			    	    {
    		    			    	    	val=Float.parseFloat(nlist.item(0).getTextContent());
    		    			    	    	if(val!=missingVal)
    		    			    	    	{
    		    			    	    		if(isGVSU)
        		    			    	    		val=(float)((float)(Math.round((val-32.0)*5.0/9.0*10000))/10000.0);
    		    			    	    		o.setAirTemperature(val);
    		    			    	    	}
    		    			    	    	else
    		    			    	    		o.setAirTemperature(Float.NaN);
    		    			    	    }
    		    			    	    catch(NumberFormatException e)
    		    			    	    {
    		    			    	    	o.setAirTemperature(Float.NaN);
    		    			    	    }
    		    			    	}
    		    			    	nlist=null;
    		    			    	nlist=message.getElementsByTagName("wtmp1");
    		    			    	if(nlist!=null&&nlist.getLength()>0)
    		    			    	{
    		    			    	    try
    		    			    	    {
    		    			    	    	val=Float.parseFloat(nlist.item(0).getTextContent());
    		    			    	    	if(val!=missingVal)
    		    			    	    		o.setWaterTemperature(val);
    		    			    	    	else
    		    			    	    		o.setWaterTemperature(Float.NaN);
    		    			    	    }
    		    			    	    catch(NumberFormatException e)
    		    			    	    {
    		    			    	    	o.setWaterTemperature(Float.NaN);
    		    			    	    }
    		    			    	}
    		    			    	nlist=null;
    		    			    	nlist=message.getElementsByTagName("wvhgt");
    		    			    	if(nlist!=null&&nlist.getLength()>0)
    		    			    	{
    		    			    	    try
    		    			    	    {
    		    			    	    	val=Float.parseFloat(nlist.item(0).getTextContent());
    		    			    	    	if(val!=missingVal)
    		    			    	    		o.setSigWaveHeight(val);
    		    			    	    	else
    		    			    	    		o.setSigWaveHeight(Float.NaN);
    		    			    	    }
    		    			    	    catch(NumberFormatException e)
    		    			    	    {
    		    			    	    	o.setSigWaveHeight(Float.NaN);
    		    			    	    }
    		    			    	}
    		    			    	nlist=null;
    		    			    	nlist=message.getElementsByTagName("dompd");
    		    			    	if(nlist!=null&&nlist.getLength()>0)
    		    			    	{
    		    			    	    try
    		    			    	    {
    		    			    	    	val=Float.parseFloat(nlist.item(0).getTextContent());
    		    			    	    	if(val!=missingVal)
    		    			    	    		o.setWavePeroid(val);
    		    			    	    	else
    		    			    	    		o.setWavePeroid(Float.NaN);
    		    			    	    }
    		    			    	    catch(NumberFormatException e)
    		    			    	    {
    		    			    	    	o.setWavePeroid(Float.NaN);
    		    			    	    }
    		    			    	}
    		    			    	nlist=null;
    		    			    	nlist=message.getElementsByTagName("dewpt1");
    		    			    	if(nlist!=null&&nlist.getLength()>0)
    		    			    	{
    		    			    	    try
    		    			    	    {
    		    			    	    	val=Float.parseFloat(nlist.item(0).getTextContent());
    		    			    	    	if(val!=missingVal)
    		    			    	    		o.setDewPoint(val);
    		    			    	    	else
    		    			    	    		o.setDewPoint(Float.NaN);
    		    			    	    }
    		    			    	    catch(NumberFormatException e)
    		    			    	    {
    		    			    	    	o.setDewPoint(Float.NaN);
    		    			    	    }
    		    			    	}
    		    			    	nlist=null;
    		    			    	nlist=message.getElementsByTagName("baro1");
    		    			    	if(nlist!=null&&nlist.getLength()>0)
    		    			    	{
    		    			    	    try
    		    			    	    {
    		    			    	    	val=Float.parseFloat(nlist.item(0).getTextContent());
    		    			    	    	if(val!=missingVal)
    		    			    	    	{
    		    			    	    		if(isGVSU)
    		    			    	    		    val=(float)(val*33.86389);
    		    			    	    		o.setBarometricPressure(val);
    		    			    	    	}
    		    			    	    	else
    		    			    	    		o.setBarometricPressure(Float.NaN);
    		    			    	    }
    		    			    	    catch(NumberFormatException e)
    		    			    	    {
    		    			    	    	o.setBarometricPressure(Float.NaN);
    		    			    	    }
    		    			    	}
    		    			    	nlist=null;
    		    			    	nlist=message.getElementsByTagName("rh1");
    		    			    	if(nlist!=null&&nlist.getLength()>0)
    		    			    	{
    		    			    	    try
    		    			    	    {
    		    			    	    	val=Float.parseFloat(nlist.item(0).getTextContent());
    		    			    	    	if(val!=missingVal)
    		    			    	    		o.setRelativeHumidity(Float.parseFloat(nlist.item(0).getTextContent()));
    		    			    	    	else
    		    			    	    		o.setRelativeHumidity(Float.NaN);
    		    			    	    }
    		    			    	    catch(NumberFormatException e)
    		    			    	    {
    		    			    	    	o.setRelativeHumidity(Float.NaN);
    		    			    	    }
    		    			    	}
    		    			    	nlist=null;
    		    			    	nlist=message.getElementsByTagName("spcond");
    		    			    	if(nlist!=null&&nlist.getLength()>0)
    		    			    	{
    		    			    	    try
    		    			    	    {
    		    			    	    	val=Float.parseFloat(nlist.item(0).getTextContent());
    		    			    	    	if(val!=missingVal)
    		    			    	    		o.setWaterConductivity(Float.parseFloat(nlist.item(0).getTextContent()));
    		    			    	    	else
    		    			    	    		o.setWaterConductivity(Float.NaN);
    		    			    	    }
    		    			    	    catch(NumberFormatException e)
    		    			    	    {
    		    			    	    	o.setWaterConductivity(Float.NaN);
    		    			    	    }
    		    			    	}
    		    			    	nlist=null;
    		    			    	nlist=message.getElementsByTagName("ph");
    		    			    	if(nlist!=null&&nlist.getLength()>0)
    		    			    	{
    		    			    	    try
    		    			    	    {
    		    			    	    	val=Float.parseFloat(nlist.item(0).getTextContent());
    		    			    	    	if(val!=missingVal)
    		    			    	    		o.setPh(Float.parseFloat(nlist.item(0).getTextContent()));
    		    			    	    	else
    		    			    	    		o.setPh(Float.NaN);
    		    			    	    }
    		    			    	    catch(NumberFormatException e)
    		    			    	    {
    		    			    	    	o.setPh(Float.NaN);
    		    			    	    }
    		    			    	}
    		    			    	nlist=null;
    		    			    	nlist=message.getElementsByTagName("ysiturbntu");
    		    			    	if(nlist!=null&&nlist.getLength()>0)
    		    			    	{
    		    			    	    try
    		    			    	    {
    		    			    	    	val=Float.parseFloat(nlist.item(0).getTextContent());
    		    			    	    	if(val!=missingVal)
    		    			    	    		o.setYsiTurbidity(Float.parseFloat(nlist.item(0).getTextContent()));
    		    			    	    	else
    		    			    	    		o.setYsiTurbidity(Float.NaN);
    		    			    	    }
    		    			    	    catch(NumberFormatException e)
    		    			    	    {
    		    			    	    	o.setYsiTurbidity(Float.NaN);
    		    			    	    }
    		    			    	}
    		    			    	nlist=null;
    		    			    	nlist=message.getElementsByTagName("ysichlrfu");
    		    			    	if(nlist!=null&&nlist.getLength()>0)
    		    			    	{
    		    			    	    try
    		    			    	    {
    		    			    	    	val=Float.parseFloat(nlist.item(0).getTextContent());
    		    			    	    	if(val!=missingVal)
    		    			    	    		o.setYsiChlorophyll(Float.parseFloat(nlist.item(0).getTextContent()));
    		    			    	    	else
    		    			    	    		o.setYsiChlorophyll(Float.NaN);
    		    			    	    }
    		    			    	    catch(NumberFormatException e)
    		    			    	    {
    		    			    	    	o.setYsiChlorophyll(Float.NaN);
    		    			    	    }
    		    			    	}
    		    			    	nlist=null;
    		    			    	nlist=message.getElementsByTagName("ysibgarfu");
    		    			    	if(nlist!=null&&nlist.getLength()>0)
    		    			    	{
    		    			    	    try
    		    			    	    {
    		    			    	    	val=Float.parseFloat(nlist.item(0).getTextContent());
    		    			    	    	if(val!=missingVal)
    		    			    	    		o.setYsiBlueGreenAlgae(Float.parseFloat(nlist.item(0).getTextContent()));
    		    			    	    	else
    		    			    	    		o.setYsiBlueGreenAlgae(Float.NaN);
    		    			    	    }
    		    			    	    catch(NumberFormatException e)
    		    			    	    {
    		    			    	    	o.setYsiBlueGreenAlgae(Float.NaN);
    		    			    	    }
    		    			    	}
    		    			    	nlist=null;
    		    			    	nlist=message.getElementsByTagName("do");
    		    			    	if(nlist!=null&&nlist.getLength()>0)
    		    			    	{
    		    			    	    try
    		    			    	    {
    		    			    	    	val=Float.parseFloat(nlist.item(0).getTextContent());
    		    			    	    	if(val!=missingVal)
    		    			    	    		o.setDissovledOxygen(Float.parseFloat(nlist.item(0).getTextContent()));
    		    			    	    	else
    		    			    	    		o.setDissovledOxygen(Float.NaN);
    		    			    	    }
    		    			    	    catch(NumberFormatException e)
    		    			    	    {
    		    			    	    	o.setDissovledOxygen(Float.NaN);
    		    			    	    }
    		    			    	}
    		    			    	nlist=null;
    		    			    	nlist=message.getElementsByTagName("dosat");
    		    			    	if(nlist!=null&&nlist.getLength()>0)
    		    			    	{
    		    			    	    try
    		    			    	    {
    		    			    	    	val=Float.parseFloat(nlist.item(0).getTextContent());
    		    			    	    	if(val!=missingVal)
    		    			    	    		o.setDissovledOxygenSaturation(Float.parseFloat(nlist.item(0).getTextContent()));
    		    			    	    	else
    		    			    	    		o.setDissovledOxygenSaturation(Float.NaN);
    		    			    	    }
    		    			    	    catch(NumberFormatException e)
    		    			    	    {
    		    			    	    	o.setDissovledOxygenSaturation(Float.NaN);
    		    			    	    }
    		    			    	}
    		    			    	/*nlist=null;
    		    			    	nlist=message.getElementsByTagName("chloro");
    		    			    	if(nlist!=null&&nlist.getLength()>0)
    		    			    	{
    		    			    	    try
    		    			    	    {
    		    			    	    	val=Float.parseFloat(nlist.item(0).getTextContent());
    		    			    	    	if(val!=missingVal)
    		    			    	    		o.setChlorophy2Concentration(Float.parseFloat(nlist.item(0).getTextContent()));
    		    			    	    	else
    		    			    	    		o.setChlorophy2Concentration(Float.NaN);
    		    			    	    }
    		    			    	    catch(NumberFormatException e)
    		    			    	    {
    		    			    	    	o.setChlorophy2Concentration(Float.NaN);
    		    			    	    }
    		    			    	}*/
    		    			    	int depthCnt=1;
    		    			    	String depthTagFmt="dp%s%s";
    		    			    	String wtpTagFmt="tp%s%s";
    		    			    	String dname="dp001";
    		    			    	String wtpname="tp001";
    		    			    	String prefix=null;
    		    			    	NodeList dlist,wtplist;
    		    			    	double tdepth,twtemp;
    		    			    	while((dlist=message.getElementsByTagName(dname))!=null&&
    		    			    			(wtplist=message.getElementsByTagName(wtpname))!=null&&
    		    			    			dlist.getLength()>0&&wtplist.getLength()>0)
    		    			    	{
    		    			    		
    		    			    		try
    		    			    		{
    		    			    			tdepth=Double.parseDouble(dlist.item(0).getTextContent());
    		    			    			twtemp=Double.parseDouble(wtplist.item(0).getTextContent());
    		    			    			if(twtemp!=missingVal)
    		    			    			{
    		    			    			    if(isGVSU)
    		    			    				    twtemp=(float)((float)(Math.round((twtemp-32.0)*5.0/9.0*10000))/10000.0);
    		    			    			    o.setThermalString(tdepth,
    		    			    					    twtemp,
    		    			    					    ObsZ.WATER_TEMP);
    		    			    			}
    		    			    		}
    		    			    		catch(NumberFormatException e)
    		    			    		{

    		    			    		}
    		    			    		
    		    			    		if(++depthCnt<10)
    		    			    			prefix="00";
    		    			    		else if(depthCnt<100)
    		    			    			prefix="0";
    		    			    		else if(depthCnt>999)
    		    			    			break;
    		    			    		dname=String.format(depthTagFmt,prefix,depthCnt);
    					    			wtpname=String.format(wtpTagFmt,prefix,depthCnt);
    		    			    	}
    		    			    	if(o.getThermalString()!=null&&o.getThermalString().size()>0)
    		    			    	{
    		    			    		Collections.sort(o.getThermalString(), new Comparator<ObsZ>(){

    										public int compare(ObsZ o1, ObsZ o2) {
    											// TODO Auto-generated method stub
    											if(o1.depth>o2.depth)
    											    return 1;
    											else if(o1.depth-o2.depth<0.0001)
    												return 0;
    											else
    												return -1;
    										}
    		                                              
    		                             });
    		    			    	}
    		    			    	
    		    			    	depthCnt=1;
    		    			    	depthTagFmt="clcon-dpt%s%s";
    		    			    	String chloroTagFmt="clcon%s%s";
    		    			    	dname="clcon-dpt001";
    		    			    	String chloroname="clcon001";
    		    			    	prefix=null;
    		    			    	dlist=null;
    		    			    	NodeList chlorolist;
    		    			    	while((dlist=message.getElementsByTagName(dname))!=null&&
    		    			    			(chlorolist=message.getElementsByTagName(chloroname))!=null&&
    		    			    			dlist.getLength()>0&&chlorolist.getLength()>0)
    		    			    	{
    		    			    		
    		    			    		try
    		    			    		{
    		    			    			o.setChlorophy2Concentration(Double.parseDouble(dlist.item(0).getTextContent()),
    		    			    					Double.parseDouble(chlorolist.item(0).getTextContent()),
    		    			    					ObsZ.CHLOROPHY2_CON);
    		    			    		}
    		    			    		catch(NumberFormatException e)
    		    			    		{

    		    			    		}
    		    			    		
    		    			    		if(++depthCnt<10)
    		    			    			prefix="00";
    		    			    		else if(depthCnt<100)
    		    			    			prefix="0";
    		    			    		else if(depthCnt>999)
    		    			    			break;
    		    			    		dname=String.format(depthTagFmt,prefix,depthCnt);
    		    			    		chloroname=String.format(chloroTagFmt,prefix,depthCnt);
    		    			    	}
    		    			    	if(o.getChlorophy2Concentration()!=null&&o.getChlorophy2Concentration().size()>0)
    		    			    	{
    		    			    		Collections.sort(o.getChlorophy2Concentration(), new Comparator<ObsZ>(){

    										public int compare(ObsZ o1, ObsZ o2) {
    											// TODO Auto-generated method stub
    											if(o1.depth>o2.depth)
    											    return 1;
    											else if(o1.depth-o2.depth<0.0001)
    												return 0;
    											else
    												return -1;
    										}
    		                                              
    		                             });
    		    			    	}
    		    			    }
    		    			    else
    		    			    {
    		    			    	if(log!=null)
    		    	    				log.severe("UGLOSXMLParser: Can not find tag 'met'");
    		    			    	continue;
    		    			    }
    		    			    obs.add(o);
    			    			
    			    		}
    			    	}
    			    }
    			   
    			    
    		    }
    		}
    		catch(Exception e)
    		{
    			if(log!=null)
    				log.severe(String.format("UGLOSXMLParser: Parsing error on  -- %s", e.getMessage()));
    		}
    	}
    	return obs;
    }
    
}

package utest;


import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.Ignore;
import org.junit.Assert;
import java.io.*;
import java.nio.channels.*;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Properties;

import static org.glc.glos.coastwatch.CoastTodayParser.*;


public class ParserTest {

	private static String regex;
	private static ArrayList<String> rawData;
	private static int[] offsets={4,7,9,11,12,20,21,22,28,34,38,43,48,53,59,66,71,76,81,87,93};
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		regex="^(\\d{4})(\\d{3})(\\d{2})(\\d{2})([\\s\\d]{1})([\\s\\S]{8})([\\s\\d]{1})(\\d{1})\\s+([-+]?\\d{1,3}\\.\\d)\\s+([-+]?\\d{1,3}\\.\\d)\\s+(\\d{1,3})\\s+([-+]?\\d{1,2}\\.\\d)\\s+([-+]?\\d{1,2}\\.\\d)\\s+(\\d{1,3})\\s+([-+]?\\d{1,4}\\.\\d)\\s+([-+]?\\d{1,4}\\.\\d)\\s+([-+]?\\d{1,2}\\.\\d)\\s+([-+]?\\d{1,2}\\.\\d)\\s+([-+]?\\d{1,2}\\.\\d)(.*)";
		String temp=null;
		InputStream is=ParserTest.class.getResourceAsStream("datasource.dat");
		if(is!=null)
		{
			BufferedReader br=new BufferedReader(new InputStreamReader(is));
			if(br!=null)
			{
				rawData=new ArrayList<String>();
				while((temp=br.readLine())!=null)
					rawData.add(temp);
				br.close();
			}
			
		}
		else
			System.exit(-1);
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
	}

	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
	}
    @Ignore
    public void testParseByRegex()
    {
    	long begin=System.nanoTime();
    	ArrayList l=ParseByRegularExpression(regex, rawData, null);
    	long end=System.nanoTime();
    	System.out.println(String.format("Regex: %d -- Count: %d",(end-begin),l.size()));
    }
    @Ignore
    public void testParseBySubstring()
    {
    	long begin=System.nanoTime();
    	ArrayList l=ParseBySubstring(offsets, rawData, null);
    	long end=System.nanoTime();
    	System.out.println(String.format("Substring: %d -- Count: %d",(end-begin),l.size()));
    }
    @Ignore 
    public void testTime()
    {
    	long foobar=1196831040000L;
		java.util.Date date=new Date(foobar);
		String foob=date.toString();
		Calendar cal=Calendar.getInstance();
		cal.set(Calendar.YEAR, 2007);
		cal.set(Calendar.DAY_OF_YEAR,339);
		cal.set(Calendar.HOUR_OF_DAY, 0);
		cal.set(Calendar.MINUTE, 0);
		cal.set(Calendar.SECOND, 0);
		cal.set(Calendar.MILLISECOND, 10);
		date=cal.getTime();
		String foob1=date.toString();
		java.sql.Timestamp sqlDate=new java.sql.Timestamp(date.getTime());
		sqlDate.setNanos(0);		
		String foob2=sqlDate.toString();
		Assert.assertEquals(foob, foob1);
    }
    @Test
    public void testAccessPermission()
    {
    	try
    	{
    		File f=new File("testlock.properties");
    		FileOutputStream fos=new FileOutputStream(f);
    		FileLock lock=fos.getChannel().lock();
    		Properties prop=new Properties();
    		prop.setProperty("test", "1936878");
    		prop.store(fos, "test only");
    		lock.release();
    		fos.close();
    	}
    	catch(Exception e)
    	{
    		e.printStackTrace();
    	}
    	
    }
}

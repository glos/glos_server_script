package utest;


import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.Ignore;
import org.junit.Assert;
import java.util.TimeZone;
import java.util.Calendar;
import java.util.Date;
import java.util.Properties;
import java.sql.Timestamp;
import java.io.FileInputStream;
import org.glc.glos.coastwatch.*;

public class SqlTest {

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
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
	public void testTimeZone()
	{
		//String[] ids=TimeZone.getAvailableIDs();
		//for(String tmp:ids)
		//	System.out.println(tmp);
		String temppath=System.getProperty("java.io.tmpdir");
		long t=1197403200000L;
		Calendar cal=Calendar.getInstance(TimeZone.getTimeZone("GMT"));
		Timestamp ts1=new Timestamp(t);
		Date d=new Date(t);
		//Calendar cal=Calendar.getInstance(Time);
		cal.set(Calendar.YEAR, 2007);
		cal.set(Calendar.DAY_OF_YEAR, 400);
		cal.set(Calendar.HOUR_OF_DAY, 10);
		cal.set(Calendar.MINUTE, 0);
		cal.set(Calendar.SECOND, 0);
		cal.set(Calendar.MILLISECOND, 0);
		System.out.println(cal.getTimeInMillis());
		Calendar cal1=Calendar.getInstance();
		cal1.set(Calendar.YEAR, 2007);
		cal1.set(Calendar.DAY_OF_YEAR, 400);
		cal1.set(Calendar.HOUR_OF_DAY, 10);
		cal1.set(Calendar.MINUTE, 0);
		cal1.set(Calendar.SECOND, 0);
		cal1.set(Calendar.MILLISECOND, 0);
		System.out.println(cal1.getTimeInMillis());
	}
	@Test
	public void testUpdateMgr()
	{
		try
		{
			Class.forName(ConfigManager.getJDBCDriver());
			//org.glc.glos.dbman.DumpObs.deleteme();
		}
		catch(Exception e){}
		AntCommandor.March(AntCommandor.FindAnts(null),null);
		Properties pp=System.getProperties();
		for(Object k:pp.keySet())
		{
			System.out.println(k.toString()+"="+pp.get(k));
		}
		try
		{
			Properties p1=new Properties();
			FileInputStream fis=new FileInputStream("stations.dat");
			p1.load(fis);
			fis.close();
			fis=new FileInputStream("stations_glin.dat");
			Properties p2=new Properties();
			p2.load(fis);
			fis.close();
			Assert.assertTrue(p1.size()==p2.size());
			for(Object key:p1.keySet())
			{
				Assert.assertEquals(p1.get(key), p2.get(key));
			}
			
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
	}
}

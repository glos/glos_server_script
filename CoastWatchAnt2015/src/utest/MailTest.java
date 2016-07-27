package utest;


import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.glc.glos.coastwatch.MailSender;

public class MailTest {

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
	@Test
	public void testSendMail()
	{
		MailSender.Send(new String[]{"test","foobar"}, 
				        "mail.great-lakes.net", 
				        "coastwatch.glos.admin@glc.org", 
				        new String[]{"gwang@glc.org","gwang1@emich.edu"}, null);
	}

}

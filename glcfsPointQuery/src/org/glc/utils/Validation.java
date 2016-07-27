package org.glc.utils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.Hashtable;
import java.util.Map;
import java.util.Collections;

import javax.servlet.ServletRequest;

public class Validation {
	private final static String EMAIL_REG="^[_A-Za-z0-9-+]+(\\.[_A-Za-z0-9-+]+)*@[A-Za-z0-9-]+(\\.[A-Za-z0-9]+)*(\\.[A-Za-z]{2,})$"; 
    private final static String WILD_CARD_EMAIL_DOMAIN="^\\*@[A-Za-z0-9-]+(\\.[A-Za-z0-9]+)*(\\.[A-Za-z]{2,})$"; 
	private final static String PHONE_REG="^(?:\\+?1[-. ]?)?\\(?([0-9]{3})\\)?[-. ]?([0-9]{3})[-. ]?([0-9]{4})$";
	private final static String ZIP_REG="^\\d{5}(-\\d{4})?$";
	private final static String POSTAL_REG="^([A-Z]\\d[A-Z]\\s\\d[A-Z]\\d)$";
    private final static String URL_REG="^(http:\\/\\/|ftp:\\/\\/|https:\\/\\/)?[\\w\\-_]+(\\.[\\w\\-_]+)+([\\w\\-\\.,@?^=%&amp;:/~\\+#]*[\\w\\-\\@?^=%&amp;/~\\+#])?";
    private static Pattern UrlPattern;
    private static Pattern EmailPattern;
    private static Pattern EmailDomainPattern;
    private static Pattern PhonePattern;
    private static Pattern ZipcodePattern;
    private static Pattern PostalcodePattern;
    
	static
    {
		//Pattern is thread-safe. Matcher is not
		UrlPattern=Pattern.compile(URL_REG);
		EmailPattern=Pattern.compile(EMAIL_REG);
		EmailDomainPattern=Pattern.compile(WILD_CARD_EMAIL_DOMAIN);
		PhonePattern=Pattern.compile(PHONE_REG);
		ZipcodePattern=Pattern.compile(ZIP_REG);
		PostalcodePattern=Pattern.compile(POSTAL_REG);
    }
	public static boolean isValidUrl(String url)
	{
		if(url!=null)
		{
			Matcher matcher=UrlPattern.matcher(url.trim());
			return matcher.matches();
		}
		return false;
	}
	public static boolean isValidEmail(String email)
    {
    	if(email!=null)
    	{
    		Matcher matcher = EmailPattern.matcher(email.trim());
  		    return matcher.matches();

    	}
    	return false;
    }
	public static boolean isValidEmailDomain(String email)
    {
    	if(email!=null)
    	{
    		Matcher matcher = EmailDomainPattern.matcher(email.trim());
  		    return matcher.matches();

    	}
    	return false;
    }
	public static String isValidPhone(String phone)
	{
		if(phone!=null)
		{
			Matcher matcher=PhonePattern.matcher(phone.trim());
			if(matcher.matches())
				return matcher.replaceAll("($1) $2-$3");
			else
				return null;
		}
		return null;
	}
	public static boolean isValidPassword(String passwd)
	{
		return true;
	}
	public static boolean isValidCity(String city)
	{
		return true;
	}
	public static boolean isValidStateFIPS(String fips,String country)
	{
		if(fips!=null&&country!=null&&fips.length()==2)
		{
			if(country.equals(FIPSCodes.CountryCode.US))
			{
				return FIPSCodes.US_State_FIPS.containsKey(fips);
			}
			else if(country.equals(FIPSCodes.CountryCode.CA))
			{
				return FIPSCodes.CA_Province_FIPS.containsKey(fips);
			}
		}
		return false;
	}
	public static boolean isValidNorthAmericanCountry(String c)
	{
		if(c!=null)
		{
			return c.equals(FIPSCodes.CountryCode.US)||
			c.equals(FIPSCodes.CountryCode.CA);
		}
		return false;
	}
	public static boolean isUS(String us)
	{
		if(us!=null)
		{
			return us.equals(FIPSCodes.CountryCode.US);
		}
		return false;
	}
	public static boolean isCA(String ca)
	{
		if(ca!=null)
		{
			return ca.equals(FIPSCodes.CountryCode.CA);
		}
		return false;
	}
	public static boolean isValidPostalCode(String zip,String c)
	{
		if(zip!=null&&c!=null)
		{
			if(c.equals(FIPSCodes.CountryCode.US))
			{
				Matcher matcher=ZipcodePattern.matcher(zip.trim());
				return matcher.matches();
			}
			else if(c.equals(FIPSCodes.CountryCode.CA))
			{
				Matcher matcher=PostalcodePattern.matcher(zip.trim());
				return matcher.matches();
			}
		}
		return false;
	}
	public static boolean basicValidation(ServletRequest request,String pname)
	{
		if(request==null||pname==null)return false;
		String tmp=request.getParameter(pname);
		if(tmp!=null)
		{
			tmp=tmp.trim();
			return !tmp.equals("")&&!tmp.equals("undefined");
			
		}
		return false;
	}
	public static boolean basicGroupValidation(ServletRequest request,String pname)
	{
		if(request==null||pname==null)return false;
		String[] tmp=request.getParameterValues(pname);
		boolean result=true;
		if(tmp!=null&&tmp.length>0)
		{
			for(String s:tmp)
			{
			    if(s==null)
			    {
			    	result=false;
			    	break;
			    }
			    s=s.trim();
			    if(s.equals("")||s.equals("undefined"))
			    {
			    	result=false;
			    	break;
			    }
			}
		}
		else
			result=false;
		return result;
	}
	public static boolean basicGroupValidationAllowEmpty(ServletRequest request,String pname)
	{
		if(request==null||pname==null)return false;
		String[] tmp=request.getParameterValues(pname);
		boolean result=true;
		if(tmp!=null&&tmp.length>0)
		{
			for(String s:tmp)
			{
			    if(s==null)
			    {
			    	result=false;
			    	break;
			    }
			    s=s.trim();
			    if(s.equals("undefined"))
			    {
			    	result=false;
			    	break;
			    }
			}
		}
		else
			result=false;
		return result;
	}
	
}

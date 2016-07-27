package org.glc.utils;

import java.util.Collections;
import java.util.Hashtable;
import java.util.Map;
import java.util.Vector;
import java.util.List;

public class FIPSCodes {
	public static class CountryCode
	{
		public static String US="us";
		public static String CA="ca";
	}
	
	private static Hashtable<String,String> _US_State_FIPS;
    private static Hashtable<String,String> _CA_Province_FIPS;
    //private static Vector<String> _US_State_FIPS_Sorted_Key;
    
    public static final List<String> US_State_FIPS_Sorted_Key;
    public static final List<String> CA_Province_FIPS_Sorted_Key;
    public static final Map<String,String> US_State_FIPS;
    public static final Map<String,String> CA_Province_FIPS;
    
    static
    {
    	_US_State_FIPS=new Hashtable<String,String>();
		_US_State_FIPS.put("01", "Alabama");
		_US_State_FIPS.put("02", "Alaska");
		_US_State_FIPS.put("04", "Arizona");
		_US_State_FIPS.put("05", "Alaska");
		_US_State_FIPS.put("06", "California");
		_US_State_FIPS.put("08", "Colorado");
		_US_State_FIPS.put("09", "Connecticut");
		_US_State_FIPS.put("10", "Delaware");
		_US_State_FIPS.put("11", "District of Columbia");
		_US_State_FIPS.put("12", "Florida");
		_US_State_FIPS.put("13", "Georgia");
		_US_State_FIPS.put("15", "Hawaii");
		_US_State_FIPS.put("16", "Idaho");
		_US_State_FIPS.put("17", "Illinois");
		_US_State_FIPS.put("18", "Indiana");
		_US_State_FIPS.put("19", "Iowa");
		_US_State_FIPS.put("20", "Kansas");
		_US_State_FIPS.put("21", "Kentucky");
		_US_State_FIPS.put("22", "Louisiana");
		_US_State_FIPS.put("23", "Maine");
		_US_State_FIPS.put("24", "Maryland");
		_US_State_FIPS.put("25", "Massachusetts");
		_US_State_FIPS.put("26", "Michigan");
		_US_State_FIPS.put("27", "Minnesota");
		_US_State_FIPS.put("28", "Mississippi");
		_US_State_FIPS.put("29", "Missouri");
		_US_State_FIPS.put("30", "Montana");
		_US_State_FIPS.put("31", "Nebraska");
		_US_State_FIPS.put("32", "Nevada");
		_US_State_FIPS.put("33", "New Hampshire");
		_US_State_FIPS.put("34", "New Jersey");
		_US_State_FIPS.put("35", "New Mexico");
		_US_State_FIPS.put("36", "New York");
		_US_State_FIPS.put("37", "North Carolina");
		_US_State_FIPS.put("38", "North Dakota");
		_US_State_FIPS.put("39", "Ohio");
		_US_State_FIPS.put("40", "Oklahoma");
		_US_State_FIPS.put("41", "Oregon");
		_US_State_FIPS.put("42", "Pennsylvania");
		_US_State_FIPS.put("44", "Rhode Island");
		_US_State_FIPS.put("45", "South Carolina");
		_US_State_FIPS.put("46", "South Dakota");
		_US_State_FIPS.put("47", "Tennessee");
		_US_State_FIPS.put("48", "Texas");
		_US_State_FIPS.put("49", "Utah");
		_US_State_FIPS.put("50", "Vermont");
		_US_State_FIPS.put("51", "Virginia");
		_US_State_FIPS.put("53", "Washington");
		_US_State_FIPS.put("54", "West Virginia");
		_US_State_FIPS.put("55", "Wisconsin");
		_US_State_FIPS.put("56", "Wyoming");
		_US_State_FIPS.put("60", "American Samoa");
		_US_State_FIPS.put("66", "Guam");
		_US_State_FIPS.put("72", "Puerto Rico");
		_US_State_FIPS.put("78", "Virgin Islands");
		
		_CA_Province_FIPS=new Hashtable<String,String>();
		_CA_Province_FIPS.put("01","Alberta");
		_CA_Province_FIPS.put("02","British Columbia");
		_CA_Province_FIPS.put("03","Manitoba");
		_CA_Province_FIPS.put("04","New Brunswick");
		_CA_Province_FIPS.put("05","Newfoundland");
		_CA_Province_FIPS.put("06","Northwest Territories");
		_CA_Province_FIPS.put("07","Nova Scotia");
		_CA_Province_FIPS.put("08","Ontario");
		_CA_Province_FIPS.put("09","Prince Edward Island");
		_CA_Province_FIPS.put("10","Quebec");
		_CA_Province_FIPS.put("11","Saskatchewan");
		_CA_Province_FIPS.put("12","Yukon Territory");
		
		US_State_FIPS=Collections.unmodifiableMap(_US_State_FIPS);
		CA_Province_FIPS=Collections.unmodifiableMap(_CA_Province_FIPS);
		
		Vector<String> key=new Vector<String>(US_State_FIPS.keySet());
		Collections.sort(key);
		US_State_FIPS_Sorted_Key=Collections.unmodifiableList(key);
		
		key=new Vector<String>(CA_Province_FIPS.keySet());
		Collections.sort(key);
		CA_Province_FIPS_Sorted_Key=Collections.unmodifiableList(key);
    }
}

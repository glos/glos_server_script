package us.glos.glcfs.helpers;

public class Unit2English {
	private static double POWER_OF_TEN=10000.0;
	public static double Convert2En(double val,String uname)
    {
    	if(uname.equalsIgnoreCase("m/s"))
    	    return Math.round(val/0.514*POWER_OF_TEN)/POWER_OF_TEN;
    	else if(uname.equalsIgnoreCase("Celsius"))
    		return Math.round((val*9.0/5.0+32.0)*POWER_OF_TEN)/POWER_OF_TEN;
    	else if(uname.equalsIgnoreCase("mb"))
    		return Math.round(val/33.86389*POWER_OF_TEN)/POWER_OF_TEN;
    	else if(uname.equalsIgnoreCase("meter"))
    		return Math.round(val*3.2808399*POWER_OF_TEN)/POWER_OF_TEN;
    	else
    		return val;
    }
	public static String Convert2EnUnit(String uname)
    {
    	if(uname.equalsIgnoreCase("m/s"))
    		return "knots";
    	else if(uname.equalsIgnoreCase("Celsius"))
    		return "fahrenheit";
    	else if(uname.equalsIgnoreCase("mb"))
    		return "inHg";
    	else if(uname.equalsIgnoreCase("meter"))
    		return "feet";
    	else
    		return uname;
    }
}

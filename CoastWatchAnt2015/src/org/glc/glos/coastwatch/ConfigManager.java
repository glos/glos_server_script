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

import java.util.Properties;
import java.util.Calendar;
import java.util.TimeZone;
import java.io.InputStream;
import java.io.FileInputStream;
import java.io.IOException;


public final class ConfigManager {
	private final static String Table_Prefix[]={"JAN_1",
		                                 "JAN_2",
		                                 "FEB_1",
		                                 "FEB_2",
		                                 "MAR_1",
		                                 "MAR_2",
		                                 "APL_1",
		                                 "APL_2",
		                                 "MAY_1",
		                                 "MAY_2",
		                                 "JUN_1",
		                                 "JUN_2",
		                                 "JUL_1",
		                                 "JUL_2",
		                                 "AUG_1",
		                                 "AUG_2",
		                                 "SEP_1",
		                                 "SEP_2",
		                                 "OCT_1",
		                                 "OCT_2",
		                                 "NOV_1",
		                                 "NOV_2",
		                                 "DEC_1",
		                                 "DEC_2"
		                                  };
	public final static String NEWLINE = System.getProperty("line.separator");
	public final static String CONFIG_FILE_NAME="./glos_obs_settings.properties";
	public final static String TODAY_URL="Today_URL";
	public final static String PLATFORM_URL="Platform_URL";
	public final static String LOG_LEVEL="Log_Level";
	public final static String LOG_FILE="Log_File";
	public final static String LOG_PLATFORM_FILE="Log_Platform_File";
	public final static String LOG_DATA_AVAIL_FILE="Log_Data_Avail_File";
	public final static String DATA_AVAIL_TABLE="Data_Avail_Table";
	public final static String DATA_OFFSET="Format_Offset";
	public final static String DATA_FORMAT_REG="Format_Reg";
	public final static String DB_URL_Format="Conn_Str_Format";
	public final static String DB_Server_Name="DB_Host";
	public final static String DB_Name="DB_Name";
	public final static String DB_Port_No="DB_Port";
	public final static String JDBC_Driver="JDBC_Driver_Name";
	public final static String DB_User="DB_Account";
	public final static String DB_Passcode="DB_Password";
	public final static String Enable_Mail="Enable_Mail";
	public final static String SMTP_Server="SMTP_Server_Name";
	public final static String Mail_From_Account="Mail_From_Account";
	public final static String Mail_To_Account="Mail_To_Account";
	public final static String Update_File="Update_Record_File";
	public final static String Air_Temperature_Ids="ATMP";
	public final static String Dew_Point_Ids="DEWP";
	public final static String Wind_Direction_Ids="WDIR";
	public final static String Wind_Speed_Ids="WSPD";
	public final static String Max_Wind_Gust_Ids="GST";
	public final static String Cloud_Cover_Ids="CCVR";
	public final static String Solar_Radiation_Ids="SRAD";
	public final static String Barometric_Pressure_Ids="PRES";
	public final static String Water_Temperature_Ids="WTMP";
	public final static String Wave_Height_Ids="WVHT";
	public final static String Wave_Period_Ids="WPRD";
	public final static String THERMAL_STRING_Ids="TTAD";
	public final static String CHLOROPHY2_Ids="CLCON";
	public final static String RELATIVEHUMIDITY_Ids="RH1";
	public final static String WATER_CONDUCTIVITY_Ids="SPCOND";
	public final static String PH_Ids="PH";
	public final static String YSI_TURBIDITY_Ids="YTURBI";
	public final static String YSI_CHLOROPHYLL_Ids="YCHLOR";
	public final static String YSI_BLUE_GREEN_ALGAE_Ids="YBGALG";
	public final static String DISSOLVED_OXYGEN_Ids="DISOXY";
	public final static String DISSOLVED_OXYGEN_SATURATION_Ids="DIOSAT";
	public final static String Enable_DB_Cache="Enable_Hourly_Cache";
	public final static String Obs_Cache_Table="OBS_Cached_Table_Name";
	public final static String Obs_Cache_Sql="OBS_Cached_Sql";
	public final static String Obs_Time_Zone="Obs_Time_Zone";
	public final static String Allow_Unit_Convert="Allow_Unit_Conversion";
	public final static String Ant_Soldier_Class_Name="Ant_Army";
	public final static String Enable_Obs_Latest_Cache="Enable_Latest_Cache";
	public final static String Obs_Latest_Cache_Table="OBS_Cache_Latest_Table_Name";
	public final static String OFFSET_STR_DELIMITER=",";
	public final static String TEMP_FOLDER_PATH;
	public final static String FILE_SEPARATOR;
	static
	{
		TEMP_FOLDER_PATH=System.getProperty("java.io.tmpdir");
		FILE_SEPARATOR=System.getProperty("file.separator");
	}
	private static Properties config;
	private static int[] offsets;
	public static void initConfig() throws IOException
	{
		try
		{
			offsets=null;
			//for debug env
			InputStream is=ConfigManager.class.getClassLoader().getResourceAsStream(CONFIG_FILE_NAME);
			//for production env
			//InputStream is=new FileInputStream(CONFIG_FILE_NAME);
			if(is!=null)
			{
			    config=new Properties();
			    config.load(is);
			    is.close();
			}
			else
				throw new IOException();
		}
		catch(IOException ie)
		{
			System.err.printf("Can not read the configuration file: %s!",CONFIG_FILE_NAME);
			System.err.println();
			ie.printStackTrace();
			throw ie;
		}
	}
	public static String getProperty(String key)
	{
		Object result=null;
		if(config==null)
		{
			return null;
		}
		result=config.get(key);
		return result==null?null:result.toString();
	}
	public static String getTableNameByDate()
	{
		
		Calendar cal=Calendar.getInstance(TimeZone.getTimeZone(ConfigManager.getObservationTimeZone()));
		int dom=cal.get(Calendar.DAY_OF_MONTH);
		int month=cal.get(Calendar.MONTH);//0 to 11
		int offset=dom>15?1:0;
		if(month+offset<Table_Prefix.length)
		{
			Object val=config.get(Table_Prefix[month*2+offset]);
			if(val!=null)
				return val.toString();
		}
		return null;
	}
	public static String getDBAccount()
	{
		return getProperty(DB_User);
	}
	public static String getDBPassword()
	{
		return getProperty(DB_Passcode);
	}
	public static String getTodayURL()
	{
		return getProperty(TODAY_URL);
	}
	public static String getPlatformURL()
	{
		return getProperty(PLATFORM_URL);
	}
	public static String getLogLevel()
	{
		return getProperty(LOG_LEVEL);
	}
	public static String getLogFile()
	{
		return getProperty(LOG_FILE);
	}
	public static String getPlatformLogFile()
	{
		return getProperty(LOG_PLATFORM_FILE);
	}
	public static String getDataAvailLogFile()
	{
		return getProperty(LOG_DATA_AVAIL_FILE);
	}
	public static String getDataAvailTableName()
	{
		return getProperty(DATA_AVAIL_TABLE);
	}
	public static String getDataFormatRegx()
	{
		return getProperty(DATA_FORMAT_REG);
	}
	public static int[] getDataOffset()
	{
		if(offsets==null)
		{
			String rawStr=config.getProperty(DATA_OFFSET);
			if(rawStr!=null&&!rawStr.equals(""))
			{
				String[] nums=rawStr.split(OFFSET_STR_DELIMITER);
				if(nums!=null&&nums.length>0)
				{
					offsets=new int[nums.length];
					for(int i=0;i<nums.length;++i)
					{
						offsets[i]=Integer.parseInt(nums[i]);
					}
				}
			}
		}
		return offsets;
	}
	public static String getJDBCDriver()
	{
		return getProperty(JDBC_Driver);
	}
	public static String getConnectionString()
	{
		return String.format(getProperty(DB_URL_Format), getProperty(DB_Server_Name),getProperty(DB_Port_No),getProperty(DB_Name));
	}
	public static boolean IsMailEnabled()
	{
		boolean result=false;
		String tmp=getProperty(Enable_Mail);
		if(tmp!=null)
			result=Boolean.parseBoolean(tmp);
		return result;
	}
	public static String getSMTPMailServer()
	{
		return getProperty(SMTP_Server);
	}
	public static String getMailFromAccount()
	{
		return getProperty(Mail_From_Account);
	}
	public static String[] getMailToAccounts()
	{
		String tmp=getProperty(Mail_To_Account);
		if(tmp==null)return null;
		return tmp.split(OFFSET_STR_DELIMITER);
	}
	public static String getUpdateRecordFile()
	{
		return getProperty(Update_File);
	}
	public static boolean IsDBCacheEnabled()
	{
		boolean result=false;
		String tmp=getProperty(Enable_DB_Cache);
		if(tmp!=null)
			result=Boolean.parseBoolean(tmp);
		return result;
	}
	public static String getObservationCacheTableName()
	{
		return getProperty(Obs_Cache_Table);
	}
	public static String getObservationCacheSql()
	{
		return getProperty(Obs_Cache_Sql);
	}
	public static String getObservationTimeZone()
	{
		return getProperty(Obs_Time_Zone);
	}
	public static boolean IsDBLatestCacheEnabled()
	{
		boolean result=false;
		String tmp=getProperty(Enable_Obs_Latest_Cache);
		if(tmp!=null)
			result=Boolean.parseBoolean(tmp);
		return result;
	}
	public static String getLatestObsCacheTableName()
	{
		return getProperty(Obs_Latest_Cache_Table);
	}
	public static boolean allowUnitConversion()
	{
		boolean result=false;
		String tmp=getProperty(Allow_Unit_Convert);
		if(tmp!=null)
			result=Boolean.parseBoolean(tmp);
		return result;
	}
	public static String getCustomProperty(String key)
	{
		return getProperty(key);
	}
	public static String getAntSoldiersClassNames()
	{
		return getProperty(Ant_Soldier_Class_Name);
	}
	public static short getAirTempSensorId()
	{
		return Short.parseShort(getProperty(Air_Temperature_Ids).split(OFFSET_STR_DELIMITER)[0]);
	}
	public static short getAirTempMeasureId()
	{
		return Short.parseShort(getProperty(Air_Temperature_Ids).split(OFFSET_STR_DELIMITER)[1]);
	}
	public static short getDewPntSensorId()
	{
		return Short.parseShort(getProperty(Dew_Point_Ids).split(OFFSET_STR_DELIMITER)[0]);
	}
	public static short getDewPntMeasureId()
	{
		return Short.parseShort(getProperty(Dew_Point_Ids).split(OFFSET_STR_DELIMITER)[1]);
	}
	public static short getWndDirectSensorId()
	{
		return Short.parseShort(getProperty(Wind_Direction_Ids).split(OFFSET_STR_DELIMITER)[0]);
	}
	public static short getWndDirectMeasureId()
	{
		return Short.parseShort(getProperty(Wind_Direction_Ids).split(OFFSET_STR_DELIMITER)[1]);
	}
	public static short getWndSpeedSensorId()
	{
		return Short.parseShort(getProperty(Wind_Speed_Ids).split(OFFSET_STR_DELIMITER)[0]);
	}
	public static short getWndSpeedMeasureId()
	{
		return Short.parseShort(getProperty(Wind_Speed_Ids).split(OFFSET_STR_DELIMITER)[1]);
	}
	public static short getMaxWndGustSensorId()
	{
		return Short.parseShort(getProperty(Max_Wind_Gust_Ids).split(OFFSET_STR_DELIMITER)[0]);
	}
	public static short getMaxWndGustMeasureId()
	{
		return Short.parseShort(getProperty(Max_Wind_Gust_Ids).split(OFFSET_STR_DELIMITER)[1]);
	}
	public static short getCloudCoverSensorId()
	{
		return Short.parseShort(getProperty(Cloud_Cover_Ids).split(OFFSET_STR_DELIMITER)[0]);
	}
	public static short getCloudCoverMeasureId()
	{
		return Short.parseShort(getProperty(Cloud_Cover_Ids).split(OFFSET_STR_DELIMITER)[1]);
	}
	public static short getSolarRadiationSensorId()
	{
		return Short.parseShort(getProperty(Solar_Radiation_Ids).split(OFFSET_STR_DELIMITER)[0]);
	}
	public static short getSolarRadiationMeasureId()
	{
		return Short.parseShort(getProperty(Solar_Radiation_Ids).split(OFFSET_STR_DELIMITER)[1]);
	}
	public static short getBarPressureSensorId()
	{
		return Short.parseShort(getProperty(Barometric_Pressure_Ids).split(OFFSET_STR_DELIMITER)[0]);
	}
	public static short getBarPressureMeasureId()
	{
		return Short.parseShort(getProperty(Barometric_Pressure_Ids).split(OFFSET_STR_DELIMITER)[1]);
	}
	public static short getWaterTempSensorId()
	{
		return Short.parseShort(getProperty(Water_Temperature_Ids).split(OFFSET_STR_DELIMITER)[0]);
	}
	public static short getWaterTempMeasureId()
	{
		return Short.parseShort(getProperty(Water_Temperature_Ids).split(OFFSET_STR_DELIMITER)[1]);
	}
	public static short getWaveHeightSensorId()
	{
		return Short.parseShort(getProperty(Wave_Height_Ids).split(OFFSET_STR_DELIMITER)[0]);
	}
	public static short getWaveHeightMeasureId()
	{
		return Short.parseShort(getProperty(Wave_Height_Ids).split(OFFSET_STR_DELIMITER)[1]);
	}
	public static short getWavePeriodSensorId()
	{
		return Short.parseShort(getProperty(Wave_Period_Ids).split(OFFSET_STR_DELIMITER)[0]);
	}
	public static short getWavePeriodMeasureId()
	{
		return Short.parseShort(getProperty(Wave_Period_Ids).split(OFFSET_STR_DELIMITER)[1]);
	}
	public static short getThermalStringSensorId()
	{
		return Short.parseShort(getProperty(THERMAL_STRING_Ids).split(OFFSET_STR_DELIMITER)[0]);
	}
	public static short getThermalStringMeasureId()
	{
		return Short.parseShort(getProperty(THERMAL_STRING_Ids).split(OFFSET_STR_DELIMITER)[1]);
	}
	public static short getChlorophy2SensorId()
	{
		return Short.parseShort(getProperty(CHLOROPHY2_Ids).split(OFFSET_STR_DELIMITER)[0]);
	}
	public static short getChlorophy2MeasureId()
	{
		return Short.parseShort(getProperty(CHLOROPHY2_Ids).split(OFFSET_STR_DELIMITER)[1]);
	}
	
	public static short getRelativeHumidity2SensorId() {
		// TODO Auto-generated method stub
		return Short.parseShort(getProperty(RELATIVEHUMIDITY_Ids).split(OFFSET_STR_DELIMITER)[0]);
	}

	public static short getRelativeHumidityMeasureId() {
		// TODO Auto-generated method stub
		return Short.parseShort(getProperty(RELATIVEHUMIDITY_Ids).split(OFFSET_STR_DELIMITER)[1]);
	}
	
	public static short getWaterConductivitySensorId() {
		// TODO Auto-generated method stub
		return Short.parseShort(getProperty(WATER_CONDUCTIVITY_Ids).split(OFFSET_STR_DELIMITER)[0]);
	}

	public static short getWaterConductivityMeasureId() {
		// TODO Auto-generated method stub
		return Short.parseShort(getProperty(WATER_CONDUCTIVITY_Ids).split(OFFSET_STR_DELIMITER)[1]);
	}
	public static short getPHSensorId() {
		// TODO Auto-generated method stub
		return Short.parseShort(getProperty(PH_Ids).split(OFFSET_STR_DELIMITER)[0]);
	}

	public static short getPHMeasureId() {
		// TODO Auto-generated method stub
		return Short.parseShort(getProperty(PH_Ids).split(OFFSET_STR_DELIMITER)[1]);
	}
	
	public static short getYSITurbiditySensorId() {
		// TODO Auto-generated method stub
		return Short.parseShort(getProperty(YSI_TURBIDITY_Ids).split(OFFSET_STR_DELIMITER)[0]);
	}

	public static short getYSITurbidityMeasureId() {
		// TODO Auto-generated method stub
		return Short.parseShort(getProperty(YSI_TURBIDITY_Ids).split(OFFSET_STR_DELIMITER)[1]);
	}
	
	public static short getYSIChlorophyllSensorId() {
		// TODO Auto-generated method stub
		return Short.parseShort(getProperty(YSI_CHLOROPHYLL_Ids).split(OFFSET_STR_DELIMITER)[0]);
	}

	public static short getYSIChlorophyllMeasureId() {
		// TODO Auto-generated method stub
		return Short.parseShort(getProperty(YSI_CHLOROPHYLL_Ids).split(OFFSET_STR_DELIMITER)[1]);
	}
	
	public static short getYSIBlueGreenAlgaeSensorId() {
		// TODO Auto-generated method stub
		return Short.parseShort(getProperty(YSI_BLUE_GREEN_ALGAE_Ids).split(OFFSET_STR_DELIMITER)[0]);
	}

	public static short getYSIBlueGreenAlgaeMeasureId() {
		// TODO Auto-generated method stub
		return Short.parseShort(getProperty(YSI_BLUE_GREEN_ALGAE_Ids).split(OFFSET_STR_DELIMITER)[1]);
	}
	
	public static short getDissolvedOxygenSensorId() {
		// TODO Auto-generated method stub
		return Short.parseShort(getProperty(DISSOLVED_OXYGEN_Ids).split(OFFSET_STR_DELIMITER)[0]);
	}

	public static short getDissolvedOxygenMeasureId() {
		// TODO Auto-generated method stub
		return Short.parseShort(getProperty(DISSOLVED_OXYGEN_Ids).split(OFFSET_STR_DELIMITER)[1]);
	}
	
	public static short getDissolvedOxygenSaturationSensorId() {
		// TODO Auto-generated method stub
		return Short.parseShort(getProperty(DISSOLVED_OXYGEN_SATURATION_Ids).split(OFFSET_STR_DELIMITER)[0]);
	}

	public static short getDissolvedOxygenSaturationMeasureId() {
		// TODO Auto-generated method stub
		return Short.parseShort(getProperty(DISSOLVED_OXYGEN_SATURATION_Ids).split(OFFSET_STR_DELIMITER)[1]);
	}
}

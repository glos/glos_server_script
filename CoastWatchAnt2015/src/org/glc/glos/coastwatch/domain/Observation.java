/* Great Lakes Observing System Regional Association 
 * @Author Guan Wang
 * @Organization Great Lakes Commission
 * @Contact Pete Giencke
 *           pgiencke@glc.org
 *           734-971-9135
 *           Eisenhower Corporate Park
 *           2805 S. Industrial Hwy, Suite 100
 */
package org.glc.glos.coastwatch.domain;

import java.util.ArrayList;

public class Observation {
	public static final short NumberOfFields=21;
	//private long stationID=Long.MIN_VALUE;
	private long date=Long.MIN_VALUE;
	private short stationType=Short.MIN_VALUE;
	private String handle=null;
	private short lakeNo=Short.MIN_VALUE;
	private short dataFormat=Short.MIN_VALUE;
	private float airTemperature=Float.NaN;
	private float dewPoint=Float.NaN;
	private float windDirection=Float.NaN;
	private float windSpeed=Float.NaN;
	private float maxWindGust=Float.NaN;
	private float cloudCover=Float.NaN;
	private float solarRadiation=Float.NaN;
	private float barometricPressure=Float.NaN;
	private float waterTemperature=Float.NaN;
	private float sigWaveHeight=Float.NaN;
	private float wavePeroid=Float.NaN;
	private float northLatitude=Float.NaN;
	private float westLongitude=Float.NaN;
	private ArrayList<ObsZ> thermalString=null;
	private float relativeHumidity=Float.NaN;
	private ArrayList<ObsZ> chlorophy2Concentration=null;
	private float waterConductivity=Float.NaN;
	private float ph=Float.NaN;
	private float ysiTurbidity=Float.NaN;
	private float ysiChlorophyll=Float.NaN;
	private float ysiBlueGreenAlgae=Float.NaN;
	private float dissovledOxygen=Float.NaN;
	private float dissovledOxygenSaturation=Float.NaN;
	/*public long getStationID() {
		return stationID;
	}
	public void setStationID(long stationID) {
		this.stationID = stationID;
	}*/
	public ArrayList<ObsZ> getChlorophy2Concentration() {
		return chlorophy2Concentration;
	}
	public void setChlorophy2Concentration(double depth,double obs,short type) {
		if(this.chlorophy2Concentration == null)
			chlorophy2Concentration=new ArrayList<ObsZ>();
		ObsZ oz=new ObsZ();
		oz.depth=depth;
		oz.value=obs;
		oz.type=type;
		chlorophy2Concentration.add(oz);
	}
	public float getRelativeHumidity() {
		return relativeHumidity;
	}
	
	public void setRelativeHumidity(float relativeHumidity) {
		this.relativeHumidity = relativeHumidity;
	}
	public void setThermalString(double depth,double obs,short type)
	{
		if(thermalString==null)
			thermalString=new ArrayList<ObsZ>();
		ObsZ oz=new ObsZ();
		oz.depth=depth;
		oz.value=obs;
		oz.type=type;
		thermalString.add(oz);
	}
	
	public ArrayList<ObsZ> getThermalString()
	{
		return this.thermalString;
	}
	public long getDate() {
		return date;
	}
	public void setDate(long date) {
		this.date = date;
	}
	public short getStationType() {
		return stationType;
	}
	public void setStationType(short stationType) {
		this.stationType = stationType;
	}
	public String getHandle() {
		return handle;
	}
	public void setHandle(String handle) {
		this.handle = handle;
	}
	public short getLakeNo() {
		return lakeNo;
	}
	public void setLakeNo(short lakeNo) {
		this.lakeNo = lakeNo;
	}
	
	public short getDataFormat() {
		return dataFormat;
	}
	public void setDataFormat(short dataFormat) {
		this.dataFormat = dataFormat;
	}
	public float getAirTemperature() {
		return airTemperature;
	}
	public void setAirTemperature(float airTemperature) {
		this.airTemperature = airTemperature;
	}
	public float getDewPoint() {
		return dewPoint;
	}
	public void setDewPoint(float dewPoint) {
		this.dewPoint = dewPoint;
	}
	public float getWindDirection() {
		return windDirection;
	}
	public void setWindDirection(float windDirection) {
		this.windDirection = windDirection;
	}
	public float getWindSpeed() {
		return windSpeed;
	}
	public void setWindSpeed(float windSpeed) {
		this.windSpeed = windSpeed;
	}
	public float getMaxWindGust() {
		return maxWindGust;
	}
	public void setMaxWindGust(float maxWindGust) {
		this.maxWindGust = maxWindGust;
	}
	public float getCloudCover() {
		return cloudCover;
	}
	public void setCloudCover(float cloudCover) {
		this.cloudCover = cloudCover;
	}
	public float getSolarRadiation() {
		return solarRadiation;
	}
	public void setSolarRadiation(float solarRadiation) {
		this.solarRadiation = solarRadiation;
	}
	public float getBarometricPressure() {
		return barometricPressure;
	}
	public void setBarometricPressure(float barometricPressure) {
		this.barometricPressure = barometricPressure;
	}
	public float getWaterTemperature() {
		return waterTemperature;
	}
	public void setWaterTemperature(float waterTemperature) {
		this.waterTemperature = waterTemperature;
	}
	public float getSigWaveHeight() {
		return sigWaveHeight;
	}
	public void setSigWaveHeight(float sigWaveHeight) {
		this.sigWaveHeight = sigWaveHeight;
	}
	public float getWavePeroid() {
		return wavePeroid;
	}
	public void setWavePeroid(float wavePeroid) {
		this.wavePeroid = wavePeroid;
	}
	public float getNorthLatitude() {
		return northLatitude;
	}
	public void setNorthLatitude(float northLatitude) {
		this.northLatitude = northLatitude;
	}
	public float getWestLongitude() {
		return westLongitude;
	}
	public void setWestLongitude(float westLongitude) {
		this.westLongitude = westLongitude;
	}
	public float getWaterConductivity() {
		return waterConductivity;
	}
	public void setWaterConductivity(float waterConductivity) {
		this.waterConductivity = waterConductivity;
	}
	public float getPh() {
		return ph;
	}
	public void setPh(float ph) {
		this.ph = ph;
	}
	public float getYsiTurbidity() {
		return ysiTurbidity;
	}
	public void setYsiTurbidity(float ysiTurbidity) {
		this.ysiTurbidity = ysiTurbidity;
	}
	public float getYsiChlorophyll() {
		return ysiChlorophyll;
	}
	public void setYsiChlorophyll(float ysiChlorophyll) {
		this.ysiChlorophyll = ysiChlorophyll;
	}
	public float getYsiBlueGreenAlgae() {
		return ysiBlueGreenAlgae;
	}
	public void setYsiBlueGreenAlgae(float ysiBlueGreenAlgae) {
		this.ysiBlueGreenAlgae = ysiBlueGreenAlgae;
	}
	public float getDissovledOxygen() {
		return dissovledOxygen;
	}
	public void setDissovledOxygen(float dissovledOxygen) {
		this.dissovledOxygen = dissovledOxygen;
	}
	public float getDissovledOxygenSaturation() {
		return dissovledOxygenSaturation;
	}
	public void setDissovledOxygenSaturation(float dissovledOxygenSaturation) {
		this.dissovledOxygenSaturation = dissovledOxygenSaturation;
	}
	
	
}

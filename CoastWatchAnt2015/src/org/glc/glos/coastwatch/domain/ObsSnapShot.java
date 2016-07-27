package org.glc.glos.coastwatch.domain;

public class ObsSnapShot {
	
	private String platformHandler="";
	private long sensorId=-1;
	private long measureId=-1;
	private long date=0;
	private float longitude=-999.9f;
	private float latitude=-999.9f;
	private float altitude=-9999.9f;
	private float measureValue=-9999.9f;
	private long rowEntryDate=0;
	private long rowUpdateDate=0;
	
	public String getPlatformHandler() {
		return platformHandler;
	}
	public void setPlatformHandler(String platformHandler) {
		this.platformHandler = platformHandler;
	}
	public long getSensorId() {
		return sensorId;
	}
	public void setSensorId(long sensorId) {
		this.sensorId = sensorId;
	}
	public long getMeasureId() {
		return measureId;
	}
	public void setMeasureId(long measureId) {
		this.measureId = measureId;
	}
	public long getDate() {
		return date;
	}
	public void setDate(long date) {
		this.date = date;
	}
	public float getLongitude() {
		return longitude;
	}
	public void setLongitude(float longitude) {
		this.longitude = longitude;
	}
	public float getLatitude() {
		return latitude;
	}
	public void setLatitude(float latitude) {
		this.latitude = latitude;
	}
	public float getAltitude() {
		return altitude;
	}
	public void setAltitude(float altitude) {
		this.altitude = altitude;
	}
	public float getMeasureValue() {
		return measureValue;
	}
	public void setMeasureValue(float measureValue) {
		this.measureValue = measureValue;
	}
	public long getRowEntryDate() {
		return rowEntryDate;
	}
	public void setRowEntryDate(long rowEntryDate) {
		this.rowEntryDate = rowEntryDate;
	}
	public long getRowUpdateDate() {
		return rowUpdateDate;
	}
	public void setRowUpdateDate(long rowUpdateDate) {
		this.rowUpdateDate = rowUpdateDate;
	}
	
	
}

package org.glc.domain;

public class Coordinate {
	private double lon;
	private double lat;
	
	public Coordinate()
	{
		lon=0.0;
		lat=0.0;
	}
	public Coordinate(double lon,double lat)
	{
		this.lon=lon;
		this.lat=lat;
	}
	public double getLon() {
		return lon;
	}
	public void setLon(double lon) {
		this.lon = lon;
	}
	public double getLat() {
		return lat;
	}
	public void setLat(double lat) {
		this.lat = lat;
	}
	
}

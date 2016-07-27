package us.glos.glcfs.domain;

import org.glc.domain.Coordinate;

public class NearestCoordinate extends Coordinate {
	private double distance;
	
	public NearestCoordinate()
	{
		super();
	}
	public NearestCoordinate(double x,double y,double dist)
	{
		this.setLon(x);
		this.setLat(y);
		this.distance=dist;
	}

	public double getDistance() {
		return distance;
	}

	public void setDistance(double distance) {
		this.distance = distance;
	}
	
	
}

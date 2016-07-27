package org.glc.domain;

public class Envelope {
	private double minx;
	private double miny;
	private double maxx;
	private double maxy;
	private int srid;
	public Envelope()
	{
		minx=miny=maxx=maxy=0.0;
		srid=-1;
	}
	public Envelope(double minx,double miny,double maxx,double maxy,int srid)
	{
		this.minx=minx;
		this.miny=miny;
		this.maxx=maxx;
		this.maxy=maxy;
		this.srid=srid;
	}
	public double getMinx() {
		return minx;
	}
	public void setMinx(double minx) {
		this.minx = minx;
	}
	public double getMiny() {
		return miny;
	}
	public void setMiny(double miny) {
		this.miny = miny;
	}
	public double getMaxx() {
		return maxx;
	}
	public void setMaxx(double maxx) {
		this.maxx = maxx;
	}
	public double getMaxy() {
		return maxy;
	}
	public void setMaxy(double maxy) {
		this.maxy = maxy;
	}
	public int getSrid() {
		return srid;
	}
	public void setSrid(int srid) {
		this.srid = srid;
	}
	public String toString()
	{
		StringBuilder sb=new StringBuilder();
		sb.append("SetSRID('BOX3D(");
		sb.append(this.minx);
		sb.append(' ');
		sb.append(this.miny);
		sb.append(',');
		sb.append(this.maxx);
		sb.append(' ');
		sb.append(this.maxy);
		sb.append(")'::box3d,");
		sb.append(this.srid);
		sb.append(')');
		return sb.toString();
	}
}

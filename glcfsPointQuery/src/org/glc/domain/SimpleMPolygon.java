package org.glc.domain;

import java.util.ArrayList;

public class SimpleMPolygon {
	private ArrayList<Coordinate[]> polys=null;
	private int srid=-1;
	
	public SimpleMPolygon(int srid)
	{
		this.srid=srid;
		
	}
	public int getSRID()
	{
		return this.srid;
	}
	public boolean addSimplePolygon(Coordinate[] coords)
	{
		if(coords!=null&&coords.length>1)
		{
			if(this.polys==null)
				this.polys=new ArrayList<Coordinate[]>();
			this.polys.add(coords);
			return true;
		}
		else
		    return false;
	}
	public int getPolygonCount()
	{
		return polys==null?0:polys.size();
	}
	public Coordinate[] getPolyganByIndex(int i)
	{
		if(i>=0&&polys!=null&&i<polys.size())
		{
			return polys.get(i);
		}
		else
			return null;
	}
	public String toString()
	{
		int count=0;
		Coordinate coord=null;
		Coordinate first=null;
		StringBuilder sb=new StringBuilder();
		sb.append("MULTIPOLYGON(");
		if(polys!=null&&polys.size()>0)
		{
			for(Coordinate[] coords:polys)
			{
				if(coords!=null)
				{
					sb.append("((");
					count=coords.length;
					for(int i=0;i<count;++i)
					{
						coord=coords[i];
						if(coord!=null)
						{
							if(first==null)
								first=coord;
							sb.append(coord.getLon());
							sb.append(' ');
							sb.append(coord.getLat());
							sb.append(',');
						}
					}
					sb.append(first.getLon());
					sb.append(" ");
					sb.append(first.getLat());
					sb.append(")),");
				}
			}
			
		}
		if(sb.charAt(sb.length()-1)==',')
			sb.deleteCharAt(sb.length()-1);
		sb.append(")");
		return sb.toString();
	}
}

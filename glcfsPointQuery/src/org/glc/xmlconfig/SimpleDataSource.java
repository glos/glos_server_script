package org.glc.xmlconfig;

public class SimpleDataSource {
	public static enum DSType
	{
		JNDI,
		NONE
	}
	private String name;
	private String value;
	private DSType _type;
	private String tvalue;
	public SimpleDataSource()
	{
		this.name=null;
		this.value=null;
		this._type=DSType.NONE;
		this.tvalue=null;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getValue() {
		return value;
	}
	public void setValue(String value) {
		this.value = value;
	}
	public DSType getType() {
		return _type;
	}
	public void setTvalue(String type) {
		try
		{
		    this._type = DSType.valueOf(type.toUpperCase());
		}
		catch(Exception e)
		{
			this._type=DSType.NONE;
		}
	}
	
}

package org.glc.domain;

public class Orgnization implements java.io.Serializable{
	/**
	 * 
	 */
	private static final long serialVersionUID = -5357410611100441896L;
	private String name;
	private int id;
	private String abbrev;
	private String description;
	private String url;
	public Orgnization()
	{
		
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getAbbrev() {
		return abbrev;
	}

	public void setAbbrev(String abbrev) {
		this.abbrev = abbrev;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}
	
}

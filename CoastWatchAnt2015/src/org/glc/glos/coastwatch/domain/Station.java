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

public class Station {
	private long id=Long.MIN_VALUE;
	private float longitude=Float.NaN;
	private float latitude=Float.NaN;
	private float altitude=Float.NaN;
	private float anemoHeight=Float.NaN;
	private String handle=null;
	private String nws_handle=null;
	private String nos_handle=null;
	private String icao_handle=null;
	private String coop_handle=null;
	private String wmo_handle=null;
	private String radio_call_sign=null;
	private String ndbc_handle=null;
	private String other_handle=null;
	private long updateMillSec=0;
	private int typeId;
	public long getId() {
		return id;
	}
	public void setId(long id) {
		this.id = id;
	}
	
	public int getTypeId() {
		return typeId;
	}
	public void setTypeId(int typeId) {
		this.typeId = typeId;
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
	
	public float getAnemoHeight() {
		return anemoHeight;
	}
	public void setAnemoHeight(float anemoHeight) {
		this.anemoHeight = anemoHeight;
	}
	
	public String getHandle() {
		return handle;
	}
	public void setHandle(String handle) {
		this.handle = handle;
	}
	
	public String getCoop_handle() {
		return coop_handle;
	}
	public void setCoop_handle(String coop_handle) {
		this.coop_handle = coop_handle;
	}
	public String getIcao_handle() {
		return icao_handle;
	}
	public void setIcao_handle(String icao_handle) {
		this.icao_handle = icao_handle;
	}
	public String getNos_handle() {
		return nos_handle;
	}
	public void setNos_handle(String nos_handle) {
		this.nos_handle = nos_handle;
	}
	public String getNws_handle() {
		return nws_handle;
	}
	public void setNws_handle(String nws_handle) {
		this.nws_handle = nws_handle;
	}
	public String getRadio_call_sign() {
		return radio_call_sign;
	}
	public void setRadio_call_sign(String radio_call_sign) {
		this.radio_call_sign = radio_call_sign;
	}
	public String getWmo_handle() {
		return wmo_handle;
	}
	public void setWmo_handle(String wmo_handle) {
		this.wmo_handle = wmo_handle;
	}
	
	public String getNdbc_handle() {
		return ndbc_handle;
	}
	public void setNdbc_handle(String ndbc_handle) {
		this.ndbc_handle = ndbc_handle;
	}
	
	public String getOther_handle() {
		return other_handle;
	}
	public void setOther_handle(String other_handle) {
		this.other_handle = other_handle;
	}
	public long getUpdateMillSec() {
		return updateMillSec;
	}
	public void setUpdateMillSec(long updateMillSec) {
		this.updateMillSec = updateMillSec;
	}
	public Station(){
		
	}
	public Station(long sid,float lon,float lat,float alt)
	{
		id=sid;
		longitude=lon;
		latitude=lat;
		altitude=alt;
		
	}
}

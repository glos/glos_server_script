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

public interface ILookupID {
		
	short getAirTempSensorId();
	
	short getAirTempMeasureId();
	
	short getDewPntSensorId();
	
	short getDewPntMeasureId();
	
	short getWndDirectSensorId();
	
	short getWndDirectMeasureId();
	
	short getWndSpeedSensorId();
	
	short getWndSpeedMeasureId();
	
	short getMaxWndGustSensorId();
	
	short getMaxWndGustMeasureId();
	
	short getCloudCoverSensorId();
	
	short getCloudCoverMeasureId();
	
	short getSolarRadiationSensorId();
	
	short getSolarRadiationMeasureId();
	
	short getBarPressureSensorId();
	
	short getBarPressureMeasureId();
	
	short getWaterTempSensorId();
	
	short getWaterTempMeasureId();
	
	short getWaveHeightSensorId();
	
	short getWaveHeightMeasureId();
	
	short getWavePeriodSensorId();
	
	short getWavePeriodMeasureId();
	
	short getThermalStringSensorId();
	
	short getThermalStringMeasureId();
	
	short getChlorophy2SensorId();
	
	short getChlorophy2MeasureId();
	
	short getRelativeHumiditySensorId();
	
	short getRelativeHumidity2MeasureId();
	
	short getWaterConductivitySensorId();
	
	short getWaterConductivityMeasureId();
	
	short getPHSensorId();
	
	short getPHMeasureId();
	
	short getYSITurbiditySensorId();
	
	short getYSITurbidityMeasureId();
	
	short getYSIChlorophyllSensorId();
	
	short getYSIChlorophyllMeasureId();
	
	short getYSIBlueGreenAlgaeSensorId();
	
	short getYSIBlueGreenAlgaeMeasureId();
	
	short getDissolvedOxygenSensorId();
	
	short getDissolvedOxygenMeasureId();
	
	short getDissolvedOxygenSaturationSensorId();
	
	short getDissolvedOxygenSaturationMeasureId();
}

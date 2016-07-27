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

public class UnitConverter implements IConverter<Float>{

	public Float getResult(long m_typeid,int fromid, int toid, Float value) {
		// TODO Auto-generated method stub
		Float result=null;
		switch((int)m_typeid){
			case 8:
				//if(fromid==4&&toid==22)
					result=0.02953f*value;
				break;
			case 1:
			case 2:
				//if(fromid==1&&toid==21)
					result=1.943846f*value;
				break;
			case 4:
			case 5:
			case 9:
				//if(fromid==3&&toid==32)
					result=value*1.8f+32;
				break;
			case 10:
				result=3.2808399f*value;
				break;
		}
		return result;
	}
	

}

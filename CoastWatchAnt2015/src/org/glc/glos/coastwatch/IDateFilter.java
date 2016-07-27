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

public interface IDateFilter {
	boolean IsNewData(String handle,long time);
	void updateDate(String handle,long time);
}

/*****************************************************************
 JADE - Java Agent DEvelopment Framework is a framework to develop
 multi-agent systems in compliance with the FIPA specifications.
 Copyright (C) 2000 CSELT S.p.A.
 
 GNU Lesser General Public License
 
 This library is free software; you can redistribute it and/or
 modify it under the terms of the GNU Lesser General Public
 License as published by the Free Software Foundation, 
 version 2.1 of the License. 
 
 This library is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 Lesser General Public License for more details.
 
 You should have received a copy of the GNU Lesser General Public
 License along with this library; if not, write to the
 Free Software Foundation, Inc., 59 Temple Place - Suite 330,
 Boston, MA  02111-1307, USA.
 *****************************************************************/

package jade.core.sam;

//#DOTNET_EXCLUDE_FILE

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * An instance of this class is passed to all configured <code>SAMInfoHandler<code>-s
 * at each polling time and groups together all information collected by the SAM Service at that 
 * polling time.
 */
public class SAMInfo implements Serializable {
	private static final long serialVersionUID = 84762938792387L;

	private Map<String, AverageMeasure> entityMeasures;
	private Map<String, Long> counterValues;
	
	SAMInfo() {
		this(new HashMap<String, AverageMeasure>(), new HashMap<String, Long>());
	}
	
	SAMInfo(Map<String, AverageMeasure> entityMeasures, Map<String, Long> counterValues) {
		this.entityMeasures = entityMeasures;
		this.counterValues = counterValues;
	}
	
	/**
	 * Provides the measures of all monitored entities in form of a Map.
	 * @return A Map mapping monitored entity names to their measures
	 */
	public Map<String, AverageMeasure> getEntityMeasures() {
		return entityMeasures;
	}
	
	/**
	 * Provides the differential values of all monitored counters in form of a Map.
	 * @return A Map mapping monitored counter names to their differential values
	 */
	public Map<String, Long> getCounterValues() {
		return counterValues;
	}
	
	void update(SAMInfo info) {
		// Update entity measures
		Map<String, AverageMeasure> mm = info.getEntityMeasures();
		for (String entityName : mm.keySet()) {
			AverageMeasure newM = mm.get(entityName);
			// If this is a new entity --> add it. Otherwise update the measure we have internally
			AverageMeasure m = entityMeasures.get(entityName);
			if (m == null) {
				entityMeasures.put(entityName, newM);
			}
			else {
				m.update(newM);
			}
		}
		
		// Update counter values
		Map<String, Long> vv = info.getCounterValues();
		for (String counterName : vv.keySet()) {
			long newV = vv.get(counterName);
			// If this is a new counter --> add it. Otherwise sum to the value we have internally
			Long v = counterValues.get(counterName);
			if (v == null) {
				counterValues.put(counterName, newV);
			}
			else {
				counterValues.put(counterName, v.longValue()+newV);
			}
		}
	}
}

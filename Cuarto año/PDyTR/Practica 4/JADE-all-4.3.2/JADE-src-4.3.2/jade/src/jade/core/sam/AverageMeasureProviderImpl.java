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

/**
 * A default ready-made implementation of the AverageMeasureProvider interface that offers 
 * methods to add measure samples and automatically computes an  <code>AverageMeasure</code>
 * when the <code>getValue()</code> method is called. 
 */
public class AverageMeasureProviderImpl implements AverageMeasureProvider {

	private double sum = 0.0;
	private int nSamples = 0;
	
	public synchronized void addSample(Number value) {
		if (value != null) {
			addSample(value.doubleValue());
		}
	}
	
	public synchronized void addSample(int value) {
		nSamples++;
		sum += value;
	}
	
	public synchronized void addSample(long value) {
		nSamples++;
		sum += value;
	}
	
	public synchronized void addSample(float value) {
		nSamples++;
		sum += value;
	}
	
	public synchronized void addSample(double value) {
		nSamples++;
		sum += value;
	}
	
	public synchronized AverageMeasure getValue() {
		double avg = (nSamples != 0 ? sum / nSamples : Double.NaN);
		AverageMeasure result = new AverageMeasure(avg, nSamples);
		nSamples = 0;
		sum = 0.0;
		return result;
	}

	public Object clone() throws CloneNotSupportedException {
		AverageMeasureProviderImpl clonedAvgMeasureProvider = new AverageMeasureProviderImpl();
		clonedAvgMeasureProvider.sum = this.sum;
		clonedAvgMeasureProvider.nSamples = this.nSamples;
		return clonedAvgMeasureProvider;
	}

}

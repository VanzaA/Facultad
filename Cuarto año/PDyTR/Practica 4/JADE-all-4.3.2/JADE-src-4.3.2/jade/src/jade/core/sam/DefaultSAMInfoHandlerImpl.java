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

import jade.core.Profile;
import jade.util.Logger;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

class DefaultSAMInfoHandlerImpl implements SAMInfoHandler {

	private static final String SAM_PREFIX = "SAM_";
	
	private Map<String, PrintStream> entityFiles = new HashMap<String, PrintStream>();
	// For counters we need to keep the total value together with the Stream used to write the CSV file
	private Map<String, CounterInfo> counters = new HashMap<String, CounterInfo>();
	
	private SimpleDateFormat timeStampFormatter = new SimpleDateFormat("yyyy-MM-dd HH:mm");
	private String csvSeparator;
	
	private File samInfoDirectory;
	private String fileSeparator;
	
	private Logger myLogger = Logger.getMyLogger(getClass().getName());
	
	public void initialize(Profile p) throws Exception {
		fileSeparator = System.getProperty("file.separator");
		
		// Read (and create if necessary) the directory where to store SAM files.
		String samInfoDirectoryName = p.getParameter("jade_core_sam_SAMService_csvdirectory", ".");
		samInfoDirectory = new File(samInfoDirectoryName);
		if (!samInfoDirectory.exists()) {
			myLogger.log(Logger.CONFIG, "SAM csv directory "+samInfoDirectory+" does not exists. Creating it ...");
			boolean success = samInfoDirectory.mkdirs();
			if (!success) {
				throw new IOException("Cannot create SAM csv directory "+samInfoDirectoryName+".");
			}
		}
		else if (!samInfoDirectory.isDirectory()) {
			throw new IOException("SAM csv location "+samInfoDirectoryName+" is not a directory.");
		}		
		
		// Read the CSV separator character
		csvSeparator = p.getParameter("jade_core_sam_SAMService_csvseparator", ";");
	}

	public void shutdown() {
		// Close all files
		for (PrintStream ps : entityFiles.values()) {
			ps.close();
		}
		for (CounterInfo ci : counters.values()) {
			ci.stream.close();
		}
	}
	
	public void handle(Date timeStamp, SAMInfo info) {
		// Entities
		Map<String, AverageMeasure> entityMeasures = info.getEntityMeasures();
		for (String entityName : entityMeasures.keySet()) {
			myLogger.log(Logger.FINE, "Handling measure of entity "+entityName);
			try {
				AverageMeasure m = entityMeasures.get(entityName);
				PrintStream stream = entityFiles.get(entityName);
				if (stream == null) {
					// This is the first time we get a measure for this entity --> Initialize the file
					myLogger.log(Logger.INFO, "Creating CSV file for measures of entity "+entityName);
					File f = createFile(entityName);
					stream = new PrintStream(f);
					stream.println("Time-stamp"+csvSeparator+"Average-value"+csvSeparator+"N-samples");
					entityFiles.put(entityName, stream);
				}
				stream.println(timeStampFormatter.format(timeStamp)+csvSeparator+m.getValue()+csvSeparator+m.getNSamples());
			}
			catch (Exception e) {
				myLogger.log(Logger.WARNING, "Error writing to CSV file of entity "+entityName, e);
				// Likely someone removed the CSV file in the meanwhile. Reset everything so that at next round the file will be re-created  
				entityFiles.remove(entityName);
			}
		}
		
		// Counters
		Map<String, Long> counterValues = info.getCounterValues();
		for (String counterName : counterValues.keySet()) {
			myLogger.log(Logger.FINE, "Handling value of counter "+counterName);
			try {
				long value = counterValues.get(counterName);
				CounterInfo ci = counters.get(counterName);
				if (ci == null) {
					// This is the first time we get a value for this counter --> Initialize its csv file
					myLogger.log(Logger.INFO, "Creating CSV file for values of counter "+counterName);
					File f = createFile(counterName);
					PrintStream stream = new PrintStream(f);
					stream.println("Time-stamp"+csvSeparator+"Value"+csvSeparator+"Total-value");
					ci = new CounterInfo(stream);
					counters.put(counterName, ci);
				}
				ci.totValue += value;
				ci.stream.println(timeStampFormatter.format(timeStamp)+csvSeparator+value+csvSeparator+ci.totValue);
			}
			catch (Exception e) {
				myLogger.log(Logger.WARNING, "Error writing to CSV file of counter "+counterName, e);
				// Likely someone removed the CSV file in the meanwhile. Reset everything so that at next round the file will be re-created  
				counters.remove(counterName);
			}
		}
	}
	
	/**
	 * If the entity name has the form a/b/c then create file SAM_c.csv in sub-directory b 
	 * of directory a under the indicated csv-directory. Create directories if missing
	 */
	private File createFile(String name) throws IOException {
		File dir = samInfoDirectory;
		String[] ss = name.split("/");
		if (ss.length > 1) {
			String dirName = samInfoDirectory.getPath();
			for (int i = 0; i < ss.length -1; ++i) {
				dirName += fileSeparator+ss[i];
			}
			dir = new File(dirName); 
			if (!dir.exists()) {
				myLogger.log(Logger.INFO, "Creating directory "+dir+" ...");				
				boolean success = dir.mkdirs();
				if (!success) {
					throw new IOException("Cannot create directory "+dirName+".");
				}
			}
			name = ss[ss.length-1];
		}
		String fileName = dir.getPath()+fileSeparator+SAM_PREFIX+name+".csv";
		File file = new File(fileName);
		file.createNewFile();		
		return file;
	}
	
	
	/**
	 * Inner class CounterInfo
	 */
	private class CounterInfo {
		
		PrintStream stream;
		long totValue = 0;
		
		CounterInfo(PrintStream ps) {
			stream = ps;
		}
	} // END of inner class CounterInfo
}

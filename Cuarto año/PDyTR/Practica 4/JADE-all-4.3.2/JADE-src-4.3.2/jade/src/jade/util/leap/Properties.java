/*--- formatted by Jindent 2.1, (www.c-lab.de/~jindent) ---*/

/**
 * ***************************************************************
 * The LEAP libraries, when combined with certain JADE platform components,
 * provide a run-time environment for enabling FIPA agents to execute on
 * lightweight devices running Java. LEAP and JADE teams have jointly
 * designed the API for ease of integration and hence to take advantage
 * of these dual developments and extensions so that users only see
 * one development platform and a
 * single homogeneous set of APIs. Enabling deployment to a wide range of
 * devices whilst still having access to the full development
 * environment and functionalities that JADE provides.
 * 
 * GNU Lesser General Public License
 * 
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation,
 * version 2.1 of the License.
 * 
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the
 * Free Software Foundation, Inc., 59 Temple Place - Suite 330,
 * Boston, MA  02111-1307, USA.
 * **************************************************************
 */
package jade.util.leap;

import java.io.*;
import java.util.*;
/*#MIDP_INCLUDE_BEGIN
import javax.microedition.io.*;
import javax.microedition.rms.*;
import javax.microedition.lcdui.*;
import javax.microedition.midlet.*;
#MIDP_INCLUDE_END*/

/**
   The LEAP (environment-dependent) version of the &qote;java.util.Properties&qote; class.
   This class appears to be exactly the same in J2SE, PJAVA and MIDP.
   The internal implementation is different in the three cases however.
   In particular the J2SE and PJAVA implementation simply extend 
   java.util.Properties.
   In particular the MIDP version re-implement the load and store 
   methods to access properties from
   the .jad file of the midlet or from a properly formatted 
   RecordStore (see the <code>load()</code> and <code>store</code>
   methods.
   
   @author Steffen Rusitschka - Siemens AG
   @author Marc Schlichte - Siemens AG
   @author Nicolas Lhuillier - Motorola
   @author Giovanni Caire - TILAB
 */
//#MIDP_EXCLUDE_BEGIN
public class Properties extends java.util.Properties implements Serializable {
	/**
 	   This is required to ensure compatibility with 
 	   the J2ME version of this class in serialization/deserialization 
 	   operations.
 	 */
  private static final long     serialVersionUID = 3487495895819396L;
  
  private static final String HEADER = "LEAP-Properties";
//#MIDP_EXCLUDE_END
/*#MIDP_INCLUDE_BEGIN
public class Properties extends Hashtable {
  private static final char SEPARATOR = '=';
  private static final String JAD = "jad";
	// in MIDP2.0, names of user-def properties cannot start with MIDlet
	// for backward compatibility with JADE 3.3 we try both names
  private static final String JAD_PREFIX = "LEAP-";
  private static final String JAD_PREFIX_old = "MIDlet-LEAP-";
  private boolean             fromJad = false;
#MIDP_INCLUDE_END*/


  /**
     Default constructor.
  */
    public Properties() {
    }

    //#MIDP_EXCLUDE_BEGIN
    public static Properties toLeapProperties(java.util.Properties pp) {
    	if (pp instanceof Properties) {
    		return (Properties) pp;
    	}
    	else {
    		Properties leapPP = new Properties();
    	    Enumeration names = pp.propertyNames();

    	    while (names.hasMoreElements()) {
    	      String key = (String) names.nextElement();
    	      String value = pp.getProperty(key);
    	      leapPP.setProperty(key, value);
    	    }
    	    return leapPP;
    	}
    }
    //#MIDP_EXCLUDE_END

  /**
     Load a set of key-value pairs from a given storage element.
     All key-value pairs previously included in this Properties object
     will be lost.
     The storage element is environment-dependent:
     In a J2SE or PJAVA environment it is a file named 
     <code>storage</code>.
     In a MIDP environment it can be the JAD of the MIDlet 
     (if <code>storage</code> = "jad") or a RecordStore called 
     <code>storage</code>.
   */
  public void load(String storage) throws IOException {
  	clear();
  	//#MIDP_EXCLUDE_BEGIN
    try {
      // Search the file system
      InputStream in = new FileInputStream(storage); 
      load(in);
      in.close();
    }
    catch(IOException ioe) {
    	// Search the classpath
    	InputStream in = ClassLoader.getSystemResourceAsStream(storage);
    	if (in == null) {
    		throw new IOException("Cannot find file "+storage);
    	}
    	load(in); 
      in.close();
    }
    //#MIDP_EXCLUDE_END
    /*#MIDP_INCLUDE_BEGIN
    if (JAD.equals(storage)) {
    	fromJad = true;
  	}
  	else {
    	fromJad = false;
      recordstoreLoad(storage);
    }
    #MIDP_INCLUDE_END*/
  } 

  /**
     Store the set of key-value pairs held by this Properties object
     into a given storage element.
     The storage element is environment-dependent:
     In a J2SE or PJAVA environment it is a file named 
     <code>storage</code>.
     In a MIDP environment it is a RecordStore called 
     <code>storage</code>.
   */
  public void store(String storage) throws IOException {
  	//#J2ME_EXCLUDE_BEGIN
    OutputStream out = new FileOutputStream(storage);
    super.store(out, HEADER);
    out.close();
  	//#J2ME_EXCLUDE_END
  	/*#PJAVA_INCLUDE_BEGIN
    OutputStream out = new FileOutputStream(storage);
    super.save(out, HEADER);
    out.close();
  	#PJAVA_INCLUDE_END*/
  	/*#MIDP_INCLUDE_BEGIN
    recordstoreStore(storage);
  	#MIDP_INCLUDE_END*/
  } 

    //#APIDOC_EXCLUDE_BEGIN
  public Object clone() {
    Properties  p = new Properties();
    Enumeration enumeration = propertyNames();

    while (enumeration.hasMoreElements()) {
      String key = (String) enumeration.nextElement();
      String value = getProperty(key);
      if(value != null) {
	  p.setProperty(key, value);
      }
    }
		/*#MIDP_INCLUDE_BEGIN
    p.fromJad = fromJad;
    #MIDP_INCLUDE_END*/
    return p;
  } 
    //#APIDOC_EXCLUDE_END


  /*#J2ME_INCLUDE_BEGIN
  public synchronized Object setProperty(String key, String value) {
    return super.put(key, value);
  } 
  #J2ME_INCLUDE_END*/

  /*#MIDP_INCLUDE_BEGIN
  public String getProperty(String key) {
    String prop = (String) super.get(key);
    if (prop == null && fromJad && jade.core.Agent.midlet != null) {
      prop = jade.core.Agent.midlet.getAppProperty(JAD_PREFIX + key);
	    // in MIDP2.0, names of user-def properties cannot start with MIDlet
			// for backward compatibility with JADE 3.3 we try both names
      if (prop == null)
        prop = jade.core.Agent.midlet.getAppProperty(JAD_PREFIX_old + key);
    } 
    return prop;
  } 

  public Enumeration propertyNames() {
    return keys();
  } 
  
  private void recordstoreLoad(String name) throws IOException {
  	RecordStore rs = null;
  	try {
      rs = RecordStore.openRecordStore(name, false);
  	}
  	catch (Exception e) {
  		throw new IOException("Can't open recordstore "+name+". "+e);
  	}
  	
  	try {
      int size = rs.getNumRecords();
      for (int i = 0; i < size; i++) {
   			String line = new String(rs.getRecord(i+1));
   			int j = line.indexOf(SEPARATOR);
   			if (j >= 0) {
   				// valid line 
   				String k = line.substring(0, j).trim();
   				if (j+1 < line.length()) {
	   				String v = line.substring(j+1).trim();
  	 				if (!k.equals("") && !v.equals("")) {
   						put(k, v);
   					}
					}
   			}
      }
      rs.closeRecordStore();
  	}
  	catch (Exception e) {
  		throw new IOException("Error reading recordstore "+name+". "+e);
  	}
  }

  private void recordstoreStore(String name) throws IOException {
  	try {
  		RecordStore.deleteRecordStore(name);
  	}
  	catch (Exception e) {
  		// Do nothing 
  	}
  	
  	try {
  		RecordStore rs = RecordStore.openRecordStore(name, true);
  		Enumeration keys = keys();
  		while (keys.hasMoreElements()) {
  			Object k = keys.nextElement();
  			Object v = get(k);
  			String line = k.toString()+SEPARATOR+v.toString();
				byte[] bb = line.getBytes();
				rs.addRecord(bb,0,bb.length);
  		}
  		rs.closeRecordStore();
  	}
  	catch (Exception e) {
  		throw new IOException("Error writing recordstore "+name+". "+e);
  	}
  } 
  #MIDP_INCLUDE_END*/
}


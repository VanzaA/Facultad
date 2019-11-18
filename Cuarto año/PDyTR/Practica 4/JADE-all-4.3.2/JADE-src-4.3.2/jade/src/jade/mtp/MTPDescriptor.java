/*****************************************************************
 JADE - Java Agent DEvelopment Framework is a framework to develop 
 multi-agent systems in compliance with the FIPA specifications.
 Copyright (C) 2000 CSELT S.p.A. 
 
 The updating of this file to JADE 2.0 has been partially supported by the IST-1999-10211 LEAP Project
 
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

package jade.mtp;

import jade.core.CaseInsensitiveString;
import jade.util.leap.Serializable;

public class MTPDescriptor implements Serializable {
	private String name;
	private String className;
	private String[] addresses;
	private String[] protoNames;
	
	public MTPDescriptor(String n, String cn, String[] a, String[] pn) {
		name = n;
		className = cn;
		addresses = a;
		protoNames = pn;
	}
	
	public String getName() {
		return name;
	}
	
	public String getClassName() {
		return className;
	}
	
	public String[] getAddresses() {
		return addresses;
	}
	
	public String[] getSupportedProtocols() {
		return protoNames;
	}
	
	public boolean equals(Object obj) {
		try {
			MTPDescriptor toBeCompared = (MTPDescriptor) obj;
			if (!CaseInsensitiveString.equalsIgnoreCase(name, toBeCompared.getName())) {
				return false;
			}
			String[] comparedAddresses = toBeCompared.getAddresses();
			if (addresses == null && comparedAddresses == null) {
				// No need to compare addresses
				return true;
			}
			if (addresses.length != comparedAddresses.length) {
				return false;
			}
			for (int i = 0; i < addresses.length; ++i) {
				int j;
				for (j = 0; j < comparedAddresses.length; ++j) {
					if (CaseInsensitiveString.equalsIgnoreCase(addresses[i], comparedAddresses[j])) {
						break;
					}
				}
				if (j >= comparedAddresses.length) {
					// Address not found
					return false;
				}
			}
			return true;
		}
		catch (Exception e) {
			return false;
		}
	}

	public String toString() {
		return "(MTP :name "+name+" :class-name"+className+" :addresses "+addresses+")";
	}
	
	//#MIDP_EXCLUDE_BEGIN
	// For persistence service
	private MTPDescriptor() {
	}
	
	// For persistence service
	private void setName(String n) {
		name = n;
	}
	
	// For persistence service
	private void setClassName(String cn) {
		className = cn;
	}
	
	// For persistence service
	private void setAddresses(String[] a) {
		addresses = a;
	}
	
	// For persistence service
	private void setSupportedProtocols(String[] sp) {
		protoNames = sp;
	}
	
	
	// For persistence service
	private Long persistentID;
	
	// For persistence service
	private Long getPersistentID() {
		return persistentID;
	}
	
	// For persistence service
	private void setPersistentID(Long l) {
		persistentID = l;
	}
	//#MIDP_EXCLUDE_END	
}




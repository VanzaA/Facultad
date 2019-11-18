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

package jade.content.onto;

import java.io.Serializable;

//#J2ME_EXCLUDE_FILE
//#APIDOC_EXCLUDE_FILE

class SlotKey implements Comparable<SlotKey>, Serializable {
	String schemaName;
	String slotName;
	int position;
	private int hashcode;

	SlotKey(String schemaName, String slotName) {
		this(schemaName, slotName, -1);
	}
	
	SlotKey(String schemaName, String slotName, int position) {
		this.schemaName = schemaName;
		this.slotName = slotName;
		this.position = position;
		calcHashcode();
	}

	private void calcHashcode() {
		hashcode = 37;
		if (schemaName != null) {
			hashcode ^= schemaName.hashCode();
		}
		if (slotName != null) {
			hashcode ^= slotName.hashCode();
		}
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == null) {
			return false;
		}
		if (!(obj instanceof SlotKey)) {
			return false;
		}
		SlotKey other = (SlotKey)obj;
		if (schemaName != null) {
			if (!schemaName.equals(other.schemaName)) {
				return false;
			}
		} else {
			if (other.schemaName != null) {
				return false;
			}
		}
		if (slotName != null) {
			return slotName.equals(other.slotName);
		} else {
			return other.slotName == null;
		}
	}

	@Override
	public int hashCode() {
		return hashcode;
	}

	public int compareTo(SlotKey o) {
		return slotName.compareTo(o.slotName);
	}
	
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder("SlotKey {schemaName=");
		sb.append(schemaName);
		sb.append(" slotName=");
		sb.append(slotName);
		sb.append(" position=");
		sb.append(position);
		return sb.toString();
	}
}
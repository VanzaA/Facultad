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

package jade.domain.KBManagement;

//#MIDP_EXCLUDE_FILE

import java.util.Date;

/**
 * @author Elisabetta Cortese - TILab
 *
 */
public interface LeaseManager {
	
	//final static Date INFINITE_LEASE_TIME = new Date(-1);
	//final static Date DEFAULT_LEASE_TIME = INFINITE_LEASE_TIME;

	Date getLeaseTime(Object item);
	void setLeaseTime(Object item, Date lease);
	Object grantLeaseTime(Object item);
	boolean isExpired(Date lease);
}

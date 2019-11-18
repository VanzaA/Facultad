/*--- formatted by Jindent 2.1, (www.c-lab.de/~jindent) ---*/

/*
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
 * Copyright (C) 2001 Telecom Italia LAB S.p.A.
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

package jade.imtp.leap;

import jade.mtp.TransportAddress;

import java.util.Vector;

/**
 * Interface declaration
 * 
 * @author Giovanni Caire - TILAB
 */
public abstract class TransportProtocol {

	private static final char SLASH = '/';
	private static final char COLON = ':';
	private static final char DIESIS = '#';

	/**
	 * FIXME: should throw a dedicated exception
	 */
	public abstract String addrToString(TransportAddress ta) throws ICPException;

	/**
	 * FIXME: should throw a dedicated exception
	 */
	public abstract TransportAddress stringToAddr(String s) throws ICPException;

	/**
	 */
	public abstract TransportAddress buildAddress(String host, String port, String file, 
			String anchor);

	/**
	 * Method declaration
	 * 
	 * @return
	 * 
	 * @see
	 */
	public abstract String getName();

	/**
	 * FIXME: should throw a dedicated exception
	 */
	protected Vector parseURL(String url) throws ICPException {
		if (url == null) {
			throw new ICPException("Null URL");
		} 
		
		String protocol = null;
		String host = null;
		String port = null;
		String file = null;
		String anchor = null;
		int    fieldStart = 0;
		int    fieldEnd;

		// Protocol
		fieldEnd = url.indexOf(COLON, fieldStart);

		if (fieldEnd > 0 && url.charAt(fieldEnd+1) == SLASH && url.charAt(fieldEnd+2) == SLASH) {
			protocol = url.substring(fieldStart, fieldEnd);
		} 
		else {
			throw new ICPException("Invalid URL: "+url+".");
		} 

		fieldStart = fieldEnd+3;

		// Host
		fieldEnd = url.lastIndexOf(COLON);

		if (fieldEnd > 0) {

			// A port is specified after the host
			host = url.substring(fieldStart, fieldEnd);
			fieldStart = fieldEnd+1;

			// Port
			fieldEnd = url.indexOf(SLASH, fieldStart);

			if (fieldEnd > 0) {

				// A file is specified after the port
				port = url.substring(fieldStart, fieldEnd);
				fieldStart = fieldEnd+1;

				// File
				fieldEnd = url.indexOf(DIESIS, fieldStart);

				if (fieldEnd > 0) {

					// An anchor is specified after the file
					file = url.substring(fieldStart, fieldEnd);
					fieldStart = fieldEnd+1;

					// Anchor
					anchor = url.substring(fieldStart, url.length());
				} 
				else {

					// No anchor is specified after the file
					file = url.substring(fieldStart, url.length());
				} 
			} 
			else {

				// No file is specified after the port
				port = url.substring(fieldStart, url.length());
			} 
		} 
		else {

			// No port is specified after the host
			fieldEnd = url.indexOf(SLASH, fieldStart);

			if (fieldEnd > 0) {

				// A file is specified after the host
				host = url.substring(fieldStart, fieldEnd);
				fieldStart = fieldEnd+1;

				// File
				fieldEnd = url.indexOf(DIESIS, fieldStart);

				if (fieldEnd > 0) {

					// An anchor is specified after the file
					file = url.substring(fieldStart, fieldEnd);
					fieldStart = fieldEnd+1;

					// Anchor
					anchor = url.substring(fieldStart, url.length());
				} 
				else {

					// No anchor is specified after the file
					file = url.substring(fieldStart, url.length());
				} 
			} 
			else {

				// No file is specified after the host
				host = url.substring(fieldStart, url.length());
			} 
		} 

		Vector urlFields = new Vector(5);

		urlFields.addElement(protocol);
		urlFields.addElement(host);
		urlFields.addElement(port);
		urlFields.addElement(file);
		urlFields.addElement(anchor);

		return urlFields;
	} 

}


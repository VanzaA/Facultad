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
 * Copyright (C) 2001 Telecom Italia LAB S.p.A.
 * Copyright (C) 2001 Motorola.
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


package jade.imtp.leap.JICP;

import jade.mtp.TransportAddress;
import jade.imtp.leap.*;
import java.io.*;
//#MIDP_EXCLUDE_BEGIN
import java.net.*;
//#MIDP_EXCLUDE_END
/*#MIDP_INCLUDE_BEGIN
import javax.microedition.io.*;
#MIDP_INCLUDE_END*/
import jade.util.Logger;

/**
 * Class declaration
 * @author Steffen Rusitschka - Siemens
 */
public class JICPConnection extends Connection {

	//#MIDP_EXCLUDE_BEGIN
	protected Socket       sc;
	//#MIDP_EXCLUDE_END
	/*#MIDP_INCLUDE_BEGIN
	protected StreamConnection sc;
	#MIDP_INCLUDE_END*/
	protected InputStream  is;
	protected OutputStream os;

	protected JICPConnection() {
	}

	public JICPConnection(TransportAddress ta) throws IOException {
		this(ta, 0);
	}

	/**
	 * Constructor declaration
	 */
	public JICPConnection(TransportAddress ta, int timeout) throws IOException {
		//#MIDP_EXCLUDE_BEGIN
		// For some reason the local address or port may be in use
		while (true) {
			try { 
				//#PJAVA_EXCLUDE_BEGIN
				sc = new Socket();
				sc.connect(new InetSocketAddress(ta.getHost(), Integer.parseInt(ta.getPort())), timeout);
				//#PJAVA_EXCLUDE_END
				/*#PJAVA_INCLUDE_BEGIN
				sc = new Socket(ta.getHost(), Integer.parseInt(ta.getPort()));
				#PJAVA_INCLUDE_END*/
				is = sc.getInputStream();
				os = getOutputStream();
				break;
			}
			catch (BindException be) {
				// Do nothing and try again
			}
		}
		//#MIDP_EXCLUDE_END

		/*#MIDP_INCLUDE_BEGIN
		String url = "socket://"+ta.getHost()+":"+ta.getPort();
		sc = (StreamConnection) Connector.open(url, Connector.READ_WRITE, false);
		is = sc.openInputStream();
		os = getOutputStream();
		#MIDP_INCLUDE_END*/
	}

	//#MIDP_EXCLUDE_BEGIN
	public void setReadTimeout(int timeout) throws IOException {
		if (sc != null) {
			sc.setSoTimeout(timeout);
		}
	}

	/**
	 * Constructor declaration
	 */
	public JICPConnection(Socket s) {
		sc = s;
	}
	//#MIDP_EXCLUDE_END

	public JICPPacket readPacket() throws IOException {
		if (sc != null) {
			if (is == null) {
				//#MIDP_EXCLUDE_BEGIN
				is = sc.getInputStream();
				//#MIDP_EXCLUDE_END
				/*#MIDP_INCLUDE_BEGIN
				is = sc.openInputStream();
				#MIDP_INCLUDE_END*/
			}
			return JICPPacket.readFrom(is);
		}
		else {
			throw new IOException("Connection closed");
		}
	}

	public int writePacket(JICPPacket pkt) throws IOException {
		if (sc != null) {
			if (os == null) {
				os = getOutputStream();
			}
			int ret = pkt.writeTo(os);
			os.flush();
			return ret;
		}
		else {
			throw new IOException("Connection closed");
		}
	}

	/**
	 */
	protected OutputStream getOutputStream() throws IOException {
		return new ByteArrayOutputStream() {
			private OutputStream realOs = null;

			public void flush() throws IOException {
				if (realOs == null) {
					//#MIDP_EXCLUDE_BEGIN
					realOs = sc.getOutputStream();
					//#MIDP_EXCLUDE_END
					/*#MIDP_INCLUDE_BEGIN
					realOs = sc.openOutputStream();
					#MIDP_INCLUDE_END*/
				}
				realOs.write(buf, 0, count);
				realOs.flush();
				reset();
			}

			public void close() throws IOException {
				super.close();
				if (realOs != null) {
					realOs.close();
					realOs = null;
				}
			}
		};
	} 

	/**
	 */
	public void close() throws IOException {
		IOException firstExc = null;
		if (is != null) {
			try {is.close();} catch(IOException e) {firstExc = e;}
			is = null;
		}
		if (os != null) {
			try {os.close();} catch(IOException e) {firstExc = (firstExc != null ? firstExc : e);}
			os = null;
		}
		if (sc != null) {
			try {sc.close();} catch(IOException e) {firstExc = (firstExc != null ? firstExc : e);}
			sc = null;
		}
		if (firstExc != null) {
			throw firstExc;
		}
	} 

	//#MIDP_EXCLUDE_BEGIN
	/**
	 */
	public String getRemoteHost() throws Exception {
		return sc.getInetAddress().getHostAddress();
	}
	//#MIDP_EXCLUDE_END
}


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
package jade.imtp.leap.http;

import jade.mtp.TransportAddress;
import jade.imtp.leap.JICP.Connection;
import jade.imtp.leap.JICP.JICPPacket;
import jade.imtp.leap.JICP.JICPProtocol;

import java.io.*;
//#MIDP_EXCLUDE_BEGIN
import java.net.*;
//#ANDROID_EXCLUDE_BEGIN
//#PJAVA_EXCLUDE_BEGIN
import javax.swing.*; // Needed to traverse an authenticated Proxy
//#PJAVA_EXCLUDE_END
//#ANDROID_EXCLUDE_END
//#MIDP_EXCLUDE_END
/*#MIDP_INCLUDE_BEGIN
import javax.microedition.io.*;
#MIDP_INCLUDE_END*/

/**
 * Class declaration
 * @author Giovanni Caire - TILAB
 */
class HTTPClientConnection extends Connection {
	private static final int READY = 0;
	private static final int WRITTEN = 1;
	private static final int CLOSED = -1;

	//#MIDP_EXCLUDE_BEGIN
	private HttpURLConnection hc;
	//#MIDP_EXCLUDE_END
	/*#MIDP_INCLUDE_BEGIN
    private HttpConnection hc;
    #MIDP_INCLUDE_END*/
	private String url;
	private InputStream is;
	private OutputStream os;
	private int state;

	/**
	 * Constructor declaration
	 */
	public HTTPClientConnection(TransportAddress ta) {
		url = getProtocol() + ta.getHost() + ":" + ta.getPort() + "/jade";
		state = READY;
		//#MIDP_EXCLUDE_BEGIN
		//#PJAVA_EXCLUDE_BEGIN
		// If the HTTP connection must go through an authenticated proxy
		// set the proper authenticator
		Authenticator.setDefault(new Authenticator() {
			private String username = null;
			private String password = null;

			protected PasswordAuthentication getPasswordAuthentication() {
				if (username == null) {
					// Try as system properties first
					username = System.getProperty("http.username");
					password = System.getProperty("http.password");
					if (username == null) {
						//#ANDROID_EXCLUDE_BEGIN
						JTextField usrTF = new JTextField();
						JPasswordField pwdTF = new JPasswordField();
						Object[] message = new Object[]{"Insert username and password", usrTF, pwdTF};
						int ret = JOptionPane.showConfirmDialog(null, message, null, JOptionPane.OK_CANCEL_OPTION);
						if (ret == 0) {
							username = usrTF.getText();
							password = pwdTF.getText();
						}
						//#ANDROID_EXCLUDE_END

						/*#ANDROID_INCLUDE_BEGIN
						username = "";
						password = "";
						#ANDROID_INCLUDE_END*/
					}
				}

				return new PasswordAuthentication(username, password.toCharArray());
			}
		});
		//#PJAVA_EXCLUDE_END
		//#MIDP_EXCLUDE_END
	}

	protected String getProtocol() {
		return "http://";
	}

	//#MIDP_EXCLUDE_BEGIN
	protected HttpURLConnection open(String url) throws MalformedURLException, IOException {
		return (HttpURLConnection) (new URL(url)).openConnection();
	}
	//#MIDP_EXCLUDE_END

	public int writePacket(JICPPacket pkt) throws IOException {
		if (state == READY) {
			int ret = 0;
			//#MIDP_EXCLUDE_BEGIN
			hc = open(url);
			hc.setDoOutput(true);
			hc.setRequestMethod("POST");
			hc.connect();
			os = hc.getOutputStream();
			ret = pkt.writeTo(os);
			//#MIDP_EXCLUDE_END

			/*#MIDP_INCLUDE_BEGIN
            hc = (HttpConnection) Connector.open(url, Connector.READ_WRITE, false);
            if (pkt.getType() == JICPProtocol.CONNECT_MEDIATOR_TYPE) {
            hc.setRequestMethod(HttpConnection.GET);
            hc.setRequestProperty(HTTPHelper.RECIPIENT_ID_FIELD, pkt.getRecipientID());
            }
            else {
            hc.setRequestMethod(HttpConnection.POST);
            os = hc.openOutputStream();
            ret = pkt.writeTo(os);
            }
            #MIDP_INCLUDE_END*/

			state = WRITTEN;
			return ret;
		} else {
			throw new IOException("Write not available");
		}
	}

	public JICPPacket readPacket() throws IOException {
		if (state == WRITTEN) {
			try {
				//#MIDP_EXCLUDE_BEGIN
				is = hc.getInputStream();
				//#MIDP_EXCLUDE_END
				/*#MIDP_INCLUDE_BEGIN
                is = hc.openInputStream();
                #MIDP_INCLUDE_END*/

				return JICPPacket.readFrom(is);
			} finally {
				try {
					close();
				} catch (Exception e) {
				}
			}
		} else {
			throw new IOException("Wrong connection state "+state);
		}
	}

	/**
	 */
	public void close() throws IOException {
		state = CLOSED;
		try {
			is.close();
		} catch (Exception e) {
		}
		is = null;
		try {
			os.close();
		} catch (Exception e) {
		}
		os = null;
		try {
			//#MIDP_EXCLUDE_BEGIN
			hc.disconnect();
			//#MIDP_EXCLUDE_END
			/*#MIDP_INCLUDE_BEGIN
            hc.close();
            #MIDP_INCLUDE_END*/
		} catch (Exception e) {
		}
		hc = null;
	}

	//#MIDP_EXCLUDE_BEGIN
	/**
	 */
	public String getRemoteHost() throws Exception {
		throw new Exception("Unsupported operation");
	}
	//#MIDP_EXCLUDE_END
}


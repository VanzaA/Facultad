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

package jade.core.nodeMonitoring;

//#APIDOC_EXCLUDE_FILE
// Take care that the DOTNET build file (dotnet.xml) uses this file (it is copied just after the preprocessor excluded it)
//#J2ME_EXCLUDE_FILE

import jade.core.Node;
import jade.util.Logger;

import java.io.IOException;
import java.nio.ByteBuffer;
//#DOTNET_EXCLUDE_BEGIN
import java.net.InetSocketAddress;
import java.nio.channels.DatagramChannel;

//#DOTNET_EXCLUDE_END

/*#DOTNET_INCLUDE_BEGIN
 import System.Net.*;
 import System.Net.Sockets.*;
 #DOTNET_INCLUDE_END*/

/**
 * The <code>UDPMonitorClient</code> sends UDP ping messages 
 * in a specified interval to the main container.
 *
 * @author Roland Mungenast - Profactor
 * @since JADE 3.3
 */
class UDPMonitorClient {

	private boolean running = false;
	private boolean terminating = false;
	private boolean sendTerminationFlag = false;

	//#DOTNET_EXCLUDE_BEGIN
	private DatagramChannel channel;

	//#DOTNET_EXCLUDE_END

	/*#DOTNET_INCLUDE_BEGIN
	 private UdpClient channel;
	 private IPEndPoint receivePoint;
	 #DOTNET_INCLUDE_END*/

	private String serverHost;
	private int serverPort;
	private ByteBuffer ping;
	private int pingDelay;
	private Node node;
	private long key;
	private Thread sender;

	private Logger logger;

	/**
	 * Private class which sends ping messages in regular time intervals
	 * 
	 * @author Roland Mungenast - Profactor
	 * @since JADE 3.3
	 * @author Federico Pieri - ERXA
	 * @since JADE 3.3.NET
	 */
	private class Sender implements Runnable {

		public void run() {

			while (running) {
				updatePing();
				//#DOTNET_EXCLUDE_BEGIN
				try {
					try {
						channel.send(ping, new InetSocketAddress(serverHost, serverPort));
					} 
					catch (IOException e) {
						logger.log(Logger.WARNING, "Error sending UDP ping message to "+serverHost+":"+serverPort+" for node " + node.getName());
					} 
					Thread.sleep(pingDelay - 5);
				}
				catch (InterruptedException e) {
					// ignore --> the ping with the termination flag has to be sent immediately
				}
				//#DOTNET_EXCLUDE_END
				/*#DOTNET_INCLUDE_BEGIN
				try {
				channel.Send(ping.getUByte(), ping.capacity(), serverHost, serverPort);
				Thread.sleep(pingDelay - 5);
				} 
				catch (Exception e) 
				{
				logger.log(Logger.WARNING,"Error sending UDP ping message to "+serverHost+":"+serverPort+" for node " + node.getName());
				}
				#DOTNET_INCLUDE_END*/
			}

			try {
				//#DOTNET_EXCLUDE_BEGIN
				channel.close();
			} catch (IOException e) {
				//#DOTNET_EXCLUDE_END
				/*#DOTNET_INCLUDE_BEGIN
				 channel.Close();
				 } 
				 catch (Exception e) 
				 {
				 #DOTNET_INCLUDE_END*/
				if (logger.isLoggable(Logger.FINER))
					logger.log(Logger.FINER, "Error closing UDP channel");
			}
		}

		private void updatePing() {
			String nodeName = node.getName();
			ping = ByteBuffer.allocate(4 + nodeName.length() + 1);
			ping.position(0);
			ping.putInt(nodeName.length());
			ping.put(nodeName.getBytes());

			if (terminating && sendTerminationFlag) {
				ping.put((byte) 1);
			} else {
				ping.put((byte) 0);
			}
			
			if (terminating) {
				running = false;
			}

			ping.position(0);
		}
	}

	/**
	 * Constructor
	 * @param node Node for which to send ping messages
	 * @param serverHost hostname of the server
	 * @param serverPort port on which the server is listening for ping messages
	 */
	public UDPMonitorClient(Node node, String serverHost, int serverPort, int pingDelay, long key) {
		logger = Logger.getMyLogger(this.getClass().getName());
		this.node = node;
		this.serverHost = serverHost;
		this.serverPort = serverPort;
		this.pingDelay = pingDelay;
		this.key = key;
	}

	public String getServerHost() {
		return serverHost;
	}
	
	public long getKey() {
		return key;
	}
	
	void setPingDelay(int delay) {
		pingDelay = delay;
		sender.interrupt();
	}
	
	/**
	 * Start sending UDP ping messages to the node failure server
	 * @throws IOException if the 
	 */
	public void start() throws IOException {
		//#DOTNET_EXCLUDE_BEGIN
		channel = DatagramChannel.open();
		//#DOTNET_EXCLUDE_END
		/*#DOTNET_INCLUDE_BEGIN
		 channel = new UdpClient();
		 #DOTNET_INCLUDE_END*/
		running = true;
		sender = new Thread(new Sender());
		sender.start();

		if (logger.isLoggable(Logger.CONFIG))
			logger.log(Logger.CONFIG, "UDP monitoring client started.");
	}

	/**
	 * Stop sending UDP ping messages
	 */
	public void stop(boolean sendTerminationFlag) {
		terminating = true;
		this.sendTerminationFlag = sendTerminationFlag;
		sender.interrupt();

		if (logger.isLoggable(Logger.CONFIG))
			logger.log(Logger.CONFIG, "UDP monitoring client stopped.");

	}
	
	boolean isActive() {
		return sender != null && sender.isAlive();
	}
}

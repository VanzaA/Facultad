/*****************************************************************
JADE - Java Agent DEvelopment Framework is a framework to develop 
multi-agent systems in compliance with the FIPA specifications.
Copyright (C) 2002 TILAB

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
package jade.security;

//#J2ME_EXCLUDE_FILE

import java.net.Authenticator;
import java.net.PasswordAuthentication;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class ThreadGroupHttpAuthenticator extends Authenticator {
	
	private static final String PROXY_KEY = "PROXY";
	private static final String SERVER_DEFAULT_KEY = "SERVER_DEAFULT";
	
	private static ThreadGroupHttpAuthenticator theInstance;
	private static Map<String, PasswordAuthentication> passwordAuthentications = new HashMap<String, PasswordAuthentication>();

	private ThreadGroupHttpAuthenticator() {
	}
	
	/**
	 * Get a JVM instance of ThreadGroupHttpAuthenticator  
	 * @return ThreadGroupHttpAuthenticator
	 */
	public final static ThreadGroupHttpAuthenticator getInstance() {
		if (theInstance == null) {
			theInstance = new ThreadGroupHttpAuthenticator(); 
			
			Authenticator.setDefault(theInstance);			
		}
		return theInstance;
	}
	
	/**
	 * Set the host of proxy
	 * 
	 * @param proxyHost proxy host
	 */
	public static void setProxyHost(String proxyHost) {
		System.setProperty("http.proxyHost", proxyHost);
		System.setProperty("https.proxyHost", proxyHost);
	}

	/**
	 * Set the port of proxy
	 * 
	 * @param proxyPort proxy port
	 */
	public static void setProxyPort(String proxyPort) {
		System.setProperty("http.proxyPort", proxyPort);
		System.setProperty("https.proxyPort", proxyPort);
	}

	/**
	 * Set the list of host excluded from proxy.
	 * Use <code>|</code> to separate hosts.
	 * Permitted <code>*</code> as wildcards. 
	 * 
	 * @param nonProxyHosts list of hosts
	 */
	public static void setNonProxyHosts(String nonProxyHosts) {
		System.setProperty("http.nonProxyHosts", nonProxyHosts);
		System.setProperty("https.nonProxyHosts", nonProxyHosts);
	}
	
	/**
	 * Set proxy authentication credential
	 * 
	 * @param proxyUser authentication proxy user
	 * @param proxyPassword authentication proxy password
	 */
	public void setProxyCredential(String proxyUser, String proxyPassword) {
		setCredential(PROXY_KEY, proxyUser, proxyPassword);
	}
	
	/**
	 * Reset all proxy settings
	 */
	public void resetProxy() {
		System.clearProperty("http.proxyHost");
		System.clearProperty("https.proxyHost");
		System.clearProperty("http.proxyPort");
		System.clearProperty("https.proxyPort");
		System.clearProperty("http.nonProxyHosts");
		System.clearProperty("https.nonProxyHosts");

		resetCredential(PROXY_KEY);
	}
	
	/**
	 * Set server authentication credential for a specific thread-group
	 * 
	 * @param threadGroupName name of thread-group 
	 * @param username authentication server user
	 * @param password authentication server password
	 */
	public void setServerCredential(String threadGroupName, String username, String password) {
		setCredential(threadGroupName, username, password);
	}

	/**
	 * Set server authentication credential for a specific thread-group
	 * 
	 * @param threadGroup thread-group 
	 * @param username authentication server user
	 * @param password authentication server password
	 */
	public void setServerCredential(ThreadGroup threadGroup, String username, String password) {
		if (threadGroup != null) {
			setCredential(threadGroup.getName(), username, password);
		} else {
			setCredential(null, username, password);
		}
	}

	/**
	 * Set server authentication default credential
	 * 
	 * @param threadGroup thread-group 
	 * @param username authentication server user
	 * @param password authentication server password
	 */
	public void setDefaultServerCredential(String username, String password) {
		setCredential(null, username, password);
	}
	
	private synchronized void setCredential(String threadGroupName, String username, String password) {
		if (threadGroupName == null) {
			threadGroupName = SERVER_DEFAULT_KEY;
		}
		
		if (username != null) {
			PasswordAuthentication passwordAuthentication = new PasswordAuthentication(username, password != null ? password.toCharArray() : null);
			passwordAuthentications.put(threadGroupName, passwordAuthentication);
		} else {
			resetCredential(threadGroupName);
		}
	}

	/**
	 * Reset the server credential for specific thread-group
	 * 
	 * @param threadGroupName name of thread-group 
	 */
	public void resetServerCredential(String threadGroupName) {
		resetCredential(threadGroupName);
	}

	/**
	 * Reset the server credential for specific thread-group
	 * 
	 * @param threadGroup thread-group 
	 */
	public void resetServerCredential(ThreadGroup threadGroup) {
		if (threadGroup != null) {
			resetCredential(threadGroup.getName());
		} else {
			resetCredential(null);
		}
	}
	
	/**
	 * Reset the default server credential
	 */
	public void resetDefaultServerCredential() {
		resetCredential(null);
	}

	/**
	 * Reset all server and proxy credentials
	 */
	public synchronized void resetAllCredentials() {
		Iterator<String> it = passwordAuthentications.keySet().iterator();
		while (it.hasNext()) {
			it.remove();
		}
	}
	
	private synchronized void resetCredential(String threadGroupName) {
		if (threadGroupName == null) {
			threadGroupName = SERVER_DEFAULT_KEY;
		}
		
		passwordAuthentications.remove(threadGroupName);
	}
	
	@Override
    protected PasswordAuthentication getPasswordAuthentication() {
		PasswordAuthentication pa;
		if (getRequestorType().equals(Authenticator.RequestorType.PROXY)) {
			pa = passwordAuthentications.get(PROXY_KEY);
		} else {
			String threadGroupName = Thread.currentThread().getThreadGroup().getName();
			pa = passwordAuthentications.get(threadGroupName);
	    	if (pa == null) {
	    		pa = passwordAuthentications.get(SERVER_DEFAULT_KEY);
	    	}
		}
    	return pa;
    }
}
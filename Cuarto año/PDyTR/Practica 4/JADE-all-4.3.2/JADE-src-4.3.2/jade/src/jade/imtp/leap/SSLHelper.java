/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package jade.imtp.leap;

//#J2ME_EXCLUDE_FILE

import jade.util.*;
import java.io.File;
import java.io.FileInputStream;
import java.security.KeyStore;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;

/**
 * Helper class to deal with SSL related setup
 * @author eduard
 */
public class SSLHelper {

	/**
	 * use this to indicate which cipher suites we support
	 */
	public static final List supportedKeys =
		Collections.unmodifiableList(Arrays.asList(new String[] {"SSL_DH_anon_WITH_RC4_128_MD5"}));

	public static String[] getSupportedKeys() {
		return (String[]) supportedKeys.toArray(new String[0]);
	}
	
	private static Logger logger = Logger.getJADELogger(SSLHelper.class.getName());

	private SSLHelper() {
	}

	/**
	 *
	 * @param keystore
	 * @return true when filename arguments can be read
	 */
	public static boolean needAuth(String keystore) {
		/* TODO FIXME
		 * now we only check if we can read filename
		 *
		 */
		return new File(keystore).canRead();
	}

	/**
	 * calls {@link #needAuth(java.lang.String) } with
	 * System.getProperty("javax.net.ssl.keyStore") as argument
	 * @return
	 */
	public static boolean needAuth() {
		return needAuth(System.getProperty("javax.net.ssl.keyStore"));
	}

	public static SSLContext createContext() throws ICPException {
		return createContext("keystore", "passphrase");
	}

	/**
	 *
	 * @param keystore will be used if javax.net.ssl.keyStore is not set
	 * @param passphrase will be used if javax.net.ssl.keyStorePassword is not set
	 * @return
	 * @throws ICPException
	 */
	public static SSLContext createContext(String keystore, String passphrase) throws ICPException {
		SSLContext ctx = null;
		// default parameters
		if (System.getProperty("javax.net.ssl.keyStore") == null) {
			System.setProperty("javax.net.ssl.keyStore", keystore);
		}
		if (System.getProperty("javax.net.ssl.keyStorePassword") == null) {
			System.setProperty("javax.net.ssl.keyStorePassword", passphrase);
		}

		// create and init context
		if (needAuth()) {
			if (logger.isLoggable(Logger.FINE)) {
				logger.log(Logger.FINE, "keyStore found!");
			}
			ctx = createContextWithAuth();
		} else {
			ctx = createContextNoAuth();
		}
		return ctx;
	} // end createContext

	/**
	 * creates a SSLContext without a keystore or truststore
	 * @return
	 * @throws ICPException
	 */
	public static SSLContext createContextNoAuth() throws ICPException {
		SSLContext ctx = null;
		// Create the SSLContext without authentication if necessary
		if (ctx == null) {
			try {
				ctx = SSLContext.getInstance("TLS");
				ctx.init(null, null, null);
			} catch (Exception e) {
				throw new ICPException("Error creating SSLContext.",e);
			}
		}
		return ctx;
	}// end createContextNoAuth

	/**
	 * creates a SSLContext with a keystore, no truststore is used
	 * @return
	 * @throws ICPException
	 */
	public static SSLContext createContextWithAuth() throws ICPException {
		// Create the SSLContext with Authentication
		SSLContext ctx = null;
		try {
			// open keystore
			char[] passphrase = System.getProperty("javax.net.ssl.keyStorePassword").toCharArray();
			KeyStore ks = KeyStore.getInstance("JKS");
			ks.load(new FileInputStream(System.getProperty("javax.net.ssl.keyStore")), passphrase);
			// init KeyManager
			KeyManagerFactory kmf = KeyManagerFactory.getInstance("SunX509");
			kmf.init(ks, passphrase);
			// create and init context
			ctx = SSLContext.getInstance("TLS");
			ctx.init(kmf.getKeyManagers(), null, null);
		} catch (Exception e) {
			throw new ICPException("Error creating SSLContext.",e);
		}
		return ctx;
	}// end createContextWithAuth
}

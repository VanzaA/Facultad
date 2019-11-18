/*****************************************************************
JADE - Java Agent DEvelopment Framework is a framework to develop
multi-agent systems in compliance with the FIPA specifications.
Copyright (C) 2000 CSELT S.p.A.

The updating of this file to JADE 2.0 has been partially supported by the
IST-1999-10211 LEAP Project

This file refers to parts of the FIPA 99/00 Agent Message Transport
Implementation Copyright (C) 2000, Laboratoire d'Intelligence
Artificielle, Ecole Polytechnique Federale de Lausanne

GNU Lesser General Public License

This library is free software; you can redistribute it sand/or
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

package jade.mtp.http.https;

//#PJAVA_EXCLUDE_FILE

import jade.core.Profile;

import java.net.Socket;
import java.security.Principal;
import java.security.PrivateKey;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;
import java.security.KeyStore;
import java.io.FileInputStream;
/**
 * HTTPSKeyManager that extracts its trust material from a given KeyStore.
 * This key manager needs some configuration parameters:
 * 
 * <b>jade_mtp_http_https_keyStoreType</b>: The type of KeyStore to use (defaults to JKS)
 * 
 * <b>jade_mtp_http_https_keyStoreFile</b>: The File Containing the KeyStore. This
 * keystore can only contain a key entry (Private Key + X509 Certificate). The
 * password encrypting this key must be the same password of the KeyStore. This
 * key material will be used to authenticate local platform when remote 
 * platforms will attempt to connect with.
 * 
 * <b>jade_mtp_http_https_keyStorePass</b>: The password for the key store 
 * indicated with the parameter described above.
 * 
 * @author <a href="mailto:Joan.Ametller@uab.es">Joan Ametller Esquerra</a>
 * 
 */
public class KeyStoreKeyManager implements HTTPSKeyManager {

	public void init(Profile profile) throws Exception {

		String pass = profile.getParameter(PREFIX+"keyStorePass", "");
		String keyfile = profile.getParameter(PREFIX + "keyStoreFile", "");
		String storetype = profile.getParameter(PREFIX + "keyStoreType", "JKS");
		KeyStore ks = KeyStore.getInstance(storetype);
		ks.load(new FileInputStream(keyfile),pass.toCharArray());
		alias = ((String)ks.aliases().nextElement());
		privateKey = (PrivateKey)ks.getKey(alias, pass.toCharArray());
		Certificate[] certs = ks.getCertificateChain(alias);
		cert = new X509Certificate[certs.length];
		for(int i=0;i<certs.length;i++) cert[i] = (X509Certificate)certs[i];
	}

	public PrivateKey getPrivateKey(String arg0) {
		return privateKey;
	}

	public X509Certificate[] getCertificateChain(String arg0) {
		return cert;
	}

	public String[] getClientAliases(String arg0, Principal[] arg1) {
		return new String[]{alias};
	}

	public String[] getServerAliases(String arg0, Principal[] arg1) {
		return getClientAliases(arg0,arg1);
	}

	public String chooseServerAlias(String arg0, Principal[] arg1, Socket arg2) {
		return alias;
	}

	public String chooseClientAlias(
			String[] arg0,
			Principal[] arg1,
			Socket arg2) {
		return alias;
	}

	private PrivateKey privateKey;
	private X509Certificate[] cert;
	private String alias;
	private static final String PREFIX     = "jade_mtp_http_https_";
}

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

import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import jade.core.Profile;
import javax.net.ssl.X509TrustManager;
import javax.net.ssl.TrustManagerFactory;
import java.security.KeyStore;
import java.io.FileInputStream;
import java.io.File;
/**
 * This Trust manager authenticates certificates issued by official Top level
 * Certification Authorities. The certificate list containing Certification Authorities
 * certificates is located at JAVA_HOME/jre/lib/security/cacerts. This file is a java
 * KeyStore file. Certificate list can be listed using the following command:
 * 
 * keytool -list -keystore JAVA_HOME/jre/lib/security/cacerts
 * 
 * where JAVA_HOME is the path where java SDK is installed.
 * 
 * @author <a href="mailto:Joan.Ametller@uab.es">Joan Ametller Esquerra</a>
 * 
 */
public class StrongAuthentication implements HTTPSTrustManager {

  public X509Certificate[] getAcceptedIssuers() {
    return _tm.getAcceptedIssuers();
  }

  public void checkClientTrusted(X509Certificate[] cert, String authType)
    throws CertificateException {  
    _tm.checkClientTrusted(cert, authType);   
  }

  public void checkServerTrusted(X509Certificate[] cert, String authType)
    throws CertificateException {
    _tm.checkServerTrusted(cert, authType);
  }
  
  public void init(Profile profile) throws Exception{
    KeyStore ks = KeyStore.getInstance("JKS");
    String s = File.separator;
    String javaHome = System.getProperty("java.home");
    String defaultTs = javaHome + s + "lib" + s + "security"+ s +"cacerts";
    ks.load(new FileInputStream(defaultTs), null);
    TrustManagerFactory tmf = 
      TrustManagerFactory.getInstance("SunX509");
    tmf.init(ks);
    _tm = (X509TrustManager)tmf.getTrustManagers()[0];
  }
  
  private X509TrustManager _tm;
}

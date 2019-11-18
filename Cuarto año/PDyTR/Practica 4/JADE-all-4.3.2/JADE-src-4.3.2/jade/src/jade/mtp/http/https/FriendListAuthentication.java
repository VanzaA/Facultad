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
 
package jade.mtp.http.https;

//#PJAVA_EXCLUDE_FILE


import jade.core.Profile;

import java.io.FileInputStream;
import java.security.KeyStore;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509TrustManager;

/**
 * This class implements a concrete behaviour for HTTPS Trust decisions. If 
 * we configure the platform to use this TrustManager, we must also specify
 * two configuration properties:
 * 
 * <b>jade_mtp_http_https_firendListFile</b>: This property must indicate
 * the location of a java KeyStore in the disc.
 * 
 * <b>jade_mtp_http_https_friendListFilePass</b> Password for this KeyStore.
 * 
 * The KeyStore must contain a list of Trusted Certificates. When a new HTTPS
 * connection is stablished, trust decisions will be made in terms of
 * the certificates inside the KeyStore. If the Certificate sent by the remote
 * peer is inside of the KeyStore or signed by one of the issuers of the 
 * inside the KeyStore, the connection will be stablished. Otherwise the 
 * connection will not be possible.
 * 
 * @author <a href="mailto:Joan.Ametller@uab.es">Joan Ametller Esquerra</a>
 * 
 */
public class FriendListAuthentication implements HTTPSTrustManager {

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
    String filename = profile.getParameter("jade_mtp_http_https_friendListFile", "");
    char[] pass = profile.getParameter("jade_mtp_http_https_friendListFilePass", "").toCharArray();
    ks.load(new FileInputStream(filename), null);
    TrustManagerFactory tmf = 
      TrustManagerFactory.getInstance("SunX509");
    tmf.init(ks);
    _tm = (X509TrustManager)tmf.getTrustManagers()[0];
  }
  
  private X509TrustManager _tm;

}

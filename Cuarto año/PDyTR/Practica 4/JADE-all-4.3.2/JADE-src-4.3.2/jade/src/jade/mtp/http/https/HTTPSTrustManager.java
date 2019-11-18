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

import javax.net.ssl.X509TrustManager;
/**
 * Marker interface for all trust managers used in HTTPS MTP
 * 
 * @author <a href="mailto:Joan.Ametller@uab.es">Joan Ametller Esquerra</a>
 * 
 */
public interface HTTPSTrustManager extends X509TrustManager{
  
  /**
   * This method is used to pass profile data to trust Managers. Specific parameters
   * can be retrieved from profile instance.
   * @param profile
   */
  void init(Profile profile) throws Exception;
}

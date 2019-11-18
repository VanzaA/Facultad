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
 * Copyright (C) 2001 Broadcom Eireann Research.
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

/**
 * @author Jamie Lawrence
 * @author Giovanni Caire - TILAB
 * @version 0.1
 */
import jade.mtp.TransportAddress;
import jade.core.Profile;

/**
 * Interface declaration
 * 
 * @author LEAP
 */
public interface ICP {

  /**
   * Callback interface to be notified of command arrivals over this
   * ICP.
   */
  public static interface Listener {

    /**
     * Handle a received (still serialized) command object, i.e. deserialize it
     * and launch processing of the command.
     * @param serialized_command the command to be deserialized and processed
     * @return a byte array containing the serialized response command
     * @exception LEAPSerializationException if an error occurs during the
     * LEAP surrogate serialization mechanism
     */
    byte[] handleCommand(byte[] cmdPayload) throws LEAPSerializationException;
  }    // End of Listener interface


  /**
   * Start listening for platform management commands
   */
  public TransportAddress activate(Listener l, String peerID, Profile p) throws ICPException;

  /**
   * Stop listening for platform management commands
   */
  public void deactivate() throws ICPException;

  /**
   * Deliver a command to the specified transport address
   */
  public byte[] deliverCommand(TransportAddress ta, byte[] payload, boolean requireFreshConnection) throws ICPException;

  /**
   * Returns the protocol supported by this ICP
   */
  public TransportProtocol getProtocol();
}


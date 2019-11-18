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

package jade.proto;

//#J2ME_EXCLUDE_FILE

import jade.proto.states.*;

/**
 * Interface description
 * @author Elena Quarantotto - TILAB
 * @author Giovanni Caire - TILAB
 */
public interface TwoPhConstants {
    public static final String JADE_TWO_PHASE_COMMIT = "Jade-Two-Phase-Commit";
    /* Possible TwoPh0Initiator's returned values */
    /* update1
    public static final int ALL_PROPOSE = 1;
    public static final int PH0_TIMEOUT_EXPIRED = MsgReceiver.TIMEOUT_EXPIRED;
    public static final int SOME_FAILURE = 2;
    */
    public static final int ALL_RESPONSES_RECEIVED = 1; // update1
    /* Possible TwoPh1Initiator's returned values */
    public static final int ALL_CONFIRM = 1;
    public static final int ALL_CONFIRM_OR_INFORM = 2;
    public static final int SOME_DISCONFIRM = 3;
    public static final int PH1_TIMEOUT_EXPIRED = MsgReceiver.TIMEOUT_EXPIRED;
    
    public static final String PH0 = "PH0";
    public static final String PH1 = "PH1";
    public static final String PH2 = "PH2";
}

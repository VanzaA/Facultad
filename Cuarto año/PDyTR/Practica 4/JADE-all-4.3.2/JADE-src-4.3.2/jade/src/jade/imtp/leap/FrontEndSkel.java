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

package jade.imtp.leap;

import jade.core.FrontEnd;
import jade.core.IMTPException;
import jade.core.NotFoundException;
import jade.lang.acl.ACLMessage;

/**
 * Class declaration
 * @author Giovanni Caire - TILAB
 */
public class FrontEndSkel extends MicroSkeleton {
	static final int CREATE_AGENT = 10;
	static final int KILL_AGENT = 11;
	static final int SUSPEND_AGENT = 12;
	static final int RESUME_AGENT = 13;
	static final int MESSAGE_IN = 14;
	static final int EXIT = 15;
	static final int SYNCH = 16;

	private FrontEnd myFrontEnd;

	public FrontEndSkel(FrontEnd fe) {
		myFrontEnd = fe;
	}

	/**
	   Call the method of the local FrontEnd corresponding to command <code>c</code>.
	 */
	Command executeCommand(Command c) throws Throwable {
		try {
			switch (c.getCode()) {
			case MESSAGE_IN:
				myFrontEnd.messageIn((ACLMessage) c.getParamAt(0), (String) c.getParamAt(1));
				break;
			case CREATE_AGENT:
				myFrontEnd.createAgent((String) c.getParamAt(0), (String) c.getParamAt(1), (String[]) c.getParamAt(2));
				break;
			case KILL_AGENT:
				myFrontEnd.killAgent((String) c.getParamAt(0));
				break;
			case SUSPEND_AGENT:
				myFrontEnd.suspendAgent((String) c.getParamAt(0));
				break;
			case RESUME_AGENT:
				myFrontEnd.resumeAgent((String) c.getParamAt(0));
				break;
			case EXIT:
				myFrontEnd.exit(((Boolean) c.getParamAt(0)).booleanValue());
				break;
			case SYNCH:
				myFrontEnd.synch();
				break;
			default:
				throw new IMTPException("Unsupported command "+c.getCode());
			}
			c.reset(Command.OK);
		}
		catch (NotFoundException nfe) {
			c = createErrorRsp(nfe, true);
		}
		catch (IMTPException imtpe) {
			c = createErrorRsp(imtpe, true);
		}

		return c;
	}
}


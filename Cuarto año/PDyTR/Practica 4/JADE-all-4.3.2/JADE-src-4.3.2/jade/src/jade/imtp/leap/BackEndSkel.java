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

//#MIDP_EXCLUDE_FILE

import jade.core.BackEnd;
import jade.core.IMTPException;
import jade.core.NotFoundException;
import jade.core.ServiceException;
import jade.lang.acl.ACLMessage;
import jade.util.Logger;
import jade.security.JADESecurityException;

/**
 * Class declaration
 * @author Giovanni Caire - TILAB
 */
public class BackEndSkel extends MicroSkeleton {

	private BackEnd myBackEnd;

	public BackEndSkel(BackEnd be) {
		myBackEnd = be;
	}

	/**
	   Call the method of the local BackEnd corresponding to command <code>c</code>.
	 */
	Command executeCommand(Command c) throws Throwable {
		switch (c.getCode()) {
		case BackEndStub.MESSAGE_OUT:
			try {
				myBackEnd.messageOut((ACLMessage) c.getParamAt(0), (String) c.getParamAt(1));
				c.reset(Command.OK);
			}
			catch (NotFoundException nfe) {
				c = createErrorRsp(nfe, true);
			}
			catch (IMTPException imtpe) {
				c = createErrorRsp(imtpe, true);
			}
			break;
		case BackEndStub.BORN_AGENT:
			try {
				String name = myBackEnd.bornAgent((String) c.getParamAt(0));
				c.reset(Command.OK);
				c.addParam(name);
			}
			catch (JADESecurityException ae) {
				c = createErrorRsp(ae, true);
			}
			catch (IMTPException imtpe) {
				c = createErrorRsp(imtpe, true);
			}
			break;
		case BackEndStub.DEAD_AGENT:
			try {
				myBackEnd.deadAgent((String) c.getParamAt(0));
				c.reset(Command.OK);
			}
			catch (IMTPException imtpe) {
				c = createErrorRsp(imtpe, true);
			}
			break;
		case BackEndStub.SUSPENDED_AGENT:
			try {
				myBackEnd.suspendedAgent((String) c.getParamAt(0));
				c.reset(Command.OK);
			}
			catch (NotFoundException nfe) {
				c = createErrorRsp(nfe, true);
			}
			catch (IMTPException imtpe) {
				c = createErrorRsp(imtpe, true);
			}
			break;
		case BackEndStub.RESUMED_AGENT:
			try {
				myBackEnd.resumedAgent((String) c.getParamAt(0));
				c.reset(Command.OK);
			}
			catch (NotFoundException nfe) {
				c = createErrorRsp(nfe, true);
			}
			catch (IMTPException imtpe) {
				c = createErrorRsp(imtpe, true);
			}
			break;
		case BackEndStub.SERVICE_INVOKATION:
			try {
				// Param at 0: actor
				// Param at 1: service name
				// Param at 2: helper method name 
				// Params from 3 to n: method arguments
				Object[] methodParams = new Object[c.getParamCnt()-3];
				for (int i = 0; i < methodParams.length; ++i) {
					methodParams[i] = c.getParamAt(i+3);
				}
				Object result = myBackEnd.serviceInvokation((String) c.getParamAt(0), (String) c.getParamAt(1), (String) c.getParamAt(2), methodParams);
				c.reset(Command.OK);
				c.addParam(result);
			}
			catch (NotFoundException nfe) {
				c = createErrorRsp(nfe, true);
			}
			catch (ServiceException se) {
				c = createErrorRsp(se, true);
			}
			catch (IMTPException imtpe) {
				c = createErrorRsp(imtpe, true);
			}
			break;
		default:
			throw new IMTPException("Unsupported command "+c.getCode());
		}

		return c;
	}
}


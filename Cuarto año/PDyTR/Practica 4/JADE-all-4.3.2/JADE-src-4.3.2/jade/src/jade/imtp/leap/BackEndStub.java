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

import jade.core.BackEnd;
import jade.core.FrontEnd;
import jade.core.IMTPException;
import jade.core.MicroRuntime;
import jade.core.NotFoundException;
import jade.core.ServiceException;
import jade.core.Specifier;
import jade.imtp.leap.JICP.JICPProtocol;
import jade.lang.acl.ACLMessage;
import jade.util.Logger;
import jade.util.leap.Properties;
import jade.security.JADESecurityException;
import java.util.Vector;

/**
 * Class declaration
 * @author Giovanni Caire - TILAB
 */
public class BackEndStub extends MicroStub implements BackEnd {
	static final int BORN_AGENT = 20;
	static final int DEAD_AGENT = 21;
	static final int SUSPENDED_AGENT = 22;
	static final int RESUMED_AGENT = 23;
	static final int MESSAGE_OUT = 24;
	static final int SERVICE_INVOKATION = 25;

	private long defaultMessageStoreAndForwardTimeout = -1; // INFINITE by default
	
	public BackEndStub(Dispatcher d, Properties props) {
		super(d);
		
		try {
		    String str = props.getProperty(MicroRuntime.DEFAULT_SF_TIMEOUT_KEY);
			defaultMessageStoreAndForwardTimeout = Long.parseLong(str!=null?str:"-1");
		}
		catch (Exception e) {
			// Keep default
		}
	}	

	/**
	 */
	public String bornAgent(String name) throws JADESecurityException, IMTPException {
		//Logger.println("Executing BORN_AGENT");
		Command c = new Command(BORN_AGENT);
		c.addParam(name);
		// The BORN_AGENT command must not be postponed
		Command r = executeRemotely(c, 0);
		if (r.getCode() == Command.ERROR) {
			// One of the expected exceptions occurred in the remote BackEnd
			// --> It must be an JADESecurityException --> throw it
			throw new JADESecurityException((String) r.getParamAt(2));
		}
		if (r.getParamCnt() > 0) {
			return (String) r.getParamAt(0);
		}
		else {
			return null;
		}
	}

	/**
	 */
	public void deadAgent(String name) throws IMTPException {
		//Logger.println("Executing DEAD_AGENT");
		Command c = new Command(DEAD_AGENT);
		c.addParam(name);
		executeRemotely(c, -1);
	}

	/**
	 */
	public void suspendedAgent(String name) throws NotFoundException, IMTPException {
		//Logger.println("Executing SUSPENDED_AGENT");
		Command c = new Command(SUSPENDED_AGENT);
		c.addParam(name);
		Command r = executeRemotely(c, -1);
		if (r != null && r.getCode() == Command.ERROR) {
			// One of the expected exceptions occurred in the remote BackEnd
			// --> It must be a NotFoundException --> throw it
			throw new NotFoundException((String) r.getParamAt(2));
		}
	}

	/**
	 */
	public void resumedAgent(String name) throws NotFoundException, IMTPException {
		//Logger.println("Executing RESUMED_AGENT");
		Command c = new Command(RESUMED_AGENT);
		c.addParam(name);
		Command r = executeRemotely(c, -1);
		if (r != null && r.getCode() == Command.ERROR) {
			// One of the expected exceptions occurred in the remote BackEnd
			// --> It must be a NotFoundException --> throw it
			throw new NotFoundException((String) r.getParamAt(2));
		}
	}

	/**
	 */
	public void messageOut(ACLMessage msg, String sender) throws NotFoundException, IMTPException {
		//Logger.println("Executing MESSAGE_OUT");
		Command c = new Command(MESSAGE_OUT);
		c.addParam(msg);
		c.addParam(sender);
				
		long timeout = defaultMessageStoreAndForwardTimeout;
		String messageSFTimeout = msg.getUserDefinedParameter(ACLMessage.SF_TIMEOUT);
		if (messageSFTimeout != null) {
			try {
				timeout = Long.parseLong(messageSFTimeout);
			}
			catch (Exception e) {
				// Keep default
			}
		}
		Command r = executeRemotely(c, timeout);
		if (r != null && r.getCode() == Command.ERROR) {
			// One of the expected exceptions occurred in the remote BackEnd
			// --> It must be a NotFoundException --> throw it
			throw new NotFoundException((String) r.getParamAt(2));
		}
	}

	/**
	 */
	public Object serviceInvokation(String actor, String serviceName, String methodName, Object[] methodParams) throws NotFoundException, ServiceException, IMTPException {
		//Logger.println("Executing SERVICE_METHOD");
		Command c = new Command(SERVICE_INVOKATION);
		c.addParam(actor);
		c.addParam(serviceName);
		c.addParam(methodName);
		if (methodParams != null) {
			for (int i = 0; i < methodParams.length; ++i) {
				c.addParam(methodParams[i]);
			}
		}
		Command r = executeRemotely(c, 0);
		if (r != null && r.getCode() == Command.ERROR) {
			// One of the expected exceptions occurred in the remote BackEnd --> throw it
			if (((String) r.getParamAt(1)).equals("jade.core.NotFoundException")) {
				throw new NotFoundException((String) r.getParamAt(2));
			}
			if (((String) r.getParamAt(1)).equals("jade.core.ServiceException")) {
				throw new ServiceException((String) r.getParamAt(2));
			}
		}
		if (r.getParamCnt() > 0) {
			return r.getParamAt(0);
		}
		else {
			return null;
		}
	}

	protected void handlePostponedCommandExpired(Command c, ICPException exception) {
		// If this was a MESSAGE_OUT, send back a failure to the sender
		if (c.getCode() == MESSAGE_OUT) {
			ACLMessage msg = (ACLMessage) c.getParamAt(0);
			String sender = (String) c.getParamAt(1);
			String cause = exception != null ? ". Caused by - "+exception.getMessage() : "";
			// If a code in the form ...[n]... is specified in the message, cut any message part after
			// that code (this may be due to a nested exception)
			int kOp = cause.indexOf('[');
			int kCl = cause.indexOf(']');
			if (kOp >= 0 && kCl == kOp+2) {
				cause = cause.substring(0, kCl+1);
			}
			MicroRuntime.notifyFailureToSender(msg, sender, "Cannot deliver message in due time"+cause);
		}
	}
	
	public static final void parseCreateMediatorResponse(String responseMessage, Properties pp) {
		Vector v = Specifier.parseList(responseMessage, '#');
		for (int i = 0; i < v.size(); ++i) {
			String s = (String) v.elementAt(i);
			if(s.length()>0){
				try {
					int index = s.indexOf('=');
					String key = s.substring(0, index);
					String value = s.substring(index+1);
					pp.setProperty(key, value);
				}
				catch (Exception e) {
					Logger.println("Property format error: "+s);
					e.printStackTrace();
				}
				String mediatorId = pp.getProperty(JICPProtocol.MEDIATOR_ID_KEY);
				if (mediatorId != null) {
					pp.setProperty(MicroRuntime.CONTAINER_NAME_KEY, mediatorId);
				}
			}
		}
	}
	/**
	 * The method encodes the create mediator request, setting all the common properties 
	 * retrived in the passed property parameter.
	 * @param pp 
	 * @return a StringBuffer to allow the dispatcher to add dispatcher specific properties.
	 */
	public static final StringBuffer encodeCreateMediatorRequest(Properties pp){
		StringBuffer sb = new StringBuffer();
		appendProp(sb, JICPProtocol.MEDIATOR_CLASS_KEY,pp.getProperty(JICPProtocol.MEDIATOR_CLASS_KEY));
		appendProp(sb, JICPProtocol.MAX_DISCONNECTION_TIME_KEY, pp.getProperty(JICPProtocol.MAX_DISCONNECTION_TIME_KEY));
		appendProp(sb, FrontEnd.REMOTE_BACK_END_ADDRESSES, pp.getProperty(FrontEnd.REMOTE_BACK_END_ADDRESSES));
		appendProp(sb, MicroRuntime.OWNER_KEY, pp.getProperty(MicroRuntime.OWNER_KEY));
		appendProp(sb, MicroRuntime.AGENTS_KEY, pp.getProperty(MicroRuntime.AGENTS_KEY));
		appendProp(sb, MicroRuntime.BE_REQUIRED_SERVICES_KEY, pp.getProperty(MicroRuntime.BE_REQUIRED_SERVICES_KEY));
		appendProp(sb, JICPProtocol.KEEP_ALIVE_TIME_KEY, pp.getProperty(JICPProtocol.KEEP_ALIVE_TIME_KEY));
		appendProp(sb, MicroRuntime.PLATFORM_KEY, pp.getProperty(MicroRuntime.PLATFORM_KEY));
		appendProp(sb, JICPProtocol.MSISDN_KEY, pp.getProperty(JICPProtocol.MSISDN_KEY));
		appendProp(sb, JICPProtocol.VERSION_KEY, pp.getProperty(JICPProtocol.VERSION_KEY));
		appendProp(sb, JICPProtocol.GET_SERVER_TIME_KEY, pp.getProperty(JICPProtocol.GET_SERVER_TIME_KEY));
		return sb;
	}

	public static void appendProp(StringBuffer sb, String key, String val) {
		if ((val != null)&&(val.length()!=0)) {
			sb.append(key);
			sb.append('=');
			sb.append(val);
			sb.append('#');
		}
	}
}


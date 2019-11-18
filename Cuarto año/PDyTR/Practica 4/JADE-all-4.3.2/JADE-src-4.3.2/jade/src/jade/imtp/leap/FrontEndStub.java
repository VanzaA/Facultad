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

//#J2ME_EXCLUDE_FILE

import jade.core.FrontEnd;
import jade.core.IMTPException;
import jade.core.NotFoundException;
import jade.core.PostponedException;
import jade.core.Profile;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.imtp.leap.JICP.JICPProtocol;
import jade.util.leap.List;
import jade.util.leap.ArrayList;
import jade.util.leap.Iterator;
import jade.util.leap.Properties;
import jade.core.MicroRuntime;
import java.util.Enumeration;
import java.util.StringTokenizer;

/**
 * Class declaration
 * @author Giovanni Caire - TILAB
 */
public class FrontEndStub extends MicroStub implements FrontEnd {

	public FrontEndStub(Dispatcher d) {
		super(d);
	}	

	/**
	 */
	public void createAgent(String name, String className, String[] args) throws IMTPException {
		Command c = new Command(FrontEndSkel.CREATE_AGENT);
		c.addParam(name);
		c.addParam(className);
		c.addParam(args);
		// The CREATE_AGENT command must not be postponed  	
		executeRemotely(c, 0);
	}

	/**
	 */
	public void killAgent(String name) throws NotFoundException, IMTPException {
		Command c = new Command(FrontEndSkel.KILL_AGENT);
		c.addParam(name);
		Command r = executeRemotely(c, -1);
		if (r != null) {
			if (r.getCode() == Command.ERROR) {
				// One of the expected exceptions occurred in the remote FrontEnd
				// --> It must be a NotFoundException --> throw it
				throw new NotFoundException((String) r.getParamAt(2));
			}
		}
		else {
			throw new PostponedException();
		}
	}

	/**
	 */
	public void suspendAgent(String name) throws NotFoundException, IMTPException {
		Command c = new Command(FrontEndSkel.SUSPEND_AGENT);
		c.addParam(name);
		Command r = executeRemotely(c, -1);
		if (r != null) {
			if (r.getCode() == Command.ERROR) {
				// One of the expected exceptions occurred in the remote FrontEnd
				// --> It must be a NotFoundException --> throw it
				throw new NotFoundException((String) r.getParamAt(2));
			}
		}
		else {
			throw new PostponedException();
		}	
	}

	/**
	 */
	public void resumeAgent(String name) throws NotFoundException, IMTPException {
		Command c = new Command(FrontEndSkel.RESUME_AGENT);
		c.addParam(name);
		Command r = executeRemotely(c, -1);
		if (r != null) {
			if (r.getCode() == Command.ERROR) {
				// One of the expected exceptions occurred in the remote FrontEnd
				// --> It must be a NotFoundException --> throw it
				throw new NotFoundException((String) r.getParamAt(2));
			}
		}
		else {
			throw new PostponedException();
		}	
	}

	/**
	 */
	public void messageIn(ACLMessage msg, String receiver) throws NotFoundException, IMTPException {
		Command c = new Command(FrontEndSkel.MESSAGE_IN);
		c.addParam(msg);
		c.addParam(receiver);
		Command r = executeRemotely(c, -1);
		// We don't even throw PostponedException here since that 
		// is completely transparent to the rest of the platform. 
		if (r != null && r.getCode() == Command.ERROR) {
			// One of the expected exceptions occurred in the remote FrontEnd
			// --> It must be a NotFoundException --> throw it
			throw new NotFoundException((String) r.getParamAt(2));
		}
	}

	/**
	 */
	public void exit(boolean self) throws IMTPException {
		Command c = new Command(FrontEndSkel.EXIT);
		c.addParam(new Boolean(self));
		// The EXIT command must not be postponed
		executeRemotely(c, 0);
	}

	/**
	 */
	public void synch() throws IMTPException {
		Command c = new Command(FrontEndSkel.SYNCH);
		// The SYNCH command must not be postponed
		executeRemotely(c, 0);
	}

	public List removePendingMessages(MessageTemplate template) {
		synchronized (pendingCommands) {
			List messages = new ArrayList();
			List commands = new ArrayList();
			Enumeration e = pendingCommands.elements();
			while (e.hasMoreElements()) {
				PostponedCommand pc = (PostponedCommand) e.nextElement();
				Command c = pc.getCommand();
				if (c.getCode() == FrontEndSkel.MESSAGE_IN) {
					ACLMessage msg = (ACLMessage) c.getParamAt(0);
					if (template.match(msg)) {
						Object[] oo = new Object[]{msg, c.getParamAt(1)};
						messages.add(oo);
						commands.add(c);
					}
				}
			}
			// Remove all the commands carrying matching messages
			Iterator it = commands.iterator();
			while (it.hasNext()) {
				pendingCommands.remove(it.next());
			}	

			// Return the list of matching messages
			return messages; 
		}
	}

	public static final String encodeCreateMediatorResponse(Properties pp) {
		StringBuffer sb = new StringBuffer();
		appendProp(sb, Profile.PLATFORM_ID, pp);
		appendProp(sb, MicroRuntime.PLATFORM_ADDRESSES_KEY, pp);
		appendProp(sb, JICPProtocol.MEDIATOR_ID_KEY, pp);
		appendProp(sb, JICPProtocol.LOCAL_HOST_KEY, pp);
		appendProp(sb, Profile.AGENTS, pp, false);
		return sb.toString();
	}

	public static final String encodeProperties(Properties pp) {
		StringBuffer sb = new StringBuffer();
		Enumeration en = pp.keys();
		while (en.hasMoreElements()) {
			String key = (String) en.nextElement();
			appendProp(sb, key, pp, en.hasMoreElements());
		}
		return sb.toString();
	}
	
	private static final void appendProp(StringBuffer sb, String key, Properties pp) {
		appendProp(sb, key, pp, true);
	}

	private static final void appendProp(StringBuffer sb, String key, Properties pp, boolean appendHash) {
		Object val = pp.get(key);
		if (val != null) {
			String strVal = val.toString();
			if(strVal.length() > 0){
				sb.append(key);
				sb.append('=');
				sb.append(val);
				if (appendHash) {
					sb.append('#');
				}
			}
		}
	}

	public static final Properties parseCreateMediatorRequest(String s) throws ICPException {
		StringTokenizer st = new StringTokenizer(s, "=#");
		Properties p = new Properties();
		while (st.hasMoreTokens()) {
			String key = st.nextToken();
			if (!st.hasMoreTokens()) {
				throw new ICPException("Wrong initialization properties format.");
			}
			p.setProperty(key, st.nextToken());
		}
		return p;
	}
}


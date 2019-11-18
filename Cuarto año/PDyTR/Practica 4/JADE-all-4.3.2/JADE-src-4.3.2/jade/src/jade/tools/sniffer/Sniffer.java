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

package jade.tools.sniffer;


import java.io.StringReader;

import java.util.Map;
import java.util.TreeMap;
import jade.util.leap.Iterator;
import jade.util.leap.List;
import jade.util.leap.ArrayList;
import jade.util.Logger;

import java.util.LinkedList;
import java.util.Hashtable;
import java.util.Set;
import java.util.HashSet;

import jade.core.*;
import jade.core.behaviours.*;

import jade.domain.FIPANames;
import jade.domain.JADEAgentManagement.*;
import jade.domain.introspection.*;
import jade.domain.FIPAService;
import jade.domain.FIPAAgentManagement.Envelope;

import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.lang.acl.ACLCodec;
import jade.lang.acl.StringACLCodec;

import jade.content.lang.sl.SLCodec;

import jade.content.AgentAction;
import jade.content.onto.basic.Action;
import jade.content.onto.basic.Done;

import jade.proto.SimpleAchieveREResponder;
import jade.proto.SimpleAchieveREInitiator;

import jade.tools.ToolAgent;

import jade.util.ExtendedProperties;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.*;
import java.io.*;


/**
 * This is the <em>Sniffer</em> agent.
 * <br>
 * This class implements the low level part of the Sniffer, interacting with Jade
 * environment and with the sniffer GUI.<br>
 * At startup, the sniffer subscribes itself as an rma to be informed every time
 * an agent is born or dead, a container is created or deleted.
 * <br>
 * For more information see <a href="../../../../intro.htm" target="_top">Introduction to the Sniffer</a>.
 * <p>
 * A properties file while may be used to control different sniffer properties.
 * These optional properties are as follows:
 * <ul>
 * <li>preload - A list of preload descriptions seperated by a semi-colon. Each description
 * consists of an agent name match string and optional list of performatives each seperated by a space.
 * For details on the agent name match string, see the method isMatch().
 * If there is no @ in the agent name, it assumes the current HAP for it.
 * If the performative list is not present, then the sniffer will display all messages;
 * otherwise, only those messages that have a matching performative mentioned will be displayed.
 * <br>
 * Examples:
 * <pre>
 * preload=da0;da1 inform propose
 * preload=agent?? inform
 * preload=*
 * </pre>
 * <li>clip - A list of agent name prefixes seperated by a semi-colon which will be removed when
 * showing the agent's name in the agent box. This is helpful to eliminate common agent prefixes.
 * <br>
 * Example:
 * <pre>
 * clip=com.hp.palo-alto.;helper.
 * </pre>
 * </ul>
 * The property file is looked for in the current directory, and if not found, it looks in
 * the parent directory and continues this until the file is either found or there isn't a parent
 * directory.
 * <p>
 * The original implementation processed a .inf file. For backward compatability this has
 * been preserved but its usage should be converted to use the new .properties file. The format
 * of the .inf file is each line contains an agent name and optional list of performatives.
 * <br>
 * Example:
 * <pre>
 * da0
 * da1 inform propose
 * </pre>
 * <p>
 * Notes:
 * <ol>
 * <li>If a message is one that is to be ignored, then it is dropped totally.  If you
 *   look at the sniffer dump of messages, it will not be there.  Might want to change
 *   this.
 * <li>Should develop a GUI to allow dynamically setting which messages are filtered instead
 *   of forcing them to be in the properties file.
 * <li>Probably should allow one to turn on and off the display of the performative name.
 *   Although, it seems pretty nice to have this information and although one might
 *   consider that it clutters the display, it sure provides a lot of information with
 *   it.
 * </ol>
 *
 * @author <a href="mailto:alessandro.beneventi@re.nettuno.it"> Alessandro Beneventi </a>(Developement)
 * @author Gianluca Tanca (Concept & Early Version)
 * @author Robert Kessler University of Utah (preload configuration, don't scroll agent boxes)
 * @author Martin Griss HP Labs (display additional message information)
 * @author Dick Cowan HP Labs (property handling, display full agent name when mouse over)
 * @version $Date: 2010-06-11 15:32:31 +0200 (ven, 11 giu 2010) $ $Revision: 6352 $
 *
 */
public class Sniffer extends ToolAgent {

	public static final boolean SNIFF_ON = true;
	public static final boolean SNIFF_OFF = false;

	private Set allAgents = null;
	private Hashtable preload = null;
	private ExtendedProperties properties = null;

	private ArrayList agentsUnderSniff = new ArrayList();


	// Sends requests to the AMS
	private class AMSClientBehaviour extends SimpleAchieveREInitiator {

		private String actionName;

		public AMSClientBehaviour(String an, ACLMessage request) {
			super(Sniffer.this, request);
			actionName = an;
		}

		protected void handleNotUnderstood(ACLMessage reply) {
			myGUI.showError("NOT-UNDERSTOOD received during " + actionName);
		}

		protected void handleRefuse(ACLMessage reply) {
			myGUI.showError("REFUSE received during " + actionName);
		}

		protected void handleAgree(ACLMessage reply) {
			if(logger.isLoggable(Logger.FINE))
				logger.log(Logger.FINE,"AGREE received");
		}

		protected void handleFailure(ACLMessage reply) {
			myGUI.showError("FAILURE received during " + actionName);
		}

		protected void handleInform(ACLMessage reply) {
			if(logger.isLoggable(Logger.FINE))
				logger.log(Logger.FINE,"INFORM received");
		}

	} // End of AMSClientBehaviour class


	private class SniffListenerBehaviour extends CyclicBehaviour {

		private MessageTemplate listenSniffTemplate;

		SniffListenerBehaviour() {
			listenSniffTemplate = MessageTemplate.MatchConversationId(getName() + "-event");
		}

		public void action() {

			ACLMessage current = receive(listenSniffTemplate);
			if(current != null) {

				try {
					Occurred o = (Occurred)getContentManager().extractContent(current);
					EventRecord er = o.getWhat();
					Event ev = er.getWhat();
					String content = null;
					Envelope env = null;
					AID unicastReceiver = null;
					if(ev instanceof SentMessage) { 
						content = ((SentMessage)ev).getMessage().getPayload();
						env = ((SentMessage)ev).getMessage().getEnvelope();
						unicastReceiver = ((SentMessage)ev).getReceiver();
					} else if(ev instanceof PostedMessage) {
						content = ((PostedMessage)ev).getMessage().getPayload();
						env = ((PostedMessage)ev).getMessage().getEnvelope();
						unicastReceiver = ((PostedMessage)ev).getReceiver();
						AID sender = ((PostedMessage)ev).getSender();
						// If the sender is currently under sniff, then the message was already
						// displayed when the 'sent-message' event occurred --> just skip this message.
						if(agentsUnderSniff.contains(new Agent(sender))) {
							return;
						}
					} else {
						return;
					}

					ACLCodec codec = new StringACLCodec();
					String charset = null;  
					if ((env == null) || ((charset = env.getPayloadEncoding()) == null)) {
						charset = ACLCodec.DEFAULT_CHARSET;
					}
					ACLMessage tmp = codec.decode(content.getBytes(charset),charset);
					tmp.setEnvelope(env);
					Message msg = new Message(tmp, unicastReceiver);

					/* If this is a 'posted-message' event and the sender is
					// currently under sniff, then the message was already
					// displayed when the 'sent-message' event occurred. In that
					// case, we simply skip this message.
					if(ev instanceof PostedMessage) {
						Agent a = new Agent(msg.getSender());
						if(agentsUnderSniff.contains(a))
							return;
					}*/


					// If the message that we just got is one that should be filtered out
					// then drop it.  WARNING - this means that the log file
					// that the sniffer might dump does not include the message!!!!
					boolean filters [];
					String agentName = msg.getSender().getName();
					String key = preloadContains(agentName);
					if (key != null) {
						filters = (boolean[])preload.get(key);
						if ((msg.getPerformative() >= 0) && filters[msg.getPerformative()]) {
							myGUI.mainPanel.panelcan.canvMess.recMessage(msg);
						}
					} else {
						myGUI.mainPanel.panelcan.canvMess.recMessage(msg);
					}
				}
				catch(Throwable e) {
					//System.out.println("Serious problem Occurred");
					myGUI.showError("An error occurred parsing the incoming message.\n" +
					"          The message was lost.");
					if(logger.isLoggable(Logger.WARNING))
						logger.log(Logger.WARNING,"The sniffer lost the following message because of a parsing error:"+current);
					e.printStackTrace();
				}
			}
			else
				block();
		}

	} // End of SniffListenerBehaviour

	//
	//
	//    * Search keys in preload for a string which matches (using isMatch method)
	//     * the agent name.
	//     * @param agentName The agent name.
	//     * @return String The key which matched.
	//
	protected String preloadContains(String agentName) {
		for (Enumeration enumeration = preload.keys(); enumeration.hasMoreElements() ;) {
			String key = (String)enumeration.nextElement();
			if (isMatch(key, agentName)) {
				return key;
			}
		}
		return null;
	}

	/**
	 * Given two strings determine if they match. We iterate over the match expression
	 * string from left to right as follows:
	 * <ol>
	 * <li> If we encounter a '*' in the expression token they match.
	 * <li> If there aren't any more characters in the subject string token they don't match.
	 * <li> If we encounter a '?' in the expression token we ignore the subject string's
	 * character and move on to the next iteration.
	 * <li> If the character in the expression token isn't equal to the character in
	 * the subject string they don't match.
	 * </ol>
	 * If we complete the iteration they match only if there are the same number of
	 * characters in both strings.
	 * @param aMatchExpression An expression string with special significance to '?' and '*'.
	 * @param aString The subject string.
	 * @return True if they match, false otherwise.
	 */
	protected boolean isMatch(String aMatchExpression, String aString)
	{
		int expressionLength = aMatchExpression.length();
		for (int i = 0; i < expressionLength; i++)
		{
			char expChar = aMatchExpression.charAt(i);
			if (expChar == '*')
				return true;   // * matches the remainder of anything
			if (i == aString.length())
				return false;  // if we run out of characters they don't match
			if (expChar == '?')
				continue;      // ? matches any single character so keep going
			if (expChar != aString.charAt(i))
				return false;  // if non wild then must be exactly equal
		}
		return (expressionLength == aString.length());
	}

	private SequentialBehaviour AMSSubscribe = new SequentialBehaviour();

	/**
    @serial
	 */
	private MainWindow myGUI;

	/**
    @serial
	 */
	private String myContainerName;

	class SnifferAMSListenerBehaviour extends AMSListenerBehaviour {

		protected void installHandlers(Map handlersTable) {


			// Fill the event handler table.

			handlersTable.put(IntrospectionVocabulary.META_RESETEVENTS, new EventHandler() {
				public void handle(Event ev) {
					ResetEvents re = (ResetEvents)ev;
					myGUI.resetTree();
					allAgents.clear();
				}
			});

			handlersTable.put(IntrospectionVocabulary.ADDEDCONTAINER, new EventHandler() {
				public void handle(Event ev) {
					AddedContainer ac = (AddedContainer)ev;
					ContainerID cid = ac.getContainer();
					String name = cid.getName();
					String address = cid.getAddress();
					try {
						InetAddress addr = InetAddress.getByName(address);
						myGUI.addContainer(name, addr);
					}
					catch(UnknownHostException uhe) {
						myGUI.addContainer(name, null);
					}
				}
			});

			handlersTable.put(IntrospectionVocabulary.REMOVEDCONTAINER, new EventHandler() {
				public void handle(Event ev) {
					RemovedContainer rc = (RemovedContainer)ev;
					ContainerID cid = rc.getContainer();
					String name = cid.getName();
					myGUI.removeContainer(name);
				}
			});

			handlersTable.put(IntrospectionVocabulary.BORNAGENT, new EventHandler() {
				public void handle(Event ev) {
					BornAgent ba = (BornAgent)ev;
					ContainerID cid = ba.getWhere();
					String container = cid.getName();
					AID agent = ba.getAgent();
					myGUI.addAgent(container, agent);
					allAgents.add(agent);
					if(agent.equals(getAID()))
						myContainerName = container;
					// Here we check to see if the agent is one that we automatically will
					// start sniffing.  If so, we invoke DoSnifferAction's doSniff and start
					// the sniffing process.
					// Avoid sniffing myself to avoid infinite recursion
					if (!agent.equals(getAID())) {
						if (preloadContains(agent.getName()) != null) {
							ActionProcessor ap = myGUI.actPro;
							DoSnifferAction sa = (DoSnifferAction)ap.actions.get(ap.DO_SNIFFER_ACTION);
							sa.doSniff(agent.getName());
						} 
					}
				}
			});

			handlersTable.put(IntrospectionVocabulary.DEADAGENT, new EventHandler() {
				public void handle(Event ev) {
					DeadAgent da = (DeadAgent)ev;
					ContainerID cid = da.getWhere();
					String container = cid.getName();
					AID agent = da.getAgent();
					myGUI.removeAgent(container, agent);
					allAgents.remove(agent);

				}
			});

			handlersTable.put(IntrospectionVocabulary.MOVEDAGENT, new EventHandler() {
				public void handle(Event ev) {
					MovedAgent ma = (MovedAgent)ev;
					AID agent = ma.getAgent();
					ContainerID from = ma.getFrom();
					myGUI.removeAgent(from.getName(), agent);
					ContainerID to = ma.getTo();
					myGUI.addAgent(to.getName(), agent);
				}
			});

		}
	}	// END of inner class SnifferAMSListenerBehaviour

	/**
	 * ACLMessages for subscription and unsubscription as <em>rma</em> are created and
	 * corresponding behaviours are set up.
	 */
	public void toolSetup() {

		properties = new ExtendedProperties();

		/*
		 * preload agents from argument array if arguments present, otherwise load
		 * sniffer.properties file.
		 */
		Object[] arguments = getArguments();
		if ( (arguments != null) && ( arguments.length > 0 ))
		{
			String  s = "";
			for( int i=0; i < arguments.length; ++i ) {
				s += arguments[i].toString()+' ';
			}
			properties.setProperty("preload", s );

		} else {
			String fileName = locateFile("sniffer.properties");
			if (fileName != null) {
				try {
					properties.load(new FileInputStream(fileName));
				} catch (IOException ioe) {
					// ignore - Properties not processed
				}
			} else {
				// This is only being done for backward compatability.
				fileName = locateFile("sniffer.inf");
				if (fileName != null) {
					loadSnifferConfigurationFile(fileName, properties);
				}
			}
		}

		allAgents = new HashSet();
		preload = new Hashtable();

		String preloadDescriptions = properties.getProperty("preload", null);

		if (preloadDescriptions != null) {
			StringTokenizer parser = new StringTokenizer(preloadDescriptions, ";");
			while (parser.hasMoreElements()) {
				parsePreloadDescription(parser.nextToken());
			}
		}

		//#DOTNET_EXCLUDE_BEGIN
		// Send 'subscribe' message to the AMS
		AMSSubscribe.addSubBehaviour(new SenderBehaviour(this, getSubscribe()));

		// Handle incoming 'inform' messages
		AMSSubscribe.addSubBehaviour(new SnifferAMSListenerBehaviour());

		// Handle incoming REQUEST to start/stop sniffing agents
		addBehaviour(new RequestListenerBehaviour());

		// Schedule Behaviours for execution
		addBehaviour(AMSSubscribe);
		addBehaviour(new SniffListenerBehaviour());

		// Show Graphical User Interface
		myGUI = new MainWindow(this, properties);
		myGUI.ShowCorrect();
		//#DOTNET_EXCLUDE_END

		/*#DOTNET_INCLUDE_BEGIN
    //Create GUI
	System.Threading.ThreadStart tStart = new System.Threading.ThreadStart( createUI );
	System.Threading.Thread t = new System.Threading.Thread( tStart );
	t.Start();
	#DOTNET_INCLUDE_END*/
	}

	/*#DOTNET_INCLUDE_BEGIN
  	private void createUI()
	{
		// Show Graphical User Interface
		myGUI = new MainWindow(this, properties);
		myGUI.ShowCorrect();
		System.Windows.Forms.Application.Run();
	}

	protected void startBehaviours()
	{
		// Send 'subscribe' message to the AMS
		AMSSubscribe.addSubBehaviour(new SenderBehaviour(this, getSubscribe()));

		// Handle incoming 'inform' messages
		AMSSubscribe.addSubBehaviour(new SnifferAMSListenerBehaviour());

		// Handle incoming REQUEST to start/stop sniffing agents
		addBehaviour(new RequestListenerBehaviour());

		// Schedule Behaviours for execution
		addBehaviour(AMSSubscribe);
		addBehaviour(new SniffListenerBehaviour());
	}
  #DOTNET_INCLUDE_END*/


	private void addAgent(AID id) 
	{
		ActionProcessor ap = myGUI.actPro;
		DoSnifferAction sa = (DoSnifferAction)ap.actions.get(ap.DO_SNIFFER_ACTION);
		sa.doSniff(id.getName());
	}

	private void removeAgent(AID id) {
		ActionProcessor ap = myGUI.actPro;
		DoNotSnifferAction nsa = (DoNotSnifferAction)ap.actions.get(ap.DO_NOT_SNIFFER_ACTION);
		nsa.doNotSniff(id.getName());
	}


	/**
	 * Private function to read configuration file containing names of agents to be
	 * preloaded. Also supports message filtering based on performatives.
	 * Each line of the file lists an agent, optionally followed
	 * by a set of messages to sniff.  If there are no tokens, then we assume
	 * that means to sniff all.  The tokens are the name of the performative type
	 * such as INFORM, QUERY, etc.
	 * @deprecated Use sniffer.properties file instead.
	 */
	private void loadSnifferConfigurationFile (String aFileName, ExtendedProperties theProperties) {
		StringBuffer sb = new StringBuffer();
		BufferedReader in = null;
		try {
			in = new BufferedReader(new FileReader(aFileName));
			boolean eof = false;
			while (!eof) {
				String line = in.readLine();
				eof = (line == null);
				if (!eof) {
					line = line.trim();
					if (line.length() > 0) {
						sb.append(line);
						sb.append(";");
					}
				}
			}
		} catch(Exception e) {
			// ignore
		}
		if (in != null) {
			try {
				in.close();
			} catch (IOException ee) {
				// ignore
			}
		}
		if (sb.length() > 0) {
			theProperties.setProperty("preload", sb.toString());
		}
	}

	private String locateFile(String aFileName) {
		try {
			String path = (new File(".")).getAbsolutePath();
			while (path != null) {
				path = path.replace('\\','/');
				if (path.endsWith(".")) {
					path = path.substring(0, path.length() - 1);    // drop dot
				}

				if (path.endsWith("/")) {
					path = path.substring(0, path.length() - 1);    // drop last separator
				}
				File dir = new File(path);
				File theFile = new File(dir, aFileName);
				if (theFile.exists()) {
					return theFile.getCanonicalPath();
				} else {
					path = dir.getParent();    // reduce the path by one
				}
			}
		} catch (Exception any) {
			// ignore
		}
		return null;
	}


	private void parsePreloadDescription(String aDescription) {
		StringTokenizer st = new StringTokenizer(aDescription);
		String name = st.nextToken();
		if (!name.endsWith("*")) {
			int atPos = name.lastIndexOf('@');
			if(atPos == -1) {
				name = name + "@" + getHap();
			}
		}

		int performativeCount = ACLMessage.getAllPerformativeNames().length;
		boolean[] filter = new boolean[performativeCount];
		boolean initVal = (st.hasMoreTokens() ? false : true);
		for (int i=0; i<performativeCount; i++) {
			filter[i] = initVal;
		}
		while (st.hasMoreTokens())
		{
			int perfIndex = ACLMessage.getInteger(st.nextToken());
			if (perfIndex != -1) {
				filter[perfIndex] = true;
			}
		}
		preload.put(name, filter);
	}

	/**
	 * Cleanup during agent shutdown. This method cleans things up when
	 * <em>Sniffer</em> agent is destroyed, disconnecting from <em>AMS</em>
	 * agent and closing down the Sniffer administration <em>GUI</em>.
	 * Currently sniffed agents are also unsniffed to avoid errors.
	 */
	protected void toolTakeDown() {

		List l = (List)(agentsUnderSniff.clone());
		ACLMessage request = getSniffMsg(l, SNIFF_OFF);

		// Start a FIPARequestProtocol to sniffOff all the agents since
		// the sniffer is shutting down
		try {
			if(request != null)
				FIPAService.doFipaRequestClient(this,request);
		}
		catch(jade.domain.FIPAException e) {
			// When the AMS replies the tool notifier is no longer registered.
			// But we don't care as we are exiting
			//System.out.println(e.getMessage());
		}

		myGUI.mainPanel.panelcan.canvMess.ml.removeAllMessages();

		// Now we unsubscribe from the rma list
		send(getCancel());
		// myGUI.setVisible(false); Not needed. Can cause thread deadlock.
		myGUI.disposeAsync();

	}

	/**
	 * This method adds an AMSClientBehaviour that performs a request to the AMS for sniffing/unsniffing list of agents.
	 **/
	public void sniffMsg(List agents, boolean onFlag) {
		ACLMessage request = getSniffMsg(agents,onFlag);
		if (request != null)
			addBehaviour(new AMSClientBehaviour((onFlag?"SniffAgentOn":"SniffAgentOff"),request));

	}
	/**
	 * Creates the ACLMessage to be sent to the <em>Ams</em> with the list of the
	 * agents to be sniffed/unsniffed. The internal list of sniffed agents is also
	 * updated.
	 *
	 * @param agentVect vector containing TreeData item representing the agents
	 * @param onFlag can be:<ul>
	 *			  <li> Sniffer.SNIFF_ON  to activate sniffer on an agent/group
	 *			  <li> Sniffer.SNIFF_OFF to deactivate sniffer on an agent/group
	 *		         </ul>
	 */
	public ACLMessage getSniffMsg(List agents, boolean onFlag) {

		Iterator it = agents.iterator();

		if(onFlag) {
			SniffOn so = new SniffOn();
			so.setSniffer(getAID());
			boolean empty = true;
			while(it.hasNext()) {
				Agent a = (Agent)it.next();
				AID agentID = new AID();
				agentID.setName(a.agentName + '@' + getHap());
				if(!agentsUnderSniff.contains(a)) {
					agentsUnderSniff.add(a);
					so.addSniffedAgents(agentID);
					empty = false;
				}
			}
			if(!empty) {
				try {
					Action a = new Action();
					a.setActor(getAMS());
					a.setAction(so);

					ACLMessage requestMsg = getRequest();
					requestMsg.setOntology(JADEManagementOntology.NAME);
					getContentManager().fillContent(requestMsg, a);
					return requestMsg;
				}
				catch(Exception fe) {
					fe.printStackTrace();
				}
			}
		}

		else {
			SniffOff so = new SniffOff();
			so.setSniffer(getAID());
			boolean empty = true;
			while(it.hasNext()) {
				Agent a = (Agent)it.next();
				AID agentID = new AID();
				agentID.setName(a.agentName + '@' + getHap());
				if(agentsUnderSniff.contains(a)) {
					agentsUnderSniff.remove(a);
					so.addSniffedAgents(agentID);
					empty = false;
				}
			}
			if(!empty) {
				try {
					Action a = new Action();
					a.setActor(getAMS());
					a.setAction(so);

					ACLMessage requestMsg = getRequest();
					requestMsg.setOntology(JADEManagementOntology.NAME);
					getContentManager().fillContent(requestMsg, a);
					requestMsg.setReplyWith(getName()+ (new Date().getTime()));
					return requestMsg;
				}
				catch(Exception fe) {
					fe.printStackTrace();
				}
			}
		}
		return null;
	}


	/**
     Inner class RequestListenerBehaviour.
     This behaviour serves requests to start sniffing agents.
     If an agent does not exist it is put into the 
     preload table so that it will be sniffed as soon as it starts.
	 */
	private class RequestListenerBehaviour extends SimpleAchieveREResponder {  	
		private Action requestAction;
		private AgentAction aa;

		RequestListenerBehaviour() {
			// We serve REQUEST messages refering to the JADE Management Ontology
			super(Sniffer.this, MessageTemplate.and(MessageTemplate.MatchPerformative(ACLMessage.REQUEST),MessageTemplate.MatchOntology(JADEManagementOntology.NAME)));
		}

		protected ACLMessage prepareResponse (ACLMessage request) {
			ACLMessage response = request.createReply();
			try {			
				requestAction = (Action) getContentManager().extractContent(request);
				aa = (AgentAction) requestAction.getAction();
				if (aa instanceof SniffOn || aa instanceof SniffOff) {
					if (getAID().equals(requestAction.getActor())) {
						response.setPerformative(ACLMessage.AGREE);
						response.setContent(request.getContent());
					}
					else {
						response.setPerformative(ACLMessage.REFUSE);
						response.setContent("((unrecognised-parameter-value actor "+requestAction.getActor()+"))");
					}
				}
				else {
					response.setPerformative(ACLMessage.REFUSE);	
					response.setContent("((unsupported-act "+aa.getClass().getName()+"))");
				}
			} 
			catch (Exception e) {
				e.printStackTrace();
				response.setPerformative(ACLMessage.NOT_UNDERSTOOD);	
			}
			return response;					    		    		 	
		}

		protected ACLMessage prepareResultNotification(ACLMessage request, ACLMessage response) {	
			if (aa instanceof SniffOn) { 
				// SNIFF ON
				SniffOn requestSniffOn = (SniffOn) aa;
				// Start sniffing existing agents.
				// Put non existing agents in the preload map. We will start
				// sniffing them as soon as they start. 
				List agentsToSniff = requestSniffOn.getCloneOfSniffedAgents();											
				for (int i=0;i<agentsToSniff.size();i++) {
					AID aid = (AID)agentsToSniff.get(i); 
					if (allAgents.contains(aid)) {
						addAgent(aid);
					} 
					else {
						//not alive -> put it into preload
						int performativeCount = ACLMessage.getAllPerformativeNames().length;
						boolean[] filter = new boolean[performativeCount];
						for (int j=0; j<performativeCount;j++) {
							filter[j] = true;
						}
						preload.put(aid.getName(), filter);									
					}
				}																		
			}
			else {
				// SNIFF OFF
				SniffOff requestSniffOff = (SniffOff) aa;
				List agentsToSniff = requestSniffOff.getCloneOfSniffedAgents();											
				for (int i=0;i<agentsToSniff.size();i++) {
					AID aid = (AID)agentsToSniff.get(i); 
					removeAgent(aid);
				}
			}

			// Send back the notification
			ACLMessage result = request.createReply();
			result.setPerformative(ACLMessage.INFORM);
			Done d = new Done(requestAction);
			try {
				myAgent.getContentManager().fillContent(result, d);
			}
			catch (Exception e) {
				// Should never happen
				e.printStackTrace();
			}
			return result;
		}		
	}  // END of inner class RequestListenerBehaviour 
}

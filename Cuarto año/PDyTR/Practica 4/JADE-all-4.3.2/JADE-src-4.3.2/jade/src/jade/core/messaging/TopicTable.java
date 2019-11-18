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

package jade.core.messaging;

//#MIDP_EXCLUDE_FILE
//#APIDOC_EXCLUDE_FILE

import jade.core.AID;
import jade.lang.acl.ACLMessage;

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;
import java.util.Set;
import java.util.StringTokenizer;

/**
 * Topics can have the form x.y.z... and agents can register to "topic templates" like x.y.* meaning that they 
 * want to receive all messages concerning topics that start with x.y (e.g. x.y, x.y.z, x.y.t and so on).
 * Each '.' separated part of a topic is called a topic section.
 * Registrations of agents interest in topics are organized in a tree of TTNode-s (TopicTable Node). Each TTNode
 * corresponds to a topic section and represents a topic whose name is the full path of the TTNode in the tree.
 * Each TTNode includes 
 * - The list of agents interested in the TTNode topic
 * - The list of agents interested in the TTNode as a template 
 * - A list of child TTNode
 * @author Giovanni Caire - TILAB
 */
class TopicTable {
	private TTNode root = new TTNode("", null);
	
	/**
	 * Register the interest of an agent for a given topic
	 */
	final synchronized void register(AID aid, AID topic) {
		RegistrationInfo info = new RegistrationInfo(aid, topic);
		root.register(info); 
	}
		
	/**
	 * De-register the interest of an agent for a given topic
	 */
	final synchronized void deregister(AID aid, AID topic) {
		RegistrationInfo info = new RegistrationInfo(aid, topic);
		root.deregister(info);
	}
	
	/**
	 * Retrieve all agents that are interested in receiving a given message directed to a given topic
	 */
	final synchronized Collection getInterestedAgents(AID topic, ACLMessage msg) {
		RegistrationInfo info = new RegistrationInfo(null, topic);
		Set s = new HashSet();
		root.fillInterestedAgents(info, s);
		return s;
	}

	/**
	 * Retrieve the list of all TopicRegistration objects
	 */
	final synchronized List getAllRegistrations() {
		List l = new ArrayList();
		root.fillRegistrations(l);
		return l;
	}
	
	/**
	 * Retrieve the list of topics a given agent is interested in
	 */
	final synchronized List getRelevantTopics(AID aid) {
		List l = new ArrayList();
		root.fillRelevantTopics(aid, l);
		return l;
	}
	
	public String toString() {
		List l = getAllRegistrations();
		StringBuffer sb = new StringBuffer("TOPIC-TABLE\n");
		Iterator it = l.iterator();
		while (it.hasNext()) {
			TopicRegistration tr = (TopicRegistration) it.next();
			sb.append(tr).append('\n');
		}
		return sb.toString();
	}
	
	
	/**
	 * Inner class RegistrationInfo.
	 * This class includes the information related to a given registration properly 
	 * prepared to be registered in the TopicTable
	 */
	private class RegistrationInfo {
		private AID aid;
		private boolean template = false;
		private StringTokenizer st;
		
		private RegistrationInfo(AID aid, AID topic) {
			st = new StringTokenizer(topic.getLocalName(), ".");
			this.aid = aid;
		}
		
		/**
		 * If a topic has the form "foo.bar" returns "foo" at the first invocation, "bar" at the second and null at the third.
		 * If a topic has the form "foo.bar.*" returns "foo" at the first invocation, "bar" at the second, null at the third and isTemplate() will return true.
		 */
		private String nextName() {
			String name = null;
			if (st.hasMoreTokens()) {
				name = st.nextToken();
				if (name.equals(TopicManagementHelper.TOPIC_TEMPLATE_WILDCARD)) {
					template = true;
					name = null;
				}
			}
			return name;
		}
		
		private AID getAID() {
			return aid;
		}
		
		private boolean isTemplate() {
			return template;
		}
	} // END of inner class RegistrationInfo
	
	
	/**
	 * Inner class TTNode
	 */
	private class TTNode {
		private String name;
		private List interestedAgents = new LinkedList();
		private List templateInterestedAgents = new LinkedList();
		private Map children = new HashMap();
		private TTNode parent;
		
		private TTNode(String name, TTNode parent) {
			this.name = name;
			this.parent = parent;
		}
		
		private final void register(RegistrationInfo info) {
			String nextName = info.nextName();
			if (nextName != null) {
				TTNode childNode = (TTNode) children.get(nextName);
				if (childNode == null) {
					childNode = new TTNode(nextName, this);
					children.put(nextName, childNode);
				}
				childNode.register(info);
			}
			else {
				if (info.isTemplate()) {
					templateInterestedAgents.add(info.getAID());
				}
				else {
					interestedAgents.add(info.getAID());
				}
			}
		}
		
		private final void deregister(RegistrationInfo info) {
			String nextName = info.nextName();
			if (nextName != null) {
				TTNode childNode = (TTNode) children.get(nextName);
				if (childNode != null) {
					childNode.deregister(info);
				}
			}
			else {
				if (info.isTemplate()) {
					templateInterestedAgents.remove(info.getAID());
				}
				else {
					interestedAgents.remove(info.getAID());
				}
				// If after this de-registration this node is empty, remove it 	
				removeIfEmpty();
			}
		}
		
		private final void fillInterestedAgents(RegistrationInfo info, Collection interestedAgents) {
			// Add all agents interested in the topic represented by this node as a template
			interestedAgents.addAll(this.templateInterestedAgents);
			String nextName = info.nextName();
			if (nextName != null) {
				TTNode childNode = (TTNode) children.get(nextName);
				if (childNode != null) {
					childNode.fillInterestedAgents(info, interestedAgents);
				}
			}
			else {
				interestedAgents.addAll(this.interestedAgents);
			}
		}
		
		private final void fillRegistrations(List allRegistrations) {
			// Fill local registrations
			String topicName = getTopicName();
			if (interestedAgents.size() > 0) {
				AID topic = TopicUtility.createTopic(topicName);
				for (int i = 0; i < interestedAgents.size(); ++i) {
					AID aid = (AID) interestedAgents.get(i);
					allRegistrations.add(new TopicRegistration(aid, topic));
				}
			}
			if (templateInterestedAgents.size() > 0) {
				String templateTopicName = (topicName.length() > 0 ? topicName+'.'+TopicManagementHelper.TOPIC_TEMPLATE_WILDCARD : TopicManagementHelper.TOPIC_TEMPLATE_WILDCARD);
				AID topic = TopicUtility.createTopic(templateTopicName);
				for (int i = 0; i < templateInterestedAgents.size(); ++i) {
					AID aid = (AID) templateInterestedAgents.get(i);
					allRegistrations.add(new TopicRegistration(aid, topic));
				}
			}
			
			// Fill child nodes registrations
			Iterator it = children.values().iterator();
			while (it.hasNext()) {
				TTNode childNode = (TTNode) it.next();
				childNode.fillRegistrations(allRegistrations);
			}
		}
		
		private final void fillRelevantTopics(AID aid, List relevantTopics) {
			// Add the local topic (directly or as a template) if necessary
			String topicName = getTopicName();
			if (interestedAgents.contains(aid)) {
				relevantTopics.add(TopicUtility.createTopic(topicName));
			}
			if (templateInterestedAgents.contains(aid)) {
				String templateTopicName = (topicName.length() > 0 ? topicName+'.'+TopicManagementHelper.TOPIC_TEMPLATE_WILDCARD : TopicManagementHelper.TOPIC_TEMPLATE_WILDCARD);
				relevantTopics.add(TopicUtility.createTopic(templateTopicName));
			}
			
			// Add child nodes topics if necessary
			Iterator it = children.values().iterator();
			while (it.hasNext()) {
				TTNode childNode = (TTNode) it.next();
				childNode.fillRelevantTopics(aid, relevantTopics);
			}
		}
		
		private final void removeIfEmpty() {
			if (interestedAgents.isEmpty() && templateInterestedAgents.isEmpty() && children.isEmpty()) {
				if (parent != null) {
					parent.children.remove(name);
					if (parent != root) {
						parent.removeIfEmpty();
					}
				}
			}
		}
		
		private final String getTopicName() {
			if (this == root) {
				return "";
			}
			else if (parent == root) {
				return name;
			}
			else {
				return parent.getTopicName()+"."+name;
			}
		}
	} // END of inner class TTNode
}

/**
 * ***************************************************************
 * JADE - Java Agent DEvelopment Framework is a framework to develop
 * multi-agent systems in compliance with the FIPA specifications.
 * Copyright (C) 2000 CSELT S.p.A.
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
package jade.content.lang.sl;

/**
 * The vocabulary of the simbols used in the FIPA SL0 language
 * @author Giovanni Caire - TILAB
 */
public interface SL0Vocabulary {
  // Aggregate operators
  public static final String         SEQUENCE = "sequence";
  public static final String         SET = "set";
    
  // Generic concepts: AID and ACLMessage
  public static final String         AID = "agent-identifier";
  public static final String         AID_NAME = "name";
  public static final String         AID_ADDRESSES = "addresses";
  public static final String         AID_RESOLVERS = "resolvers";

  public static final String         ACLMSG = "fipa-acl-message";
  public static final String         ACLMSG_PERFORMATIVE = "performative";
  public static final String         ACLMSG_SENDER = "sender";
  public static final String         ACLMSG_RECEIVERS = "receivers";
  public static final String         ACLMSG_REPLY_TO = "reply-to";
  public static final String         ACLMSG_LANGUAGE = "language";
  public static final String         ACLMSG_ONTOLOGY = "ontology";
  public static final String         ACLMSG_PROTOCOL = "protocol";
  public static final String         ACLMSG_IN_REPLY_TO = "in-reply-to";
  public static final String         ACLMSG_REPLY_WITH = "reply-with";
  public static final String         ACLMSG_CONVERSATION_ID = "conversation-id";
  public static final String         ACLMSG_REPLY_BY = "reply-by";
  public static final String         ACLMSG_CONTENT = "content";
  public static final String         ACLMSG_BYTE_SEQUENCE_CONTENT = "bs-content";
  public static final String         ACLMSG_ENCODING = "encoding";
  
  // Generic propositions: 
  // TRUE_PROP (i.e. the proposition that is true under whatever condition) 
  // FALSE_PROP (i.e. the proposition that is false under whatever condition) 
  public static final String         TRUE_PROPOSITION = "true";
  public static final String         FALSE_PROPOSITION = "false";
  
  // Action operators 
  public static final String         DONE = "done";
  public static final String         DONE_ACTION = "action";
  public static final String         DONE_CONDITION = "condition";
    
  public static final String         RESULT = "result";
  public static final String         RESULT_ACTION = "action";
  public static final String         RESULT_VALUE = "value";
  /** @deprecated Use <code>RESULT_VALUE</code> instead */
  public static final String         RESULT_ITEMS = RESULT_VALUE;
    
  public static final String         ACTION = "action";
  public static final String         ACTION_ACTOR = "actor";
  public static final String         ACTION_ACTION = "action";
  
  public static final String         EQUALS = "=";
  public static final String         EQUALS_LEFT = "left";
  public static final String         EQUALS_RIGHT = "right";
}

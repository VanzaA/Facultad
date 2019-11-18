;/*****************************************************************
;JADE - Java Agent DEvelopment Framework is a framework to develop 
;multi-agent systems in ;compliance with the FIPA specifications.
;Copyright (C) 2000 CSELT S.p.A. 
;
;GNU Lesser General Public License
;
;This library is free software; you can redistribute it and/or
;modify it under the terms of the GNU Lesser General Public
;License as published by the Free Software Foundation, 
;version 2.1 of the License. 
;
;This library is distributed in the hope that it will be useful,
;but WITHOUT ANY WARRANTY; without even the implied warranty of
;MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
;Lesser General Public License for more details.
;
;You should have received a copy of the GNU Lesser General Public
;License along with this library; if not, write to the
;Free Software Foundation, Inc., 59 Temple Place - Suite 330,
;Boston, MA  02111-1307, USA.
;*****************************************************************/

;/**
;
;@author Fabio Bellifemine - CSELT S.p.A
;@version $Date: 2000-09-12 15:24:08 +0200 (mar, 12 set 2000) $ $Revision: 1857 $
;*/
;
; Remind that the ACLMessage has been defined with the following template:
; (deftemplate ACLMessage 
;              (slot communicative-act) (slot sender) (multislot receiver) 
;              (slot reply-with) (slot in-reply-to) (slot envelope) 
;              (slot conversation-id) (slot protocol) 
;              (slot language) (slot ontology) (slot content) 
;              (slot encoding) (multislot reply-to) (slot reply-by))
; refer to Fipa2000 (www.fipa.org) for the description of the 
; ACLMessage parameters.
;
; Remind that Jade has also asserted for you the fact 
; (MyAgent (name <agentname)) that is usefull to know the name of your agent
;
; Finally, remind that Jade has built a userfunction called send
; to send messages to other agents. There are two styles to call send:
; ?m <- (assert (ACLMessage (communicative-act inform) (receiver agent)))
; (send ?m)
; or, in alternative
; (send (assert (ACLMessage (communicative-act inform) (receiver agent))))
; The two following rules show the usage of both styles. One of the two
; rules can be used


(defrule proposal
 "When a 'cfp' message arrives from an agent ?s, this rule asserts a 
  'propose' message to the same sender and retract the just arrived message"
 ?m <- (ACLMessage (communicative-act CFP) (sender ?s) (content ?c) (receiver ?r))
 =>
; (send (assert (ACLMessage (communicative-act PROPOSE) (receiver ?s) (content ?c) )))
 (assert (ACLMessage (communicative-act PROPOSE) (sender ?r) (receiver ?s) (content ?c) ))
 (retract ?m)
)

(defrule send-a-message
 "When a message is asserted whose sender is this agent, the message is
  sent and then retracted from the knowledge base."
 (MyAgent (name ?n))
 ?m <- (ACLMessage (sender ?n))
 =>
 (send ?m)
 (retract ?m)
)

(watch facts)
(watch all)
(reset) 

(run)  
; if you put run here, Jess is run before waiting for a message arrival,
; if you do not put (run here, the agent waits before for the arrival of the 
; first message and then runs Jess.









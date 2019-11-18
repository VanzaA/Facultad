; Oliver Hoffmann 10 May 2001
; in collaboration with Fabio Bellifemine and Ernest Friedmann-Hill
; shows how the JADE system and a JADE/JESS agent can be started from within a running JESS engine
(call examples.JadeJessProtege.JessHashtable setRete "Jess" (engine)) ; save the pointer to this JESS engine
(call jade.Boot main (create$ "-gui" "Jess:JadeJessProtege.JadeJessAgent(Jess)")) ; start the JADE system and an agent, the agent will be "attached" to this JESS engine
(deftemplate ACLMessage ; a JADE ACL message mapped to a JESS template
  (slot Performative) ; the speech act
  (slot Sender) ; message sender
  (slot Content) ; message content
) ; if you want to use more message data to match on JESS rules, add more slots here
(defrule message ; make a JESS fact for every message the agent tells JESS about
  ?Message <- (Message ?message) ; the agent made this JESS fact for a message it received
  =>
  (assert  
    (ACLMessage ; a new Message fact with slots for relevant message data
      (Performative (call jade.lang.acl.ACLMessage getPerformative (?message getPerformative))) ; take the String describing the speech act
      (Sender (?message getSender)) ; map message sender
      (Content (?message getContent)) ; map message content
    )
  )
  (retract ?Message) ; retract the simple Message fact since it was only used to notify about the message and now we have the protege instance
)
(defrule proposal ; an example of what the agent could do with an incoming message
  (Agent ?agent) ; the agent started with the JADE system
  (ACLMessage ; a fact as specified by the ACLMessage template
    (Performative "CFP") ; meaning the message was a call for proposals
    (Sender ?sender) ; original sender
    (Content ?content) ; original content
  )
  =>
  (bind ?message (new jade.lang.acl.ACLMessage (get-member jade.lang.acl.ACLMessage PROPOSE))) ; make a new java PROPOSE ACLMessage object
  (?message setSender (?agent getAID)) ; take "my" agent as the sender
  (?message addReceiver ?sender) ; take the original sender as receiver
  (?message setContent ?content) ; just copy the original content
  (?agent send ?message) ; send a proposal back to the original sender which is a copy of the original content
) ; the ACLMessage fact is still here, you might want to retract it later
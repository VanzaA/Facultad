; Oliver Hoffmann 10 May 2001
; in collaboration with Fabio Bellifemine and Ernest Friedmann-Hill
; shows how a JADE system and a JADE/JESS/PROTEGE agent can be started from within the protege JessTab
(call examples.JadeJessProtege.JessHashtable setRete "JessProtege" (engine)) ; save the pointer to this JESS engine
(call jade.Boot main (create$ "-gui" "JessProtege:examples.JadeJessProtege.JadeJessAgent(JessProtege)")) ; start the JADE system and an agent
(defclass Message ; a JADE ACL message mapped to a protege instance
  (is-a :STANDARD-CLASS) ; class Message will inherit th standard slots
  (slot Performative) ; message speech act as a String
  (slot Sender (type any)) ; message sender, not a String, but a JADE AID
  (slot Content) ; message content as a String
)
(mapclass Message) ; make JESS facts for protege Message instances
(defrule message ; make a protege instance for every message the agent tells JESS about
  ?Message <- (Message ?message) ; the agent made this JESS fact for a message it received
  =>
  (make-instance of  Message ; a new protege instance of class Message
    (Performative (call jade.lang.acl.ACLMessage getPerformative (?message getPerformative))) ; take the String describing the speech act
    (Sender (?message getSender)) ; map the sender
    (Content (?message getContent)) ; map the content
  )
  (retract ?Message) ; retract the fact since it was only used to notify about the message and now we have the protege instance
)
(defrule proposal ; example for what the agent could do with a message
  (Agent ?agent) ; the agent started with the JADE system
  ?instance <- (object ) ; any protege instance would match here
  (test (eq (class ?instance) Message)) ; if the instance is a Message
  (test (eq (slot-get ?instance Performative) "CFP")) ; meaning the message is a call for proposals
  =>
  (bind ?message (new jade.lang.acl.ACLMessage (get-member jade.lang.acl.ACLMessage PROPOSE))) ; make a new java PROPOSE ACLMessage object
  (?message setSender (?agent getAID)) ; take "my" agent as the sender
  (?message addReceiver (slot-get ?instance Sender)) ; take the original sender as receiver
  (?message setContent (slot-get ?instance Content)) ; just copy the original content
  (?agent send ?message) ; send a proposal back to the original sender which is a copy of the original content
) ; the protege Message instance is still here so you can check it on the instances tab, but you might want to remove it with unmake-instance
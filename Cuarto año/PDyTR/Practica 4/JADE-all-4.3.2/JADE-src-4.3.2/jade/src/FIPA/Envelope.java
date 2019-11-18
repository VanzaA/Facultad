/*
 * File: ./FIPA/ENVELOPE.JAVA
 * From: FIPA.IDL
 * Date: Mon Sep 04 15:08:50 2000
 *   By: idltojava Java IDL 1.2 Nov 10 1997 13:52:11
 */

package FIPA;
public final class Envelope {
    //	instance variables
    public FIPA.AgentID[] to;
    public FIPA.AgentID[] from;
    public String comments;
    public String aclRepresentation;
    public int payloadLength;
    public String payloadEncoding;
    public FIPA.DateTime[] date;
    public String[] encrypted;
    public FIPA.AgentID[] intendedReceiver;
    public FIPA.ReceivedObject[] received;
    public FIPA.Property[][] transportBehaviour;
    public FIPA.Property[] userDefinedProperties;
    //	constructors
    public Envelope() { }
    public Envelope(FIPA.AgentID[] __to, FIPA.AgentID[] __from, String __comments, String __aclRepresentation, int __payloadLength, String __payloadEncoding, FIPA.DateTime[] __date, String[] __encrypted, FIPA.AgentID[] __intendedReceiver, FIPA.ReceivedObject[] __received, FIPA.Property[][] __transportBehaviour, FIPA.Property[] __userDefinedProperties) {
	to = __to;
	from = __from;
	comments = __comments;
	aclRepresentation = __aclRepresentation;
	payloadLength = __payloadLength;
	payloadEncoding = __payloadEncoding;
	date = __date;
	encrypted = __encrypted;
	intendedReceiver = __intendedReceiver;
	received = __received;
	transportBehaviour = __transportBehaviour;
	userDefinedProperties = __userDefinedProperties;
    }
}

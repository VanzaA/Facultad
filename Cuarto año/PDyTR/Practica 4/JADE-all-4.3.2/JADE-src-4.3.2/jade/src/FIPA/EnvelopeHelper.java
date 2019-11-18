/*
 * File: ./FIPA/ENVELOPEHELPER.JAVA
 * From: FIPA.IDL
 * Date: Mon Sep 04 15:08:50 2000
 *   By: idltojava Java IDL 1.2 Nov 10 1997 13:52:11
 */

package FIPA;
public class EnvelopeHelper {
     // It is useless to have instances of this class
     private EnvelopeHelper() { }

    public static void write(org.omg.CORBA.portable.OutputStream out, FIPA.Envelope that) {
	{
	    out.write_long(that.to.length);
	    for (int __index = 0 ; __index < that.to.length ; __index += 1) {
	        FIPA.AgentIDHelper.write(out, that.to[__index]);
	    }
	}
	{
	    if (that.from.length > (1L)) {
	        throw new org.omg.CORBA.MARSHAL(0, org.omg.CORBA.CompletionStatus.COMPLETED_MAYBE);
	    }
	    out.write_long(that.from.length);
	    for (int __index = 0 ; __index < that.from.length ; __index += 1) {
	        FIPA.AgentIDHelper.write(out, that.from[__index]);
	    }
	}
	out.write_string(that.comments);
	out.write_string(that.aclRepresentation);
	out.write_long(that.payloadLength);
	out.write_string(that.payloadEncoding);
	{
	    if (that.date.length > (1L)) {
	        throw new org.omg.CORBA.MARSHAL(0, org.omg.CORBA.CompletionStatus.COMPLETED_MAYBE);
	    }
	    out.write_long(that.date.length);
	    for (int __index = 0 ; __index < that.date.length ; __index += 1) {
	        FIPA.DateTimeHelper.write(out, that.date[__index]);
	    }
	}
	{
	    out.write_long(that.encrypted.length);
	    for (int __index = 0 ; __index < that.encrypted.length ; __index += 1) {
	        out.write_string(that.encrypted[__index]);
	    }
	}
	{
	    out.write_long(that.intendedReceiver.length);
	    for (int __index = 0 ; __index < that.intendedReceiver.length ; __index += 1) {
	        FIPA.AgentIDHelper.write(out, that.intendedReceiver[__index]);
	    }
	}
	{
	    if (that.received.length > (1L)) {
	        throw new org.omg.CORBA.MARSHAL(0, org.omg.CORBA.CompletionStatus.COMPLETED_MAYBE);
	    }
	    out.write_long(that.received.length);
	    for (int __index = 0 ; __index < that.received.length ; __index += 1) {
	        FIPA.ReceivedObjectHelper.write(out, that.received[__index]);
	    }
	}
	{
	    if (that.transportBehaviour.length > (1L)) {
	        throw new org.omg.CORBA.MARSHAL(0, org.omg.CORBA.CompletionStatus.COMPLETED_MAYBE);
	    }
	    out.write_long(that.transportBehaviour.length);
	    for (int __index = 0 ; __index < that.transportBehaviour.length ; __index += 1) {
	        out.write_long(that.transportBehaviour[__index].length);
	        for (int __index2 = 0 ; __index2 < that.transportBehaviour[__index].length ; __index2 += 1) {
	            FIPA.PropertyHelper.write(out, that.transportBehaviour[__index][__index2]);
	        }
	    }
	}
	{
	    out.write_long(that.userDefinedProperties.length);
	    for (int __index = 0 ; __index < that.userDefinedProperties.length ; __index += 1) {
	        FIPA.PropertyHelper.write(out, that.userDefinedProperties[__index]);
	    }
	}
    }
    public static FIPA.Envelope read(org.omg.CORBA.portable.InputStream in) {
        FIPA.Envelope that = new FIPA.Envelope();
	{
	    int __length = in.read_long();
	    that.to = new FIPA.AgentID[__length];
	    for (int __index = 0 ; __index < that.to.length ; __index += 1) {
	        that.to[__index] = FIPA.AgentIDHelper.read(in);
	    }
	}
	{
	    int __length = in.read_long();
	    if (__length > (1L)) {
	        throw new org.omg.CORBA.MARSHAL(0, org.omg.CORBA.CompletionStatus.COMPLETED_MAYBE);
	    }
	    that.from = new FIPA.AgentID[__length];
	    for (int __index = 0 ; __index < that.from.length ; __index += 1) {
	        that.from[__index] = FIPA.AgentIDHelper.read(in);
	    }
	}
	that.comments = in.read_string();
	that.aclRepresentation = in.read_string();
	that.payloadLength = in.read_long();
	that.payloadEncoding = in.read_string();
	{
	    int __length = in.read_long();
	    if (__length > (1L)) {
	        throw new org.omg.CORBA.MARSHAL(0, org.omg.CORBA.CompletionStatus.COMPLETED_MAYBE);
	    }
	    that.date = new FIPA.DateTime[__length];
	    for (int __index = 0 ; __index < that.date.length ; __index += 1) {
	        that.date[__index] = FIPA.DateTimeHelper.read(in);
	    }
	}
	{
	    int __length = in.read_long();
	    that.encrypted = new String[__length];
	    for (int __index = 0 ; __index < that.encrypted.length ; __index += 1) {
	        that.encrypted[__index] = in.read_string();
	    }
	}
	{
	    int __length = in.read_long();
	    that.intendedReceiver = new FIPA.AgentID[__length];
	    for (int __index = 0 ; __index < that.intendedReceiver.length ; __index += 1) {
	        that.intendedReceiver[__index] = FIPA.AgentIDHelper.read(in);
	    }
	}
	{
	    int __length = in.read_long();
	    if (__length > (1L)) {
	        throw new org.omg.CORBA.MARSHAL(0, org.omg.CORBA.CompletionStatus.COMPLETED_MAYBE);
	    }
	    that.received = new FIPA.ReceivedObject[__length];
	    for (int __index = 0 ; __index < that.received.length ; __index += 1) {
	        that.received[__index] = FIPA.ReceivedObjectHelper.read(in);
	    }
	}
	{
	    int __length = in.read_long();
	    if (__length > (1L)) {
	        throw new org.omg.CORBA.MARSHAL(0, org.omg.CORBA.CompletionStatus.COMPLETED_MAYBE);
	    }
	    that.transportBehaviour = new FIPA.Property[__length][];
	    for (int __index = 0 ; __index < that.transportBehaviour.length ; __index += 1) {
	        int __length2 = in.read_long();
	        that.transportBehaviour[__index] = new FIPA.Property[__length2];
	        for (int __index2 = 0 ; __index2 < that.transportBehaviour[__index].length ; __index2 += 1) {
	            that.transportBehaviour[__index][__index2] = FIPA.PropertyHelper.read(in);
	        }
	    }
	}
	{
	    int __length = in.read_long();
	    that.userDefinedProperties = new FIPA.Property[__length];
	    for (int __index = 0 ; __index < that.userDefinedProperties.length ; __index += 1) {
	        that.userDefinedProperties[__index] = FIPA.PropertyHelper.read(in);
	    }
	}
        return that;
    }
   public static FIPA.Envelope extract(org.omg.CORBA.Any a) {
     org.omg.CORBA.portable.InputStream in = a.create_input_stream();
     return read(in);
   }
   public static void insert(org.omg.CORBA.Any a, FIPA.Envelope that) {
     org.omg.CORBA.portable.OutputStream out = a.create_output_stream();
     write(out, that);
     a.read_value(out.create_input_stream(), type());
   }
   private static org.omg.CORBA.TypeCode _tc;
   synchronized public static org.omg.CORBA.TypeCode type() {
       int _memberCount = 12;
       org.omg.CORBA.StructMember[] _members = null;
          if (_tc == null) {
               _members= new org.omg.CORBA.StructMember[12];
               _members[0] = new org.omg.CORBA.StructMember(
                 "to",
                 org.omg.CORBA.ORB.init().create_sequence_tc(0, FIPA.AgentIDHelper.type()),
                 null);

               _members[1] = new org.omg.CORBA.StructMember(
                 "from",
                 org.omg.CORBA.ORB.init().create_sequence_tc((int) (1L), FIPA.AgentIDHelper.type()),
                 null);

               _members[2] = new org.omg.CORBA.StructMember(
                 "comments",
                 org.omg.CORBA.ORB.init().get_primitive_tc(org.omg.CORBA.TCKind.tk_string),
                 null);

               _members[3] = new org.omg.CORBA.StructMember(
                 "aclRepresentation",
                 org.omg.CORBA.ORB.init().get_primitive_tc(org.omg.CORBA.TCKind.tk_string),
                 null);

               _members[4] = new org.omg.CORBA.StructMember(
                 "payloadLength",
                 org.omg.CORBA.ORB.init().get_primitive_tc(org.omg.CORBA.TCKind.tk_long),
                 null);

               _members[5] = new org.omg.CORBA.StructMember(
                 "payloadEncoding",
                 org.omg.CORBA.ORB.init().get_primitive_tc(org.omg.CORBA.TCKind.tk_string),
                 null);

               _members[6] = new org.omg.CORBA.StructMember(
                 "date",
                 org.omg.CORBA.ORB.init().create_sequence_tc((int) (1L), FIPA.DateTimeHelper.type()),
                 null);

               _members[7] = new org.omg.CORBA.StructMember(
                 "encrypted",
                 org.omg.CORBA.ORB.init().create_sequence_tc(0, org.omg.CORBA.ORB.init().get_primitive_tc(org.omg.CORBA.TCKind.tk_string)),
                 null);

               _members[8] = new org.omg.CORBA.StructMember(
                 "intendedReceiver",
                 org.omg.CORBA.ORB.init().create_sequence_tc(0, FIPA.AgentIDHelper.type()),
                 null);

               _members[9] = new org.omg.CORBA.StructMember(
                 "received",
                 org.omg.CORBA.ORB.init().create_sequence_tc((int) (1L), FIPA.ReceivedObjectHelper.type()),
                 null);

               _members[10] = new org.omg.CORBA.StructMember(
                 "transportBehaviour",
                 org.omg.CORBA.ORB.init().create_sequence_tc((int) (1L), org.omg.CORBA.ORB.init().create_sequence_tc(0, FIPA.PropertyHelper.type())),
                 null);

               _members[11] = new org.omg.CORBA.StructMember(
                 "userDefinedProperties",
                 org.omg.CORBA.ORB.init().create_sequence_tc(0, FIPA.PropertyHelper.type()),
                 null);
             _tc = org.omg.CORBA.ORB.init().create_struct_tc(id(), "Envelope", _members);
          }
      return _tc;
   }
   public static String id() {
       return "IDL:FIPA/Envelope:1.0";
   }
}

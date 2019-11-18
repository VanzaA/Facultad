/*
 * File: ./FIPA/FIPAMESSAGEHELPER.JAVA
 * From: FIPA.IDL
 * Date: Mon Sep 04 15:08:50 2000
 *   By: idltojava Java IDL 1.2 Nov 10 1997 13:52:11
 */

package FIPA;
public class FipaMessageHelper {
     // It is useless to have instances of this class
     private FipaMessageHelper() { }

    public static void write(org.omg.CORBA.portable.OutputStream out, FIPA.FipaMessage that) {
	{
	    out.write_long(that.messageEnvelopes.length);
	    for (int __index = 0 ; __index < that.messageEnvelopes.length ; __index += 1) {
	        FIPA.EnvelopeHelper.write(out, that.messageEnvelopes[__index]);
	    }
	}
	{
	    out.write_long(that.messageBody.length);
	    out.write_octet_array(that.messageBody, 0, that.messageBody.length);
	}
    }
    public static FIPA.FipaMessage read(org.omg.CORBA.portable.InputStream in) {
        FIPA.FipaMessage that = new FIPA.FipaMessage();
	{
	    int __length = in.read_long();
	    that.messageEnvelopes = new FIPA.Envelope[__length];
	    for (int __index = 0 ; __index < that.messageEnvelopes.length ; __index += 1) {
	        that.messageEnvelopes[__index] = FIPA.EnvelopeHelper.read(in);
	    }
	}
	{
	    int __length = in.read_long();
	    that.messageBody = new byte[__length];
	    in.read_octet_array(that.messageBody, 0, that.messageBody.length);
	}
        return that;
    }
   public static FIPA.FipaMessage extract(org.omg.CORBA.Any a) {
     org.omg.CORBA.portable.InputStream in = a.create_input_stream();
     return read(in);
   }
   public static void insert(org.omg.CORBA.Any a, FIPA.FipaMessage that) {
     org.omg.CORBA.portable.OutputStream out = a.create_output_stream();
     write(out, that);
     a.read_value(out.create_input_stream(), type());
   }
   private static org.omg.CORBA.TypeCode _tc;
   synchronized public static org.omg.CORBA.TypeCode type() {
       int _memberCount = 2;
       org.omg.CORBA.StructMember[] _members = null;
          if (_tc == null) {
               _members= new org.omg.CORBA.StructMember[2];
               _members[0] = new org.omg.CORBA.StructMember(
                 "messageEnvelopes",
                 org.omg.CORBA.ORB.init().create_sequence_tc(0, FIPA.EnvelopeHelper.type()),
                 null);

               _members[1] = new org.omg.CORBA.StructMember(
                 "messageBody",
                 org.omg.CORBA.ORB.init().create_sequence_tc(0, org.omg.CORBA.ORB.init().get_primitive_tc(org.omg.CORBA.TCKind.tk_octet)),
                 null);
             _tc = org.omg.CORBA.ORB.init().create_struct_tc(id(), "FipaMessage", _members);
          }
      return _tc;
   }
   public static String id() {
       return "IDL:FIPA/FipaMessage:1.0";
   }
}

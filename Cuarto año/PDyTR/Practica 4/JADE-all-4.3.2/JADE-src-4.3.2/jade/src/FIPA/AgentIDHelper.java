/*
 * File: ./FIPA/AGENTIDHELPER.JAVA
 * From: FIPA.IDL
 * Date: Mon Sep 04 15:08:50 2000
 *   By: c:\Java\idltojava-win32\idltojava Java IDL 1.2 Nov 10 1997 13:52:11
 */

package FIPA;
public class AgentIDHelper {
     // It is useless to have instances of this class
     private AgentIDHelper() { }

    public static void write(org.omg.CORBA.portable.OutputStream out, FIPA.AgentID that) {
	out.write_string(that.name);
	{
	    out.write_long(that.addresses.length);
	    for (int __index = 0 ; __index < that.addresses.length ; __index += 1) {
	        out.write_string(that.addresses[__index]);
	    }
	}
	{
	    out.write_long(that.resolvers.length);
	    for (int __index = 0 ; __index < that.resolvers.length ; __index += 1) {
	        FIPA.AgentIDHelper.write(out, that.resolvers[__index]);
	    }
	}
	{
	    out.write_long(that.userDefinedProperties.length);
	    for (int __index = 0 ; __index < that.userDefinedProperties.length ; __index += 1) {
	        FIPA.PropertyHelper.write(out, that.userDefinedProperties[__index]);
	    }
	}
    }
    public static FIPA.AgentID read(org.omg.CORBA.portable.InputStream in) {
        FIPA.AgentID that = new FIPA.AgentID();
	that.name = in.read_string();
	{
	    int __length = in.read_long();
	    that.addresses = new String[__length];
	    for (int __index = 0 ; __index < that.addresses.length ; __index += 1) {
	        that.addresses[__index] = in.read_string();
	    }
	}
	{
	    int __length = in.read_long();
	    that.resolvers = new FIPA.AgentID[__length];
	    for (int __index = 0 ; __index < that.resolvers.length ; __index += 1) {
	        that.resolvers[__index] = FIPA.AgentIDHelper.read(in);
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
   public static FIPA.AgentID extract(org.omg.CORBA.Any a) {
     org.omg.CORBA.portable.InputStream in = a.create_input_stream();
     return read(in);
   }
   public static void insert(org.omg.CORBA.Any a, FIPA.AgentID that) {
     org.omg.CORBA.portable.OutputStream out = a.create_output_stream();
     write(out, that);
     a.read_value(out.create_input_stream(), type());
   }
   private static org.omg.CORBA.TypeCode _tc;
   synchronized public static org.omg.CORBA.TypeCode type() {
       int _memberCount = 4;
       org.omg.CORBA.StructMember[] _members = null;
          if (_tc == null) {
               _members= new org.omg.CORBA.StructMember[4];
               _members[0] = new org.omg.CORBA.StructMember(
                 "name",
                 org.omg.CORBA.ORB.init().get_primitive_tc(org.omg.CORBA.TCKind.tk_string),
                 null);

               _members[1] = new org.omg.CORBA.StructMember(
                 "addresses",
                 org.omg.CORBA.ORB.init().create_sequence_tc(0, org.omg.CORBA.ORB.init().get_primitive_tc(org.omg.CORBA.TCKind.tk_string)),
                 null);

               _members[2] = new org.omg.CORBA.StructMember(
                 "resolvers",
                 //org.omg.CORBA.ORB.init().create_sequence_tc(0, FIPA.AgentIDHelper.type()),
                 org.omg.CORBA.ORB.init().create_sequence_tc(0, org.omg.CORBA.ORB.init().create_recursive_tc(id())),
                 null);

               _members[3] = new org.omg.CORBA.StructMember(
                 "userDefinedProperties",
                 org.omg.CORBA.ORB.init().create_sequence_tc(0, FIPA.PropertyHelper.type()),
                 null);
             _tc = org.omg.CORBA.ORB.init().create_struct_tc(id(), "AgentID", _members);
          }
      return _tc;
   }
   public static String id() {
       return "IDL:FIPA/AgentID:1.0";
   }
}

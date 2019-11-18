/*
 * File: ./FIPA/PROPERTYHELPER.JAVA
 * From: FIPA.IDL
 * Date: Mon Sep 04 15:08:50 2000
 *   By: idltojava Java IDL 1.2 Nov 10 1997 13:52:11
 */

package FIPA;
public class PropertyHelper {
     // It is useless to have instances of this class
     private PropertyHelper() { }

    public static void write(org.omg.CORBA.portable.OutputStream out, FIPA.Property that) {
	out.write_string(that.keyword);
	out.write_any(that.value);
    }
    public static FIPA.Property read(org.omg.CORBA.portable.InputStream in) {
        FIPA.Property that = new FIPA.Property();
	that.keyword = in.read_string();
	that.value = in.read_any();
        return that;
    }
   public static FIPA.Property extract(org.omg.CORBA.Any a) {
     org.omg.CORBA.portable.InputStream in = a.create_input_stream();
     return read(in);
   }
   public static void insert(org.omg.CORBA.Any a, FIPA.Property that) {
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
                 "keyword",
                 org.omg.CORBA.ORB.init().get_primitive_tc(org.omg.CORBA.TCKind.tk_string),
                 null);

               _members[1] = new org.omg.CORBA.StructMember(
                 "value",
                 org.omg.CORBA.ORB.init().get_primitive_tc(org.omg.CORBA.TCKind.tk_any),
                 null);
             _tc = org.omg.CORBA.ORB.init().create_struct_tc(id(), "Property", _members);
          }
      return _tc;
   }
   public static String id() {
       return "IDL:FIPA/Property:1.0";
   }
}

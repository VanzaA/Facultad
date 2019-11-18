/*
 * File: ./FIPA/URLHELPER.JAVA
 * From: FIPA.IDL
 * Date: Mon Sep 04 15:08:50 2000
 *   By: idltojava Java IDL 1.2 Nov 10 1997 13:52:11
 */

package FIPA;
public class URLHelper {
     // It is useless to have instances of this class
     private URLHelper() { }

    public static void write(org.omg.CORBA.portable.OutputStream out, String that)  {
          out.write_string(that);
    }
    public static String read(org.omg.CORBA.portable.InputStream in) {
          String that;
          that = in.read_string();
          return that;
    }
   public static String extract(org.omg.CORBA.Any a) {
     org.omg.CORBA.portable.InputStream in = a.create_input_stream();
     return read(in);
   }
   public static void insert(org.omg.CORBA.Any a, String that) {
     org.omg.CORBA.portable.OutputStream out = a.create_output_stream();
     a.type(type());
     write(out, that);
     a.read_value(out.create_input_stream(), type());
   }
   private static org.omg.CORBA.TypeCode _tc;
   synchronized public static org.omg.CORBA.TypeCode type() {
          if (_tc == null)
             _tc = org.omg.CORBA.ORB.init().create_alias_tc(id(), "URL", org.omg.CORBA.ORB.init().get_primitive_tc(org.omg.CORBA.TCKind.tk_string));
      return _tc;
   }
   public static String id() {
       return "IDL:FIPA/URL:1.0";
   }
}

/*
 * File: ./FIPA/OPTTRANSPORTBEHAVIOURTYPEHELPER.JAVA
 * From: FIPA.IDL
 * Date: Mon Sep 04 15:08:50 2000
 *   By: idltojava Java IDL 1.2 Nov 10 1997 13:52:11
 */

package FIPA;
public class OptTransportBehaviourTypeHelper {
     // It is useless to have instances of this class
     private OptTransportBehaviourTypeHelper() { }

    public static void write(org.omg.CORBA.portable.OutputStream out, FIPA.Property[][] that)  {
          {
              if (that.length > (1L)) {
                  throw new org.omg.CORBA.MARSHAL(0, org.omg.CORBA.CompletionStatus.COMPLETED_MAYBE);
              }
              out.write_long(that.length);
              for (int __index = 0 ; __index < that.length ; __index += 1) {
                  out.write_long(that[__index].length);
                  for (int __index2 = 0 ; __index2 < that[__index].length ; __index2 += 1) {
                      FIPA.PropertyHelper.write(out, that[__index][__index2]);
                  }
              }
          }
    }
    public static FIPA.Property[][] read(org.omg.CORBA.portable.InputStream in) {
          FIPA.Property[][] that;
          {
              int __length = in.read_long();
              if (__length > (1L)) {
                  throw new org.omg.CORBA.MARSHAL(0, org.omg.CORBA.CompletionStatus.COMPLETED_MAYBE);
              }
              that = new FIPA.Property[__length][];
              for (int __index = 0 ; __index < that.length ; __index += 1) {
                  int __length2 = in.read_long();
                  that[__index] = new FIPA.Property[__length2];
                  for (int __index2 = 0 ; __index2 < that[__index].length ; __index2 += 1) {
                      that[__index][__index2] = FIPA.PropertyHelper.read(in);
                  }
              }
          }
          return that;
    }
   public static FIPA.Property[][] extract(org.omg.CORBA.Any a) {
     org.omg.CORBA.portable.InputStream in = a.create_input_stream();
     return read(in);
   }
   public static void insert(org.omg.CORBA.Any a, FIPA.Property[][] that) {
     org.omg.CORBA.portable.OutputStream out = a.create_output_stream();
     a.type(type());
     write(out, that);
     a.read_value(out.create_input_stream(), type());
   }
   private static org.omg.CORBA.TypeCode _tc;
   synchronized public static org.omg.CORBA.TypeCode type() {
          if (_tc == null)
             _tc = org.omg.CORBA.ORB.init().create_alias_tc(id(), "OptTransportBehaviourType", org.omg.CORBA.ORB.init().create_sequence_tc((int) (1L), org.omg.CORBA.ORB.init().create_sequence_tc(0, FIPA.PropertyHelper.type())));
      return _tc;
   }
   public static String id() {
       return "IDL:FIPA/OptTransportBehaviourType:1.0";
   }
}

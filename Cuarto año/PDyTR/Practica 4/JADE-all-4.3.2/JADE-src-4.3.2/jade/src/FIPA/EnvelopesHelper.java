/*
 * File: ./FIPA/ENVELOPESHELPER.JAVA
 * From: FIPA.IDL
 * Date: Mon Sep 04 15:08:50 2000
 *   By: idltojava Java IDL 1.2 Nov 10 1997 13:52:11
 */

package FIPA;
public class EnvelopesHelper {
     // It is useless to have instances of this class
     private EnvelopesHelper() { }

    public static void write(org.omg.CORBA.portable.OutputStream out, FIPA.Envelope[] that)  {
          {
              out.write_long(that.length);
              for (int __index = 0 ; __index < that.length ; __index += 1) {
                  FIPA.EnvelopeHelper.write(out, that[__index]);
              }
          }
    }
    public static FIPA.Envelope[] read(org.omg.CORBA.portable.InputStream in) {
          FIPA.Envelope[] that;
          {
              int __length = in.read_long();
              that = new FIPA.Envelope[__length];
              for (int __index = 0 ; __index < that.length ; __index += 1) {
                  that[__index] = FIPA.EnvelopeHelper.read(in);
              }
          }
          return that;
    }
   public static FIPA.Envelope[] extract(org.omg.CORBA.Any a) {
     org.omg.CORBA.portable.InputStream in = a.create_input_stream();
     return read(in);
   }
   public static void insert(org.omg.CORBA.Any a, FIPA.Envelope[] that) {
     org.omg.CORBA.portable.OutputStream out = a.create_output_stream();
     a.type(type());
     write(out, that);
     a.read_value(out.create_input_stream(), type());
   }
   private static org.omg.CORBA.TypeCode _tc;
   synchronized public static org.omg.CORBA.TypeCode type() {
          if (_tc == null)
             _tc = org.omg.CORBA.ORB.init().create_alias_tc(id(), "Envelopes", org.omg.CORBA.ORB.init().create_sequence_tc(0, FIPA.EnvelopeHelper.type()));
      return _tc;
   }
   public static String id() {
       return "IDL:FIPA/Envelopes:1.0";
   }
}

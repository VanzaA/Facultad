/*
 * File: ./FIPA/MTSHELPER.JAVA
 * From: FIPA.IDL
 * Date: Mon Sep 04 15:08:50 2000
 *   By: idltojava Java IDL 1.2 Nov 10 1997 13:52:11
 */

package FIPA;
public class MTSHelper {
     // It is useless to have instances of this class
     private MTSHelper() { }

    public static void write(org.omg.CORBA.portable.OutputStream out, FIPA.MTS that) {
        out.write_Object(that);
    }
    public static FIPA.MTS read(org.omg.CORBA.portable.InputStream in) {
        return FIPA.MTSHelper.narrow(in.read_Object());
    }
   public static FIPA.MTS extract(org.omg.CORBA.Any a) {
     org.omg.CORBA.portable.InputStream in = a.create_input_stream();
     return read(in);
   }
   public static void insert(org.omg.CORBA.Any a, FIPA.MTS that) {
     org.omg.CORBA.portable.OutputStream out = a.create_output_stream();
     write(out, that);
     a.read_value(out.create_input_stream(), type());
   }
   private static org.omg.CORBA.TypeCode _tc;
   synchronized public static org.omg.CORBA.TypeCode type() {
          if (_tc == null)
             _tc = org.omg.CORBA.ORB.init().create_interface_tc(id(), "MTS");
      return _tc;
   }
   public static String id() {
       return "IDL:FIPA/MTS:1.0";
   }
   public static FIPA.MTS narrow(org.omg.CORBA.Object that)
	    throws org.omg.CORBA.BAD_PARAM {
        if (that == null)
            return null;
        if (that instanceof FIPA.MTS)
            return (FIPA.MTS) that;
	if (!that._is_a(id())) {
	    throw new org.omg.CORBA.BAD_PARAM();
	}
        org.omg.CORBA.portable.Delegate dup = ((org.omg.CORBA.portable.ObjectImpl)that)._get_delegate();
        FIPA.MTS result = new FIPA._MTSStub(dup);
        return result;
   }
}

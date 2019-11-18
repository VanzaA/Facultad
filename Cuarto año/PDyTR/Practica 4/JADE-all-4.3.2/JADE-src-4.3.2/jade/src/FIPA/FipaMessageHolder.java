/*
 * File: ./FIPA/FIPAMESSAGEHOLDER.JAVA
 * From: FIPA.IDL
 * Date: Mon Sep 04 15:08:50 2000
 *   By: idltojava Java IDL 1.2 Nov 10 1997 13:52:11
 */

package FIPA;
public final class FipaMessageHolder
     implements org.omg.CORBA.portable.Streamable{
    //	instance variable 
    public FIPA.FipaMessage value;
    //	constructors 
    public FipaMessageHolder() {
	this(null);
    }
    public FipaMessageHolder(FIPA.FipaMessage __arg) {
	value = __arg;
    }

    public void _write(org.omg.CORBA.portable.OutputStream out) {
        FIPA.FipaMessageHelper.write(out, value);
    }

    public void _read(org.omg.CORBA.portable.InputStream in) {
        value = FIPA.FipaMessageHelper.read(in);
    }

    public org.omg.CORBA.TypeCode _type() {
        return FIPA.FipaMessageHelper.type();
    }
}

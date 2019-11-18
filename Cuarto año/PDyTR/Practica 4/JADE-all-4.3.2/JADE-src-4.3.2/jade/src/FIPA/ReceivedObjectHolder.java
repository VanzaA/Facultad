/*
 * File: ./FIPA/RECEIVEDOBJECTHOLDER.JAVA
 * From: FIPA.IDL
 * Date: Mon Sep 04 15:08:50 2000
 *   By: idltojava Java IDL 1.2 Nov 10 1997 13:52:11
 */

package FIPA;
public final class ReceivedObjectHolder
     implements org.omg.CORBA.portable.Streamable{
    //	instance variable 
    public FIPA.ReceivedObject value;
    //	constructors 
    public ReceivedObjectHolder() {
	this(null);
    }
    public ReceivedObjectHolder(FIPA.ReceivedObject __arg) {
	value = __arg;
    }

    public void _write(org.omg.CORBA.portable.OutputStream out) {
        FIPA.ReceivedObjectHelper.write(out, value);
    }

    public void _read(org.omg.CORBA.portable.InputStream in) {
        value = FIPA.ReceivedObjectHelper.read(in);
    }

    public org.omg.CORBA.TypeCode _type() {
        return FIPA.ReceivedObjectHelper.type();
    }
}

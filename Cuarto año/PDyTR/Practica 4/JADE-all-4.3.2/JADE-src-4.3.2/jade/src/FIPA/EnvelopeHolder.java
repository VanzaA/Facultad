/*
 * File: ./FIPA/ENVELOPEHOLDER.JAVA
 * From: FIPA.IDL
 * Date: Mon Sep 04 15:08:50 2000
 *   By: idltojava Java IDL 1.2 Nov 10 1997 13:52:11
 */

package FIPA;
public final class EnvelopeHolder
     implements org.omg.CORBA.portable.Streamable{
    //	instance variable 
    public FIPA.Envelope value;
    //	constructors 
    public EnvelopeHolder() {
	this(null);
    }
    public EnvelopeHolder(FIPA.Envelope __arg) {
	value = __arg;
    }

    public void _write(org.omg.CORBA.portable.OutputStream out) {
        FIPA.EnvelopeHelper.write(out, value);
    }

    public void _read(org.omg.CORBA.portable.InputStream in) {
        value = FIPA.EnvelopeHelper.read(in);
    }

    public org.omg.CORBA.TypeCode _type() {
        return FIPA.EnvelopeHelper.type();
    }
}

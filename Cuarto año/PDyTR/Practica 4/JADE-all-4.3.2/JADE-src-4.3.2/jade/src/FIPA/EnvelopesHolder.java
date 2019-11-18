/*
 * File: ./FIPA/ENVELOPESHOLDER.JAVA
 * From: FIPA.IDL
 * Date: Mon Sep 04 15:08:50 2000
 *   By: idltojava Java IDL 1.2 Nov 10 1997 13:52:11
 */

package FIPA;
public final class EnvelopesHolder
    implements org.omg.CORBA.portable.Streamable
{
    //	instance variable 
    public FIPA.Envelope[] value;
    //	constructors 
    public EnvelopesHolder() {
	this(null);
    }
    public EnvelopesHolder(FIPA.Envelope[] __arg) {
	value = __arg;
    }
    public void _write(org.omg.CORBA.portable.OutputStream out) {
        FIPA.EnvelopesHelper.write(out, value);
    }

    public void _read(org.omg.CORBA.portable.InputStream in) {
        value = FIPA.EnvelopesHelper.read(in);
    }

    public org.omg.CORBA.TypeCode _type() {
        return FIPA.EnvelopesHelper.type();
    }
}

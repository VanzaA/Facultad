/*
 * File: ./FIPA/OPTTRANSPORTBEHAVIOURTYPEHOLDER.JAVA
 * From: FIPA.IDL
 * Date: Mon Sep 04 15:08:50 2000
 *   By: idltojava Java IDL 1.2 Nov 10 1997 13:52:11
 */

package FIPA;
public final class OptTransportBehaviourTypeHolder
    implements org.omg.CORBA.portable.Streamable
{
    //	instance variable 
    public FIPA.Property[][] value;
    //	constructors 
    public OptTransportBehaviourTypeHolder() {
	this(null);
    }
    public OptTransportBehaviourTypeHolder(FIPA.Property[][] __arg) {
	value = __arg;
    }
    public void _write(org.omg.CORBA.portable.OutputStream out) {
        FIPA.OptTransportBehaviourTypeHelper.write(out, value);
    }

    public void _read(org.omg.CORBA.portable.InputStream in) {
        value = FIPA.OptTransportBehaviourTypeHelper.read(in);
    }

    public org.omg.CORBA.TypeCode _type() {
        return FIPA.OptTransportBehaviourTypeHelper.type();
    }
}

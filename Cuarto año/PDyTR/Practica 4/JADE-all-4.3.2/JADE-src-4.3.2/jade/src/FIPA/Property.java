/*
 * File: ./FIPA/PROPERTY.JAVA
 * From: FIPA.IDL
 * Date: Mon Sep 04 15:08:50 2000
 *   By: idltojava Java IDL 1.2 Nov 10 1997 13:52:11
 */

package FIPA;
public final class Property {
    //	instance variables
    public String keyword;
    public org.omg.CORBA.Any value;
    //	constructors
    public Property() { }
    public Property(String __keyword, org.omg.CORBA.Any __value) {
	keyword = __keyword;
	value = __value;
    }
}

/*
 * File: ./FIPA/RECEIVEDOBJECT.JAVA
 * From: FIPA.IDL
 * Date: Mon Sep 04 15:08:50 2000
 *   By: idltojava Java IDL 1.2 Nov 10 1997 13:52:11
 */

package FIPA;
public final class ReceivedObject {
    //	instance variables
    public String by;
    public String from;
    public FIPA.DateTime date;
    public String id;
    public String via;
    //	constructors
    public ReceivedObject() { }
    public ReceivedObject(String __by, String __from, FIPA.DateTime __date, String __id, String __via) {
	by = __by;
	from = __from;
	date = __date;
	id = __id;
	via = __via;
    }
}

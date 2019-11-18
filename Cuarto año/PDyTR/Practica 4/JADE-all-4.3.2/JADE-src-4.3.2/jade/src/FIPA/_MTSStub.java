/*
 * File: ./FIPA/_MTSSTUB.JAVA
 * From: FIPA.IDL
 * Date: Mon Sep 04 15:08:50 2000
 *   By: idltojava Java IDL 1.2 Nov 10 1997 13:52:11
 */

package FIPA;
public class _MTSStub
	extends org.omg.CORBA.portable.ObjectImpl
    	implements FIPA.MTS {

    public _MTSStub(org.omg.CORBA.portable.Delegate d) {
          super();
          _set_delegate(d);
    }

    private static final String _type_ids[] = {
        "IDL:FIPA/MTS:1.0"
    };

    public String[] _ids() { return (String[]) _type_ids.clone(); }

    //	IDL operations
    //	    Implementation of ::FIPA::MTS::message
    public void message(FIPA.FipaMessage aFipaMessage)
 {
           org.omg.CORBA.Request r = _request("message");
           org.omg.CORBA.Any _aFipaMessage = r.add_in_arg();
           FIPA.FipaMessageHelper.insert(_aFipaMessage, aFipaMessage);
           r.send_oneway();
   }

};

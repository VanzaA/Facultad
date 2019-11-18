/*
 * File: ./FIPA/_MTSIMPLBASE.JAVA
 * From: FIPA.IDL
 * Date: Mon Sep 04 15:08:50 2000
 *   By: idltojava Java IDL 1.2 Nov 10 1997 13:52:11
 */

package FIPA;
public abstract class _MTSImplBase extends org.omg.CORBA.DynamicImplementation implements FIPA.MTS {
    // Constructor
    public _MTSImplBase() {
         super();
    }
    // Type strings for this class and its superclases
    private static final String _type_ids[] = {
        "IDL:FIPA/MTS:1.0"
    };

    public String[] _ids() { return (String[]) _type_ids.clone(); }

    private static java.util.Dictionary _methods = new java.util.Hashtable();
    static {
      _methods.put("message", new java.lang.Integer(0));
     }
    // DSI Dispatch call
    public void invoke(org.omg.CORBA.ServerRequest r) {
       switch (((java.lang.Integer) _methods.get(r.op_name())).intValue()) {
           case 0: // FIPA.MTS.message
              {
              org.omg.CORBA.NVList _list = _orb().create_list(0);
              org.omg.CORBA.Any _aFipaMessage = _orb().create_any();
              _aFipaMessage.type(FIPA.FipaMessageHelper.type());
              _list.add_value("aFipaMessage", _aFipaMessage, org.omg.CORBA.ARG_IN.value);
              r.params(_list);
              FIPA.FipaMessage aFipaMessage;
              aFipaMessage = FIPA.FipaMessageHelper.extract(_aFipaMessage);
                            this.message(aFipaMessage);
              org.omg.CORBA.Any __return = _orb().create_any();
              __return.type(_orb().get_primitive_tc(org.omg.CORBA.TCKind.tk_void));
              r.result(__return);
              }
              break;
            default:
              throw new org.omg.CORBA.BAD_OPERATION(0, org.omg.CORBA.CompletionStatus.COMPLETED_MAYBE);
       }
 }
}

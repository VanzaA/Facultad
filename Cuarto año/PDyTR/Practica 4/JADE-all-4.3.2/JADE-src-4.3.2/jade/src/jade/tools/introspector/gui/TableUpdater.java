/*****************************************************************
JADE - Java Agent DEvelopment Framework is a framework to develop 
multi-agent systems in compliance with the FIPA specifications.
Copyright (C) 2000 CSELT S.p.A. 

GNU Lesser General Public License

This library is free software; you can redistribute it and/or
modify it under the terms of the GNU Lesser General Public
License as published by the Free Software Foundation, 
version 2.1 of the License. 

This library is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
Lesser General Public License for more details.

You should have received a copy of the GNU Lesser General Public
License along with this library; if not, write to the
Free Software Foundation, Inc., 59 Temple Place - Suite 330,
Boston, MA  02111-1307, USA.
*****************************************************************/


package jade.tools.introspector.gui;

import java.io.UnsupportedEncodingException;

import jade.lang.acl.ACLMessage;
import jade.lang.acl.ACLCodec;
import jade.lang.acl.StringACLCodec;

import jade.domain.FIPAAgentManagement.Envelope;

import jade.domain.introspection.SentMessage;
import jade.domain.introspection.PostedMessage;
import jade.domain.introspection.ReceivedMessage;

/**
   This class receives a MessageEvent and updates the message table
   accordingly.

   @author Andrea Squeri, Corti Denis, Ballestracci Paolo -  Universita` di Parma
*/
public class TableUpdater implements Runnable {

  MessageTableModel modelFrom;
  MessageTableModel modelTo;
  ACLMessage msg;

  public TableUpdater(MessagePanel wnd, SentMessage sm) {
    try {
      modelFrom = null;
      modelTo = wnd.getOutProcessedModel();
      String charset; 
      Envelope e;
      jade.domain.introspection.ACLMessage m = sm.getMessage();
      if (((e = m.getEnvelope()) == null) ||
          ((charset = e.getPayloadEncoding()) == null)) {
        charset = ACLCodec.DEFAULT_CHARSET;
      }
      String s = m.getPayload();
      ACLCodec codec = new StringACLCodec();
      msg = codec.decode(s.getBytes(charset),charset);
      msg.setEnvelope(e);
    }
    catch(ACLCodec.CodecException aclce) {
      aclce.printStackTrace();
    } catch ( UnsupportedEncodingException exception ) {
    	exception.printStackTrace();
    }
  }

  public TableUpdater(MessagePanel wnd, PostedMessage pm) {
    try {
      modelFrom = null;
      modelTo = wnd.getInPendingModel();
      String charset; 
      Envelope e;
      jade.domain.introspection.ACLMessage m = pm.getMessage();
      if (((e = m.getEnvelope()) == null) ||
          ((charset = e.getPayloadEncoding()) == null)) {
        charset = ACLCodec.DEFAULT_CHARSET;
      }
      String s = m.getPayload();
      ACLCodec codec = new StringACLCodec();
      msg = codec.decode(s.getBytes(charset),charset);
      msg.setEnvelope(e);
    }
    catch(ACLCodec.CodecException aclce) {
      aclce.printStackTrace();
    } catch ( UnsupportedEncodingException exception ) {
		exception.printStackTrace();
	}
  }

  public TableUpdater(MessagePanel wnd, ReceivedMessage rm) {
    try {
      modelFrom = wnd.getInPendingModel();
      modelTo = wnd.getInProcessedModel();
      String charset; 
      Envelope e;
      jade.domain.introspection.ACLMessage m = rm.getMessage();
      if (((e = m.getEnvelope()) == null) ||
          ((charset = e.getPayloadEncoding()) == null)) {
        charset = ACLCodec.DEFAULT_CHARSET;
      }
      String s = m.getPayload();
      ACLCodec codec = new StringACLCodec();
      msg = codec.decode(s.getBytes(charset),charset);
      msg.setEnvelope(e);
    }
    catch(ACLCodec.CodecException aclce) {
      aclce.printStackTrace();
    } catch ( UnsupportedEncodingException exception ) {
		exception.printStackTrace();
	}
  }


  public void run() {

    if(modelFrom != null) {

      for(int i = 0; i < modelFrom.getRowCount(); i++) {
        ACLMessage m = (ACLMessage)modelFrom.getValueAt(i, 0);
	String s1 = m.toString();
	String s2 = msg.toString();
	if(s1.equalsIgnoreCase(s2)) {
          modelFrom.removeRow(i);
          break;
        }
      }

    }
    if(modelTo != null) {
      modelTo.addRow(msg);
    }

  }

}

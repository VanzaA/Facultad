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

package jade.core.replication;

//#MIDP_EXCLUDE_FILE

import java.util.Vector;

import jade.core.Node;
import jade.core.NodeDescriptor;
import jade.core.SliceProxy;
import jade.core.GenericCommand;
import jade.core.AID;
import jade.core.ContainerID;
import jade.core.IMTPException;
import jade.core.ServiceException;
import jade.core.NotFoundException;


/**
   The remote proxy for the JADE kernel-level service managing
   the main-container replication subsystem installed in the platform.

   @author Giovanni Rimassa - FRAMeTech s.r.l.
*/
public class MainReplicationProxy extends SliceProxy implements MainReplicationSlice {


    public int getLabel() throws IMTPException {
	try {
	    GenericCommand cmd = new GenericCommand(H_GETLABEL, NAME, null);

	    Node n = getNode();
	    Object result = n.accept(cmd);
	    if((result != null) && (result instanceof Throwable)) {
		if(result instanceof IMTPException) {
		    throw (IMTPException)result;
		}
		else {
		    throw new IMTPException("An undeclared exception was thrown", (Throwable)result);
		}
	    }

	    return ((Integer)result).intValue();
	}
	catch(ServiceException se) {
	    throw new IMTPException("Unable to access remote node", se);
	}
    }

    public String getPlatformManagerAddress() throws IMTPException {
	try {
	    GenericCommand cmd = new GenericCommand(H_GETPLATFORMMANAGERADDRESS, NAME, null);

	    Node n = getNode();
	    Object result = n.accept(cmd);
	    if((result != null) && (result instanceof Throwable)) {
		if(result instanceof IMTPException) {
		    throw (IMTPException)result;
		}
		else {
		    throw new IMTPException("An undeclared exception was thrown", (Throwable)result);
		}
	    }

	    return (String)result;
	}
	catch(ServiceException se) {
	    throw new IMTPException("Unable to access remote node", se);
	}
    }

    public void addReplica(String sliceName, String smAddr, int sliceIndex, NodeDescriptor dsc, Vector services) throws IMTPException {
	try {
	    GenericCommand cmd = new GenericCommand(H_ADDREPLICA, NAME, null);
	    cmd.addParam(sliceName);
	    cmd.addParam(smAddr);
	    cmd.addParam(new Integer(sliceIndex));
	    cmd.addParam(dsc);
	    cmd.addParam(services);

	    Node n = getNode();
	    Object result = n.accept(cmd);
	    if((result != null) && (result instanceof Throwable)) {
		if(result instanceof IMTPException) {
		    throw (IMTPException)result;
		}
		else {
		    throw new IMTPException("An undeclared exception was thrown", (Throwable)result);
		}
	    }
	}
	catch(ServiceException se) {
	    throw new IMTPException("Unable to access remote node", se);
	}
    }

    public void removeReplica(String smAddr, int sliceIndex) throws IMTPException { 
	try {
	    GenericCommand cmd = new GenericCommand(H_REMOVEREPLICA, NAME, null);
	    cmd.addParam(smAddr);
	    cmd.addParam(new Integer(sliceIndex));

	    Node n = getNode();
	    Object result = n.accept(cmd);
	    if((result != null) && (result instanceof Throwable)) {
		if(result instanceof IMTPException) {
		    throw (IMTPException)result;
		}
		else {
		    throw new IMTPException("An undeclared exception was thrown", (Throwable)result);
		}
	    }
	}
	catch(ServiceException se) {
	    throw new IMTPException("Unable to access remote node", se);
	}
    }

    public void fillGADT(AID[] agents, ContainerID[] containers) throws IMTPException {
	try {
	    GenericCommand cmd = new GenericCommand(H_FILLGADT, NAME, null);
	    cmd.addParam(agents);
	    cmd.addParam(containers);

	    Node n = getNode();
	    Object result = n.accept(cmd);
	    if((result != null) && (result instanceof Throwable)) {
		if(result instanceof IMTPException) {
		    throw (IMTPException)result;
		}
		else {
		    throw new IMTPException("An undeclared exception was thrown", (Throwable)result);
		}
	    }
	}
	catch(ServiceException se) {
	    throw new IMTPException("Unable to access remote node", se);
	}
    }
    
    /*public void bornAgent(AID name, ContainerID cid, JADEPrincipal principal String ownership) throws IMTPException, NameClashException, NotFoundException, JADESecurityException {
	try {
	    GenericCommand cmd = new GenericCommand(H_BORNAGENT, NAME, null);
	    cmd.addParam(name);
	    cmd.addParam(cid);
	    cmd.addParam(ownership);

	    Node n = getNode();
	    Object result = n.accept(cmd);
	    if((result != null) && (result instanceof Throwable)) {
		if(result instanceof IMTPException) {
		    throw (IMTPException)result;
		}
		else if(result instanceof NameClashException) {
		    throw (NameClashException)result;
		}
		else if(result instanceof NotFoundException) {
		    throw (NotFoundException)result;
		}
		else if(result instanceof JADESecurityException) {
		    throw (JADESecurityException)result;
		}
		else {
		    throw new IMTPException("An undeclared exception was thrown", (Throwable)result);
		}
	    }
	}
	catch(ServiceException se) {
	    throw new IMTPException("Unable to access remote node", se);
	}
    }

    public void deadAgent(AID name) throws IMTPException, NotFoundException {
	try {
	    GenericCommand cmd = new GenericCommand(H_DEADAGENT, NAME, null);
	    cmd.addParam(name);

	    Node n = getNode();
	    Object result = n.accept(cmd);
	    if((result != null) && (result instanceof Throwable)) {
		if(result instanceof IMTPException) {
		    throw (IMTPException)result;
		}
		else if(result instanceof NotFoundException) {
		    throw (NotFoundException)result;
		}
		else {
		    throw new IMTPException("An undeclared exception was thrown", (Throwable)result);
		}
	    }
	}
	catch(ServiceException se) {
	    throw new IMTPException("Unable to access remote node", se);
	}
    }*/

    public void suspendedAgent(AID name) throws IMTPException, NotFoundException {
	try {
	    GenericCommand cmd = new GenericCommand(H_SUSPENDEDAGENT, NAME, null);
	    cmd.addParam(name);

	    Node n = getNode();
	    Object result = n.accept(cmd);
	    if((result != null) && (result instanceof Throwable)) {
		if(result instanceof IMTPException) {
		    throw (IMTPException)result;
		}
		else if(result instanceof NotFoundException) {
		    throw (NotFoundException)result;
		}
		else {
		    throw new IMTPException("An undeclared exception was thrown", (Throwable)result);
		}
	    }
	}
	catch(ServiceException se) {
	    throw new IMTPException("Unable to access remote node", se);
	}
    }

    /*
    public void resumedAgent(AID name) throws IMTPException, NotFoundException {
	try {
	    GenericCommand cmd = new GenericCommand(H_RESUMEDAGENT, NAME, null);
	    cmd.addParam(name);

	    Node n = getNode();
	    Object result = n.accept(cmd);
	    if((result != null) && (result instanceof Throwable)) {
		if(result instanceof IMTPException) {
		    throw (IMTPException)result;
		}
		else if(result instanceof NotFoundException) {
		    throw (NotFoundException)result;
		}
		else {
		    throw new IMTPException("An undeclared exception was thrown", (Throwable)result);
		}
	    }
	}
	catch(ServiceException se) {
	    throw new IMTPException("Unable to access remote node", se);
	}
    }

    public void newMTP(MTPDescriptor mtp, ContainerID cid) throws IMTPException {
	try {
	    GenericCommand cmd = new GenericCommand(H_NEWMTP, NAME, null);
	    cmd.addParam(mtp);
	    cmd.addParam(cid);

	    Node n = getNode();
	    Object result = n.accept(cmd);
	    if((result != null) && (result instanceof Throwable)) {
		if(result instanceof IMTPException) {
		    throw (IMTPException)result;
		}
		else {
		    throw new IMTPException("An undeclared exception was thrown", (Throwable)result);
		}
	    }
	}
	catch(ServiceException se) {
	    throw new IMTPException("Unable to access remote node", se);
	}
    }

    public void deadMTP(MTPDescriptor mtp, ContainerID cid) throws IMTPException {
	try {
	    GenericCommand cmd = new GenericCommand(H_DEADMTP, NAME, null);
	    cmd.addParam(mtp);
	    cmd.addParam(cid);

	    Node n = getNode();
	    Object result = n.accept(cmd);
	    if((result != null) && (result instanceof Throwable)) {
		if(result instanceof IMTPException) {
		    throw (IMTPException)result;
		}
		else {
		    throw new IMTPException("An undeclared exception was thrown", (Throwable)result);
		}
	    }
	}
	catch(ServiceException se) {
	    throw new IMTPException("Unable to access remote node", se);
	}
    }*/

    public void newTool(AID tool) throws IMTPException {
	try {
	    GenericCommand cmd = new GenericCommand(H_NEWTOOL, NAME, null);
	    cmd.addParam(tool);

	    Node n = getNode();
	    Object result = n.accept(cmd);
	    if((result != null) && (result instanceof Throwable)) {
		if(result instanceof IMTPException) {
		    throw (IMTPException)result;
		}
		else {
		    throw new IMTPException("An undeclared exception was thrown", (Throwable)result);
		}
	    }
	}
	catch(ServiceException se) {
	    throw new IMTPException("Unable to access remote node", se);
	}
    }

    /*
    public void deadTool(AID tool) throws IMTPException {
	try {
	    GenericCommand cmd = new GenericCommand(H_DEADTOOL, NAME, null);
	    cmd.addParam(tool);

	    Node n = getNode();
	    Object result = n.accept(cmd);
	    if((result != null) && (result instanceof Throwable)) {
		if(result instanceof IMTPException) {
		    throw (IMTPException)result;
		}
		else {
		    throw new IMTPException("An undeclared exception was thrown", (Throwable)result);
		}
	    }
	}
	catch(ServiceException se) {
	    throw new IMTPException("Unable to access remote node", se);
	}
    }
    */

}

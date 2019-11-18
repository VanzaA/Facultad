package jade.core.nodeMonitoring;

//#J2ME_EXCLUDE_FILE
//#APIDOC_EXCLUDE_FILE

import jade.core.GenericCommand;
import jade.core.Node;
import jade.core.SliceProxy;
import jade.core.IMTPException;
import jade.core.ServiceException;

public class UDPNodeMonitoringProxy extends SliceProxy implements UDPNodeMonitoringSlice {
	/*
	 * Request a given node to start sending UDP packets
	 */
	public void activateUDP(String label, String host, int port, int pingDelay, long key) throws IMTPException, ServiceException {
		GenericCommand cmd = new GenericCommand(H_ACTIVATEUDP, UDPNodeMonitoringService.NAME, null);
		cmd.addParam(label);
		cmd.addParam(host);
		cmd.addParam(new Integer(port));
		cmd.addParam(new Integer(pingDelay));
		cmd.addParam(new Long(key));

		Node n = getNode();
		Object result = n.accept(cmd);
		if ((result != null) && (result instanceof Throwable)) {
			if (result instanceof IMTPException) {
				throw (IMTPException) result;
			} 
			else {
				throw new IMTPException("Unexpected exception in remote site.", (Throwable) result);
			}
		}
	}

	/*
	 * Request a given node to stop sending UDP packets
	 */
	public void deactivateUDP(String label, long key) throws IMTPException {
		try {
			GenericCommand cmd = new GenericCommand(H_DEACTIVATEUDP, UDPNodeMonitoringService.NAME, null);
			cmd.addParam(label);
			cmd.addParam(new Long(key));
	
			Node n = getNode();
			Object result = n.accept(cmd);
			if ((result != null) && (result instanceof Throwable)) {
				if (result instanceof IMTPException) {
					throw (IMTPException) result;
				} 
				else {
					throw new IMTPException("Unexpected exception in remote site.", (Throwable) result);
				}
			}
		}
		catch (ServiceException se) {
			throw new IMTPException("Unexpected error contacting remote node.", se);
		}
	}
}

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

package jade.core.nodeMonitoring;

//#APIDOC_EXCLUDE_FILE
//#MIDP_EXCLUDE_FILE

import jade.core.Node;
import jade.core.NodeFailureMonitor;
import jade.core.NodeEventListener;
import jade.core.IMTPException;

import jade.util.Logger;


/**
 *  The <code>BlockingNodeFailureMonitor</code> class detects node failures and
 *  notifies its registered listener using blocking calls to the ping() 
 * remote method of the monitored Node.
 *
 * @author Giovanni Rimassa - FRAMeTech s.r.l.
 * @author Roland Mungenast - Profactor
 * @see jade.core.NodeFailureMonitor
 */
public class BlockingNodeFailureMonitor extends NodeFailureMonitor
  implements Runnable {

    private boolean nodeExited = false;
    private boolean stopped = false;
    
    private Logger myLogger = Logger.getMyLogger(getClass().getName());

    public void start(Node n, NodeEventListener nel) {
    	super.start(n, nel);
    	
      Thread thread = new Thread(this);
      thread.setName(target.getName()+"-failure-monitor");
      thread.start();
    }
    
    public void run() {
      fireNodeAdded();
      while(!nodeExited && !stopped) {
        try {
          nodeExited = target.ping(true); // Hang on this call
          if(myLogger.isLoggable(Logger.INFO)) {
            myLogger.log(Logger.INFO,"PING from node " + target.getName() + " returned [" + (nodeExited ? "EXIT]" : "GO ON]"));
          }
        }
        catch(IMTPException imtpe1) { // Connection down
          if(myLogger.isLoggable(Logger.INFO)) {
            myLogger.log(Logger.INFO,"PING from node " + target.getName() + " exited with exception. "+imtpe1.getMessage());
          }
          
          if(!stopped) {
            fireNodeUnreachable();
          }
          try {
            target.ping(false); // Try a non blocking ping to check
    
            if(myLogger.isLoggable(Logger.INFO))
              myLogger.log(Logger.INFO,"PING from node " + target.getName() + " returned OK");
            
            if(!stopped) {
              fireNodeReachable();
            }
          }
          catch(IMTPException imtpe2) { // Object down
            nodeExited = true;
          }
        }
        catch(Throwable t) {
          t.printStackTrace();
        }
      } // END of while
      
      // If we reach this point without being explicitly stopped the node is no longer active
      if(!stopped) {
        fireNodeRemoved();
      }
    }
 
    public void stop() {
      try {
      stopped = true;
      target.interrupt();
      
      } catch(IMTPException imtpe) {
        if(myLogger.isLoggable(Logger.INFO))
          myLogger.log(Logger.INFO,"-- The node <" + target.getName() + "> is already dead --" );
        // Ignore it: the node must be dead already...
      }
    }

}

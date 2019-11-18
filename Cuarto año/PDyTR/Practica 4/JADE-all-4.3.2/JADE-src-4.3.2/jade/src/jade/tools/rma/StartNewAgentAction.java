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

package jade.tools.rma;

import java.awt.Frame;

import jade.gui.AgentTree;
import jade.BootHelper;
import jade.core.Profile;
import jade.core.Specifier;

import java.util.StringTokenizer;
import java.util.Vector;
import java.util.Enumeration;
import jade.util.leap.List;
import jade.util.leap.ArrayList;

/**

   @author Francisco Regi, Andrea Soracchi - Universita` di Parma
   @version $Date: 2010-04-08 15:54:18 +0200 (gio, 08 apr 2010) $ $Revision: 6298 $
 */
class StartNewAgentAction extends ContainerAction {

	private rma myRMA;
	private Frame mainWnd;

	public StartNewAgentAction(rma anRMA, Frame f,ActionProcessor actPro) {
		super ("StartNewAgentActionIcon","Start New Agent",actPro);
		myRMA = anRMA;
		mainWnd = f;
	}

	public void doAction(AgentTree.ContainerNode node ) {

		String containerName = node.getName();
		int result = doStartNewAgent(containerName);
	}

	private int doStartNewAgent(String containerName) {
		int result = StartDialog.showStartNewDialog(containerName, mainWnd);
		if (result == StartDialog.OK_BUTTON) {

			String agentName = StartDialog.getAgentName();
			String className = StartDialog.getClassName();
			String container = StartDialog.getContainer();
			String agentUser = StartDialog.getAgentUser ();
			String arguments = StartDialog.getArguments();

			if((agentName.trim().length() > 0) && (className.trim().length() >0)) {
				char argsDelimiter = ',';
				if ("true".equals(myRMA.getProperty(Profile.STYLE_3_X, "false"))) {
					argsDelimiter = ' ';
				}
				Vector v = Specifier.parseList(arguments, argsDelimiter);
				Object[] args = v.toArray();				
				myRMA.newAgent(agentName, className, args, agentUser, container);
			}
		}
		return result;
	}

}  // End of StartNewAgentAction










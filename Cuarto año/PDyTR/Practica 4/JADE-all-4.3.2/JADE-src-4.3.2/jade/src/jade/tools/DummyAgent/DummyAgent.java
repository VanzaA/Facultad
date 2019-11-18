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

package jade.tools.DummyAgent;

//Import required Java classes 
import java.awt.*;
import javax.swing.*;

//Import required Jade classes
import jade.core.*;
import jade.core.behaviours.Behaviour;

/**
@author Giovanni Caire - CSELT S.p.A
@version $Date: 2009-04-08 11:41:41 +0200 (mer, 08 apr 2009) $ $Revision: 6110 $
 */

public class DummyAgent extends Agent 
{
	private transient DummyAgentGui myGui;

	public static void main(String args[]) {
		DummyAgent d = new DummyAgent();
		d.setup();
	}
	// Extends the Agent setup method
	protected void setup()
	{
		///////////////////////////////
		// Create and display agent GUI
		myGui = new DummyAgentGui(this);
		myGui.showCorrect();

		///////////////////////
		// Add agent behaviour
		Behaviour b = new DummyBehaviour(this);
		addBehaviour(b);	

	}

	protected void takeDown() {
		disposeGUI();
	}

	public DummyAgentGui getGui()
	{
		return myGui;
	}

	protected void beforeMove() {
		disposeGUI();
	}

	protected void afterMove() {
		restoreGUI();
	}

	protected void afterClone() {
		restoreGUI();
	}

	public void afterLoad() {
		restoreGUI();
	}

	public void beforeFreeze() {
		disposeGUI();
	}

	public void afterThaw() {
		restoreGUI();
	}

	public void beforeReload() {
		disposeGUI();
	}

	public void afterReload() {
		restoreGUI();
	}

	private void restoreGUI() {
		myGui = new DummyAgentGui(this);
		myGui.showCorrect();
	}

	private void disposeGUI() {
		if(myGui != null) {
			final DummyAgentGui gui = myGui;
			myGui = null;
			SwingUtilities.invokeLater(new Runnable() {
				public void run() {
					gui.setVisible(false);
					gui.dispose();
				}
			});
		}
	}

}


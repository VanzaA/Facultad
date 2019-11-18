package jade.tools.logging.gui;

import java.awt.event.*;
import javax.swing.*;

class SetDefaultLoggingSystemAction extends AbstractAction {
	private LogManagerGUI gui;
	
	public SetDefaultLoggingSystemAction(LogManagerGUI gui) {
		super ("Set default logging system");
		this.gui = gui;
	}
	
	public void actionPerformed(ActionEvent e) {
		gui.setDefaultLoggingSystem();
	}
}

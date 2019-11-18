package jade.tools.logging.gui;

import java.awt.event.*;
import javax.swing.*;

class ExitAction extends AbstractAction {
	private LogManagerGUI gui;
	
	public ExitAction(LogManagerGUI gui) {
		super ("Exit");
		this.gui = gui;
	}
	
	public void actionPerformed(ActionEvent e) {
		gui.exit();
	}
}

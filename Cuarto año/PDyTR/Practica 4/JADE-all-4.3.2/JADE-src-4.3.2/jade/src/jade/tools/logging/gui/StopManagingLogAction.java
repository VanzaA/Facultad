package jade.tools.logging.gui;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

class StopManagingLogAction extends AbstractAction {
	private LogManagerGUI gui;
	
	public StopManagingLogAction(LogManagerGUI gui) {
		super ("Stop Managing Log");
		this.gui = gui;
	}
	
	public void actionPerformed(ActionEvent e) {
		gui.stopManagingLog();
	}
}

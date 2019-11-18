package jade.tools.logging.gui;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

class StartManagingLogAction extends AbstractAction {
	private LogManagerGUI gui;
	
	public StartManagingLogAction(LogManagerGUI gui) {
		super ("Start Managing Log");
		this.gui = gui;
	}
	
	public void actionPerformed(ActionEvent e) {
		gui.startManagingLog();
	}
}

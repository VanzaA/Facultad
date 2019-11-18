package jade.tools.logging.gui;

import java.awt.event.*;
import javax.swing.*;

class SetLoggingSystemAction extends AbstractAction {
	private ContainerLogWindow gui;
	
	public SetLoggingSystemAction(ContainerLogWindow gui) {
		super ("Set logging system");
		this.gui = gui;
	}
	
	public void actionPerformed(ActionEvent e) {
		gui.setLoggingSystem();
	}
}

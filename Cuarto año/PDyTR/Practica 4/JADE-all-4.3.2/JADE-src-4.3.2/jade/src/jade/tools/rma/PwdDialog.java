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

import java.awt.Dimension;
import javax.swing.*;

/**   
   @author Fabio Bellifemine - TILAB
   @version $Date: 2002-06-11 14:57:29 +0200 (mar, 11 giu 2002) $ $Revision: 3218 $
 */
class PwdDialog extends JPanel {

    JTextField usr = new JTextField("", 15);
    JPasswordField key = new JPasswordField("", 15);

    PwdDialog() {
  	usr.setMaximumSize(usr.getPreferredSize());
  	usr.setMinimumSize(usr.getPreferredSize());

		JPanel usrPanel = new JPanel();
  	usrPanel.setLayout(new BoxLayout(usrPanel, BoxLayout.X_AXIS));
		usrPanel.add(new JLabel("Username"));
		usrPanel.add(Box.createHorizontalGlue());
		usrPanel.add(Box.createRigidArea(new Dimension(10, 0)));
		usrPanel.add(usr);

  	key.setPreferredSize(usr.getPreferredSize());
  	key.setMaximumSize(key.getPreferredSize());
  	key.setMinimumSize(key.getPreferredSize());
  	
		JPanel keyPanel = new JPanel();
  	keyPanel.setLayout(new BoxLayout(keyPanel, BoxLayout.X_AXIS));
		keyPanel.add(new JLabel("Password"));
		keyPanel.add(Box.createHorizontalGlue());
		keyPanel.add(Box.createRigidArea(new Dimension(10, 0)));
		keyPanel.add(key);

  	setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
  	add(usrPanel);
  	add(keyPanel);
    }

    String getUserName() {
	return usr.getText();
    }
    char[] getPassword() {
	return key.getPassword();
    }

} 


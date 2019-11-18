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

import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

/**
@author Giovanni Rimassa - Universita` di Parma
@version $Date: 2000-09-12 15:24:08 +0200 (mar, 12 set 2000) $ $Revision: 1857 $
*/

/**
 * This class is useful to make MainFrame handle
 * the WindowCLose event.
 */
class WindowCloser extends WindowAdapter {

  private rma myRMA;

  public WindowCloser(rma anRMA) {
    myRMA = anRMA;
  }

  public void windowClosing(WindowEvent e) {
      myRMA.doDelete();
  }
}

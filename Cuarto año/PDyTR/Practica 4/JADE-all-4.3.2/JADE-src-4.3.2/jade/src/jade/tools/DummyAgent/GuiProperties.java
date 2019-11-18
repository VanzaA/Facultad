
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

import javax.swing.*;
import java.util.Properties;
import java.io.*;
/**
@author Giovanni Caire - CSELT S.p.A
@version $Date: 2000-09-12 15:24:08 +0200 (mar, 12 set 2000) $ $Revision: 1857 $
*/

/**
 * This class encapsulates some informations used by the program
 */
public class GuiProperties {
  protected static UIDefaults MyDefaults;
  protected static GuiProperties foo = new GuiProperties();
  public static final String ImagePath = "";
  static {
    Object[] icons = {
      "delete",LookAndFeel.makeIcon(foo.getClass(), "images/delete.gif"),
      "reset",LookAndFeel.makeIcon(foo.getClass(), "images/reset.gif"),
      "open", LookAndFeel.makeIcon(foo.getClass(), "images/open.gif"),
      "openq",LookAndFeel.makeIcon(foo.getClass(), "images/openq.gif"),
      "save",LookAndFeel.makeIcon(foo.getClass(), "images/save.gif"),
      "saveq",LookAndFeel.makeIcon(foo.getClass(), "images/saveq.gif"),
      "send",LookAndFeel.makeIcon(foo.getClass(), "images/send.gif"),
      "set",LookAndFeel.makeIcon(foo.getClass(), "images/set.gif"),
      "reply",LookAndFeel.makeIcon(foo.getClass(), "images/reply.gif"),
      "view",LookAndFeel.makeIcon(foo.getClass(), "images/view.gif")
    };

    MyDefaults = new UIDefaults (icons);
  }

  public static final Icon getIcon(String key) {
    Icon i = MyDefaults.getIcon(key);
    if (i == null) {
      System.out.println(key);
      System.exit(-1);
      return null;
    }
    else return MyDefaults.getIcon(key);
  }
}


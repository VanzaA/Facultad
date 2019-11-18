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



package jade.tools.introspector.gui;

import javax.swing.LookAndFeel;
import javax.swing.UIDefaults;
import javax.swing.Icon;
import java.util.Properties;

import jade.util.Logger;

  /**
   Javadoc documentation for the file
   @author Tiziana Trucco
   
   @version $Date: 2004-07-19 17:54:06 +0200 (lun, 19 lug 2004) $ $Revision: 5217 $
 */

/**
 * This class loads the icons used.
 * @see jade.gui.GuiProperties
 */

public class GuiProperties{

  protected static UIDefaults MyDefaults;
  protected static GuiProperties foo = new GuiProperties();
  public static final String ImagePath = "";
  //loging
  private static Logger logger = Logger.getMyLogger(GuiProperties.class.getName());
  
   static{
    Object[] icons = {
     "Introspector.readyIcon",LookAndFeel.makeIcon(foo.getClass(), "images/behaviour.gif"),
     "Introspector.blockedIcon",LookAndFeel.makeIcon(foo.getClass(), "images/blocked.gif"),
     "Introspector.runningIcon",LookAndFeel.makeIcon(foo.getClass(), "images/running.gif"),
    };
    MyDefaults = new UIDefaults (icons);
	}

   public static final Icon getIcon(String key){
    Icon i = MyDefaults.getIcon(key);
        if (i == null){
	  	if(logger.isLoggable(Logger.WARNING))
	  		logger.log(Logger.WARNING,"Mistake with Icon");
		// System.exit(-1);
		  return null;
		}
	  else
	  	return MyDefaults.getIcon(key);
	}

}  // End of GuiProperties

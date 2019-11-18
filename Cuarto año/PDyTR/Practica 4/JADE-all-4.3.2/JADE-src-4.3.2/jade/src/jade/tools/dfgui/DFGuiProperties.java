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


package jade.tools.dfgui;

import javax.swing.*;
import java.util.Properties;
import java.io.*;

/**
@author Giovanni Caire - CSELT S.p.A.
@version $Date: 2000-09-15 16:55:33 +0200 (ven, 15 set 2000) $ $Revision: 1867 $
*/
/**
 * This class encapsulates some informations used by the program
 */
class DFGuiProperties 
{
	protected static UIDefaults MyDefaults;
	protected static DFGuiProperties foo = new DFGuiProperties();
	public static final String ImagePath = "";
 	static 
	{
		Object[] icons = 
		{
			"exitdf",LookAndFeel.makeIcon(foo.getClass(), "images/exitdf.gif"),
			"closegui",LookAndFeel.makeIcon(foo.getClass(), "images/closegui.gif"),
			"refresh", LookAndFeel.makeIcon(foo.getClass(), "images/refresh.gif"),
			"view", LookAndFeel.makeIcon(foo.getClass(), "images/view.gif"),
			"search", LookAndFeel.makeIcon(foo.getClass(), "images/search.gif"),
			"modify",LookAndFeel.makeIcon(foo.getClass(), "images/modify.gif"),
			"deregister",LookAndFeel.makeIcon(foo.getClass(), "images/deregister.gif"),
			"registeragent",LookAndFeel.makeIcon(foo.getClass(), "images/registeragent.gif"),
			"federatedf",LookAndFeel.makeIcon(foo.getClass(), "images/federatedf.gif"),
			"about",LookAndFeel.makeIcon(foo.getClass(), "images/about.gif"),
			"searchwithconstraints",LookAndFeel.makeIcon(foo.getClass(), "images/searchfed.gif"),	
			"refreshapplet",LookAndFeel.makeIcon(foo.getClass(), "images/connec.gif")	
		};

		MyDefaults = new UIDefaults (icons);
	}

	// synchronized to allows several df to use the same gui code. 
	synchronized public static final Icon getIcon(String key) 
	{
		Icon i = MyDefaults.getIcon(key);
		if (i == null) 
		{
			System.out.println(key);
			System.exit(-1);
			return null;
		}
		else return MyDefaults.getIcon(key);
	}
}


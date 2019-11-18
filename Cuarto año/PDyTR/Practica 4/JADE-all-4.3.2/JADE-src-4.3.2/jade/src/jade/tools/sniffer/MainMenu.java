/*****************************************************************
JADE - Java Agent DEvelopment Framework is a framework to develop multi-agent systems in compliance with the FIPA specifications.
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


package jade.tools.sniffer;

//#DOTNET_EXCLUDE_BEGIN
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.Icon;
import javax.swing.JMenuBar;
import javax.swing.JFrame;
import jade.gui.AboutJadeAction;
//#DOTNET_EXCLUDE_END
/*#DOTNET_INCLUDE_BEGIN
import System.Windows.Forms.MainMenu;
import System.Windows.Forms.MenuItem;
#DOTNET_INCLUDE_END*/

    /**
   Javadoc documentation for the file
   @author Francisco Regi, Andrea Soracchi - Universita` di Parma
   <Br>
   <a href="mailto:a_soracchi@libero.it"> Andrea Soracchi(e-mail) </a>
   @version $Date: 2005-04-15 17:45:02 +0200 (ven, 15 apr 2005) $ $Revision: 5669 $
   */

   /**
    * Sets up the menu bar and the relative menus
    */

//#DOTNET_EXCLUDE_BEGIN
public class MainMenu extends JMenuBar
//#DOTNET_EXCLUDE_END
/*#DOTNET_INCLUDE_BEGIN
public class MainMenuSniffer extends MainMenu
#DOTNET_INCLUDE_END*/
{

 //#DOTNET_EXCLUDE_BEGIN
 private JMenuItem tmp;
 private JMenu menu;
 //#DOTNET_EXCLUDE_END
 /*#DOTNET_INCLUDE_BEGIN
 private MenuItem menu;
 private MenuItem tmp;
 #DOTNET_INCLUDE_END*/
 private ActionProcessor actPro;

 void paintM(boolean enable,SnifferAction obj){
    //#DOTNET_EXCLUDE_BEGIN
    tmp = menu.add(obj);
    tmp.setEnabled(enable);
    //#DOTNET_EXCLUDE_END
    /*#DOTNET_INCLUDE_BEGIN
    tmp = new MenuItem();
	tmp.set_Text( obj.getActionName() );
	tmp.add_Click( new System.EventHandler(obj.OnClick) );
	tmp.set_Enabled( enable );
	menu.get_MenuItems().Add( tmp );
	#DOTNET_INCLUDE_END*/
 }

 //#DOTNET_EXCLUDE_BEGIN
 public MainMenu(MainWindow mainWnd,ActionProcessor actPro) {
 //#DOTNET_EXCLUDE_END
 /*#DOTNET_INCLUDE_BEGIN
 public MainMenuSniffer(MainWindow mainWnd,ActionProcessor actPro) {
 #DOTNET_INCLUDE_END*/
   super();
   this.actPro=actPro;

   //#DOTNET_EXCLUDE_BEGIN
   menu = new JMenu ("Actions");
   //#DOTNET_EXCLUDE_END
   /*#DOTNET_INCLUDE_BEGIN
   menu = new MenuItem();
   menu.set_Text("Actions");
   #DOTNET_INCLUDE_END*/
   paintM(true,(SnifferAction)actPro.actions.get(actPro.DO_SNIFFER_ACTION));
   paintM(true,(SnifferAction)actPro.actions.get(actPro.DO_NOT_SNIFFER_ACTION));
   paintM(true,(SnifferAction)actPro.actions.get(actPro.SWOW_ONLY_ACTION));
   //#DOTNET_EXCLUDE_BEGIN
   menu.addSeparator();
   //#DOTNET_EXCLUDE_END
   /*#DOTNET_INCLUDE_BEGIN
   MenuItem sep = new MenuItem();
   sep.set_Text( "-" );
   menu.get_MenuItems().Add( sep );
   #DOTNET_INCLUDE_END*/

   paintM(true,(SnifferAction)actPro.actions.get(actPro. CLEARCANVAS_ACTION));

   //#DOTNET_EXCLUDE_BEGIN
   menu.addSeparator();
   //#DOTNET_EXCLUDE_END
   /*#DOTNET_INCLUDE_BEGIN
   sep = new MenuItem();
   sep.set_Text( "-" );
   menu.get_MenuItems().Add( sep );
   #DOTNET_INCLUDE_END*/

   paintM(true,(SnifferAction)actPro.actions.get(actPro.DISPLAYLOGFILE_ACTION));
   paintM(true,(SnifferAction)actPro.actions.get(actPro.WRITELOGFILE_ACTION));
   paintM(true,(SnifferAction)actPro.actions.get(actPro.WRITEMESSAGELIST_ACTION));

   //#DOTNET_EXCLUDE_BEGIN
   menu.addSeparator();
   //#DOTNET_EXCLUDE_END
   /*#DOTNET_INCLUDE_BEGIN
   sep = new MenuItem();
   sep.set_Text( "-" );
   menu.get_MenuItems().Add( sep );
   #DOTNET_INCLUDE_END*/
 
   paintM(true,(SnifferAction)actPro.actions.get(actPro. EXIT_SNIFFER_ACTION));
   
   //#DOTNET_EXCLUDE_BEGIN
   add(menu);
   menu = new JMenu ("About");
   menu.add(new AboutJadeAction((JFrame)mainWnd));
   menu.add(new AboutBoxAction((JFrame)mainWnd));
   add(menu);
   //#DOTNET_EXCLUDE_END
   /*#DOTNET_INCLUDE_BEGIN
   this.get_MenuItems().Add( menu );
   menu = new MenuItem();
   menu.set_Text( "About" );
   this.get_MenuItems().Add( menu );
   #DOTNET_INCLUDE_END*/
  }

} 

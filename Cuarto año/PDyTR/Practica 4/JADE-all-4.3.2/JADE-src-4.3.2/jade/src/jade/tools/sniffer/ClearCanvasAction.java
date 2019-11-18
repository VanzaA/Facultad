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
package jade.tools.sniffer;

import java.awt.event.ActionEvent;
//#DOTNET_EXCLUDE_BEGIN
import java.awt.Font;
//#DOTNET_EXCLUDE_END
/*#DOTNET_INCLUDE_BEGIN
import System.Drawing.Font;
import System.Drawing.FontStyle;
#DOTNET_INCLUDE_END*/

   /**
   Javadoc documentation for the file
   @author Francisco Regi, Andrea Soracchi - Universita` di Parma
   <Br>
   <a href="mailto:a_soracchi@libero.it"> Andrea Soracchi(e-mail) </a>
   @version $Date: 2005-04-15 17:45:02 +0200 (ven, 15 apr 2005) $ $Revision: 5669 $
   */

 /**
 * Clears the Message Canvas.
 *
 * @see jade.tools.sniffer.FixedAction
 */

public class ClearCanvasAction extends FixedAction{

  private MainPanel mainPanel;
  //#DOTNET_EXCLUDE_BEGIN
  private Font font = new Font("Helvetica",Font.ITALIC,12);
  //#DOTNET_EXCLUDE_END
  /*#DOTNET_INCLUDE_BEGIN
  private Font font = new Font("Helvetica", 12, FontStyle.Italic);
  #DOTNET_INCLUDE_END*/

  public ClearCanvasAction(ActionProcessor actPro,MainPanel mainPanel){
    super("ClearCanvasActionIcon","Clear Canvas",actPro);
    this.mainPanel=mainPanel;
  }

  public void doAction (){
    mainPanel.panelcan.canvMess.removeAllMessages();
    //#DOTNET_EXCLUDE_BEGIN
    mainPanel.textArea.setFont(font);
    mainPanel.textArea.setText("                                                                 No Message");
    //#DOTNET_EXCLUDE_END
    /*#DOTNET_INCLUDE_BEGIN
	mainPanel.textArea.set_Font(font);
    mainPanel.textArea.set_Text("                                                                 No Message");
	#DOTNET_INCLUDE_END*/
  }

} 

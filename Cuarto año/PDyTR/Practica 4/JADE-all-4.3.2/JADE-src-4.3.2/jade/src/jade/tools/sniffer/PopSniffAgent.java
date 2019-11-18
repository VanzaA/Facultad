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
import java.awt.event.ActionEvent;
//#DOTNET_EXCLUDE_END
import jade.util.leap.List;
import jade.util.leap.ArrayList;

/*#DOTNET_INCLUDE_BEGIN
import System.Windows.Forms.MenuItem;
import System.Windows.Forms.MouseEventArgs;
#DOTNET_INCLUDE_END*/

  /**
   Javadoc documentation for the file
   @author Francisco Regi, Andrea Soracchi - Universita` di Parma
   <Br>
   <a href="mailto:a_soracchi@libero.it"> Andrea Soracchi(e-mail) </a>
   @version $Date: 2005-04-15 17:45:02 +0200 (ven, 15 apr 2005) $ $Revision: 5669 $
 */

  /**
   * This class includes the method ActionPerformed that is
   * associated with the PopupMenu of the Agent in the canvas.
   * @see jade.tools.sniffer.PopNoSniffAgent
   * @see jade.tools.sniffer.PopShowAgent
   */

public class PopSniffAgent 
	//#DOTNET_EXCLUDE_BEGIN
	extends AbstractPopup 
	//#DOTNET_EXCLUDE_END
	/*#DOTNET_INCLUDE_BEGIN
	extends MenuItem
	#DOTNET_INCLUDE_END*/
{

private PopupAgent popAg;
private Sniffer mySniffer;
private List sniffedAgent=new ArrayList();
private MMCanvas canvAgent;

 public PopSniffAgent(PopupAgent popAg,Sniffer mySniffer,MMCanvas canvAgent) {
   super ("Do Sniff this Agent");
   this.popAg=popAg;
   this.mySniffer=mySniffer;
   this.canvAgent=canvAgent;
 }
 //#DOTNET_EXCLUDE_BEGIN
 public void actionPerformed(ActionEvent avt) {
 //#DOTNET_EXCLUDE_END
 /*#DOTNET_INCLUDE_BEGIN
 public void OnClick(System.EventArgs e) {
 #DOTNET_INCLUDE_END*/
   if (!canvAgent.isPresent(popAg.agent.agentName))
     canvAgent.addAgent(popAg.agent);   // add Agent in the Canvas
     canvAgent.rAgfromNoSniffVector(popAg.agent);
     sniffedAgent.add(popAg.agent);
     mySniffer.sniffMsg(sniffedAgent,Sniffer.SNIFF_ON);   // Sniff the Agents
     sniffedAgent.clear();
   }

} 

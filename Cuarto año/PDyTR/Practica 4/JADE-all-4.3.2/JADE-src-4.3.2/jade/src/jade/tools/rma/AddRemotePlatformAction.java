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

import jade.gui.AIDGui;
import jade.core.AID;

/**
   
   @author Tiziana Trucco - CSELT S.p.A.
   @version $Date: 2001-04-18 18:26:13 +0200 (mer, 18 apr 2001) $ $Revision: 2180 $
 */
class AddRemotePlatformAction extends FixedAction
{

  private rma myRMA;
  private MainWindow main;
  
  AddRemotePlatformAction(rma anRMA,ActionProcessor actPro,MainWindow mW) {
  	
     super ("AddRemotePlatformActionIcon","Add Platform via AMS AID",actPro);
     myRMA = anRMA;
     main = mW;
      
  }

   public void doAction() {
    
   	 AIDGui gui = new AIDGui(main);
   	 gui.setTitle("Insert the remote AMS AID");
   	 AID remoteAMS = gui.ShowAIDGui(null,true,true);
   	 try{
   	 	if (remoteAMS != null)
		    myRMA.addRemotePlatform(remoteAMS);
   	 			
   	    	 }catch(Exception e){e.printStackTrace();}
  }

}  // End of ShowDFGuiAction



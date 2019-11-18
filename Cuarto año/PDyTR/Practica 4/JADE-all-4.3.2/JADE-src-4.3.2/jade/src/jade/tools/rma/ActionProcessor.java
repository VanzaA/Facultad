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

import java.util.Map;
import javax.swing.JTree;
import javax.swing.tree.TreePath;
import javax.swing.JOptionPane;
import javax.swing.JFrame;
import java.util.HashMap;
import jade.gui.AgentTree;

/**
 
 @author Francisco Regi, Andrea Soracchi - Universita` di Parma
 @version $Date: 2006-06-05 13:19:41 +0200 (lun, 05 giu 2006) $ $Revision: 5885 $
 */
class ActionProcessor {
	
	private MainPanel panel;
	private RMAAction action;
	
	public static final String  START_ACTION="Start new Agent";
	public static final String  MANAGE_MTPS_ACTION = "Manage Installed MTPs";
	public static final String  INSTALL_MTP_ACTION="Install a new MTP";
	public static final String  UNINSTALL_MTP_ACTION="Uninstall an MTP";
	public static final String  KILL_ACTION="Kill";
	public static final String  SUSPEND_ACTION="Suspend Agent";
	public static final String  RESUME_ACTION="Resume Agent";
	public static final String  CHANGE_AGENT_OWNERSHIP_ACTION = "Change Agent Ownership";
	public static final String  CUSTOM_ACTION="Custom Agent";
	public static final String  SNIFFER_ACTION="Start Sniffer";     
	public static final String  DUMMYAG_ACTION="Start DummyAgent";
	public static final String  LOGGERAG_ACTION="Start LoggerAgent";
	public static final String  INTROSPECTOR_ACTION = "Start IntrospectAgent "; 
	public static final String  CLOSE_ACTION="Close RMA";
	public static final String  EXIT_ACTION="Exit RMA";
	public static final String  SHUTDOWN_ACTION="Shutdown action";
	public static final String  SHOWDF_ACTION="ShowDfGui Action";
	public static final String  MOVEAGENT_ACTION="Migrate Agent";
	public static final String  CLONEAGENT_ACTION="Clone Agent";
	public static final String  SAVEAGENT_ACTION="Save Agent";
	public static final String  LOADAGENT_ACTION="Load Agent";
	public static final String  FREEZEAGENT_ACTION="Freeze Agent";
	public static final String  THAWAGENT_ACTION="Thaw Agent";
	public static final String  SAVECONTAINER_ACTION="Save Container";
	public static final String  LOADCONTAINER_ACTION="Load Container";
	public static final String  ADDREMOTEPLATFORM_ACTION ="Add Platform via AMS AID";
	public static final String  ADDREMOTEPLATFORMFROMURL_ACTION = "Add Platform via URL"; 
	public static final String  VIEWPLATFORM_ACTION ="View AP Description";
	public static final String  REFRESHAPDESCRIPTION_ACTION = "Refresh AP Description";
	public static final String  REMOVEREMOTEAMS_ACTION = "Remove Remote Platform";
	public static final String  REFRESHAMSAGENT_ACTION = "Refresh Agent List";
	public static final String  REGISTERREMOTEAGENTWITHAMS_ACTION = "Register Remote Agent with local AMS";
	
	public static final Map actions=new HashMap();
	
	public ActionProcessor(rma anRma,MainWindow mWnd,MainPanel panel) {
		this.panel=panel;
		actions.put(START_ACTION,new StartNewAgentAction(anRma, mWnd,this));
		actions.put(MANAGE_MTPS_ACTION,new ManageMTPsAction(mWnd,this));
		actions.put(INSTALL_MTP_ACTION,new InstallMTPAction(anRma,this));
		actions.put(UNINSTALL_MTP_ACTION,new UninstallMTPAction(anRma,this));
		actions.put(KILL_ACTION,new KillAction(KILL_ACTION, anRma,this));
		actions.put(SUSPEND_ACTION,new SuspendAction(anRma,this));
		actions.put(RESUME_ACTION,new ResumeAction(anRma,this));
		actions.put(CHANGE_AGENT_OWNERSHIP_ACTION, new ChangeAgentOwnershipAction(anRma, this, mWnd));
		actions.put(CUSTOM_ACTION,new CustomAction(anRma, mWnd,this));
		actions.put(SNIFFER_ACTION,new SnifferAction(anRma,this));
		actions.put(DUMMYAG_ACTION,new DummyAgentAction(anRma,this));
		actions.put(LOGGERAG_ACTION,new LogManagerAgentAction(anRma,this));
		actions.put(INTROSPECTOR_ACTION,new IntrospectorAction(anRma,this));
		actions.put(CLOSE_ACTION,new CloseRMAAction(anRma,this));
		actions.put(EXIT_ACTION,new ExitAction(anRma,this));
		actions.put(SHUTDOWN_ACTION,new ShutDownAction(anRma,this));
		actions.put(SHOWDF_ACTION,new ShowDFGuiAction(anRma,this));
		actions.put(MOVEAGENT_ACTION, new MoveAgentAction(anRma,this,mWnd));
		actions.put(CLONEAGENT_ACTION, new CloneAgentAction(anRma, this,mWnd));
		actions.put(SAVEAGENT_ACTION, new SaveAgentAction(anRma, this));
		actions.put(LOADAGENT_ACTION, new LoadAgentAction(anRma, this, mWnd));
		actions.put(FREEZEAGENT_ACTION, new FreezeAgentAction(anRma, this));
		actions.put(THAWAGENT_ACTION, new ThawAgentAction(anRma, this, mWnd));
		actions.put(SAVECONTAINER_ACTION, new SaveContainerAction(anRma, this));
		actions.put(LOADCONTAINER_ACTION, new LoadContainerAction(anRma, this));
		actions.put(ADDREMOTEPLATFORM_ACTION, new AddRemotePlatformAction(anRma,this,mWnd));
		actions.put(VIEWPLATFORM_ACTION, new ViewAPDescriptionAction(anRma,this));
		actions.put(REFRESHAPDESCRIPTION_ACTION, new RefreshAPDescriptionAction(anRma,this));
		actions.put(REMOVEREMOTEAMS_ACTION,new RemoveRemoteAMSAction(anRma,this));
		actions.put(REFRESHAMSAGENT_ACTION, new RefreshAMSAgentAction(anRma,this));
		actions.put(ADDREMOTEPLATFORMFROMURL_ACTION, new AddRemotePlatformFromURLAction(anRma,this,mWnd));
		actions.put(REGISTERREMOTEAGENTWITHAMS_ACTION, new RegisterRemoteAgentAction(anRma,this));
		
	} // End builder
	
	public void process(RMAAction a) {
		int lungpath;
		AgentTree.Node now;
		
		FixedAction fx;
		TreePath paths[];
		action = a;
		paths = panel.treeAgent.tree.getSelectionPaths();
		
		
		// Fixed actions are without parameters, so they are executed once,
		// regardless how many tree elements are selected
		if (action instanceof FixedAction)
			fixedAct();
		
		// Other actions are executed for every selected tree element. This
		// means that, if no selection is present, no action is performed.
		else {
			if(paths != null) {
				lungpath=paths.length;
				for (int i=0;i<lungpath;i++) {
					now = (AgentTree.Node) (paths[i].getLastPathComponent());
					if (action instanceof AgentAction) agentAct(now);
					else if (action instanceof ContainerAction) containerAct(now);
					else if (action instanceof GenericAction) genericAct(now);
					else if(action instanceof PlatformAction) platformAct(now);
				}
			}
			else {
				//path null
				if (action instanceof AgentAction)
					JOptionPane.showMessageDialog(new JFrame(),"You must select an agent in the Tree","Start Procedure Error",JOptionPane.ERROR_MESSAGE);
				else
					if
					(action instanceof DummyAgentAction || action instanceof SnifferAction || action instanceof IntrospectorAction || action instanceof LogManagerAgentAction )	
						containerAct(null);
					else
						if(action instanceof PlatformAction)
							JOptionPane.showMessageDialog(new JFrame(), "You must select a platform","Error", JOptionPane.ERROR_MESSAGE);
						else
							JOptionPane.showMessageDialog(new JFrame(), "You must select an agent-platform or a agent-container in the Tree","Start Procedure Error", JOptionPane.ERROR_MESSAGE);
				
			}
		}
		
	} // End Process
	
	private void fixedAct(){
		FixedAction fx=(FixedAction)action;
		fx.doAction();
	}
	
	private void agentAct(AgentTree.Node node){
		AgentAction ag=(AgentAction) action;
		AgentTree.AgentNode nod;
		if(node instanceof AgentTree.AgentNode) {
			nod=(AgentTree.AgentNode)node;
			ag.doAction(nod);
		}    
	}
	
	private void containerAct(AgentTree.Node node){
		ContainerAction ac=(ContainerAction) action;
		AgentTree.ContainerNode nod;
		if ((ac instanceof DummyAgentAction || ac instanceof SnifferAction || ac instanceof IntrospectorAction || ac instanceof LogManagerAgentAction) && (node == null || node instanceof AgentTree.SuperContainer))
			ac.doAction(null);
		else	
			try{
				if(node instanceof AgentTree.ContainerNode){
					nod=(AgentTree.ContainerNode)node;
					ac.doAction(nod);
				}
				else throw new StartException();
			} catch(StartException ex) {
				StartException.handle();
			}
	}
	
	private void genericAct(AgentTree.Node node){
		AgentTree.AgentNode nod1;
		AgentTree.ContainerNode nod2;
		GenericAction ga=(GenericAction) action;
		if(node instanceof AgentTree.ContainerNode){
			nod2=(AgentTree.ContainerNode)node;
			ga.doAction(nod2);
		}
		else if(node instanceof AgentTree.AgentNode) {
			nod1=(AgentTree.AgentNode)node;
			ga.doAction(nod1);
		}
	}
	
	private void platformAct(AgentTree.Node node){
		
		PlatformAction ac = (PlatformAction) action; 
		if((node instanceof AgentTree.LocalPlatformFolderNode) || (node instanceof AgentTree.RemotePlatformNode))
			ac.doAction(node);
		
	}
	
}

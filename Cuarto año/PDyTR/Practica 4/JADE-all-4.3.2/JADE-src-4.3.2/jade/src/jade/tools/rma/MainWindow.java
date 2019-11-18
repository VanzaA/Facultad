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

import java.awt.*;

import java.net.InetAddress;

import java.util.Enumeration;
import java.util.Vector;
import java.util.List;
import java.util.LinkedList;
import java.util.Map;
import java.util.TreeMap;
import java.util.Iterator;

import javax.swing.*;
import javax.swing.tree.*;
import javax.swing.tree.MutableTreeNode;

import jade.core.AID;
import jade.core.ContainerID;

import jade.gui.AgentTreeModel;
import jade.gui.AgentTree;

import jade.lang.acl.ACLMessage;
import jade.gui.GuiProperties;
import jade.gui.APDescriptionPanel;
import jade.domain.FIPAAgentManagement.APDescription;
import jade.domain.FIPAAgentManagement.AMSAgentDescription;

/**

 @author Francisco Regi, Andrea Soracchi - Universita` di Parma
 @version $Date: 2010-04-12 18:07:05 +0200 (lun, 12 apr 2010) $ $Revision: 6302 $
 */
class MainWindow extends JFrame {

	private MainPanel tree;
	private ActionProcessor actPro;
	private PopupMenuAgent popA;
	private PopupMenuFrozenAgent popFA;
	private PopupMenuContainer popC;
	private PopupMenuPlatform popP;
	private PopupMenuRemotePlatform popRP;
	private InstallMTPDialog installDlg = new InstallMTPDialog(this, true);
	private ManageMTPsDialog manageDlg;
	private String logojade = "images/logosmall.jpg";

	private List containerNames = new LinkedList();
	private Map addresses = new TreeMap(String.CASE_INSENSITIVE_ORDER);


	public MainWindow (rma anRMA) {
		super(anRMA.getName() +" - JADE Remote Agent Management GUI");

		manageDlg = new ManageMTPsDialog(anRMA, this, false, addresses);
		tree = new MainPanel(anRMA, this);
		actPro = new ActionProcessor(anRMA, this, tree);
		setJMenuBar(new MainMenu(this,actPro));
		popA = new PopupMenuAgent(actPro);
		popFA = new PopupMenuFrozenAgent(actPro);
		popC = new PopupMenuContainer(actPro);
		popP = new PopupMenuPlatform(actPro);
		popRP = new PopupMenuRemotePlatform(actPro);
		tree.treeAgent.setNewPopupMenu(AgentTree.AGENT_TYPE, popA);
		tree.treeAgent.setNewPopupMenu(AgentTree.FROZEN_AGENT_TYPE, popFA);
		tree.treeAgent.setNewPopupMenu(AgentTree.CONTAINER_TYPE, popC);
		tree.treeAgent.setNewPopupMenu(AgentTree.REMOTE_PLATFORM_TYPE, popRP);
		JPopupMenu popupRemote = new JPopupMenu();
		JMenuItem temp = popupRemote.add((RMAAction)actPro.actions.get(actPro.CUSTOM_ACTION));
		temp.setIcon(null);
		temp = popupRemote.add((RMAAction)actPro.actions.get(actPro.REGISTERREMOTEAGENTWITHAMS_ACTION));
		temp.setIcon(null);
		temp.setEnabled(false);
		tree.treeAgent.setNewPopupMenu(AgentTree.REMOTE_AGENT_TYPE, popupRemote);
		tree.treeAgent.setNewPopupMenu(AgentTree.TREE_ROOT_TYPE, popP);
		JPopupMenu popLocalPlatform = new JPopupMenu();
		JMenuItem tmp = popLocalPlatform.add((RMAAction)actPro.actions.get(actPro.VIEWPLATFORM_ACTION));
		tmp.setIcon(null);
		popLocalPlatform.add((RMAAction)actPro.actions.get(actPro.MANAGE_MTPS_ACTION));
		tree.treeAgent.setNewPopupMenu(AgentTree.LOCAL_PLATFORM_TYPE, popLocalPlatform);

		setForeground(Color.black);
		setBackground(Color.lightGray);
		Image image = getToolkit().getImage(getClass().getResource(logojade));
		setIconImage(image);
		addWindowListener(new WindowCloser(anRMA));

		getContentPane().add(new ToolBar(tree,this,actPro),"North"); // new ToolBar(tree, this, ActionProcessor.actions)
		getContentPane().add(tree,"Center");
	}

	public void ShowCorrect() {
		pack();
		setSize(600, 400);
		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
		int centerX = (int)screenSize.getWidth() / 2;
		int centerY = (int)screenSize.getHeight() / 2;
		setLocation(centerX - 300, centerY - 200);
		tree.adjustDividersLocation();

		setVisible(true);
		toFront();
	}

	// Perform asynchronous disposal to avoid nasty InterruptedException printout.
	public void disposeAsync() {
		//dispose();
		final Window toDispose = this;
		// Make AWT Event Dispatcher thread dispose RMA window for us.
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				toDispose.dispose();
			}
		});

	}

	public AgentTreeModel getModel() {
		return tree.treeAgent.getModel();
	}

	public void resetTree() {
		Runnable resetIt = new Runnable() {

			public void run() {
				tree.treeAgent.clearLocalPlatform();
			}
		};
		SwingUtilities.invokeLater(resetIt);
	}

	public void addContainer(final String name, final InetAddress addr) {
		Runnable addIt = new Runnable() {
			public void run() {
				//MutableTreeNode node = tree.treeAgent.createNewNode(name, 0);
				//tree.treeAgent.addContainerNode((AgentTree.ContainerNode)node,"FIPACONTAINER",addr);
				tree.treeAgent.addContainerNode(name, addr);
				containerNames.add(name);
				manageDlg.setData(containerNames, addresses);
			}
		};
		SwingUtilities.invokeLater(addIt);
	}

	public void removeContainer(final String name) {

		// Remove a container from the tree model
		Runnable removeIt = new Runnable() {

			public void run() {
				tree.treeAgent.removeContainerNode(name);
				containerNames.remove(name);
				manageDlg.setData(containerNames, addresses);
			}
		};
		SwingUtilities.invokeLater(removeIt);
	}

	public void addAgent(final String containerName, final AID agentID, final String state, final String ownership) {

		// Add an agent to the specified container
		Runnable addIt = new Runnable() {
			public void run() {
				String agentName = agentID.getName();
				Iterator add = agentID.getAllAddresses();
				String agentAddresses = "";
				while(add.hasNext()) {
					agentAddresses = agentAddresses + add.next() + " ";
				}
				tree.treeAgent.addAgentNode(agentName, agentAddresses, containerName, state, ownership);
			}
		};
		SwingUtilities.invokeLater(addIt);
	}

	public void removeAgent(final String containerName, final AID agentID) {

		// Remove an agent from the specified container
		Runnable removeIt = new Runnable() {
			public void run() {
				String agentName = agentID.getName();
				tree.treeAgent.removeAgentNode(containerName, agentName);
			}
		};
		SwingUtilities.invokeLater(removeIt);
	}

	public void modifyAgent(final String containerName, final AID agentID, final String state, final String ownership) {

		// Remove an agent from the specified container
		Runnable modifyIt = new Runnable() {
			public void run() {
				String agentName = agentID.getName();
				tree.treeAgent.modifyAgentNode(containerName, agentName, null, state, ownership);
			}
		};
		SwingUtilities.invokeLater(modifyIt);
	}

	public void moveAgent(final String fromContainer, final String toContainer, final AID agentID) {

		// Move an agent from a container node to another
		Runnable moveIt = new Runnable() {
			public void run() {
				String agentName = agentID.getName();
				tree.treeAgent.moveAgentNode(fromContainer, toContainer, agentName);
			}
		};
		SwingUtilities.invokeLater(moveIt);
	}

	public void modifyFrozenAgent(final String oldContainer, final String newContainer, final AID agentID) {

		// Freeze an agent to the specified container
		Runnable freezeIt = new Runnable() {
			public void run() {
				String agentName = agentID.getName();
				tree.treeAgent.freezeAgentNode(oldContainer, newContainer, agentName);
			}
		};
		SwingUtilities.invokeLater(freezeIt);
	}

	public void modifyThawedAgent(final String oldContainer, final String newContainer, final AID agentID) {

		// Thaw an agent to the specified container
		Runnable thawIt = new Runnable() {
			public void run() {
				String agentName = agentID.getName();
				tree.treeAgent.thawAgentNode(oldContainer, newContainer, agentName);
			}
		};
		SwingUtilities.invokeLater(thawIt);
	}

	public void addAddress(final String address, final String where) {
		Runnable addIt = new Runnable() {
			public void run() {
				List addrs = (List)addresses.get(where);
				if(addrs == null) {
					addrs = new LinkedList();
					addresses.put(where, addrs);
				}
				addrs.add(address);
				manageDlg.setData(containerNames, addresses);
			}
		};
		SwingUtilities.invokeLater(addIt);


	}

	public void addRemotePlatformFolder(){
		Runnable addIt = new Runnable(){
			public void run(){
				PopupMenuPlatform menu = new PopupMenuPlatform(actPro);
				tree.treeAgent.setNewPopupMenu(AgentTree.REMOTE_PLATFORMS_FOLDER_TYPE, menu);
				tree.treeAgent.addRemotePlatformsFolderNode();

			}
		};
		SwingUtilities.invokeLater(addIt);
	}


	public void addRemotePlatform(AID name,APDescription profile){

		final APDescription desc = profile;
		final AID ams = name;
		Runnable addIt = new Runnable(){

			public void run(){
				tree.treeAgent.addRemotePlatformNode(ams,desc);

			}
		};
		SwingUtilities.invokeLater(addIt);
	}


	public void refreshRemoteAgentsInRemotePlatform(final APDescription platform, final Iterator remoteAgents){
		// Add an agent to a specified AMS
		Runnable addIt = new Runnable() {
			public void run() {
				// Clear all previous agents
				tree.treeAgent.clearRemotePlatformAgents(platform.getName());
				
				// Add new current agents
				while(remoteAgents.hasNext()){
					AMSAgentDescription agent = (AMSAgentDescription) remoteAgents.next();
					tree.treeAgent.addRemoteAgentNode(agent,platform.getName());
				}
			}
		};
		SwingUtilities.invokeLater(addIt);
	}

	public void removeRemotePlatform(final String platformName){

		Runnable addIt = new Runnable(){

			public void run(){
				tree.treeAgent.removeRemotePlatformNode(platformName);

			}
		};
		SwingUtilities.invokeLater(addIt);
	}

	public void removeAddress(final String address, final String where) {
		Runnable removeIt = new Runnable() {
			public void run() {
				List addrs = (List)addresses.get(where);
				if (addrs != null) {
					addrs.remove(address);
					if(addrs.isEmpty())
						addresses.remove(where);
					manageDlg.setData(containerNames, addresses);
				}
			}
		};
		SwingUtilities.invokeLater(removeIt);
	}


	public void refreshLocalPlatformName(final String name){

		Runnable refreshName = new Runnable(){
			public void run(){
				tree.treeAgent.refreshLocalPlatformName(name);
			}

		};
		SwingUtilities.invokeLater(refreshName);

	}



	public void showErrorDialog(String text, ACLMessage msg) {
		String messages[] = new String[3];
		messages[0] = text;
		messages[1] = "";
		messages[2] = "Do you want to view the ACL message ?";
		int answer = JOptionPane.showConfirmDialog(this, messages, "RMA Error !!!", JOptionPane.YES_NO_OPTION, JOptionPane.ERROR_MESSAGE);
		switch(answer) {
		case JOptionPane.YES_OPTION:
			jade.gui.AclGui.showMsgInDialog(msg, this);
			break;
		default:
			break;
		}
	}

	public boolean showExitDialog(String message) {
		int n = JOptionPane.showConfirmDialog(this, "Are you really sure to exit ?", message, JOptionPane.YES_NO_OPTION);
		if(n == JOptionPane.YES_OPTION)
			return true;
		else
			return false;
	}

	public boolean showInstallMTPDialog(jade.domain.JADEAgentManagement.InstallMTP imtp) {
		String[] names = (String[])containerNames.toArray(new String[0]);
		installDlg.reset(names, imtp.getContainer().getName());
		installDlg.pack();
		installDlg.setVisible(true);
		imtp.setContainer(new ContainerID(installDlg.getContainer(), null));
		imtp.setAddress(installDlg.getAddress());
		imtp.setClassName(installDlg.getClassName());
		return installDlg.isConfirmed();
	}

	public boolean showUninstallMTPDialog(jade.domain.JADEAgentManagement.UninstallMTP umtp) {
		String where = umtp.getContainer().getName();
		List addrs = (List)addresses.get(where);
		if(addrs == null) {
			JOptionPane.showMessageDialog(this, "No MTPs are currently installed on this container.",
					"Error during MTP removal", JOptionPane.ERROR_MESSAGE);
			return false;
		}

		Object[] names = addrs.toArray();
		String address = (String)JOptionPane.showInputDialog(this, "Choose the MTP to remove.",
				"Remove an MTP", JOptionPane.INFORMATION_MESSAGE,
				null, names, names[0]);
		if(address != null) {
			umtp.setAddress(address);
			return true;
		}
		else
			return false;
	}

	public void showManageMTPsDialog() {
		manageDlg.showCentered();
	}


	public void viewAPDescriptionDialog(APDescription ap, String title){

		if (ap != null)
		{
			APDescriptionPanel.showAPDescriptionInDialog(ap,this,title);
		}

	}




	private void setUI(String ui) {
		try {
			UIManager.setLookAndFeel("com.sun.java.swing.plaf."+ui);
			SwingUtilities.updateComponentTreeUI(this);
			pack();
		}
		catch(Exception e) {
			System.out.println(e);
			e.printStackTrace(System.out);
		}
	}

	/**
	 enables Motif L&F
	 */
	public void setUI2Motif() {
		setUI("motif.MotifLookAndFeel");
	}

	/**
	 enables Windows L&F
	 */
	public void setUI2Windows() {
		setUI("windows.WindowsLookAndFeel");
	}

	/**
	 enables Multi L&F
	 */
	public void setUI2Multi() {
		setUI("multi.MultiLookAndFeel");
	}

	/**
	 enables Metal L&F
	 */
	public void setUI2Metal() {
		setUI("metal.MetalLookAndFeel");
	}

} 

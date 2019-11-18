package jade.tools.logging.gui;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

import java.net.InetAddress;
import java.net.URL;
import java.util.Map;
import java.util.HashMap;

import jade.content.onto.basic.Action;
import jade.core.Agent;
import jade.core.AID;
import jade.core.ContainerID;
import jade.gui.AgentTree;
import jade.tools.logging.LogManager;
import jade.tools.logging.JavaLoggingLogManagerImpl;
import jade.domain.FIPAException;
import jade.domain.FIPANames;
import jade.domain.FIPAService;
import jade.domain.JADEAgentManagement.CreateAgent;
import jade.domain.JADEAgentManagement.KillAgent;
import jade.domain.JADEAgentManagement.JADEManagementOntology;
import jade.gui.AclGui;
import jade.lang.acl.ACLMessage;

/**
 * LogManager agent main GUI
 * 
 * @author Giovanni Caire - TILAB
 */
public class LogManagerGUI extends JFrame {
	private static final String DEFAULT_LOG_MANAGER_CLASS = "jade.tools.logging.JavaLoggingLogManagerImpl";
	
	private Agent myAgent;
	
	private AgentTree agentTree;
	private JDesktopPane desktopPane;
	private JSplitPane jsp;
	
	private AbstractAction startManagingLogAction, stopManagingLogAction, setDefaultLoggingSystemAction, exitAction;
	
	private Map managedContainers = new HashMap();
	private LogManager defaultLogManager;
	
	public LogManagerGUI(Agent a) {
		super(a.getName());
		
		myAgent = a;
		
		setIconImage(getToolkit().getImage(getClass().getResource("/jade/gui/images/logger.gif")));
		setTitle("JADE Log Manager Agent ("+myAgent.getLocalName()+")");
		
		startManagingLogAction = new StartManagingLogAction(this);
		stopManagingLogAction = new StopManagingLogAction(this);
		setDefaultLoggingSystemAction = new SetDefaultLoggingSystemAction(this);
		exitAction = new ExitAction(this);
		
		//////////////////////////////////////////////
		// Main menu and toolbar
		//////////////////////////////////////////////
		JMenuBar jmb = new JMenuBar();
		JMenu menu = null;
		menu = new JMenu("Settings");
		menu.add(setDefaultLoggingSystemAction);
		menu.addSeparator();
		menu.add(exitAction);
		jmb.add(menu);
		
		menu = new JMenu("Logs");
		menu.add(startManagingLogAction);
		menu.add(stopManagingLogAction);
		jmb.add(menu);
		
		setJMenuBar(jmb);
		
		JToolBar bar = new JToolBar();
		URL url = null;
		Dimension d = new Dimension(32, 32);
		
		JButton startB = new JButton();
		startB.setToolTipText("Start managing log on the selected container");
		startB.setAction(startManagingLogAction);
		url = getClass().getClassLoader().getResource("jade/tools/logging/gui/images/bullet1.gif");
		startB.setIcon(new ImageIcon(url));
		startB.setText(null);
		startB.setMaximumSize(d);
		startB.setMinimumSize(d);
		startB.setPreferredSize(d);
		
		JButton stopB = new JButton();
		stopB.setToolTipText("Stop managing log on the selected container");
		stopB.setAction(stopManagingLogAction);
		url = getClass().getClassLoader().getResource("jade/tools/logging/gui/images/bullet2.gif");
		stopB.setIcon(new ImageIcon(url));
		stopB.setText(null);
		stopB.setMaximumSize(d);
		stopB.setMinimumSize(d);
		stopB.setPreferredSize(d);
		
		JButton setB = new JButton();
		setB.setToolTipText("Set the default logging system to be managed");
		setB.setAction(setDefaultLoggingSystemAction);
		url = getClass().getClassLoader().getResource("jade/gui/images/tick_blue.gif");
		setB.setIcon(new ImageIcon(url));
		setB.setText(null);
		setB.setMaximumSize(d);
		setB.setMinimumSize(d);
		setB.setPreferredSize(d);
		
		bar.add(setB);
		bar.addSeparator();
		bar.add(startB);
		bar.add(stopB);
		
		getContentPane().add(bar, BorderLayout.NORTH);
		
		//////////////////////////////////////////////
		// Agent tree and space for internal frames
		//////////////////////////////////////////////
		Font f;
		f = new Font("SanSerif", Font.PLAIN, 14);
		setFont(f);
		agentTree = new AgentTree(f);
		JPopupMenu popup = new JPopupMenu();
		popup.add(startManagingLogAction);
		popup.add(stopManagingLogAction);
		agentTree.setNewPopupMenu(AgentTree.CONTAINER_TYPE, popup);
		agentTree.tree.setSize(new Dimension(300, 600));
		
		desktopPane = new JDesktopPane();
		desktopPane.setBackground(Color.lightGray);
		
		jsp = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, new JScrollPane(agentTree.tree), new JScrollPane(desktopPane));
		jsp.setContinuousLayout(true);		
		jsp.setDividerLocation(300);
		
		getContentPane().add(jsp, BorderLayout.CENTER);
		
		addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				exit();
			}
		});
		
		// Initialize the default LogManager
		defaultLogManager = new JavaLoggingLogManagerImpl();
	}
	
	
	/////////////////////////////////////////////
	// Methods called by the LogManagerAgent
	/////////////////////////////////////////////
	public void showCorrect() {
		pack();
		setSize(800, 600);
		
		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
		int       centerX = (int) screenSize.getWidth()/2;
		int       centerY = (int) screenSize.getHeight()/2;
		setLocation(centerX-getWidth()/2, centerY-getHeight()/2);
		setVisible(true);
		toFront();
	}
	
	public void resetTree() {
		Runnable r = new Runnable() {
			public void run() {
				agentTree.clearLocalPlatform();
			}
		};
		SwingUtilities.invokeLater(r);
	}
	
	public void addContainer(final String name, final InetAddress address) {
		Runnable r = new Runnable() {
			public void run() {
				agentTree.addContainerNode(name, address);
			}
		};
		SwingUtilities.invokeLater(r);
	}
	
	public void removeContainer(final String name) {
		Runnable r = new Runnable() {
			public void run() {
				agentTree.removeContainerNode(name);
			}
		};
		SwingUtilities.invokeLater(r);
	}
	
	public void refreshLocalPlatformName(final String name) {
		Runnable r = new Runnable(){
			public void run(){
				agentTree.refreshLocalPlatformName(name);
			}
			
		};
		SwingUtilities.invokeLater(r);
	}
	
	
	/////////////////////////////////////////////////////
	// Action handling methods
	/////////////////////////////////////////////////////
	void startManagingLog() {
		AgentTree.Node node = agentTree.getSelectedNode();
		if (node != null && node instanceof AgentTree.ContainerNode) {
			String containerName = node.getName();
			System.out.println("Container name = "+containerName);
			ContainerLogWindow window = (ContainerLogWindow) managedContainers.get(containerName); 
			if (window != null) {
				System.out.println("Window found");
				window.moveToFront();
			}
			else {
				System.out.println("Window NOT found");
				AID controller = null;
				int state = 0; 
				try {
					if (!containerName.equals(myAgent.here().getName())) {
						// Request the AMS to start a Controller on the requested container
						controller = createController(containerName);
					}
					state = 1;
					
					window = new ContainerLogWindow(myAgent, containerName, controller, defaultLogManager, this);
					window.pack();
					window.setSize(600, 400);
					window.setVisible(true);
					managedContainers.put(containerName, window);
					desktopPane.add(window);
					window.moveToFront();
				}
				catch (FIPAException fe) {
					String msg = (state == 0 ? "Cannot create Log Helper agent on container "+containerName : "Cannot retrieve logging information from container "+containerName);
					int res = JOptionPane.showConfirmDialog(this, msg+"\nWould you like to see the message?", "WARNING", JOptionPane.YES_NO_OPTION);
					if (res == JOptionPane.YES_OPTION) {
						AclGui.showMsgInDialog(fe.getACLMessage(), this);
					}
				}
			}
		}
	}
	
	void stopManagingLog() {
		AgentTree.Node node = agentTree.getSelectedNode();
		if (node != null && node instanceof AgentTree.ContainerNode) {
			String containerName = node.getName();
			final ContainerLogWindow window = (ContainerLogWindow) managedContainers.remove(containerName); 
			if (window != null) {
				AID controller = window.getController();
				if (controller != null) {
					// Kill the controller
					killController(controller);
				}
				// Close the window for the seleced container
				EventQueue.invokeLater(new Runnable() {
					public void run() {
						window.dispose();
					}
				});
			}
		}
	}
	
	void setDefaultLoggingSystem() {
		LogManager lm = initializeLogManager();
		if (lm != null) {
			defaultLogManager = lm;
		}
	}
	
	void exit() {
		myAgent.doDelete();
	}	
	
	
	////////////////////////////////////
	// Utility methods
	////////////////////////////////////
	LogManager initializeLogManager() {
		String className = null;
		try {
			className = JOptionPane.showInputDialog(this, "Insert the fully qualified class name of the LogManager implementation for the desired logging system");
			if (className != null) {
				return (LogManager) Class.forName(className).newInstance();
			}
		}
		catch (Exception e) {
			JOptionPane.showMessageDialog(this, "Cannot create a LogManager of class "+className+" ["+e+"]");
		}
		return null;
	}

	private ACLMessage createAMSRequest() {
		ACLMessage request = new ACLMessage(ACLMessage.REQUEST);
		request.addReceiver(myAgent.getAMS());
		request.setProtocol(FIPANames.InteractionProtocol.FIPA_REQUEST);
		request.setLanguage(FIPANames.ContentLanguage.FIPA_SL);
		request.setOntology(JADEManagementOntology.getInstance().getName());
		return request;
	}
	
	private AID createController(String containerName) throws FIPAException {
		ACLMessage request = createAMSRequest();
		
		CreateAgent ca = new CreateAgent();
		String localName = myAgent.getLocalName()+"-helper-on-"+containerName;
		ca.setAgentName(localName);
		ca.setClassName("jade.tools.logging.LogHelperAgent");
		ca.addArguments(myAgent.getAID());
		ca.setContainer(new ContainerID(containerName, null));
		
		Action act = new Action();
		act.setActor(myAgent.getAMS());
		act.setAction(ca);
		
		try {
			myAgent.getContentManager().fillContent(request, act);
			ACLMessage inform = FIPAService.doFipaRequestClient(myAgent, request, 10000);
			if (inform != null) {
				return new AID(localName, AID.ISLOCALNAME);
			}
			else {
				throw new FIPAException("Response timeout expired");
			}
		}
		catch (FIPAException fe) {
			throw fe;
		}
		catch (Exception e) {
			// Should never happen
			e.printStackTrace();
		}
		return null;
	}
	
	private void killController(AID controller) {
		ACLMessage request = createAMSRequest();
		
		KillAgent ka = new KillAgent();
		ka.setAgent(controller);
		
		Action act = new Action();
		act.setActor(myAgent.getAMS());
		act.setAction(ka);
		
		try {
			myAgent.getContentManager().fillContent(request, act);
			FIPAService.doFipaRequestClient(myAgent, request, 10000);
		}
		catch (Exception e) {
			// Should never happen
			e.printStackTrace();
		}
	}	
}

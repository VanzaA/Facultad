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

package jade.tools.logging.gui;

import jade.content.onto.basic.Action;
import jade.content.onto.basic.Result;
import jade.core.AID;
import jade.core.Agent;
import jade.core.ContainerID;
import jade.util.leap.List;
import jade.util.leap.ArrayList;
import jade.util.leap.Iterator;
import jade.tools.logging.LogManager;
import jade.tools.logging.ontology.*;
import jade.domain.FIPAException;
import jade.domain.FIPANames;
import jade.domain.FIPAService;
import jade.gui.AclGui;
import jade.lang.acl.ACLMessage;

import java.awt.*;
import java.net.URL;

import javax.swing.*;
import javax.swing.event.InternalFrameListener;
import javax.swing.event.InternalFrameEvent;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;


/**
 * This class implements a window to manage logs inside a given container.
 * 
 * @author Giovanni Caire - TILAB
 */
public class ContainerLogWindow extends JInternalFrame implements InternalFrameListener {	
	private static final int NAME_COLUMN = 0;
	private static final int LEVEL_COLUMN = 1;		
	private static final int HANDLERS_COLUMN = 2;		
	private static final int FILE_COLUMN = 3;
	
	private Agent myAgent;
	
	private String containerName;
	private AID controller;
	private LogManagerGUI mainGui;
	
	private LogManager myLogManager;
	private LogTable myTable;
	private JComboBox levelCombo;
	private JTextField loggingSystemTF;
	
	private AbstractAction setLoggingSystemAction = new SetLoggingSystemAction(this);
	
	public ContainerLogWindow(Agent a, String containerName, AID controller, LogManager logManager, LogManagerGUI gui) throws FIPAException {
		super(containerName);
		
		myAgent = a;
		
		this.containerName = containerName;
		this.controller = controller;
		
		myLogManager = logManager;
		mainGui = gui;	
		
		setClosable(false);
		setIconifiable(true);
		setMaximizable(true);
		setResizable(true);
		
		JToolBar bar = new JToolBar();		
		JButton button = null;
		URL url = null;
		button = new JButton();
		button.setToolTipText("Set logging system");
		button.setAction(setLoggingSystemAction);
		url = getClass().getClassLoader().getResource("jade/gui/images/tick_blue.gif");
		button.setIcon(new ImageIcon(url));
		button.setText(null);
		bar.add(button);
		bar.addSeparator();
		bar.add(new JLabel("Logging system:"));
		bar.addSeparator();
		loggingSystemTF = new JTextField(64);
		loggingSystemTF.setText(myLogManager.getName());
		loggingSystemTF.setEditable(false);
		bar.add(loggingSystemTF);
		
		getContentPane().add(bar, BorderLayout.NORTH);
		
		
		// Retrieve logging information
		List infos = retrieveLogInfo();
		
		myTable = new LogTable(infos);
		JTable table = new JTable(myTable);
		getContentPane().add(new JScrollPane(table), BorderLayout.CENTER);
		
		//Allows the modification to the value in the cell	
		levelCombo = new JComboBox();
		List levels = myLogManager.getLogLevels();
		for (int i = 0; i < levels.size(); ++i) {
			LevelInfo l = (LevelInfo) levels.get(i);
			levelCombo.addItem(l.getName());
		}
		
		TableCellEditor levelEditor = new DefaultCellEditor(levelCombo);	
		TableColumnModel columnModel = table.getColumnModel();
		TableColumn setLevelColumn = columnModel.getColumn(LEVEL_COLUMN); 
		setLevelColumn.setCellEditor(levelEditor);
	}
	
	public String getContainerName() {
		return containerName;
	}
	
	public AID getController() {
		return controller;
	}
	
	private List retrieveLogInfo() throws FIPAException {
		List tmp = null;
		if (controller != null) {
			tmp = remoteRetrieveLogInfo(controller);
		}
		else {
			tmp = myLogManager.getAllLogInfo();
		}
		// Now sort log info in alphabetical order
		List infos = new ArrayList(tmp.size());
		Iterator it = tmp.iterator();
		while (it.hasNext()) {
			LoggerInfo li = (LoggerInfo) it.next();
			String name = li.toString();
			int i = 0;
			while (i < infos.size() && name.compareTo(infos.get(i).toString()) >= 0) {
				++i;
			}
			infos.add(i, li);
		}
		return infos;
	}
	
	private void setLogLevel(String name, int level) throws FIPAException {
		if (controller != null) {
			remoteSetLogLevel(controller, name, level);
		}
		else {
			myLogManager.setLogLevel(name, level);
		}
	}
	
	private void setLogFile(String name, String fileName) throws FIPAException {
		if (controller != null) {
			remoteSetLogFile(controller, name, fileName);
		}
		else {
			myLogManager.setFile(name, fileName);
		}
	}
	
	
	///////////////////////////////////////
	// InternalFrameListener interface
	///////////////////////////////////////
	public void internalFrameActivated(InternalFrameEvent e) {
		this.moveToFront();
	}
	
	public void internalFrameDeactivated(InternalFrameEvent e) {}
	public void internalFrameClosed(InternalFrameEvent e) {}
	public void internalFrameClosing(InternalFrameEvent e) {}
	public void internalFrameIconified(InternalFrameEvent e) {}
	public void internalFrameDeiconified(InternalFrameEvent e) {}
	public void internalFrameOpened(InternalFrameEvent e) {}
	
	private String getLevelName(int n) {
		List l = myLogManager.getLogLevels();
		Iterator it = l.iterator();
		while (it.hasNext()) {
			LevelInfo li = (LevelInfo) it.next();
			if (n == li.getValue()) {
				return li.getName();
			}
		}
		return "UNKNOWN";
	}
	
	private int getLevelValue(String name) {
		List l = myLogManager.getLogLevels();
		Iterator it = l.iterator();
		while (it.hasNext()) {
			LevelInfo li = (LevelInfo) it.next();
			if (name.equals(li.getName())) {
				return li.getValue();
			}
		}
		return 0;
	}
	
	/**
	 * Inner class LogTable
	 */
	private class LogTable extends AbstractTableModel {
		private List logInfos;
		
		public LogTable(List infos){
			logInfos = infos;
		}
		
		public void refresh(List infos) {
			logInfos = infos;
			validate();
		}
		
		public int getRowCount(){
			return logInfos.size();
		}
		
		public int getColumnCount(){
			return 4;
		}
		
		
		// Level and file cells are editables
		public boolean isCellEditable(int row, int column){
			return column == LEVEL_COLUMN || column == FILE_COLUMN;
		}
		
		public Object getValueAt(int row, int column){
			LoggerInfo info = (LoggerInfo) logInfos.get(row);
			switch(column) {
			case NAME_COLUMN:
				return info.getName();
			case LEVEL_COLUMN:
				return getLevelName(info.getLevel());
			case HANDLERS_COLUMN:
				StringBuffer sb = new StringBuffer();
				List l = info.getHandlers();
				if (l != null) {
					Iterator it = l.iterator();
					while (it.hasNext()) {
						sb.append(it.next());
						if (it.hasNext()) {
							sb.append(", ");
						}
					}
				}
				return sb.toString();
			case FILE_COLUMN:
				return info.getFile();
			}
			return null;
		}
		
		public void setValueAt(Object value, int row, int column){
			LoggerInfo info = (LoggerInfo) logInfos.get(row);
			try {
				if (column == LEVEL_COLUMN) {
					int level = getLevelValue((String) value);
					setLogLevel(info.getName(), level);
					info.setLevel(level);
				}
				else if (column == FILE_COLUMN) {
					setLogFile(info.getName(), (String) value);
					info.setFile((String) value);
				}
			}
			catch (FIPAException fe) {
				int res = JOptionPane.showConfirmDialog(mainGui, "Cannot set "+getColumnName(column)+" to logger "+info.getName()+" in container "+containerName+"\nWould you like to see the message?", "WARNING", JOptionPane.YES_NO_OPTION);
				if (res == JOptionPane.YES_OPTION) {
					AclGui.showMsgInDialog(fe.getACLMessage(), mainGui);
				}
			}
		}
		
		public String getColumnName(int column){
			switch (column){
			case NAME_COLUMN: 
				return "Logger Name"; 
			case LEVEL_COLUMN: 
				return "Level";	
			case HANDLERS_COLUMN: 
				return "Handlers";	
			case FILE_COLUMN: 
				return "Log file";	
			}
			return null;
		}
	} // END of inner class LogTable
	
	
	///////////////////////////////////
	// Action handling methods
	///////////////////////////////////
	void setLoggingSystem() {
		LogManager lm = mainGui.initializeLogManager();
		if (lm != null) {
			LogManager old = myLogManager;
			myLogManager = lm;
			try {
				List infos = retrieveLogInfo();
				loggingSystemTF.setText(myLogManager.getName());
				myTable.refresh(infos);
			}
			catch (FIPAException fe) {
				int res = JOptionPane.showConfirmDialog(this, "Cannot retrieve logging information from container "+containerName+"\nWould you like to see the message?", "WARNING", JOptionPane.YES_NO_OPTION);
				if (res == JOptionPane.YES_OPTION) {
					AclGui.showMsgInDialog(fe.getACLMessage(), mainGui);
				}
				// Restore the old log manager
				myLogManager = old;
			}
		}
	}
	
	
	////////////////////////////////////////////
	// Private utility methods
	////////////////////////////////////////////
	private ACLMessage createHelperRequest(AID helper) {
		ACLMessage request = new ACLMessage(ACLMessage.REQUEST);
		request.addReceiver(helper);
		request.setProtocol(FIPANames.InteractionProtocol.FIPA_REQUEST);
		request.setLanguage(FIPANames.ContentLanguage.FIPA_SL);
		request.setOntology(LogManagementOntology.getInstance().getName());
		return request;
	}
	
	private List remoteRetrieveLogInfo(AID helper) throws FIPAException {
		ACLMessage request = createHelperRequest(helper);
		
		GetAllLoggers gal = new GetAllLoggers(myLogManager.getClass().getName(), null);
		
		Action act = new Action();
		act.setActor(helper);
		act.setAction(gal);
		
		try {
			myAgent.getContentManager().fillContent(request, act);
			ACLMessage inform = FIPAService.doFipaRequestClient(myAgent, request, 10000);
			if (inform != null) {
				Result res = (Result) myAgent.getContentManager().extractContent(inform);
				return res.getItems();
			}
			else {
				throw new FIPAException("Response timeout expired");
			}
		}
		catch (FIPAException fe) {
			throw fe;
		}
		catch (Exception e) {
			e.printStackTrace();
			throw new FIPAException(e.getMessage());
		}
	}
	
	private void remoteSetLogLevel(AID helper, String name, int level) throws FIPAException {
		ACLMessage request = createHelperRequest(helper);
		
		SetLevel sl = new SetLevel(name, level);
		
		Action act = new Action();
		act.setActor(helper);
		act.setAction(sl);
		
		try {
			myAgent.getContentManager().fillContent(request, act);
			ACLMessage inform = FIPAService.doFipaRequestClient(myAgent, request, 10000);
			if (inform == null) {
				throw new FIPAException("Response timeout expired");
			}
		}
		catch (FIPAException fe) {
			throw fe;
		}
		catch (Exception e) {
			e.printStackTrace();
			throw new FIPAException(e.getMessage());
		}
	}
	
	private void remoteSetLogFile(AID helper, String name, String file) throws FIPAException {
		ACLMessage request = createHelperRequest(helper);
		
		SetFile sf = new SetFile(name, file);
		
		Action act = new Action();
		act.setActor(helper);
		act.setAction(sf);
		
		try {
			myAgent.getContentManager().fillContent(request, act);
			ACLMessage inform = FIPAService.doFipaRequestClient(myAgent, request, 10000);
			if (inform == null) {
				throw new FIPAException("Response timeout expired");
			}
		}
		catch (FIPAException fe) {
			throw fe;
		}
		catch (Exception e) {
			e.printStackTrace();
			throw new FIPAException(e.getMessage());
		}
	}	
}


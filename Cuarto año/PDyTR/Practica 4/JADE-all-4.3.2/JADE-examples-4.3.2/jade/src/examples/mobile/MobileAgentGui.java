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



package examples.mobile;


// Import required Java classes 
import jade.core.Location;
import jade.gui.GuiEvent;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Iterator;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.table.TableColumn;

/**
 * This is the GUI of the MobileAGent. Becuase in Java a new Thread is spawn
 * for each GUI, the communication with the Agent thread is based on
 * event passing.
 */
public class MobileAgentGui extends JFrame implements ActionListener
{
  private MobileAgent          myAgent;
  private LocationTableModel visitedSiteListModel;
  private JTable            visitedSiteList;
  private LocationTableModel availableSiteListModel;
  private JTable            availableSiteList;
  private JTextField counterText; 

  private static String MOVELABEL = "MOVE";
  private static String CLONELABEL = "CLONE";
  private static String EXITLABEL = "EXIT";
  private static String PAUSELABEL = "Stop Counter";
  private static String CONTINUELABEL = "Continue Counter";
  private static String REFRESHLABEL = "Refresh Locations";


	// Constructor
	MobileAgentGui(MobileAgent a)
	{
		super();
		myAgent = a;
		setTitle("GUI of "+a.getLocalName());
		setSize(505,405);

		////////////////////////////////
		// Set GUI window layout manager
	
		JPanel main = new JPanel();
		main.setLayout(new BoxLayout(main,BoxLayout.Y_AXIS));

		JPanel counterPanel = new JPanel();
		counterPanel.setLayout(new BoxLayout(counterPanel, BoxLayout.X_AXIS));
		
		JButton pauseButton = new JButton("STOP COUNTER");
		pauseButton.addActionListener(this);
		JButton continueButton = new JButton("CONTINUE COUNTER");
		continueButton.addActionListener(this);
		JLabel counterLabel = new JLabel("Counter value: ");
		counterText = new JTextField();
		counterPanel.add(pauseButton);
		counterPanel.add(continueButton);
		counterPanel.add(counterLabel);
		counterPanel.add(counterText);
		
		main.add(counterPanel);
		
	   ///////////////////////////////////////////////////
		// Add the list of available sites to the NORTH part 
		availableSiteListModel = new LocationTableModel();
		availableSiteList = new JTable(availableSiteListModel);
		availableSiteList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

		JPanel availablePanel = new JPanel();
		availablePanel.setLayout(new BorderLayout());

		JScrollPane avPane = new JScrollPane();
		avPane.getViewport().setView(availableSiteList);
		availablePanel.add(avPane, BorderLayout.CENTER);
		availablePanel.setBorder(BorderFactory.createTitledBorder("Available Locations"));
	  availableSiteList.setRowHeight(20);

		main.add(availablePanel);
		
		TableColumn c;
		c = availableSiteList.getColumn(availableSiteList.getColumnName(0));
		c.setHeaderValue("ID");
		c = availableSiteList.getColumn(availableSiteList.getColumnName(1));
		c.setHeaderValue("Name");
		c = availableSiteList.getColumn(availableSiteList.getColumnName(2));
		c.setHeaderValue("Protocol");
		c = availableSiteList.getColumn(availableSiteList.getColumnName(3));
		c.setHeaderValue("Address");

		///////////////////////////////////////////////////
		// Add the list of visited sites to the CENTER part 
		JPanel visitedPanel = new JPanel();
		visitedPanel.setLayout(new BorderLayout());
		visitedSiteListModel = new LocationTableModel();
		visitedSiteList = new JTable(visitedSiteListModel);
		JScrollPane pane = new JScrollPane();
		pane.getViewport().setView(visitedSiteList);
	  visitedPanel.add(pane,BorderLayout.CENTER);
		visitedPanel.setBorder(BorderFactory.createTitledBorder("Visited Locations"));
	  visitedSiteList.setRowHeight(20);

		main.add(visitedPanel);

			// Column names
	
		c = visitedSiteList.getColumn(visitedSiteList.getColumnName(0));
		c.setHeaderValue("ID");
		c = visitedSiteList.getColumn(visitedSiteList.getColumnName(1));
		c.setHeaderValue("Name");
		c = visitedSiteList.getColumn(visitedSiteList.getColumnName(2));
		c.setHeaderValue("Protocol");
		c = visitedSiteList.getColumn(visitedSiteList.getColumnName(3));
		c.setHeaderValue("Address");

	
		/////////////////////////////////////////////////////////////////////
		// Add the control buttons to the SOUTH part 
		// Move button
		JPanel p = new JPanel();
		JButton b = new JButton(REFRESHLABEL);
		b.addActionListener(this);
		p.add(b);
		b = new JButton(MOVELABEL);
		b.addActionListener(this);
		p.add(b);
		b = new JButton(CLONELABEL);
		b.addActionListener(this);
		p.add(b);
		// Exit button
		b = new JButton(EXITLABEL);
		b.addActionListener(this);
		p.add(b);
		main.add(p);
		
		getContentPane().add(main, BorderLayout.CENTER);
	}

  void displayCounter(int value){
    counterText.setText(Integer.toString(value));
    //counterText.fireActionPerformed();
  }

  public void updateLocations(Iterator list) {
    availableSiteListModel.clear();
    for ( ; list.hasNext(); ) {
    	Object obj = list.next();
      availableSiteListModel.add((Location) obj);
    }
    availableSiteListModel.fireTableDataChanged();
  }

	public void actionPerformed(ActionEvent e)
	{
		String command = e.getActionCommand();

		// MOVE
		if      (command.equalsIgnoreCase(MOVELABEL)) {
		  Location dest;
		  int sel = availableSiteList.getSelectedRow();
		  if (sel >= 0)
		    dest = availableSiteListModel.getElementAt(sel);
		  else
		    dest = availableSiteListModel.getElementAt(0);
              
		  GuiEvent ev = new GuiEvent((Object) this,myAgent.MOVE_EVENT);
		  ev.addParameter(dest);
      myAgent.postGuiEvent(ev);	 
		}
		// CLONE
		else if      (command.equalsIgnoreCase(CLONELABEL)) {
		  Location dest;
		  int sel = availableSiteList.getSelectedRow();
		  if (sel >= 0)
		    dest = availableSiteListModel.getElementAt(sel);
		  else
		    dest = availableSiteListModel.getElementAt(0);
		  GuiEvent ev = new GuiEvent((Object) this, myAgent.CLONE_EVENT);
		  ev.addParameter(dest);
      myAgent.postGuiEvent(ev);

		}
		// EXIT
		else if (command.equalsIgnoreCase(EXITLABEL)) {
      GuiEvent ev = new GuiEvent(null,myAgent.EXIT);
			myAgent.postGuiEvent(ev);
		}
		else if (command.equalsIgnoreCase(PAUSELABEL)) {
      GuiEvent ev = new GuiEvent(null,myAgent.STOP_EVENT);
		  myAgent.postGuiEvent(ev);
		}
		else if (command.equalsIgnoreCase(CONTINUELABEL)) {
		     GuiEvent ev = new GuiEvent(null,myAgent.CONTINUE_EVENT);
		     myAgent.postGuiEvent(ev);
		}
		else if (command.equalsIgnoreCase(REFRESHLABEL)) {
		     GuiEvent ev = new GuiEvent(null,myAgent.REFRESH_EVENT); 
         myAgent.postGuiEvent(ev);
		}
	}
	
	void showCorrect()
	{
		///////////////////////////////////////////
		// Arrange and display GUI window correctly
		pack();
		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
		int centerX = (int)screenSize.getWidth() / 2;
		int centerY = (int)screenSize.getHeight() / 2;
		setLocation(centerX - getWidth() / 2, centerY - getHeight() / 2);
		show();
	}
	
	public void addVisitedSite(Location site)
	{
		visitedSiteListModel.add(site);
		visitedSiteListModel.fireTableDataChanged();

	}
}
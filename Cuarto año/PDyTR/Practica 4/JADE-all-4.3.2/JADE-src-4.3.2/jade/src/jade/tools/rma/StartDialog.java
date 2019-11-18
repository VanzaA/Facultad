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

import jade.core.Agent;
import jade.gui.ClassSelectionDialog;
import jade.util.ClassFinderFilter;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.GridLayout;
import java.awt.Panel;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.Document;

/**
 * @author Francisco Regi, Andrea Soracchi - Universita` di Parma
 * @version $Date: 2010-01-28 12:11:59 +0100 (gio, 28 gen 2010) $ $Revision:
 *          5206 $
 */

public class StartDialog extends JDialog implements ActionListener {

	protected static ExtTextField extTextFieldAgentName;
	protected static Panel panelClassname;
	protected static JButton jButtonSelectClassname;
	protected static JComboBox jComboBoxClassnameCombo;
	protected static DefaultComboBoxModel modelClassnameCombo;
	protected static JTextField jTextFieldContainer;
	protected static JTextField jTextFieldArguments;
	protected static JTextField jTextFieldAgentUser;

	protected static JLabel jLabelAgentName = new JLabel("Agent Name");
	protected static JLabel jLabelClassname = new JLabel("Class Name");
	protected static JLabel jLabelArguments = new JLabel("Arguments");
	protected static JLabel jLabelOwner = new JLabel("Owner");
	protected static JLabel jLabelContainer = new JLabel("Container");

	protected static JButton jButtonOk = new JButton("OK");
	protected static JButton jButtonCancel = new JButton("Cancel");

	private final static String ttAgentName = "Name of the Agent to start";
	private final static String ttClassname = "Class Name of the Agent to start";
	private final static String ttArguments = "Arguments passed to the agent constructor";
	private final static String ttOwner = "The user under which the agent has to be started";
	private final static String ttContainer = "Container on which the Agent will start";
	private final static String ttSelectClassname = "Search in classpath for classes extending Agent";

	public final static int OK_BUTTON = 0;
	public final static int CANCEL_BUTTON = 1;

	private static StartDialog dialog;

	private static int choice = CANCEL_BUTTON;
	private static String classname;
	private static String agentname;

	private ClassSelectionDialog csd;

	private static class AgentClassFilter implements ClassFinderFilter {
		private final static String[] excluded = new String[] {
			"jade.domain.ams",
			"jade.tools.ToolNotifier",
			"jade.tools.logging.LogHelperAgent"
		};

		// Exclude all classes that are
		// - Not concrete (abstract or interfaces)
		// - Contained in the jade.core package (a part from the jade.core.Agent class itself)
		// - Explicitly mentioned in the "exclude" array
		public boolean include(Class superClazz, Class clazz) {
			String clazzName = clazz.getName();
			int modifiers = clazz.getModifiers();
			boolean doInclude = ((modifiers & (ClassSelectionDialog.ACC_ABSTRACT | ClassSelectionDialog.ACC_INTERFACE)) == 0);
			if (doInclude) {
				if (clazzName.startsWith("jade.core")) {
					doInclude = Agent.class.getName().equals(clazzName);
				}
			}
			if (doInclude) {
				for (int i = 0; i < excluded.length; i++) {
					if (excluded[i].equals(clazzName)) {
						doInclude = false;
						break;
					}
				}
			}
			return doInclude;
		}
	}
    private static class ExtTextField extends JTextField implements ActionListener, DocumentListener {

		private StartDialog startDialog;

		public ExtTextField() {
			super(0);
			addActionListener(this);
			Document doc = this.getDocument();
			doc.addDocumentListener(this);
		}

		public void setStartDialog(StartDialog startDialog) {
			this.startDialog = startDialog;
		}

		public void actionPerformed(ActionEvent e) {
			// nothing to do
		}

		public void insertUpdate(DocumentEvent e) {
			startDialog.updateOkButtonEnabled();
		}

		public void removeUpdate(DocumentEvent e) {
			startDialog.updateOkButtonEnabled();
		}

		public void changedUpdate(DocumentEvent e) {
			// nothing to do
		}
	}

	static {
		classname = "";
		agentname = "";

		extTextFieldAgentName = new ExtTextField();
		extTextFieldAgentName.setEditable(false);
		extTextFieldAgentName.setToolTipText(ttAgentName);
		jLabelAgentName.setToolTipText(ttAgentName);

		// className = new JTextField ("jade.core.Agent");
		//
		// className.setEditable(true);
		//
		// className.setToolTipText(classNameToolTip);

		jLabelClassname.setToolTipText(ttClassname);

//		ClassFinder finder = new ClassFinder();
//		long t0 = System.currentTimeMillis();
//		long t1, t2, t3;
//		finder = new ClassFinder();
//		t1 = System.currentTimeMillis() - t0;
//		Vector v = finder.findSubclasses(Agent.class.getName());
//		t2 = System.currentTimeMillis() - t0;
//		Vector agentClasses = new Vector(v.size());
//		for (int i = 0; i < v.size(); i++) {
//			agentClasses.add(((Class) v.get(i)).getName());
//		}
//		t3 = System.currentTimeMillis() - t0;
//		System.out.println("t1=" + t1);
//		System.out.println("t2=" + t2);
//		System.out.println("t3=" + t3);

//		classNameCombo = new JComboBox(agentClasses);

		modelClassnameCombo = new DefaultComboBoxModel();

		jComboBoxClassnameCombo = new JComboBox(modelClassnameCombo);
		jComboBoxClassnameCombo.setEditable(true);
		jComboBoxClassnameCombo.setToolTipText(ttClassname);
		jComboBoxClassnameCombo.setSelectedItem(classname);

//		jButtonSelectClassname = new BasicArrowButton(BasicArrowButton.EAST);
		jButtonSelectClassname = new JButton("...");
		jButtonSelectClassname.setToolTipText(ttSelectClassname);
		jButtonSelectClassname.setPreferredSize(new Dimension(20, 0));

		panelClassname = new Panel(new BorderLayout());
		panelClassname.add(jComboBoxClassnameCombo, BorderLayout.CENTER);
		panelClassname.add(jButtonSelectClassname, BorderLayout.EAST);

		jTextFieldArguments = new JTextField();
		jTextFieldArguments.setEditable(true);
		jTextFieldArguments.setToolTipText(ttArguments);
		jLabelArguments.setToolTipText(ttArguments);

		jTextFieldAgentUser = new JTextField();
		jTextFieldAgentUser.setEditable(true);
		jTextFieldAgentUser.setToolTipText(ttOwner);
		jLabelOwner.setToolTipText(ttOwner);

		jTextFieldContainer = new JTextField("0");
		jTextFieldContainer.setEditable(true);
		jTextFieldContainer.setToolTipText(ttContainer);
		jLabelContainer.setToolTipText(ttContainer);

		dialog = new StartDialog();
	}

	protected StartDialog() {
		super((Frame)null, "Insert Start Parameters", true);

		getContentPane().setLayout(new GridLayout(6, 2));

		getContentPane().add(jLabelAgentName);
		getContentPane().add(extTextFieldAgentName);

		getContentPane().add(jLabelClassname);
//		getContentPane().add(classNameCombo);
		getContentPane().add(panelClassname);

		getContentPane().add(jLabelArguments);
		getContentPane().add(jTextFieldArguments);

		getContentPane().add(jLabelOwner);
		getContentPane().add(jTextFieldAgentUser);

		getContentPane().add(jLabelContainer);
		getContentPane().add(jTextFieldContainer);

		jButtonOk.addActionListener(this);
		jButtonCancel.addActionListener(this);
		jButtonSelectClassname.addActionListener(this);
		jComboBoxClassnameCombo.addActionListener(this);
		extTextFieldAgentName.setStartDialog(this);

		getContentPane().add(jButtonOk);
		getContentPane().add(jButtonCancel);
	}

	public void doShow(String agentNameP) {
		extTextFieldAgentName.setText(agentNameP);
		jComboBoxClassnameCombo.setSelectedItem(classname);
		jButtonOk.setEnabled(false);
		pack();
		setLocationRelativeTo(null);
		// FIXME: Enable this when we will move to JAVA 5
		// setAlwaysOnTop(true);
		setVisible(true);
	}

	public Dimension getPreferredSize() {
		return (new Dimension(540, 150));
	}

	private void insertOrMoveComboItem(String s) {
		if (s != null) {
			s = s.trim();
			for (int i = modelClassnameCombo.getSize()-1; i >= 0; i--) {
				if (s.equals(modelClassnameCombo.getElementAt(i))) {
					modelClassnameCombo.removeElementAt(i);
				}
			}
			modelClassnameCombo.insertElementAt(s, 0);
		}
	}

	void updateOkButtonEnabled() {
		String selClassname = (String)jComboBoxClassnameCombo.getSelectedItem();
		String currAgentName = extTextFieldAgentName.getText(); 
		boolean okEnabled =
			selClassname != null && selClassname.length() > 0 &&
			currAgentName != null && currAgentName.length() > 0;
		jButtonOk.setEnabled(okEnabled);
	}

	public void actionPerformed(ActionEvent evt) {
		if (evt.getSource() == jComboBoxClassnameCombo) {
			updateOkButtonEnabled();
		} else if (evt.getSource() == jButtonSelectClassname) {
			if (csd == null) {
				csd = new ClassSelectionDialog(this, "Select Agent class", Agent.class.getName(), new AgentClassFilter());
			}
			List l = new ArrayList(1);
			l.add(0, "jade.core.Agent");
			if (csd.doShow(l) == ClassSelectionDialog.DLG_OK) {
				setClassName(csd.getSelectedClassname());
			}
		} else {
			choice = CANCEL_BUTTON;
	
			if (evt.getSource() == jButtonOk) {
				choice = OK_BUTTON;
				agentname = extTextFieldAgentName.getText();
		        classname = (String)jComboBoxClassnameCombo.getSelectedItem();
				insertOrMoveComboItem(classname);
			}
			dispose();
		}
	}

	public static int showStartNewDialog(String containerName, Frame owner) {
		choice = CANCEL_BUTTON;

		extTextFieldAgentName.setEditable(true);
		jTextFieldContainer.setEditable(false);

		setContainer(containerName);
		dialog.doShow("");

		return choice;
	}

	public static String getAgentName() {
		return agentname;
	}

	public static String getClassName() {
		return classname;
	}

	public static String getArguments()	{
		return jTextFieldArguments.getText().trim();
	}

	public static String getAgentUser()	{
		return jTextFieldAgentUser.getText().trim();
	}

	public static String getContainer() {
		return jTextFieldContainer.getText();
	}

	public static void setAgentName(String agentNameP) {
		extTextFieldAgentName.setText(agentNameP);
	}

	public static void setClassName(String classNameP) {
		jComboBoxClassnameCombo.setSelectedItem(classNameP);
	}

	public static void setContainer(String containerP) {
		jTextFieldContainer.setText(containerP);
	}

}

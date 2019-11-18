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

package jade.gui;

//#J2ME_EXCLUDE_FILE

import jade.util.ClassFinder;
import jade.util.ClassFinderFilter;
import jade.util.ClassFinderListener;

import java.awt.BorderLayout;
import java.awt.Cursor;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Vector;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.AbstractTableModel;

public class ClassSelectionDialog extends JDialog implements WindowListener, ActionListener, ListSelectionListener {

	private static final long serialVersionUID = 1L;
	private JPanel jContentPane = null;
	private JPanel jPanel = null;
	private JButton jButtonOk = null;
	private JButton jButtonCancel = null;
	private JLabel jLabelStatus = null;
	private JScrollPane jScrollPane = null;
	private JTable jTable = null;
	private ClassesTableModel jTableModel;
	private int result;
	private String selectedClassname;
	public final static int DLG_OK = 1;
	public final static int DLG_CANCEL = 0;
	private boolean classesLoaded;
	private String classname;
	private ClassFinderFilter classfilter;

	public static final int ACC_INTERFACE = 0x0200;
	public static final int ACC_ABSTRACT = 0x0400;

	private class ClassFilter implements ClassFinderFilter {
		public boolean include(Class superClazz, Class clazz) {
			int modifiers = clazz.getModifiers();
			boolean doInclude = ((modifiers & (ACC_ABSTRACT | ACC_INTERFACE)) == 0);
			if (doInclude) {
				doInclude = !clazz.getName().equals(classname);
			}
			return doInclude;
		}
	}

	private class ClassUpdater implements Runnable, ClassFinderListener {

		private final static int UPDATE_EVERY = 1;

		private int numberOfClasses;
		private List classNamesCache;
		private String classname;
		private ClassFinderFilter classfilter;

		public ClassUpdater(String classname, ClassFinderFilter classfilter) {
			this.classname = classname;
			this.classfilter = classfilter;
		}

		public void add(Class clazz, URL location) {
			numberOfClasses++;
			classNamesCache.add(clazz.getName());
			if ((numberOfClasses % UPDATE_EVERY) == 0) {
				appendToList(classNamesCache);
				classNamesCache.clear();
			}
		}

		public void run() {
			classNamesCache = new ArrayList(UPDATE_EVERY);
			numberOfClasses = 0;
			ClassFinder cf = new ClassFinder();
			cf.findSubclasses(classname, this, classfilter);
			if (classNamesCache.size() > 0) {
				appendToList(classNamesCache);
				classNamesCache.clear();
			}
			// last call, with empty list, to update status message
			appendToList(classNamesCache);
			classNamesCache = null;
			classesLoaded = true;
		}
	}

	public static class ClassesTableModel extends AbstractTableModel {
		private static final long serialVersionUID = 1L;

		private Vector dynamicRowData;
		private Vector staticRowData;

		public ClassesTableModel() {
			dynamicRowData = new Vector();
			staticRowData = new Vector();
		}

		public String getColumnName(int col) {
			return "Classname";
		}

		public int getRowCount() {
			return dynamicRowData.size()+staticRowData.size();
		}

		public int getColumnCount() {
			return 1;
		}

		public Object getValueAt(int row, int col) {
			return getRowValue(row);
		}

		public boolean isCellEditable(int row, int col) {
			return false;
		}

		public void appendStaticRows(Collection newRows) {
			if (newRows.size() > 0) {
				int firstRow = staticRowData.size();
				staticRowData.addAll(newRows);
				fireTableRowsInserted(firstRow, staticRowData.size()-1);
			}
		}

		public void setDynamicRows(Collection rows) {
			dynamicRowData.clear();
			fireTableRowsDeleted(0, dynamicRowData.size());
			if (rows.size() > 0) {
				dynamicRowData.addAll(rows);
				fireTableRowsInserted(0, dynamicRowData.size()-1);
			}
		}

		public String getRowValue(int index) {
			if (index < dynamicRowData.size()) {
				return (String)dynamicRowData.get(index);
			} else {
				return (String)staticRowData.get(index-dynamicRowData.size());
			}
		}
	}

	private void appendToList(List list) {
		synchronized (jTable) {
			boolean stillSearching = list.size() > 0;
			if (stillSearching) {
				jTableModel.appendStaticRows(list);
			}
			updateLabelStatus(stillSearching);
		}
	}

	private void updateLabelStatus(boolean stillSearching) {
		if (stillSearching) {
			jLabelStatus.setText("Searching in classpath for classes that extend "+classname+" ("+jTableModel.getRowCount()+" found so far)");
		} else {
			setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
			jLabelStatus.setText("Classpath contains "+jTableModel.getRowCount()+" classes that extend "+classname);
		}
	}

	public String getSelectedClassname() {
		return selectedClassname;
	}

	/**
	 * @param owner
	 */
	public ClassSelectionDialog(Dialog owner, String title, String classname, ClassFinderFilter classfilter) {
		super(owner, title, true);
		initialize();
		classesLoaded = false;
		this.classname = classname;
		this.classfilter = classfilter;
	}

	public ClassSelectionDialog(Dialog owner, String title, String classname) {
		this(owner, title, classname, null);
	}

	public int doShow(Collection firstRows) {
		jButtonOk.setEnabled(false);
		synchronized (jTable) {
			jTableModel.setDynamicRows(firstRows);
		}
		pack();
		setLocationRelativeTo(null);
		// FIXME: Enable this when we will move to JAVA 5
		// setAlwaysOnTop(true);
		setVisible(true);
		return result;
	}

	public int doShow() {
		return doShow(new Vector(0));
	}

	/**
	 * This method initializes this
	 * 
	 * @return void
	 */
	private void initialize() {
		this.setSize(300, 200);
		this.setModal(true);
		this.setContentPane(getJContentPane());
		this.addWindowListener(this);
	}

	/**
	 * This method initializes jContentPane
	 * 
	 * @return javax.swing.JPanel
	 */
	private JPanel getJContentPane() {
		if (jContentPane == null) {
			jLabelStatus = new JLabel();
			jLabelStatus.setPreferredSize(new Dimension(0, 15));
			jContentPane = new JPanel();
			jContentPane.setLayout(new BorderLayout());
			jContentPane.add(getJPanel(), BorderLayout.SOUTH);
			jContentPane.add(jLabelStatus, BorderLayout.NORTH);
			jContentPane.add(getJScrollPane(), BorderLayout.CENTER);
		}
		return jContentPane;
	}

	/**
	 * This method initializes jPanel
	 * 
	 * @return javax.swing.JPanel
	 */
	private JPanel getJPanel() {
		if (jPanel == null) {
			GridLayout gridLayout = new GridLayout();
			gridLayout.setRows(1);
			gridLayout.setColumns(2);
			jPanel = new JPanel();
			jPanel.setLayout(gridLayout);
			jPanel.setPreferredSize(new Dimension(20, 20));
			jPanel.add(getJButtonOk(), null);
			jPanel.add(getJButtonCancel(), null);
		}
		return jPanel;
	}

	/**
	 * This method initializes jButtonOk
	 * 
	 * @return javax.swing.JButton
	 */
	private JButton getJButtonOk() {
		if (jButtonOk == null) {
			jButtonOk = new JButton();
			jButtonOk.setText("Ok");
			jButtonOk.addActionListener(this);
		}
		return jButtonOk;
	}

	/**
	 * This method initializes jButtonCancel
	 * 
	 * @return javax.swing.JButton
	 */
	private JButton getJButtonCancel() {
		if (jButtonCancel == null) {
			jButtonCancel = new JButton();
			jButtonCancel.setText("Cancel");
			jButtonCancel.addActionListener(this);
		}
		return jButtonCancel;
	}

	public void windowActivated(WindowEvent e) {
		// TODO Auto-generated method stub

	}

	public void windowClosed(WindowEvent e) {
		// TODO Auto-generated method stub

	}

	public void windowClosing(WindowEvent e) {
		// TODO Auto-generated method stub

	}

	public void windowDeactivated(WindowEvent e) {
		// TODO Auto-generated method stub

	}

	public void windowDeiconified(WindowEvent e) {
		// TODO Auto-generated method stub

	}

	public void windowIconified(WindowEvent e) {
		// TODO Auto-generated method stub
	}

	public void windowOpened(WindowEvent e) {
		if (!classesLoaded) {
			setCursor(new Cursor(Cursor.WAIT_CURSOR));
			updateLabelStatus(true);
			ClassUpdater cu = new ClassUpdater(classname, classfilter == null ? new ClassFilter() : classfilter);
			new Thread(cu).start();
		}
	}

	/**
	 * This method initializes jScrollPane
	 * 
	 * @return javax.swing.JScrollPane
	 */
	private JScrollPane getJScrollPane() {
		if (jScrollPane == null) {
			jScrollPane = new JScrollPane();
			jScrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
			jScrollPane.setViewportView(getJTable());
		}
		return jScrollPane;
	}

	/**
	 * This method initializes jTable
	 * 
	 * @return javax.swing.JTable
	 */
	private JTable getJTable() {
		if (jTable == null) {
			jTable = new JTable();
			jTable.setAutoCreateColumnsFromModel(true);
			jTable.setDoubleBuffered(true);
			jTable.setAutoResizeMode(JTable.AUTO_RESIZE_LAST_COLUMN);
			jTable.setModel(getClassesTableModel());
			jTable.setVisible(true);
			jTable.getSelectionModel().addListSelectionListener(this);
			jTable.addMouseListener(new MouseAdapter() {
				public void mouseClicked(MouseEvent e) {
					if (e.getClickCount() == 2) {
						jButtonOk.doClick();
					}
				}
			});
		}
		return jTable;
	}

	private ClassesTableModel getClassesTableModel() {
		if (jTableModel == null) {
			jTableModel = new ClassesTableModel();
		}
		return jTableModel;
	}

	public void actionPerformed(ActionEvent e) {
		if (e.getSource() == jButtonOk) {
			result = DLG_OK;
			int index = jTable.getSelectedRow(); 
			if (index >= 0) {
				selectedClassname = jTableModel.getRowValue(index);
			}
		} else {
			result = DLG_CANCEL;
		}
		dispose();
	}

	public void valueChanged(ListSelectionEvent e) {
		if (!e.getValueIsAdjusting()) {
			jButtonOk.setEnabled(true);
		}
	}
}

/**
 * JADE - Java Agent DEvelopment Framework is a framework to develop
 * multi-agent systems in compliance with the FIPA specifications.
 * Copyright (C) 2000 CSELT S.p.A.
 *
 * GNU Lesser General Public License
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation,
 * version 2.1 of the License.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the
 * Free Software Foundation, Inc., 59 Temple Place - Suite 330,
 * Boston, MA  02111-1307, USA.
 */
package jade;

//#APIDOC_EXCLUDE_FILE
//#J2ME_EXCLUDE_FILE

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.filechooser.*;

import java.awt.*;
import java.awt.event.*;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Enumeration;
import java.util.Vector;

import jade.util.ExtendedProperties;


import jade.gui.TreeHelp;

/**
 * This class create the gui for the jade configuration
 * @author Tiziana Trucco - CSELT S.p.A.
 * @author Dick Cowan - HP Labs
 * @author Dominic Greenwood - Whitestein Technologies AG
 * @version $Date: 2010-04-19 16:16:41 +0200 (lun, 19 apr 2010) $ $Revision: 6320 $
 */
public class BootGUI extends JDialog {
    static String EXTENSION = "conf";
    static String TITLE = "--JADE Properties--";

    Vector propertiesVector = null;
    File currentDir = null;
    JTextField statusField = new JTextField();
    JPanel topPanel = new JPanel();
    JPanel propertyPanel = new JPanel();
    JPanel buttonPanel = new JPanel();
    String propertyFileName = null;
    ExtendedProperties outProp = null;
    BootGUI thisBootGui;
    Boot3 booter;

    /**
     * Constructor - launches the GUI configurator.
     * @param theBooter The Boot class.
     */
    public BootGUI(Boot3 theBooter) {
        super();
        thisBootGui = this;
        
        booter = theBooter;
        propertiesVector = createPropertyVector(booter.getProperties());

        setTitle("JADE Configurator");

        Border raisedbevel = BorderFactory.createRaisedBevelBorder();
        JPanel mainPanel = new JPanel();

        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        mainPanel.setBorder(raisedbevel);
        topPanel.setLayout(new BoxLayout(topPanel, BoxLayout.Y_AXIS));
        propertyPanel.setLayout(new BoxLayout(propertyPanel,
                                              BoxLayout.Y_AXIS));

        for (Enumeration it = propertiesVector.elements();
                it.hasMoreElements(); ) {
            singlePanel propPanel = new singlePanel();
            PropertyType p = (PropertyType) it.nextElement();
            JPanel panel = propPanel.newSinglePanel(p);

            propertyPanel.add(panel);
        }

        ////////////////////////
        // Status message
        ////////////////////////
        JPanel statusPanel = new JPanel();

        statusPanel.setLayout(new BorderLayout());
        statusField.setEditable(false);
        statusField.setFont(new Font("Monospaced", Font.PLAIN, 12));


        statusField.setPreferredSize(new Dimension(600, 50));

        //statusField.setMaximumSize(new Dimension(200,50));
        statusField.setMinimumSize(new Dimension(50, 50));

        //if(modified)
        // statusField.setText("Warning: default parameter overriden by \ncommand line ones");
        statusPanel.add(statusField, BorderLayout.CENTER);
        buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.X_AXIS));

        JButton openB = new JButton("Open File");

        openB.setToolTipText("Read configuration from file");
        openB.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                String param = (String) e.getActionCommand();

                if (param.equals("Open File")) {
                    JFileChooser chooser = new JFileChooser();

                    chooser.setFileFilter(new myFileFilter());

                    if (currentDir != null) {
                        chooser.setCurrentDirectory(currentDir);
                    }

                    int returnVal = chooser.showOpenDialog(null);

                    if (returnVal == JFileChooser.APPROVE_OPTION) {
                        currentDir = chooser.getCurrentDirectory();

                        String fileName =
                            chooser.getSelectedFile().getAbsolutePath();

                        try {
                            loadPropertiesFromFile(fileName);
                            propertyFileName = fileName;
                            updateProperties();
                        } catch (FileNotFoundException fe) {
                            System.out.println("File not found Exception");
                        } catch (IOException ioe) {
                            System.out.println("IO Exception");
                        }
                    }
                }
            }
        });
        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                System.exit(0);
            }
        });
        buttonPanel.add(openB);

        JButton saveB = new JButton("Save File");

        saveB.setToolTipText("Save configuration into a file");
        saveB.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                String param = (String) e.getActionCommand();

                if (param.equals("Save File")) {
                    ExtendedProperties propToSave = extractPropertiesFromGui();

                    //propToSave.list(System.out);
                    try {
                        booter.setProperties(propToSave);

                        propToSave = booter.getProperties();

                        //propToSave.list(System.out);
                        JFileChooser chooser = new JFileChooser();

                        chooser.setFileFilter(new myFileFilter());

                        if (currentDir != null) {
                            chooser.setCurrentDirectory(currentDir);
                        }

                        int returnVal = chooser.showSaveDialog(null);

                        if (returnVal == JFileChooser.APPROVE_OPTION) {
                            currentDir = chooser.getCurrentDirectory();

                            String fileName =
                                chooser.getSelectedFile().getAbsolutePath();
                            boolean ext = hasExtension(fileName);

                            if (ext == false) {
                                fileName = fileName.concat(".conf");
                            }

                            propertyFileName = fileName;

                            try {
                                FileOutputStream out =
                                    new FileOutputStream(fileName);
				// do not save -conf=true otherwise 
				// -conf <fileName starts the GUI again
				propToSave.put(BootProfileImpl.CONF_KEY,"false");
                                propToSave.store(out, TITLE);
                                out.close();

                                outProp = propToSave;

                                //dispose();
                            } catch (FileNotFoundException e1) {
                                System.out
                                    .println("File not found Exception");
                            } catch (IOException e2) {
                                System.out.println("IO exception");
                            }
                        }
                    } catch (BootException be) {
                        statusField.setText(be.getMessage());
                    }
                }
            }
        });
        buttonPanel.add(saveB);

        JButton runB = new JButton("Run");

        runB.setToolTipText("Launch the system");
        runB.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                String param = (String) e.getActionCommand();

                if (param.equals("Run")) {
                    ExtendedProperties propToSave = extractPropertiesFromGui();

                    try {
                        booter.setProperties(propToSave);

                        propToSave = booter.getProperties();

                        boolean different = false;

                        if (propertyFileName != null) {

                            // compares the properties from gui with those in the file
                            ExtendedProperties p =
                                readPropertiesFromFile(propertyFileName);

                            //p.list(System.out);
                            different = compareProperties(propToSave, p);
                        }

                        if (different || (propertyFileName == null)) {
                            int val = JOptionPane.showConfirmDialog(topPanel, "Save this configuration?",
                                                 "JADE Configurator", JOptionPane.YES_NO_CANCEL_OPTION);
                            if (val == JOptionPane.CANCEL_OPTION) {
                                return;
                            } else if (val == JOptionPane.YES_OPTION) {
                                //Save file
                                JFileChooser chooser = new JFileChooser();

                                chooser.setFileFilter(new myFileFilter());

                                if (currentDir != null) {
                                    chooser.setCurrentDirectory(currentDir);
                                }

                                int returnVal = chooser.showSaveDialog(null);

                                if (returnVal
                                        == JFileChooser.APPROVE_OPTION) {
                                    currentDir =
                                        chooser.getCurrentDirectory();

                                    String fileName =
                                        chooser.getSelectedFile()
                                            .getAbsolutePath();
                                    boolean ext = hasExtension(fileName);

                                    if (ext == false) {
                                        fileName = fileName.concat(".conf");
                                    }

                                    try {
                                        FileOutputStream out =
                                            new FileOutputStream(fileName);

                                        propToSave.store(out, TITLE);
                                        out.close();
                                    } catch (FileNotFoundException e1) {
                                        System.out.println(
                                            "File not found exception");
                                    } catch (IOException e2) {
                                        System.out.println("IO exception");
                                    }
                                }
                            }
                        }

                        outProp = propToSave;

                        dispose();
                    } catch (BootException be) {
                        statusField.setText(be.getMessage());
                    } catch (FileNotFoundException e1) {
                        System.out.println("File not found");
                    } catch (IOException e2) {
                        System.out.println("Io Exception");
                    }
                }
            }
        });
        buttonPanel.add(runB);

        JButton exitB = new JButton("Exit");

        exitB.setToolTipText("Exit without executing");
        exitB.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                String param = (String) e.getActionCommand();

                if (param.equals("Exit")) {
                    System.exit(0);
                }
            }
        });
        buttonPanel.add(exitB);

        JButton helpB = new JButton("Help");

        helpB.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                String param = (String) e.getActionCommand();

                if (param.equals("Help")) {
                    TreeHelp help = new TreeHelp(thisBootGui, "Boot Help",
                                                 "help/BOOTGUI.html");

                    // must insert the listener for the close action
                    help.setVisible(true);
                    help.requestFocus();
                }
            }
        });
        buttonPanel.add(helpB);
        topPanel.add(buttonPanel);
        topPanel.add(propertyPanel);
        mainPanel.add(topPanel);
        mainPanel.add(statusPanel);
        getContentPane().add(mainPanel, BorderLayout.CENTER);
        setResizable(false);
        setModal(true);
        ShowCorrect();
    }

    /**
     * Extract the values of the configuration properties from the GUI.
     */
    ExtendedProperties extractPropertiesFromGui() {
        ExtendedProperties out = new ExtendedProperties();
        int size = propertyPanel.getComponentCount();

        for (Enumeration it = propertiesVector.elements();
                it.hasMoreElements(); ) {
            PropertyType prop = (PropertyType) it.nextElement();
            String name = prop.getName();
            String type = prop.getType();
            String defaultVal = prop.getDefaultValue();
            boolean found = false;

            for (int i = 0; (i < size) &&!found; i++) {
                JPanel singlePanel = (JPanel) propertyPanel.getComponent(i);
                JLabel label = (JLabel) singlePanel.getComponent(0);

                if (name.equalsIgnoreCase(label.getText())) {
                    found = true;

                    if (type.equalsIgnoreCase(PropertyType.COMBO_TYPE)) {

                        //JComboBox
                        JComboBox box =
                            (JComboBox) singlePanel.getComponent(1);

                        out.setProperty(name.toLowerCase(),
                                        box.getSelectedItem().toString());
                    }

                    else if (type.equalsIgnoreCase(PropertyType.BOOLEAN_TYPE)) {

                        //JCheckBox
                        JCheckBox box =
                            (JCheckBox) singlePanel.getComponent(1);

                        out.setProperty(name.toLowerCase(),
                                        (new Boolean(box.isSelected()))
                                            .toString());
                    } else {

                        //JTextField
                        JTextField textField =
                            (JTextField) singlePanel.getComponent(1);
                        String text = textField.getText();

                        //if the user not specificy a value the default one is saved.
                        if (text.length() == 0) {
                            text = defaultVal;
                        }

                        out.setProperty(name.toLowerCase(), text);
                    }
                }
            }
        }

        //System.out.println("extract from GUI: ");
        //out.list(System.out);
        return out;
    }

    /**
     * Compare two property collections.
     */ 
    boolean compareProperties(ExtendedProperties p1,
                                      ExtendedProperties p2) {
        Enumeration keys = p1.keys();
        boolean modified = false;

        while (keys.hasMoreElements() &&!modified) {
            String k1 = (String) keys.nextElement();
            String v1 = p1.getProperty(k1);
            String v2 = p2.getProperty(k1);

            if (v1 == null) {
                modified = (v2 != null);
            } else {
                modified = !(v1.equalsIgnoreCase(v2));
            }
        }

        return modified;
    }

    /**
     * Update the gui when a new file is opened.
     * For every property it sets in the vector of property the value read in the file
     * or if it is absent the default value.
     */
    void updateProperties() {
        int size = propertyPanel.getComponentCount();

        for (Enumeration it = propertiesVector.elements();
                it.hasMoreElements(); ) {
            PropertyType prop = (PropertyType) it.nextElement();
            String name = prop.getName();
            String type = prop.getType();
            String newValue = prop.getDefaultValue();
            boolean found = false;

            for (int i = 0; (i < size) &&!found; i++) {
                JPanel singlePanel = (JPanel) propertyPanel.getComponent(i);
                JLabel label = (JLabel) singlePanel.getComponent(0);

                if (name.equalsIgnoreCase(label.getText())) {
                    found = true;
                    if (type.equalsIgnoreCase(PropertyType.BOOLEAN_TYPE)) {
                        //JCheckBox
                        JCheckBox box = (JCheckBox) singlePanel.getComponent(1);
                        box.setSelected(newValue.equalsIgnoreCase("true"));
                    } else {
                        //JTextField
                        JTextField textField = (JTextField) singlePanel.getComponent(1);
                        textField.setText(newValue);
                    }
                }
            }
        }
    }

    /**
     * Show the gui in the center of the screen
     */
    void ShowCorrect() {

        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        Dimension mySize = getPreferredSize();
        int x = (screenSize.width - mySize.width) / 2;
        int y = (screenSize.height - mySize.height) / 2;

        setBounds(x, y, mySize.width, mySize.height);
        setResizable(true);
        pack();
        setVisible(true);
        toFront();
    }

    /**
     * Read the properties from file and update the vector of properties.
     */
    void loadPropertiesFromFile(String fileName)
            throws FileNotFoundException, IOException {
        ExtendedProperties p = readPropertiesFromFile(fileName);

        // update the properties in the vector of properties
        // for every property set the value read in the file and set the command line value to null.
        Enumeration e = p.keys();

        while (e.hasMoreElements()) {
            boolean found = false;
            String name = (String) e.nextElement();
            Enumeration it = propertiesVector.elements();

            while (it.hasMoreElements() &&!found) {
                PropertyType pt = (PropertyType) it.nextElement();

                if (pt.getName().equalsIgnoreCase(name)) {
                    found = true;

                    pt.setDefaultValue(p.getProperty(name));
                }
            }
        }
    }

    /**
     * Read the properties from a specific file.
     */
    ExtendedProperties readPropertiesFromFile(String fileName) throws FileNotFoundException, IOException {
        ExtendedProperties p = new ExtendedProperties();
        FileInputStream in = new FileInputStream(fileName);

        p.load(in);
        in.close();

        return p;
    }

    /**
     * Verify if the file written by the user has the right extension. (Used in the save action)
     */
    boolean hasExtension(String fileName) {
        String ext = null;
        boolean out = false;
        int i = fileName.lastIndexOf('.');

        if ((i > 0) && (i < fileName.length() - 1)) {
            ext = fileName.substring(i + 1);
        }

        if (ext != null) {
            if (ext.equalsIgnoreCase("conf")) {
                out = true;
            }
        }

        return out;
    }

    /**
     * Returns a list of PropertyType used by the BootGUI to initialize the GUI.
     */
    Vector createPropertyVector(ExtendedProperties theProperties) {
        Vector pv = new Vector();
        String[] loginEnum = {"Simple", "Unix", "NT", "Kerberos"};
        pv.add(new PropertyType(BootProfileImpl.LOGIN_KEY,
                                PropertyType.COMBO_TYPE,
                                loginEnum,
                                theProperties.getProperty(BootProfileImpl.LOGIN_KEY),
                                "User Authentication context",
                                false));

        pv.add(new PropertyType(BootProfileImpl.MAIN_HOST,
                                PropertyType.STRING_TYPE,
                                theProperties.getProperty(BootProfileImpl.MAIN_HOST),
                                "Host Name of the main-container",
                                false));
                                        
        pv.add(new PropertyType(BootProfileImpl.GUI_KEY,
                                PropertyType.BOOLEAN_TYPE,
                                new Boolean(theProperties.getBooleanProperty(BootProfileImpl.GUI_KEY, false)).toString(),
                                "Select to launch the RMA Gui",
                                false));
                             
        pv.add(new PropertyType(BootProfileImpl.MAIN_PORT,
                                PropertyType.STRING_TYPE,
                                new Integer(theProperties.getIntProperty(BootProfileImpl.MAIN_PORT,
                                            BootProfileImpl.DEFAULT_PORT)).toString(),
                                "Port Number of the main-container",
                                false));
                                        
        pv.add(new PropertyType(BootProfileImpl.NAME_KEY,
                                PropertyType.STRING_TYPE,
                                theProperties.getProperty(BootProfileImpl.NAME_KEY),
                                "The symbolic plaform name",
                                false));
                                        
        pv.add(new PropertyType(BootProfileImpl.CONTAINER_KEY,
                                PropertyType.BOOLEAN_TYPE,
                                new Boolean(theProperties.getBooleanProperty(BootProfileImpl.CONTAINER_KEY, false)).toString(),
                                "Select to launch an agent-container",
                                false));
                                             
        pv.add(new PropertyType(BootProfileImpl.MTP_KEY,
                                PropertyType.STRING_TYPE,
                                theProperties.getProperty(BootProfileImpl.MTP_KEY),
                                "List of MTPs to activate",
                                false));
                                       
        pv.add(new PropertyType(BootProfileImpl.NOMTP_KEY,
                                PropertyType.BOOLEAN_TYPE,
                                new Boolean(theProperties.getBooleanProperty(BootProfileImpl.NOMTP_KEY, false)).toString(),
                                "Disable all external MTPs on this container",
                                false));
                                         
        pv.add(new PropertyType(BootProfileImpl.ACLCODEC_KEY,
                                PropertyType.STRING_TYPE,
                                theProperties.getProperty(BootProfileImpl.ACLCODEC_KEY),
                                "List of ACLCodec to install",
                                false));
                                            
        pv.add(new PropertyType(BootProfileImpl.AGENTS,
                                PropertyType.STRING_TYPE,
                                theProperties.getProperty(BootProfileImpl.AGENTS),
                                "Agents to launch",
                                false));
        pv.add(new PropertyType(BootProfileImpl.NOMOBILITY_KEY,
                                PropertyType.BOOLEAN_TYPE,
                                new Boolean(theProperties.getBooleanProperty(BootProfileImpl.NOMOBILITY_KEY,false)).toString(),
                                "Disable Mobility",
                                false));
        return pv;
    }

    /**
     * A JPanel for a single property.
     */
    class singlePanel extends JPanel {
        singlePanel() {
            super();
        }

        JPanel newSinglePanel(PropertyType property) {
            JPanel mainP = new JPanel(new FlowLayout(FlowLayout.LEFT));
            Border etched = BorderFactory.createEtchedBorder(Color.white,
                                Color.gray);
            String name = property.getName();
            JLabel nameLabel = new JLabel(name.toUpperCase());

            nameLabel.setPreferredSize(new Dimension(80, 26));
            nameLabel.setMaximumSize(new Dimension(80, 26));
            nameLabel.setMinimumSize(new Dimension(80, 26));
            mainP.add(nameLabel);

            String type = property.getType();
            JComboBox valueCombo;
            JCheckBox valueBox;
            JTextField valueText;

            // if the property has a command line value than it is used
            // otherwise is used the default value          
            String value = property.getDefaultValue();

            if (type.equalsIgnoreCase(PropertyType.COMBO_TYPE)) {
                valueCombo = new JComboBox(property.getComboValues());
                valueCombo.setSelectedIndex(0);
                valueCombo.setToolTipText(property.getToolTip());
                mainP.add(valueCombo);
            } else if (type.equalsIgnoreCase(PropertyType.BOOLEAN_TYPE)) {
                valueBox = new JCheckBox();
                valueBox.setSelected((new Boolean(value)).booleanValue());
                valueBox.setToolTipText(property.getToolTip());
                mainP.add(valueBox);
            } else {
                valueText = new JTextField();
                valueText.setBorder(etched);
                if (type.equalsIgnoreCase(PropertyType.INTEGER_TYPE)) {
                    valueText.setPreferredSize(new Dimension(100, 26));
                    valueText.setMaximumSize(new Dimension(100,26));
                } else {
                    valueText.setPreferredSize(new Dimension(600, 26));
                }
                valueText.setMinimumSize(new Dimension(50, 26));
                valueText.setText(value);
                valueText.setToolTipText(property.getToolTip());
                mainP.add(valueText);
            }

            return mainP;
        }
    }

    /**
     * Extends FileFilter in order to show only files with extension ".conf".
     */
    class myFileFilter extends FileFilter {

        public boolean accept(File f) {
            if (f.isDirectory()) {
                return true;
            }

            String ext = getExtension(f);

            if (ext != null) {
                if (ext.equals(EXTENSION)) {
                    return true;
                } else {
                    return false;
                }
            }

            return false;
        }

        public String getDescription() {
            return "Configuration file (*." + EXTENSION + ")";
        }

        String getExtension(File f) {
            String ext = null;
            String s = f.getName();
            int i = s.lastIndexOf('.');

            if ((i > 0) && (i < s.length() - 1)) {
                ext = s.substring(i + 1).toLowerCase();
            }

            return ext;
        }
    }
}

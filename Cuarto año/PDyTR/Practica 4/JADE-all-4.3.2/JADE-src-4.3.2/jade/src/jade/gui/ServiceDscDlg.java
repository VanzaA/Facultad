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

//#APIDOC_EXCLUDE_FILE
//#J2ME_EXCLUDE_FILE

// Import required Java classes 
import java.awt.*;
import java.awt.event.*;
import java.io.Serializable;

import javax.swing.*;
import java.util.Iterator;
import java.util.Properties;
import java.util.Enumeration;

// Import required JADE classes
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.FIPAAgentManagement.Property;
import jade.lang.acl.ISO8601;

/**
@author Giovanni Caire - Adriana Quinto- CSELT S.p.A.
@version $Date: 2011-06-06 09:19:06 +0200(lun, 06 giu 2011) $ $Revision: 6415 $
 */

public class ServiceDscDlg extends JDialog {
	JTextField txtName,txtType,txtOwner;
	VisualStringList ontologiesListPanel,protocolsListPanel,languagesListPanel;
	VisualPropertiesList propertiesListPanel;
	ServiceDescription serviceDesc;
	ServiceDescription out;
	boolean editable;
	boolean checkSlots;

	/*#DOTNET_INCLUDE_BEGIN
	Component myComponent;
	private Component getOwner() {return myComponent;}
	#DOTNET_INCLUDE_END*/

	// CONSTRUCTORS
	ServiceDscDlg(Frame parent) 
	{
		super(parent);
		/*#DOTNET_INCLUDE_BEGIN
		myComponent = parent;
		#DOTNET_INCLUDE_END*/
	}

	ServiceDscDlg(Dialog parent) 
	{
		super(parent);
		/*#DOTNET_INCLUDE_BEGIN
		myComponent = parent;
		#DOTNET_INCLUDE_END*/
	}

	/**
	This method shows the gui to display a ServiceDescription.
	@param dsc the dsc to show
	@param ed true if the gui must be editable, false otherwise
	@param checkMandatorySlots true to force the check of the mandatory slots, false otherwise
	@return a ServiceDescription if the Ok button is pressed, false otherwise.
	 */
	ServiceDescription viewSD(ServiceDescription dsc, boolean ed, boolean checkMandatorySlots)
	{

		setTitle("Service");
		out = null;
		editable = ed;
		checkSlots = checkMandatorySlots;

		if(dsc != null)
			serviceDesc = dsc;
		else
			serviceDesc = new ServiceDescription();

		JPanel main = new JPanel();
		main.setLayout(new BoxLayout(main,BoxLayout.Y_AXIS));

		JPanel p = new JPanel();
		JLabel l;

		//Name	
		p.setLayout(new BoxLayout(p, BoxLayout.X_AXIS));
		l = new JLabel("Name");
		l.setPreferredSize(new Dimension(130,20));
		p.add(l);
		p.add(Box.createHorizontalGlue());
		txtName = new JTextField();
		txtName.setPreferredSize(new Dimension(200,20));
		txtName.setText(serviceDesc.getName());
		txtName.setEditable(editable);
		p.add(txtName);
		main.add(p);
		main.add(Box.createRigidArea(new Dimension (0,3)));

		//Type
		p = new JPanel();
		p.setLayout(new BoxLayout(p, BoxLayout.X_AXIS));
		l = new JLabel("Type");
		l.setPreferredSize(new Dimension(130,20));
		p.add(l);
		p.add(Box.createHorizontalGlue());
		txtType = new JTextField();
		txtType.setPreferredSize(new Dimension (200,20));
		txtType.setText(serviceDesc.getType());
		txtType.setEditable(editable);
		p.add(txtType);
		main.add(p);
		main.add(Box.createRigidArea(new Dimension (0,3)));

		//Ownership
		p = new JPanel();
		p.setLayout(new BoxLayout(p,BoxLayout.X_AXIS));
		l = new JLabel("Ownership");
		l.setPreferredSize(new Dimension(130,20));
		p.add(l);
		p.add(Box.createHorizontalGlue());
		txtOwner = new JTextField();
		txtOwner.setPreferredSize(new Dimension(200,20));
		txtOwner.setText(serviceDesc.getOwnership());
		txtOwner.setEditable(editable);
		p.add(txtOwner);
		main.add(p);
		main.add(Box.createRigidArea(new Dimension (0,3)));

		//Languages
		p = new JPanel();
		p.setLayout(new BorderLayout());
		p.setBorder(BorderFactory.createTitledBorder("Languages"));	
		languagesListPanel = new VisualStringList(serviceDesc.getAllLanguages(),getOwner());
		languagesListPanel.setDimension(new Dimension(350,40));
		languagesListPanel.setEnabled(editable);
		p.add(languagesListPanel);
		main.add(p);
		main.add(Box.createRigidArea(new Dimension (0,3)));


		//Ontologies
		p = new JPanel();
		p.setLayout(new BorderLayout());
		p.setBorder(BorderFactory.createTitledBorder("Ontologies"));	
		ontologiesListPanel = new VisualStringList(serviceDesc.getAllOntologies(),getOwner());
		ontologiesListPanel.setDimension(new Dimension(350,40));
		ontologiesListPanel.setEnabled(editable);
		p.add(ontologiesListPanel);
		main.add(p);
		main.add(Box.createRigidArea(new Dimension (0,3)));

		//Protocols.
		p = new JPanel();
		p.setLayout(new BorderLayout());
		p.setBorder(BorderFactory.createTitledBorder("Protocols"));
		protocolsListPanel = new VisualStringList(serviceDesc.getAllProtocols(),getOwner());
		protocolsListPanel.setDimension(new Dimension(350,40));
		protocolsListPanel.setEnabled(editable);
		p.add(protocolsListPanel);
		main.add(p);


		//Properties
		p = new JPanel();
		p.setLayout(new BorderLayout());
		p.setBorder(BorderFactory.createTitledBorder("Properties"));
		//#DOTNET_EXCLUDE_BEGIN
		Iterator temp = serviceDesc.getAllProperties();
		//#DOTNET_EXCLUDE_END
		/*#DOTNET_INCLUDE_BEGIN
		jade.util.leap.Iterator temp = serviceDesc.getAllProperties();
		#DOTNET_INCLUDE_END*/
		Properties props = new Properties();
		while(temp.hasNext())
		{
			Property singleProp = (Property)temp.next();
			props.setProperty(singleProp.getName(),singleProp.getValue().toString()); 
		}
		propertiesListPanel = new VisualPropertiesList(props,getOwner());
		propertiesListPanel.setDimension(new Dimension(350,40));
		propertiesListPanel.setEnabled(editable);
		p.add(propertiesListPanel);
		main.add(p);

		//Button Panel
		p = new JPanel();
		p.setLayout(new BoxLayout(p, BoxLayout.X_AXIS));
		JButton bOK = new JButton("OK");

		bOK.addActionListener( new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{    
				String param = (String) e.getActionCommand();
				if (param.equals("OK"))
				{
					if(editable)
					{

						if(checkSlots)
						{	

							if(getSaveText(txtName) == null)
							{
								JOptionPane.showMessageDialog(null,"The name must not be empty !","Error Message", JOptionPane.ERROR_MESSAGE);
								return;
							}


							if(getSaveText(txtType) == null)
							{
								JOptionPane.showMessageDialog(null,"The type must not be empty !","Error Message",JOptionPane.ERROR_MESSAGE);
								return;
							}
						}

						out = new ServiceDescription();
						out.setName(getSaveText(txtName));
						out.setType(getSaveText(txtType));

						out.setOwnership(getSaveText(txtOwner));

						Enumeration lang = languagesListPanel.getContent();
						while(lang.hasMoreElements())
							out.addLanguages((String)lang.nextElement());

						Enumeration onto = ontologiesListPanel.getContent();
						while(onto.hasMoreElements())
							out.addOntologies((String)onto.nextElement());

						//Protocols
						Enumeration proto = protocolsListPanel.getContent();
						while(proto.hasMoreElements())
							out.addProtocols((String)proto.nextElement());

						Properties ps = propertiesListPanel.getContentProperties();	
						Enumeration keys = ps.propertyNames();
						while(keys.hasMoreElements())
						{
							Property tp = new Property();
							String key = (String)keys.nextElement();
							tp.setName(key);
							Object val = ps.getProperty(key);
							// try if the property is a long or a float or a datetime
							try {
								val = Long.valueOf(val.toString());
							} catch (NumberFormatException e1) {
								try {
									val = Double.valueOf(val.toString());
								} catch (NumberFormatException e2) {
									try {
										val = ISO8601.toDate(val.toString());
									} catch (Exception e3) {
									}
								}
							}
							// set the value of this property
							tp.setValue((Serializable)val);
							out.addProperties(tp);
						}

					}
					else
						out = serviceDesc;			

					dispose();
				}
			} 
		} );

		p.add(bOK);

		if(editable)
		{
			//CancelButton
			JButton bCancel = new JButton("Cancel");
			p.add(bCancel);
			bCancel.addActionListener( new ActionListener()
			{
				public void actionPerformed(ActionEvent e)
				{    
					String param = (String) e.getActionCommand();
					if (param.equals("Cancel"))
					{
						out = null;
						dispose();
					}
				} 
			});
			p.add(bCancel);
		}
		main.add(p);

		getContentPane().add(main, BorderLayout.CENTER);
		//pack();
		setModal(true);
		setResizable(false);

		try{
			//#DOTNET_EXCLUDE_BEGIN
			int x = getOwner().getX() + (getOwner().getWidth() - getWidth()) / 2;
			int y = getOwner().getY() + (getOwner().getHeight() - getHeight()) / 2; 
			//#DOTNET_EXCLUDE_END
			/*#DOTNET_INCLUDE_BEGIN
    		int x = getOwner().getX() + (getOwner().WIDTH - getWidth()) / 2;
    		int y = getOwner().getY() + (getOwner().HEIGHT - getHeight()) / 2; 
			#DOTNET_INCLUDE_END*/

			setLocation(x>0 ? x:0,y>0 ? y:0);
		}
		catch(Exception e){}
		pack();
		setVisible(true);

		return out;		

	}

	/**
	Return the string relative to service description fields if not empty, null otherwise
	 */
	private String getSaveText(JTextField field){
		try{
			String out = field.getText().trim();
			return (out.length() == 0 ? null : out);
		}catch( Exception e){
			return null;
		}

	}

}

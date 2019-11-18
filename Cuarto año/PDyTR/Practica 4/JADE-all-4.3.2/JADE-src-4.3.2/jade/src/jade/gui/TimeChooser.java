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

// Import required Java classes
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.border.*;

import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Date;
import java.text.*;

/**
 * The <code>TimeChooser</code> class can be used to let the user
 * define a certain point in time by means of a dialog window.<p> <p>
 * @author Giovanni Rimassa - Universita' di Parma
 * @version $Date: 2010-04-15 11:26:09 +0200 (gio, 15 apr 2010) $ $Revision: 6311 $

 */

public class TimeChooser implements ActionListener
{
	private Date        date;
	private int         mode;
	private int         retVal;
	private JDialog     dlg;
	private JTextField  year, month, day, hour, min, sec;

	JToggleButton absButton;
	JToggleButton relButton;

    //#APIDOC_EXCLUDE_BEGIN
	public static final int ABSOLUTE = 0;
	public static final int RELATIVE = 1;
	public static final int OK     = 1;
	public static final int CANCEL = 0;
    //#APIDOC_EXCLUDE_END

    /**
       Default constructor.
    */
	public TimeChooser()
	{
		retVal = CANCEL;
		date = null;
	}

    /**
       Create a time chooser set at the given time instant.
       @param d The initial time instant for this time chooser.
    */
	public TimeChooser(Date d)
	{
		retVal = CANCEL;
		date = d;
	}

    /**
       Allow the user to manipulate a time instant through this
       dialog.
       @param parent The parent component for this dialog.
    */
	public int showEditTimeDlg(JFrame parent)
	{
		Calendar cal;

		dlg = new JDialog(parent, "Edit time");

/*
// THIS PART IS RELATED TO THE EDITING OF RELATIVE TIMES (NOT YET IMPLEMENTED)
		JPanel modePanel = new JPanel();
		absButton = new JToggleButton("Absolute");
		relButton = new JToggleButton("Relative");
		relButton.setPreferredSize(absButton.getPreferredSize());
		absButton.setSelected(true);
		relButton.setSelected(false);
		mode = ABSOLUTE;
		absButton.addActionListener(this);
		modePanel.add(absButton);
		relButton.addActionListener(this);
		modePanel.add(relButton);

		dlg.getContentPane().add(modePanel);

		dlg.getContentPane().add(Box.createVerticalStrut(5));
*/
		// Controls to set the time
		cal = new GregorianCalendar();
		if (date != null)
			cal.setTime(date);

		JPanel timePanel = new JPanel();
		timePanel.setLayout(new GridLayout(2,3));
		((GridLayout) (timePanel.getLayout())).setHgap(5);
		
		year = new JTextField(4);
		year.setEditable(false);
		addTimeUnitLine(timePanel, cal.get(Calendar.YEAR), year, "Year:",Integer.MAX_VALUE);
		month = new JTextField(4);
		month.setEditable(false);
		addTimeUnitLine(timePanel, cal.get(Calendar.MONTH)+1, month, "Month:",12);
		day = new JTextField(4);
		day.setEditable(false);
		addTimeUnitLine(timePanel, cal.get(Calendar.DATE), day, "Day:",31);
		hour = new JTextField(4);
		hour.setEditable(false);
		addTimeUnitLine(timePanel, cal.get(Calendar.HOUR_OF_DAY), hour, "Hour:",23);
		min = new JTextField(4);
		min.setEditable(false);
		addTimeUnitLine(timePanel, cal.get(Calendar.MINUTE), min, "Min:",59);
		sec = new JTextField(4);
		sec.setEditable(false);
		addTimeUnitLine(timePanel, cal.get(Calendar.SECOND), sec, "Sec:",59);

		timePanel.setBorder(new TitledBorder("Time"));
		dlg.getContentPane().add(timePanel, BorderLayout.CENTER);


		// Buttons to set/reset the edited time
		JPanel buttonPanel = new JPanel();
		JButton setButton = new JButton("Set");
		JButton resetButton = new JButton("Reset");
		JButton cancelButton = new JButton("Cancel");
		setButton.setPreferredSize(cancelButton.getPreferredSize());
		resetButton.setPreferredSize(cancelButton.getPreferredSize());
		setButton.addActionListener(this);
		resetButton.addActionListener(this);
		cancelButton.addActionListener(this);
		buttonPanel.add(setButton);
		buttonPanel.add(resetButton);
		buttonPanel.add(cancelButton);

		dlg.getContentPane().add(buttonPanel, BorderLayout.SOUTH);

		// Display the dialog window
		dlg.setModal(true);
		dlg.pack();
		dlg.setResizable(false);
		if (parent != null) // Locate the dialog relatively to the parent
		{
			dlg.setLocation(parent.getX() + (parent.getWidth() - dlg.getWidth()) / 2, parent.getY() + (parent.getHeight() - dlg.getHeight()) / 2);
		}
		else // Locate the dialog relatively to the screen
		{
			Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
			//#DOTNET_EXCLUDE_BEGIN
			int centerX = (int)screenSize.getWidth() / 2;
			int centerY = (int)screenSize.getHeight() / 2;
			//#DOTNET_EXCLUDE_END
			/*#DOTNET_INCLUDE_BEGIN
			int centerX = (int)screenSize.width / 2;
			int centerY = (int)screenSize.height / 2;
			#DOTNET_INCLUDE_END*/
			dlg.setLocation(centerX - dlg.getWidth() / 2, centerY - dlg.getHeight() / 2);
		}
		dlg.setVisible(true);

		return(retVal);
	}

    /**
       Allow the user to view a time instant through a read-only
       version of this dialog.
       @param parent The parent component of this dialog.
    */
	public void showViewTimeDlg(JFrame parent)
	{
		String s;
		JPanel p;

		dlg = new JDialog(parent, "View Time");
		dlg.getContentPane().setLayout(new BoxLayout(dlg.getContentPane(), BoxLayout.Y_AXIS));

		// Time indication label
		p = new JPanel();
		if (date == null)
		{
			s = "No time indication to display";
		}
		else
		{	
			DateFormat df = DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.SHORT);
			s = df.format(date);
		}
		JLabel l = new JLabel(s);
		p.add(l);
		dlg.getContentPane().add(p);

		dlg.getContentPane().add(Box.createVerticalStrut(5));

		// Close button
		p = new JPanel();
		JButton b = new JButton("Close");
		b.addActionListener(this);
		p.add(b);
		dlg.getContentPane().add(p);

		// Display the dialog window
		dlg.setModal(true);
		dlg.pack();
		dlg.setResizable(false);
		if (parent != null) // Locate the dialog relatively to the parent
		{
			dlg.setLocation(parent.getX() + (parent.getWidth() - dlg.getWidth()) / 2, parent.getY() + (parent.getHeight() - dlg.getHeight()) / 2);
		}
		else // Locate the dialog relatively to the screen
		{
			Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
			//#DOTNET_EXCLUDE_BEGIN
			int centerX = (int)screenSize.getWidth() / 2;
			int centerY = (int)screenSize.getHeight() / 2;
			//#DOTNET_EXCLUDE_END
			/*#DOTNET_INCLUDE_BEGIN
			int centerX = (int)screenSize.width / 2;
			int centerY = (int)screenSize.height / 2;
			#DOTNET_INCLUDE_END*/
			dlg.setLocation(centerX - dlg.getWidth() / 2, centerY - dlg.getHeight() / 2);
		}
		dlg.setVisible(true);
	}


    //#APIDOC_EXCLUDE_BEGIN
	public void actionPerformed(ActionEvent e)
	{
		String command = e.getActionCommand();
		if (command.equals("Set"))
		{
			Integer I;
			I = new Integer(year.getText());
			int YY = I.intValue();
			I = new Integer(month.getText());
			int MM = I.intValue();
			I = new Integer(day.getText());
			int DD = I.intValue();
			I = new Integer(hour.getText());
			int hh = I.intValue();
			I = new Integer(min.getText());
			int mm = I.intValue();
			I = new Integer(sec.getText());
			int ss = I.intValue();

			Calendar cal = new GregorianCalendar(YY,MM-1,DD,hh,mm,ss);
			date = cal.getTime();

			retVal = OK;
			dlg.dispose();
		}
		else if (command.equals("Reset"))
		{
			date = null;
			retVal = OK;
			dlg.dispose();
		}
		else if (command.equals("Cancel"))
		{
			retVal = CANCEL;
			dlg.dispose();
		}
		else if (command.equals("Close"))
		{
			dlg.dispose();
		}
		else if (command.equals("Absolute"))
		{
			absButton.setSelected(true);
			relButton.setSelected(false);
			mode = ABSOLUTE;
		}
		else if (command.equals("Relative"))
		{
			relButton.setSelected(true);
			absButton.setSelected(false);
			mode = RELATIVE;
		}

	}
    //#APIDOC_EXCLUDE_END

    /**
       Retrieve the time instant this time chooser is set to.
       @return The selected time instant.
    */
	public Date getDate()
	{
		return(date);
	}

    /**
       Set the time instant for thistime chooser.
       @param d The time instant for this chooser to point to.
    */
	public void setDate(Date d)
	{
		date = d;
	}

	private void addTimeUnitLine(JPanel tp, int timeUnit, final JTextField timeUnitEdit, final String timeUnitLabel, final int limit)
	{
		JPanel up = new JPanel();
		((FlowLayout) (up.getLayout())).setHgap(0);
		
		JLabel l = new JLabel(timeUnitLabel);
		
		timeUnitEdit.setText(String.valueOf(timeUnit));

		JButton B1 = new JButton("+");
		B1.addActionListener(new	ActionListener ()
					     			{
										public void actionPerformed(ActionEvent e)
										{
											Integer i = new Integer(timeUnitEdit.getText());
											int ii = i.intValue() + 1;
											if(ii <= limit)
											  timeUnitEdit.setText(String.valueOf(ii));
										}
									} );
		JButton B2 = new JButton("-");
		B2.addActionListener(new	ActionListener ()
					     			{
										public void actionPerformed(ActionEvent e)
										{
											Integer i = new Integer(timeUnitEdit.getText());
											int ii = i.intValue() - 1;
											int inf_limit = (timeUnitLabel.equalsIgnoreCase("Hour:") || timeUnitLabel.equalsIgnoreCase("Min:") || timeUnitLabel.equalsIgnoreCase("Sec:")? 0 : 1);
											if(ii >= inf_limit)
											  timeUnitEdit.setText(String.valueOf(ii));
										}
									} );

		B1.setMargin(new Insets(2,4,2,4));
		B2.setMargin(new Insets(2,4,2,4));
		Dimension d = new Dimension();
		d.height = B1.getPreferredSize().height;
		d.width = (new JLabel("XXXXX")).getPreferredSize().width;
		l.setPreferredSize(d);
		l.setAlignmentX((float) 1);
		timeUnitEdit.setPreferredSize(new Dimension(50, d.height));

		up.add(l);
		up.add(B1);
		up.add(timeUnitEdit);
		up.add(B2);
		tp.add(up);
	}

}





		

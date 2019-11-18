package jade.imtp.leap.sms;

//#J2ME_EXCLUDE_FILE

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

import jade.util.leap.Properties;
import java.io.IOException;

/**
   @author Giovanni Caire - TILAB
 */
public class SMSManagerGui extends JFrame {
	
	private JTextField msisdnTF, portTF;
	private JTextArea textTA;
	
	private SMSManager theSMSManager;
		
	public SMSManagerGui() {
		super("SMS Manager GUI");
	}
	
	public void init(Properties pp) {		
		theSMSManager = SMSManager.getInstance(pp);
		
		if (theSMSManager != null) {
			setSize(getProperSize(320, 400));
			setResizable(false);

			JPanel main = new JPanel();
			main.setLayout(new GridLayout(2, 1));
			
			JPanel p = new JPanel();
			p.setLayout(new GridLayout(5, 1));
			// MSISDN 
			p.add(new JLabel("Telephon number:"));
			msisdnTF = new JTextField();
			p.add(msisdnTF);
			// PORT 
			p.add(new JLabel("Port:"));
			portTF = new JTextField();
			p.add(portTF);
			// TEXT
			p.add(new JLabel("Message text:"));

			main.add(p);
			
			p = new JPanel();
			p.setLayout(new GridLayout(1, 1));
			textTA = new JTextArea();
			p.add(textTA);
			
			main.add(p);
			
			getContentPane().add(main, BorderLayout.CENTER);
			
			// Command buttons
			p = new JPanel();
			JButton b = new JButton("Send");
			b.addActionListener(new ActionListener() {
		  	public void actionPerformed(ActionEvent e) {
		  		try {
			  		String msisdn = msisdnTF.getText().trim();
			  		String tmp = portTF.getText();
			  		int port = -1;
			  		if (tmp != null && tmp.trim().length() > 0) {
			  			port = Integer.parseInt(portTF.getText());
			  		}
			  		String txt = textTA.getText();
			  		theSMSManager.sendTextMessage(msisdn, port, txt);
		  		}
		  		catch (Exception ex) {
		  			ex.printStackTrace();
		  		}
		  	}
			} );
			p.add(b);
			getContentPane().add(p, BorderLayout.SOUTH);
			
			showCorrect();
		}
		else {
			System.out.println("Cannot retrieve the SMS Manager");
		}			
	}
	
	private void showCorrect(){
		// Get the size of the default screen
		Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
		int scrH = (int) dim.getHeight();
		int scrW = (int) dim.getWidth();
		setLocation((int) (scrW * 0.2), (int) (scrH * 0.1) );
		setVisible(true);
	}

	private Dimension getProperSize(int maxX, int maxY) {
		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
		int x = (screenSize.width < maxX ? screenSize.width : maxX);
		int y = (screenSize.height < maxY ? screenSize.height : maxY);
		return new Dimension(x, y);
	}
	
	public static void main(String[] args) {
		if (args != null && args.length == 1) {
			try {
				Properties pp = new Properties();
				pp.load(args[0]);
				SMSManagerGui gui = new SMSManagerGui();
				gui.init(pp); 
			}
			catch(IOException ioe) {
				ioe.printStackTrace();
			}
		}
		else {
			System.out.println("USAGE: java SMSManagerGui <properties-file>");
		}
	}
}
	
package examples.replication;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

class CreateReplicaDialog extends JDialog {
	
	private ValueProviderAgent myAgent;
	
	private JTextField replicaNameTxt;
	private JTextField locationTxt;
	
	CreateReplicaDialog(ValueProviderAgent a) {
		super();
		
		myAgent = a;
		
		getContentPane().setLayout(new BorderLayout());
		setTitle("Create new replica");
		
		JPanel pInfo = new JPanel();
		pInfo.setLayout(new GridLayout(2, 2));
		pInfo.setBorder(BorderFactory.createEmptyBorder(5,5,5,5));
		
		pInfo.add(new JLabel("Replica name:"));
		replicaNameTxt = new JTextField(10);
		pInfo.add(replicaNameTxt);
		
		pInfo.add(new JLabel("Location:"));
		locationTxt = new JTextField(10);
		pInfo.add(locationTxt);
		
		getContentPane().add(pInfo, BorderLayout.CENTER);
		
		JPanel pButtons = new JPanel();
		JButton bOK = new JButton("OK");
		bOK.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				myAgent.createReplica(replicaNameTxt.getText(), locationTxt.getText());
				dispose();
			}
		});
		pButtons.add(bOK);
		JButton bCancel = new JButton("Cancel");
		bCancel.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				dispose();
			}
		});
		pButtons.add(bCancel);
		bOK.setPreferredSize(bCancel.getPreferredSize());
		
		getContentPane().add(pButtons, BorderLayout.SOUTH);
		
		pack();
	}

}

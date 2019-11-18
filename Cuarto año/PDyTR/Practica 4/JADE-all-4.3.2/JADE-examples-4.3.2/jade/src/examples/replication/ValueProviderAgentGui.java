package examples.replication;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

public class ValueProviderAgentGui extends JFrame implements ChangeListener {
	public static final int MIN = 0;
	public static final int MAX = 30;


	private ValueProviderAgent myAgent;
	
	public ValueProviderAgentGui(ValueProviderAgent a, int initialValue) {
		super();

		myAgent = a;
		
		setTitle("Agent "+myAgent.getLocalName());
		
		// Label in the north part of the GUI.
		JLabel sliderLabel = new JLabel("Select a value using the slider", JLabel.CENTER);
		sliderLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
		sliderLabel.setBorder(BorderFactory.createEmptyBorder(10,0,0,0));
		getContentPane().add(sliderLabel, BorderLayout.NORTH);

		// Slider in the center part of the GUI.
		JSlider slider = new JSlider(JSlider.HORIZONTAL, MIN, MAX, initialValue);
		slider.addChangeListener(this);

		// Turn on labels at major tick marks.
		slider.setMajorTickSpacing(10);
		slider.setMinorTickSpacing(1);
		slider.setPaintTicks(true);
		slider.setPaintLabels(true);
		slider.setBorder(BorderFactory.createEmptyBorder(0,0,10,10));
		Font font = new Font("Serif", Font.ITALIC, 15);
		slider.setFont(font);
		slider.setPreferredSize(new Dimension(600, 70));
		
		getContentPane().add(slider, BorderLayout.CENTER);

		// Create Replica button in the south part of the GUI
		JButton button = new JButton("Create replica");
		button.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				CreateReplicaDialog d = new CreateReplicaDialog(myAgent);
				d.setModal(true);
				d.setVisible(true);
			}
		});
		JPanel p = new JPanel();
		p.add(button);
		getContentPane().add(p, BorderLayout.SOUTH);
		
		// Kill the agent (and therefore the GUI itself) when the user clicks on the X in the top right corner
		addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				myAgent.doDelete();
			}
		});
		
		pack();
		setResizable(false);
	}
	
	
	// This is invoked whenever the user moves the slider
	@Override
	public void stateChanged(ChangeEvent e) {
        JSlider source = (JSlider) e.getSource();
		myAgent.setValue(source.getValue());
	}

}

package examples.O2AInterface;

import jade.core.Agent;
import jade.core.behaviours.Behaviour;
import jade.core.behaviours.TickerBehaviour;

public class CounterAgent extends Agent implements CounterManager1,
		CounterManager2 {
	/**
	 * This class is a agent that implements O2AInterface
	 * 
	 * @author Giovanni Iavarone - Michele Izzo
	 */

	// Instance variables
	// ////////////////////////////////
	protected int i = 0;
	protected Behaviour counter;
	 
	/**
     * Construct the CounterAgent agent. 
     */
	public CounterAgent() {
		registerO2AInterface(CounterManager1.class, this);

		registerO2AInterface(CounterManager2.class, this);
	}

	/**
	 * ActivateCounter the agent. This method adds a ticker behavior that
	 * increases the counter every 5 seconds and prints the value
	 */
	@Override
	public void activateCounter() {

		counter = new TickerBehaviour(this, 5000) {
			/**
			 * 
			 */
			private static final long serialVersionUID = 1L;

			protected void onTick() {
				i = i + 1;
				System.out.println("counter :" + i);
			}
		};

		addBehaviour(counter);
	}

	/**
	 * deactivateCounter the agent. This is method remove a ticker behavior
	 */
	public void deactivateCounter() {
		System.out.println("remove Ticker Behaviour!");
		removeBehaviour(counter);

	}

}

// End of CounterAgent class


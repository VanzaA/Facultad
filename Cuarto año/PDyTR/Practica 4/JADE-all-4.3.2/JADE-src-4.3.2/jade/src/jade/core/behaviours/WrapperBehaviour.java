package jade.core.behaviours;

import jade.core.Agent;
import jade.core.behaviours.Behaviour;
import jade.core.behaviours.CompositeBehaviour;
import jade.util.leap.Collection;

/**
 * This behaviour allows modifying on the fly the way an existing behaviour object works
 * The following piece of code provides an example where we modify the <code>done()</code>
 * method of an existing behaviour object to print on the standard output a proper message when the behaviour 
 * is completing.
 * 
 <pr><hr><blockquote><pre>
 Behaviour b = // get the behaviour object 
 addBehaviour(new WrapperBehaviour(b) {
   public boolean done() {
     boolean ret = super.done();
     if (ret) {
       System.out.println("done() method returned true --> The behaviour is completing!");
     }
     return ret;
   }
 });
 </pre></blockquote><hr>
 *  
 */
public class WrapperBehaviour extends Behaviour {
	private Behaviour wrappedBehaviour;
	
	public WrapperBehaviour(Behaviour wrapped) {
		super(wrapped.myAgent);
		wrappedBehaviour = wrapped;
		// Set a dummy parent to the wrapped-behaviour to allow propagation of upward events and root calculations 
		// from the wrapped behaviour to this WrapperBehaviour
		wrappedBehaviour.setParent(new CompositeBehaviour() {
			// Redefine the handle() method to propagate events (note that these are always upwards events
			// from the wrapped-behaviour) to this WrapperBehaviour 
			protected void handle(RunnableChangedEvent rce) {
				WrapperBehaviour.this.handle(rce);
			}

			// Redefine the root() method to propagate root calculations to this WrapperBehaviour 
			public Behaviour root() {
				return WrapperBehaviour.this.root();
			}
			protected void scheduleFirst() {}
			protected void scheduleNext(boolean currentDone, int currentResult) {}
			protected boolean checkTermination(boolean currentDone, int currentResult) {return false;}
			protected Behaviour getCurrent() {return null;}
			public Collection getChildren() {return null;}
		});
	}
	
	//#APIDOC_EXCLUDE_BEGIN
	/**
	 * This method is used internally by the framework. Developer should not call or redefine it.
	 */
	protected void handleBlockEvent() {
		// Notify upwards
		super.handleBlockEvent();

		// Then notify downwards
		myEvent.init(false, NOTIFY_DOWN);
		handle(myEvent);
	}

	/**
	 * This method is used internally by the framework. Developer should not call or redefine it.
	 */
	public void handleRestartEvent() {
		// Notify downwards
		myEvent.init(true, NOTIFY_DOWN);
		handle(myEvent);

		// Then notify upwards
		super.handleRestartEvent();
	}
	//#APIDOC_EXCLUDE_END
	
	protected void handle(RunnableChangedEvent rce) {
		super.handle(rce);
		// If the event is downwards, propagate it to the wrapped-behaviour 
		if (!rce.isUpwards()) {
			wrappedBehaviour.handle(rce);
		}
	}
	
	public void setAgent(Agent a) {
		super.setAgent(a);
		wrappedBehaviour.setAgent(a);
	}
	
	void setParent(CompositeBehaviour parent) {
		super.setParent(parent);
		wrappedBehaviour.setWrappedParent(parent);
	}
	
	public void setDataStore(DataStore ds) {
		wrappedBehaviour.setDataStore(ds);
	}
	
	public DataStore getDataStore() {
		return wrappedBehaviour.getDataStore();
	}
	
	public void reset() {
		wrappedBehaviour.reset();
		super.reset();
	}
	
	public void onStart() {
		wrappedBehaviour.onStart();
	}
	
	public void action() {
		wrappedBehaviour.action();
	}
	
	public boolean done() {
		return wrappedBehaviour.done();
	}
	
	public int onEnd() {
		return wrappedBehaviour.onEnd();
	}
	
	public Behaviour getWrappedBehaviour() {
		return wrappedBehaviour;
	}
}

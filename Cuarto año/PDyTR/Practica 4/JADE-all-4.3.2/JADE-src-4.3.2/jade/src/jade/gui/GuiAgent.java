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

// Import required java classes
import java.util.Vector;

// Import required Jade classes
import jade.core.*;
import jade.core.behaviours.*;

/**
When a program instantiates a GUI, the Java programming language 
starts a new thread, different from the Agent thread.
The Agent thread generally is active because it has its tasks to perform 
and also the GUI thread is active, in respect to the Agent thread, because 
its behaviour depends on the user actions (e.g. pressing a button, using 
the nemu bar,...) and not only on the agent task.
Therefore, an appropriate mechanism is needed to manage the interaction 
between these two active threads.
It is not a good practice allowing one thread to just call the method of 
another thread because of the difference in the thread space.
What should be done, instead is one thread requesting the other to execute a 
method, each thread in its one execution space.
Since its common to have an agent with a GUI, this class is for this purpose.
This class extends the <code>jade.core.Agent </code> class: at the start-up 
it instantiate ad ad-hoc behaviour that manages a queue of 
<code>jade.gui.GuiEvent</code>,event objects that can be received by other threads.  
A thread (in particular a GUI)to notify an event to an Agent should create 
a new Object of type <code>jade.gui.GuiEvent</code>and pass it as a parameter 
to the call of the method <code>postGuiEvent</code> of the
<code>jade.gui.GuiAgent</code> object. Notice that an object of type 
<code>GuiEvent</code> has two mandatory attributes and an optional 
list of parameters that can be added to the event object.
After the method <code>postGuiEvent</code> is called,the agent reacts 
by waking up all its active behaviours, and in particular the one that causes
the Agent thread to execute the method <code>onGuiEvent</code>.

@see jade.core.Agent
@see jade.gui.GuiEvent
@author Giovanni Caire - CSELT S.p.A.
@version $Date: 2005-04-15 17:45:02 +0200 (ven, 15 apr 2005) $ $Revision: 5669 $
*/
public abstract class GuiAgent extends Agent
{
  private static final long     serialVersionUID = 3487495895819010L;
	
	/**
	@serial
	*/
	private Vector guiEventQueue;
	/**
	@serial
	*/
	private Boolean guiEventQueueLock;

	////////////////////////
	// GUI HANDLER BEHAVIOUR
	private class GuiHandlerBehaviour extends SimpleBehaviour
	{
  	private static final long     serialVersionUID = 3487495895819011L;
		protected GuiHandlerBehaviour()
		{
			super(GuiAgent.this);
		}

		public void action()
		{
			if (!guiEventQueue.isEmpty())
			{
				GuiEvent ev = null;  				
				synchronized(guiEventQueueLock)
				{
					try
					{
						ev  = (GuiEvent) guiEventQueue.elementAt(0);
						guiEventQueue.removeElementAt(0);
					}
					catch (ArrayIndexOutOfBoundsException ex)
					{
						ex.printStackTrace(); // Should never happen
					}
					/*#DOTNET_INCLUDE_BEGIN
					 catch (Exception exc)
					 {
					 ev = null;
					 guiEventQueue.removeElementAt(0);
					 }
					 #DOTNET_INCLUDE_END*/
				}
				//#DOTNET_EXCLUDE_BEGIN
				onGuiEvent(ev);
				//#DOTNET_EXCLUDE_END
				/*#DOTNET_INCLUDE_BEGIN
				if (ev != null)
					onGuiEvent(ev);
				else
					block();
				#DOTNET_INCLUDE_END*/
			}
			else
				block();
		}

		public boolean done()
		{
			return(false);
		}
	}

    /**
       Default constructor.
    */
    public GuiAgent()
    {
	super();
	guiEventQueue = new Vector();
	guiEventQueueLock = new Boolean(true);

	// Add the GUI handler behaviour
	Behaviour b = new GuiHandlerBehaviour();
	addBehaviour(b);
    }

    /**
       Posts an event from the GUI thread to the agent event queue.
       @param e The GUI event to post.
    */
    public void postGuiEvent(GuiEvent e)
    {
	synchronized(guiEventQueueLock)
	    {
		guiEventQueue.addElement( (Object) e );
		doWake();
	    }
    }

	/////////////////////////////////////////////////////////////////////////
	// METHODS TO POST PREDEFINED EXIT AND CLOSEGUI EVENTS IN GUI EVENT QUEUE
	/*public void postExitEvent(Object g)
	{
		GuiEvent e = new GuiEvent(g, GuiEvent.EXIT);
		postGuiEvent(e);
	}

	public void postCloseGuiEvent(Object g)
	{
		GuiEvent e = new GuiEvent(g, GuiEvent.CLOSEGUI);
		postGuiEvent(e);
	}*/


	/**
	   Abstract method to handle posted GUI events. Subclasses of
	   <code>GuiAgent</code> will implement their own reactions to
	   GUI events starting with this method.
	   @param ev The GUI event to handle.
	*/
	protected abstract void onGuiEvent(GuiEvent ev);
	
}

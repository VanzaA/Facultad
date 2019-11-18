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

// Import required Jade classes
import jade.core.*;
import jade.util.leap.List;
import jade.util.leap.ArrayList;
import jade.util.leap.Iterator;

/**
This class defines the object type <code>GuiEvent</code> used to notify 
an event to a GuiAgent. It has two mandatory attributes:the source of the event 
and an integer identifying the type of event and an optional list of parameters 
than can be added to the event object.The type of each parameter must extends
<code>java.lang.Object</code>; therefore primitive object (e.g.int) should be wrapped
into appropriate objects(e.g <code>java.lang.Integer</code>).

@see jade.gui.GuiAgent
@author Giovanni Caire - CSELT S.p.A.
@version $Date: 2003-11-20 11:55:37 +0100 (gio, 20 nov 2003) $ $Revision: 4572 $
*/
public class GuiEvent 
{
    //#APIDOC_EXCLUDE_BEGIN
    protected Object source;
    protected int type;
    //#APIDOC_EXCLUDE_END

  private List parameters;

    /**
       Create a GUI event.
       @param eventSource The logical source of this event.
       @param eventType An integer value, identifying the kind of this
       event.
    */ 
  public GuiEvent(Object eventSource, int eventType)
  {
    source = eventSource;
    type = eventType;	
    parameters = new ArrayList();
  }

    /**
       Retrieve the kind of this GUI event.
       @return The kind of this event.
    */
    public int getType()
    {
	return(type);
    }

    /**
       Retrieve the logical source of this GUI event.
       @return The event source, or <code>null</code> if no source was
       set.
    */
    public Object getSource()
    {
	return(source);
    }

  /** Add a new parameter to this event.
  * @param param is the parameter 
  **/
  public void addParameter(Object param) {
    parameters.add(param);
  }     

  /**
   * Get the parameter in the given position.
   * @return the Object with the parameter.
   **/
  public Object getParameter(int number) {
    return parameters.get(number);
  }

  /**
   * Get an Iterator over all the parameters.
   * @return An iterator, scanning the event parameter list.
   **/
  public Iterator getAllParameter() {
    return parameters.iterator();
  }
}

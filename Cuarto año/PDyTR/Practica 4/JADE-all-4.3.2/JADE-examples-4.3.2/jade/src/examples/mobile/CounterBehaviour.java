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

package examples.mobile;

import jade.core.*;
import jade.core.behaviours.*;

/**
The behaviour uses two resources, in particular the counter cnt
and the flag cntEnabled, of the agent object. 
It increments by one its value, displays it, blocks 
for two seconds, and repeats forever. 
@author Giovanni Caire - CSELT S.p.A
@version $Date: 2002-07-16 11:20:11 +0200 (mar, 16 lug 2002) $ $Revision: 3271 $
*/

class CounterBehaviour extends SimpleBehaviour
{
	CounterBehaviour(Agent a)
	{
		super(a);
	}

	public boolean done()
	{
		return false;
	}

	public void action()
	{
		// If counting is enabled, print current number and increment counter
		if ( ((MobileAgent) myAgent).cntEnabled )
		{
			((MobileAgent) myAgent).cnt++;
			((MobileAgent) myAgent).displayCounter();

		}
		
		// Block the behaviour for 2 seconds
		
		block(2000);

		return;
	}
}


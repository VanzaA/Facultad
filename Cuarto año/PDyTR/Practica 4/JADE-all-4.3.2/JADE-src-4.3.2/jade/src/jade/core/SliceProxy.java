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

package jade.core;

/**
 An implementation of the <code>Service.Slice</code> interface,
 supporting routed dispatching of horizontal commands.
*/
public class SliceProxy implements Service.Slice 
{

	public SliceProxy() 
	{
		this(null, null);
	}

	public SliceProxy(Service svc, Node n) 
	{
		myService = svc;
		myNode = n;
	}

	public Service getService() 
	{
		return myService;
	}

	public Node getNode() throws ServiceException 
	{
		return myNode;
	}

	public void setNode(Node n) 
	{
		myNode = n;
	}

	/**
	 Try to serve an incoming horizontal command, routing it to
	 a remote slice implementation.

	 @param cmd The command to serve, possibly through the network.
	 */
	public VerticalCommand serve(HorizontalCommand cmd) 
	{
		try 
		{
			cmd.setReturnValue(myNode.accept(cmd));
		}
		catch(IMTPException imtpe) 
		{
			cmd.setReturnValue(new ServiceException("An error occurred while routing the command to the remote implementation", imtpe));
		}
		// No local processing of this command is required
		return null;
	}

	void setLocalNodeDescriptor(NodeDescriptor dsc) {
		localNodeDescriptor = dsc;
	}
	
	private Node myNode;
	private transient NodeDescriptor localNodeDescriptor;
	private transient Service myService;

}

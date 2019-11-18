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


import jade.core.behaviours.Behaviour;
import jade.util.leap.Serializable;

/**

   The <code>Service</code> interface represents a centralized view of
   a JADE kernel-level service. Most JADE services are actually
   distributed, and each part of theirs, that is deployed at a given
   network node (container), is called <em>service slice</em>. The various slices
   of a service work together to carry out that service's task.

   @author Giovanni Rimassa - FRAMeTech s.r.l.

 */
public interface Service {
	/**
	   Service independent incoming vertical command issued on the Main 
	   container whenever a new node is added
	 */
	public static final String NEW_NODE = "New-Node";
	/**
	   Service independent incoming vertical command issued on the Main 
	   container whenever a node is removed
	 */
	public static final String DEAD_NODE = "Dead-Node";
	/**
	   Service independent incoming vertical command issued on the Main 
	   container whenever a node that was monitored by a Main container
	   replica is adopted (i.e. the local Main container starts monitoring it) 
	 */
	public static final String ADOPTED_NODE = "Adopted-Node";
	/**
	   Service independent incoming vertical command issued on the Main 
	   container whenever a new slice of a given service is added
	 */
	public static final String NEW_SLICE = "New-Slice";
	/**
	   Service independent incoming vertical command issued on the Main 
	   container whenever a slice of a given service is removed
	 */
	public static final String DEAD_SLICE = "Dead-Slice";
	/**
	   Service independent vertical command issued on the Main 
	   container whenever a new main replica is added
	 */
	public static final String NEW_REPLICA = "New-Replica";
	/**
	   Service independent incoming vertical command issued on the Main 
	   container whenever a main replica is removed
	 */
	public static final String DEAD_REPLICA = "Dead-Replica";
	/**
	   Service independent incoming vertical command issued on a peripheral 
	   container when a fault of the PlatformManager is detected 
	 */
	public static final String DEAD_PLATFORM_MANAGER = "Dead-Platform-Manager";
	/**
	   Service independent incoming vertical command issued on a peripheral 
	   container when it re-attaches to a recovered Main Container (see the FaultRecoveryService)
	 */
	public static final String REATTACHED = "Reattached";
	/**
	   Service independent incoming vertical command issued on a peripheral 
	   container when it reconnects to a new master Main Container (see the MainReplicationService)
	 */
	public static final String RECONNECTED = "Reconnected";

	/**
       The <code>Slice</code> nested interface represents that part of
       a service that is deployed at a given network node.
	 */
	public interface Slice extends Serializable {

		/**
	   Access the service object which this slice is a part of.

	   @return A <code>Service</code> object, that has
	   <code>this</code> as one of its slices.

	   @see jade.core.Service#getSlice(String name)
		 */
		Service getService();

		/**
	   Access the node where this slice resides.

	   @returns The node where this service slice is actually
	   running.
	   @throws ServiceException If some problems occur in
	   retrieving the local node.
		 */
		Node getNode() throws ServiceException;

		/**
	   Serves an incoming horizontal command, performing any
	   required immediate processing, before turning it into a
	   vertical command to be processed by the incoming filter
	   chain.
	   @param cmd The command that is to be served.

	   @return A vertical command, that will be processed by the
	   incoming filter chain of the receiving node. If
	   <code>null</code> is returned, no filter/sink processing
	   will happen. This feature can be used to decouple incoming
	   horizontal interaction patterns from vertical incoming
	   commands (e.g. no incoming vertical command is generated
	   until a required set of horizontal commands has been
	   received).
		 */
		VerticalCommand serve(HorizontalCommand cmd);




	}

	//#APIDOC_EXCLUDE_BEGIN
	//#DOTNET_EXCLUDE_BEGIN
	// 15/4/05 We had to deprecate this class because of the .NET integration
	/**
       An implementation of the <code>Slice</code> interface,
       supporting routed dispatching of horizontal commands.
			 @deprecated use the class jade.core.SliceProxy instead of this inner class
	 */
	public class SliceProxy extends jade.core.SliceProxy implements Slice {

		public SliceProxy() {
			super();
		}
		
		public SliceProxy(Service svc, Node n) {
			super(svc, n);
		}
		/*public SliceProxy() {
			this(null, null);
		}

		public SliceProxy(Service svc, Node n) {
			myService = svc;
			myNode = n;
		}

		public Service getService() {
			return myService;
		}

		public Node getNode() throws ServiceException {
			return myNode;
		}

		public void setNode(Node n) {
			myNode = n;
		}*/

		/**
	   Try to serve an incoming horizontal command, routing it to
	   a remote slice implementation.

	   @param cmd The command to serve, possibly through the network.
		 *
		public VerticalCommand serve(HorizontalCommand cmd) {
			try {
				cmd.setReturnValue(myNode.accept(cmd));
			}
			catch(IMTPException imtpe) {
				cmd.setReturnValue(new ServiceException("An error occurred while routing the command to the remote implementation", imtpe));
			}
			// No local processing of this command is required
			return null;
		}

		private Node myNode;
		private transient Service myService;
		*/
	}
	//#DOTNET_EXCLUDE_END
	//#APIDOC_EXCLUDE_END

	/**
       Retrieve the name of this service, that can be used to look up
       its slices in the Service Finder.

       @return The name of this service.
       @see jade.core.ServiceFinder
	 */
	String getName();

	/**
       Retrieve by name a slice of this service. For distributed
       services, the returned slice will generally be some kind of
       proxy object to the real, remote slice.
       The actual proxy management policy (caching, reconnection,
       etc.) is decided by concrete services.

       @param name A name for the requested slice. The name must be
       unique within this service.
       @return The <code>Slice<code> object that is a part of this
       service and is identified by the given name, or
       <code>null</code> if no such slice exists.
       @throws ServiceException If some underlying error (e.g. a
       network problem) occurs, that does not allow to decide whether
       the requested slice exists or not.
	 */
	Slice getSlice(String name) throws ServiceException;

	/**
       Retrieve the locally installed slice of this service. A service
       without horizontal interfaces can safely return
       <code>null</code> from this method.

       @return The slice of this service that resides on the local
       platform node, or <code>null</code> if no such slice exists.
	 */
	Slice getLocalSlice();

	/**
       Retrieve the whole array of slices that compose this service.

       @return An array of <code>Service.Slice</code> objects, whose
       elements are the slices of this service deployed at the
       different platform nodes.
       @throws ServiceException If some underlying error (e.g. a
       network problem) occurs, that does not allow to retrieve the
       full slice list.
	 */
	Slice[] getAllSlices() throws ServiceException;

	/**
       Retrieve the interface through which the different service
       slices will communicate, that is, the service <i>Horizontal
       Interface</i>.

       @return A <code>Class</code> object, representing the interface
       that is implemented by the slices of this service.  Let
       <code>s</code> be the <code>Class</code> object corresponding
       to the <code>Service.Slice</code> interface, and let
       <code>c</code> be the returned <code>Class</code> object. Then,
       the two following conditions must hold:
       <ol>
       <li><code>c.isInterface() == true</code></li>
       <li><code>s.isAssignableFrom(c) == true</code></li>
       </ol>
	 */
	Class getHorizontalInterface();

	/**
       Query by how many slices this service is composed at present.

       @return The number of slices belonging to this service. An
       active service must have at least one slice.
	 */
	int getNumberOfSlices();


	/**
       Access the command filter this service needs to perform its
       tasks. This filter will be installed within the local command
       processing engine. 
       Note that when called multiple times with the same value of the <code>direction</code> 
       parameter this method MUST always return the same object!

       @param direction One of the two constants
       <code>Filter.INCOMING</code> and <code>Filter.OUTGOING</code>,
       distinguishing between the two filter chains managed by the
       command processor.
       @return A <code>Filter</code> object, used by this service to
       intercept and process kernel-level commands. If the service
       does not wish to install a command filter for one or both
       directions, it can just return <code>null</code> when
       appropriate.
       @see jade.core.CommandProcessor
	 */
	Filter getCommandFilter(boolean direction);


	/**
       Access the command sink this service uses to handle its own
       vertical commands.
       @param side One of the two constants
       <code>Sink.COMMAND_SOURCE</code> or
       <code>Sink.COMMAND_TARGET</code>, to state whether this sink
       will handle locally issued commands or commands incoming from
       remote nodes.
       @return Concrete services must return their own implementation
       of the <code>Sink</code> interface, that will be invoked by the
       kernel in order to consume any incoming vertical command owned
       by this service. If the service does not wish to install a
       command sink, it can just return <code>null</code>.

       @see jade.core.Service#getOwnedCommands()
	 */
	Sink getCommandSink(boolean side);


	/**
       Access the names of the vertical commands this service wants to
       handle as their final destination. This set must not overlap
       with the owned commands set of any previously installed
       service, or an exception will be raised and service
       activation will fail.

       @return An array containing the names of all the vertical
       commands this service wants to own. If this service has no such
       commands (it acts purely as a command filter), it can return an
       empty array, or <code>null</code> as well.

       @see jade.core.Service#getCommandSink()
	 */
	String[] getOwnedCommands();

	/**
       Get the helper for accessing this service.

       @param a The agent which the helper is requested for.
       @return The ServiceHelper to be used by the agent.
       @see AgentToolkit#getHelper
       @see Agent#getHelper
	 */
	ServiceHelper getHelper(Agent a) throws ServiceException;

	/**
       Retrieve a behaviour that is associated with this service, and
       that will be deployed within the AMS. Typical uses for this
       behaviour will be to handle a service-specific ontology and
       actions.

       @return A <code>Behaviour</code> object associated with this
       service, or <code>null</code> if no such behaviour exists.
	 */
	Behaviour getAMSBehaviour();

	/**
       Performs the passive initialization step of the service. This
       method is called <b>before</b> activating the service. Its role
       should be simply the one of a constructor, setting up the
       internal data as needed.
       Service implementations should not use the Service Manager and
       Service Finder facilities from within this method. A
       distributed initialization protocol, if needed, should be
       exectuted within the <code>boot()</code> method.

       @param ac The agent container this service is activated on.
       @param p The configuration profile for this service.
       @throws ProfileException If the given profile is not valid.
	 */
	void init(AgentContainer ac, Profile p) throws ProfileException;

	/**
       Performs the active initialization step of a kernel-level
       service. When JADE kernel calls this method, the service has
       already been already associated with its container and
       registered with the Service Manager.

       @param p The configuration profile for this service.
       @throws ServiceException If a problem occurs during service
       initialization.
	 */
	void boot(Profile p) throws ServiceException;

	/**
       Performs the shutdown step of a kernel-level service.
       The JADE kernel calls this method just before uninstalling
       this service
	 */
	void shutdown();


	/**
       Allows submitting a vertical command for processing.
       The given vertical command must be owned by this service
       (i.e. its name must be one of the constants contained in the
       array returned by <code>getOwnedCommands()</code>, or an
       exception is thrown

       @param cmd The command to submit to the service.
       @return The result of the command, or <code>null</code> if this
       command produced no result. If an exception was produced, it
       will not be thrown, but will be returned as well.
       @throws ServiceException If the passed command does not belong
       to this service.
	 */
	Object submit(VerticalCommand cmd) throws ServiceException;

}

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


//#APIDOC_EXCLUDE_FILE

// We use Hashtables since we need synchronized access
import java.util.Hashtable; 

/**
   Processes JADE kernel-level commands, managing a filter/sink system
   to support dynamically configurable platform services.

   @author Giovanni Rimassa - FRAMeTech s.r.l.
   @author Giovanni Caire - TILAB
 */
class CommandProcessor {
	private Filter firstDownFilter;
	private Filter firstUpFilter;

	private final Hashtable downSinks;
	private final Hashtable upSinks;
	private SinksFilter lastDownFilter;
	private SinksFilter lastUpFilter;


	public CommandProcessor() {
		downSinks = new Hashtable(4);
		upSinks = new Hashtable(4);
		lastDownFilter = new SinksFilter(downSinks);
		lastUpFilter = new SinksFilter(upSinks);
		firstDownFilter = lastDownFilter;
		firstUpFilter = lastUpFilter;
	}


	/**
       Add a new filter to the filter chain.

       @param f The new filter to add.
       @param direction Whether to add this filter to the outgoing or
       incoming filter chain.
	 */
	public void addFilter(Filter f, boolean direction) {
		if(direction == Filter.INCOMING) {
			firstUpFilter = insertFilter(f, firstUpFilter);
		}
		else {
			firstDownFilter = insertFilter(f, firstDownFilter);
		}
	}

	/**
       Insert a Filter in the chain started by <code>first</code>.
       @return The Filter at the beginning of the chain (may have
       changed)
	 */
	private synchronized Filter insertFilter(Filter f, Filter first) {
		if (f != null) {
			if (f.getPreferredPosition() < first.getPreferredPosition()) {
				// Insert at the beginning of the filter chain
				f.setNext(first);
				return f;
			}
			else {
				Filter current = first;
				Filter next = current.getNext();
				while (true) {
					if (f.getPreferredPosition() < next.getPreferredPosition()) {
						// Insert between current and next
						f.setNext(next);
						current.setNext(f);
						break;
					}
					else {
						current = next;
						next = current.getNext();
					}
				}
			}
		}
		return first;
	}

	/**
       Remove a filter from the filter chain.

       @param f The filter to remove.
	 */
	public void removeFilter(Filter f, boolean direction) {
		if(direction == Filter.INCOMING) {
			firstUpFilter = removeFilter(f, firstUpFilter);
		}
		else {
			firstDownFilter = removeFilter(f, firstDownFilter);
		}
	}

	/**
       Remove a Filter from the chain started by <code>first</code>.
       @return The Filter at the beginning of the chain (may have
       changed)
	 */
	private synchronized Filter removeFilter(Filter f, Filter first) {
		if (first != null) {
			if (f != null) {
				if (f.equals(first)) {
					// Remove the first element of the filter chain
					return first.getNext();
				}
				else {
					Filter current = first;
					Filter next = current.getNext();
					while (true) {
						if (f.equals(next)) {
							// Remove next
							current.setNext(next.getNext());
							break;
						}
						else {
							current = next;
							next = current.getNext();
						}
					}
				}
			}
		}
		return first;
	}

	/**
       Register a command sink object to handle the given vertical commmand set.

       @param snk A service-specific implementation of the
       <code>Sink</code> interface, managing a set of vertical
       commands.
       @param side One of the two constants
       <code>Sink.COMMAND_SOURCE</code> or
       <code>Sink.COMMAND_TARGET</code>, to state whether this sink
       will handle locally issued commands or commands incoming from
       remote nodes.
       @param commandNames An array containing all the names of the
       vertical commands the new sink wants to handle.
       @throws ServiceException If some other sink is already
       registered for a member of the <code>commandNames</code> set.
	 */
	public synchronized void registerSink(Sink snk, boolean side, String serviceName) throws ServiceException {

		Hashtable sinks;
		if(side == Sink.COMMAND_SOURCE) {
			sinks = downSinks;
		}
		else {
			sinks = upSinks;
		}
		sinks.put(serviceName, snk);
	}

	/**
       Deregister a sink that is currently handling a given set of vertical commands.

       @param side One of the two constants
       <code>Sink.COMMAND_SOURCE</code> or
       <code>Sink.COMMAND_TARGET</code>, to state whether the sink to
       be removed is handling locally issued commands or commands
       incoming from remote nodes.
       @param commandNames An array containing all the names of the
       vertical commands currently handled by the sink to be removed.
       @throws ServiceException If a member of the
       <code>commandNames</code> set has no associated command sink.
	 */
	public synchronized void deregisterSink(boolean side, String serviceName) throws ServiceException {

		Hashtable sinks;
		if(side == Sink.COMMAND_SOURCE) {
			sinks = downSinks;
		}
		else {
			sinks = upSinks;
		}

		sinks.remove(serviceName);
	}

	/**
       Process an outgoing command object, carrying out the action it
       represents.  This method is not synchronized and it must be
       reentrant and as fast as possible, because it is going to be a
       bottleneck of the kernel call flow.

       @param cmd The <code>VerticalCommand</code> object to process.
	 */
	public Object processOutgoing(VerticalCommand cmd) {
		firstDownFilter.filter(cmd);
		return cmd.getReturnValue();
	}

	/**
       Process an incoming command object, carrying out the action it
       represents.  This method is not synchronized and it must be
       reentrant and as fast as possible, because it is going to be a
       bottleneck of the kernel call flow.

       @param cmd The <code>VerticalCommand</code> object to process.
	 */
	public Object processIncoming(VerticalCommand cmd) {
		firstUpFilter.filter(cmd);
		return cmd.getReturnValue();
	}

	/**
	  Inner class SinksFilter.
	  This class makes the set of sinks in a given direction
	  look like a single filter that always stands at the end of 
	  the filter chain.
	 */
	private class SinksFilter extends Filter {
		private Hashtable mySinks;

		private SinksFilter(Hashtable ht) {
			mySinks = ht;
			preferredPosition = LAST+1;
		}

		protected boolean accept(VerticalCommand cmd) {
			String service = cmd.getService();
			if (service != null) {
				Sink s = (Sink) mySinks.get(service);
				if (s != null) {
					//System.out.println("Sink "+s+" consuming command "+cmd.getName());
					s.consume(cmd);
				}
			}
			// Do not propagate since this is always the last filter in the chain
			return false;
		}
	} // END of inner class SinksFilter	
}

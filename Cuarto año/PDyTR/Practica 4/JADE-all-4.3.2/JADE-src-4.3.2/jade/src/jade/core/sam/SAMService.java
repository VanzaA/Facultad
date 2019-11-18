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

package jade.core.sam;

//#DOTNET_EXCLUDE_FILE

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import jade.core.Agent;
import jade.core.AgentContainer;
import jade.core.BaseService;
import jade.core.Filter;
import jade.core.HorizontalCommand;
import jade.core.IMTPException;
import jade.core.Node;
import jade.core.Profile;
import jade.core.ProfileException;
import jade.core.Service;
import jade.core.ServiceException;
import jade.core.ServiceHelper;
import jade.core.Specifier;
import jade.core.VerticalCommand;
import jade.core.management.AgentManagementSlice;
import jade.core.replication.MainReplicationSlice;
import jade.util.Logger;

/**
 * JADE Kernel service supporting System Activity Monitoring (SAM).
 */
public class SAMService extends BaseService {
	public static final String POLLING_PERIOD = "jade_core_sam_SAMService_pollingperiod";
	public static final int POLLING_PERIOD_DEFAULT = 1; // 15 minutes
	
	public static final String SAM_INFO_HANDLERS = "jade_core_sam_SAMService_handlers";
	public static final String SAM_INFO_HANDLERS_DEFAULT = "jade.core.sam.DefaultSAMInfoHandlerImpl";
	
	private List<EntityInfo> monitoredEntities = new ArrayList<EntityInfo>();
	private List<CounterInfo> monitoredCounters = new ArrayList<CounterInfo>();
	
	private Poller poller;

	private SAMHelper myHelper = new SAMHelperImpl();
	private ServiceComponent localSlice = new ServiceComponent();
	private Filter outgoingFilter = null;
	
	private Profile myProfile;
	
	
	public String getName() {
		return SAMHelper.SERVICE_NAME;
	}
	
	@Override
	public void init(AgentContainer ac, Profile p) throws ProfileException {
		super.init(ac, p);
		if (p.isMain()) {
			outgoingFilter = new Filter() {
				@Override
				public boolean accept(VerticalCommand cmd) {
					String name = cmd.getName();
					try {
						if (name.equals(AgentManagementSlice.SHUTDOWN_PLATFORM)) {
							// If the platform is shutting down stop polling: some 
							// peripheral container may be already down causing annoying exceptions
							if (poller != null) {
								poller.stopPolling();
							}
						}
						else if (name.equals(MainReplicationSlice.LEADERSHIP_ACQUIRED)) {
							// If this is a backup Main Container that has just taken the leadership
							// start polling again
							startPolling();
						}
					}
					catch (Exception e) {
						myLogger.log(Logger.WARNING, "Error processing command "+name+". ", e);
					}
					
					// Never veto a command
					return true;
				}
			};
		}
	}
	
	@Override
	public void boot(Profile p) throws ServiceException {
		super.boot(p);
		myProfile = p;
		if (myProfile.isMasterMain()) {
			startPolling();
		}
	}
	
	private void startPolling() throws ServiceException {
		int periodMinutes = POLLING_PERIOD_DEFAULT;
		try {
			periodMinutes = Integer.parseInt(myProfile.getParameter(POLLING_PERIOD, null));
		}
		catch (Exception e) {
			// Keep default;
		}
		myLogger.log(Logger.CONFIG, "Polling period = "+periodMinutes+" minutes");
		
		try {
			String hh = myProfile.getParameter(SAM_INFO_HANDLERS, SAM_INFO_HANDLERS_DEFAULT);
			Vector handlerClasses = new Vector();
			if (!hh.equalsIgnoreCase("none")) {
				handlerClasses = Specifier.parseList(hh, ';');
			}
			SAMInfoHandler[] handlers = new SAMInfoHandler[handlerClasses.size()];
			for (int i = 0; i < handlerClasses.size(); ++i) {
				String className = (String) handlerClasses.get(i);
				myLogger.log(Logger.CONFIG, "Loading SAMInfoHandler class = "+className+"...");
				handlers[i] = (SAMInfoHandler) Class.forName(className).newInstance();
				handlers[i].initialize(myProfile);
				myLogger.log(Logger.CONFIG, "SAMInfoHandler of class = "+className+" successfully initialized");
			}
			poller = new Poller(this, periodMinutes * 60000, handlers);
			poller.startPolling();
		}
		catch (Exception e) {
			throw new ServiceException("Error initializing SAMInfoHandler", e);
		}
	}
	
	@Override
	public void shutdown() {
		if (poller != null) {
			poller.stopPolling();
		}
		super.shutdown();
	}
	
	@Override
	public Filter getCommandFilter(boolean direction) {
		if (direction == Filter.OUTGOING) {
			return outgoingFilter;
		} else {
			return null;
		}
	}

	@Override
	public ServiceHelper getHelper(Agent a) {
		return myHelper;
	}

	@Override
	public Class getHorizontalInterface() {
		return SAMSlice.class;
	}
	
	@Override
	public Service.Slice getLocalSlice() {
		return localSlice;
	}
	
	private Map<String, AverageMeasure> getEntityMeasures() {
		// Mutual exclusion with modifications of entities/providers
		synchronized (myHelper) {
			Map<String, AverageMeasure> entityMeasures = new HashMap<String, AverageMeasure>();
			for (EntityInfo info : monitoredEntities) {
				entityMeasures.put(info.getName(), info.getMeasure());
			}
			return entityMeasures;
		}
	}
	
	private Map<String, Long> getCounterValues() {
		// Mutual exclusion with modifications of counters/providers
		synchronized (myHelper) {
			Map<String, Long> counterValues = new HashMap<String, Long>();
			for (CounterInfo info : monitoredCounters) {
				counterValues.put(info.getName(), info.getValue());
			}
			return counterValues;
		}
	}
	
	
	/**
	 * Inner class SAMHelperImpl
	 */
	private class SAMHelperImpl implements SAMHelper {

		public synchronized void addEntityMeasureProvider(String entityName, final MeasureProvider provider) {
			// Wrap the "one shot" MeasureProvider into an AverageMeasureProvider to treat all providers in a uniform way
			addEntityMeasureProvider(entityName, new AverageMeasureProvider() {
				public AverageMeasure getValue() {
					Number value = provider.getValue();
					if (value != null) {
						return new AverageMeasure(value.doubleValue(), 1);
					}
					else {
						return new AverageMeasure(0, 0);
					}
				}
			});
		}

		public synchronized void addEntityMeasureProvider(String entityName, AverageMeasureProvider provider) {
			EntityInfo info = getEntityInfo(entityName);
			info.addProvider(provider);
		}

		public synchronized void addCounterValueProvider(String counterName, CounterValueProvider provider) {
			CounterInfo info = getCounterInfo(counterName);
			info.addProvider(provider);
		}
		
		public void addHandler(SAMInfoHandler handler, boolean first) {
			if (poller != null) {
				poller.addHandler(handler, first);
			}
		}

		public void removeHandler(SAMInfoHandler handler) {
			if (poller != null) {
				poller.removeHandler(handler);
			}
		}
		
		public void init(Agent a) {
			// Nothing to do as there is a single helper for all agents
		}
	} // END of inner class SAMHelperImpl
	
	
	/**
	 * Inner class ServiceComponent
	 */
	private class ServiceComponent  implements Service.Slice {
		// Implementation of the Service.Slice interface
		public Service getService() {
			return SAMService.this;
		}
		
		public Node getNode() throws ServiceException {
			try {
				return SAMService.this.getLocalNode();
			}
			catch(IMTPException imtpe) {
				throw new ServiceException("Problem contacting the IMTP Manager", imtpe);
			}
		}
		
		public VerticalCommand serve(HorizontalCommand cmd) {
			try {
				String cmdName = cmd.getName();
				if(cmdName.equals(SAMSlice.H_GETSAMINFO)) {
					// Collect all SAM information from the local node
					SAMInfo info = new SAMInfo(getEntityMeasures(), getCounterValues());
					cmd.setReturnValue(info);
				}
			}
			catch(Throwable t) {
				cmd.setReturnValue(t);
			}
			// Do not issue any VerticalCommand
			return null;
		}
	}  // END of inner class ServiceComponent

	
	/**
	 * Inner class EntityInfo
	 */
	private class EntityInfo {
		private String name;
		private List<AverageMeasureProvider> providers = new ArrayList<AverageMeasureProvider>();
		
		EntityInfo(String name) {
			this.name = name;
		}
		
		String getName() {
			return name;
		}
		
		void addProvider(AverageMeasureProvider provider) {
			providers.add(provider);
		}
		
		AverageMeasure getMeasure() {
			AverageMeasure result = new AverageMeasure();
			for (AverageMeasureProvider p : providers) {
				AverageMeasure m = p.getValue();
				result.update(m);
			}
			return result;
		}
	} // END of inner class EntityInfo
	
	private EntityInfo getEntityInfo(String entityName) {
		for (EntityInfo info : monitoredEntities) {
			if (info.getName().equals(entityName)) {
				return info;
			}
		}
		// Entity not found --> create it
		EntityInfo info = new EntityInfo(entityName);
		monitoredEntities.add(info);
		return info;
	}

	
	/**
	 * Inner class CounterInfo
	 */
	private class CounterInfo {
		private String name;
		private List<CounterValueProvider> providers = new ArrayList<CounterValueProvider>();
		private List<Long> previousTotalValues = new ArrayList<Long>();
		
		CounterInfo(String name) {
			this.name = name;
		}
		
		String getName() {
			return name;
		}
		
		void addProvider(CounterValueProvider provider) {
			providers.add(provider);
			previousTotalValues.add((long) 0);
		}
		
		long getValue() {
			long result = 0;
			for (int i = 0; i < providers.size(); ++i) {
				CounterValueProvider p = providers.get(i);
				long v = p.getValue();
				if (p.isDifferential()) {
					// The provider returns a differential value. Add it directly
					result += v;
				}
				else {
					// The provider returns a total value. Add the differential value and update the previous total value 
					result += v - previousTotalValues.get(i);
					previousTotalValues.set(i, v);
				}
			}
			return result;
		}
	} // END of inner class CounterInfo

	private CounterInfo getCounterInfo(String counterName) {
		for (CounterInfo info : monitoredCounters) {
			if (info.getName().equals(counterName)) {
				return info;
			}
		}
		// Counter not found --> create it
		CounterInfo info = new CounterInfo(counterName);
		monitoredCounters.add(info);
		return info;
	}
}

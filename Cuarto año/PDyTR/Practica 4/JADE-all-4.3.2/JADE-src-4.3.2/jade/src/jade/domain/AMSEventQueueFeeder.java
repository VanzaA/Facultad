package jade.domain;

//#APIDOC_EXCLUDE_FILE
//#MIDP_EXCLUDE_FILE


import java.util.Date;

import jade.core.AID;
import jade.core.AgentManager;
import jade.core.Channel;
import jade.core.Location;
import jade.core.ContainerID;
import jade.core.event.MTPEvent;
import jade.core.event.PlatformEvent;
import jade.domain.FIPAAgentManagement.AMSAgentDescription;
import jade.domain.introspection.AddedContainer;
import jade.domain.introspection.AddedMTP;
import jade.domain.introspection.BornAgent;
import jade.domain.introspection.ChangedAgentOwnership;
import jade.domain.introspection.DeadAgent;
import jade.domain.introspection.EventRecord;
import jade.domain.introspection.FrozenAgent;
import jade.domain.introspection.MovedAgent;
import jade.domain.introspection.PlatformDescription;
import jade.domain.introspection.RemovedContainer;
import jade.domain.introspection.RemovedMTP;
import jade.domain.introspection.ResumedAgent;
import jade.domain.introspection.SuspendedAgent;
import jade.domain.introspection.ThawedAgent;
import jade.util.InputQueue;

public class AMSEventQueueFeeder implements AgentManager.Listener {
	private InputQueue eventQueue;
	private Location localContainer;
	private ams theAms;

	public AMSEventQueueFeeder(InputQueue eventQueue, Location localContainer) {
		this.eventQueue = eventQueue;
		this.localContainer = localContainer;
	}

	public InputQueue getQueue() {
		return eventQueue;
	}
	
	void setAms(ams ams) {
		theAms = ams;
		// Generate a PlatformDescription event in case some AddedMTP or RemovedMTP event happened when the AMS was not yet initialized
		PlatformDescription ap = new PlatformDescription();
		ap.setPlatform(theAms.getDescriptionAction(null));
		EventRecord er = new EventRecord(ap, localContainer);
		er.setWhen(new Date());
		eventQueue.put(er);
	}

	public void bornAgent(PlatformEvent ev) {
		ContainerID cid = ev.getContainer();
		AID agentID = ev.getAgent();
		String ownership = ev.getNewOwnership();

		BornAgent ba = new BornAgent();
		ba.setAgent(agentID);
		ba.setWhere(cid);
		ba.setState(AMSAgentDescription.ACTIVE);
		ba.setOwnership(ownership);
		ba.setClassName((String) agentID.getAllUserDefinedSlot().get(AID.AGENT_CLASSNAME));

		EventRecord er = new EventRecord(ba, localContainer);
		er.setWhen(ev.getTime());
		eventQueue.put(er);
	}

	public void deadAgent(PlatformEvent ev) {
		ContainerID cid = ev.getContainer();
		AID agentID = ev.getAgent();

		DeadAgent da = new DeadAgent();
		da.setAgent(agentID);
		da.setWhere(cid);
		if (ev.getContainerRemoved()) {
			da.setContainerRemoved(new Boolean(true));
		}

		EventRecord er = new EventRecord(da, localContainer);
		er.setWhen(ev.getTime());
		eventQueue.put(er);
	}

	public void suspendedAgent(PlatformEvent ev) {
		ContainerID cid = ev.getContainer();
		AID name = ev.getAgent();

		SuspendedAgent sa = new SuspendedAgent();
		sa.setAgent(name);
		sa.setWhere(cid);

		EventRecord er = new EventRecord(sa, localContainer);
		er.setWhen(ev.getTime());
		eventQueue.put(er);
	}

	public void resumedAgent(PlatformEvent ev) {
		ContainerID cid = ev.getContainer();
		AID name = ev.getAgent();

		ResumedAgent ra = new ResumedAgent();
		ra.setAgent(name);
		ra.setWhere(cid);

		EventRecord er = new EventRecord(ra, localContainer);
		er.setWhen(ev.getTime());
		eventQueue.put(er);
	}

	public void frozenAgent(PlatformEvent ev) {
		ContainerID cid = ev.getContainer();
		AID name = ev.getAgent();
		ContainerID bufferContainer = ev.getNewContainer();

		FrozenAgent fa = new FrozenAgent();
		fa.setAgent(name);
		fa.setWhere(cid);
		fa.setBufferContainer(bufferContainer);

		EventRecord er = new EventRecord(fa, localContainer);
		er.setWhen(ev.getTime());
		eventQueue.put(er);
	}

	public void thawedAgent(PlatformEvent ev) {
		ContainerID cid = ev.getContainer();
		AID name = ev.getAgent();
		ContainerID bufferContainer = ev.getNewContainer();

		ThawedAgent ta = new ThawedAgent();
		ta.setAgent(name);
		ta.setWhere(cid);
		ta.setBufferContainer(bufferContainer);

		EventRecord er = new EventRecord(ta, localContainer);
		er.setWhen(ev.getTime());
		eventQueue.put(er);
	}

	public void movedAgent(PlatformEvent ev) {
		ContainerID from = ev.getContainer();
		ContainerID to = ev.getNewContainer();
		AID agentID = ev.getAgent();

		MovedAgent ma = new MovedAgent();
		ma.setAgent(agentID);
		ma.setFrom(from);
		ma.setTo(to);

		EventRecord er = new EventRecord(ma, localContainer);
		er.setWhen(ev.getTime());
		eventQueue.put(er);
	}

	public void changedAgentPrincipal(PlatformEvent ev) {
		ContainerID cid = ev.getContainer();
		AID name = ev.getAgent();

		ChangedAgentOwnership cao = new ChangedAgentOwnership();
		cao.setAgent(name);
		cao.setWhere(cid);
		cao.setFrom(ev.getOldOwnership());
		cao.setTo(ev.getNewOwnership());

		EventRecord er = new EventRecord(cao, localContainer);
		er.setWhen(ev.getTime());
		eventQueue.put(er);
	}

	public void addedContainer(PlatformEvent ev) {
		ContainerID cid = ev.getContainer();
		String name = cid.getName();

		AddedContainer ac = new AddedContainer();
		ac.setContainer(cid);

		EventRecord er = new EventRecord(ac, localContainer);
		er.setWhen(ev.getTime());
		eventQueue.put(er);
	}

	public void removedContainer(PlatformEvent ev) {
		ContainerID cid = ev.getContainer();
		String name = cid.getName();

		RemovedContainer rc = new RemovedContainer();
		rc.setContainer(cid);

		EventRecord er = new EventRecord(rc, localContainer);
		er.setWhen(ev.getTime());
		eventQueue.put(er);
	}

	public synchronized void changedContainerPrincipal(PlatformEvent ev) {
		// FIXME: There is no element in the IntrospectionOntology
		// corresponding to this event
	}

	public synchronized void addedMTP(MTPEvent ev) {
		Channel ch = ev.getChannel();
		ContainerID cid = ev.getPlace();
		String proto = ch.getProtocol();
		String address = ch.getAddress();

		// Generate a suitable AMS event
		AddedMTP amtp = new AddedMTP();
		amtp.setAddress(address);
		amtp.setProto(proto);
		amtp.setWhere(cid);

		EventRecord er = new EventRecord(amtp, localContainer);
		er.setWhen(ev.getTime());
		eventQueue.put(er);

		if (theAms != null) {
			// The PlatformDescription has changed --> Generate a suitable event
			PlatformDescription ap = new PlatformDescription();
			ap.setPlatform(theAms.getDescriptionAction(null));
			er = new EventRecord(ap, localContainer);
			er.setWhen(ev.getTime());
			eventQueue.put(er);
		}
	}

	public synchronized void removedMTP(MTPEvent ev) {
		Channel ch = ev.getChannel();
		ContainerID cid = ev.getPlace();
		String proto = ch.getProtocol();
		String address = ch.getAddress();

		RemovedMTP rmtp = new RemovedMTP();
		rmtp.setAddress(address);
		rmtp.setProto(proto);
		rmtp.setWhere(cid);

		EventRecord er = new EventRecord(rmtp, localContainer);
		er.setWhen(ev.getTime());
		eventQueue.put(er);

		if (theAms != null) {
			// The PlatformDescription has changed --> Generate a suitable event
			PlatformDescription ap = new PlatformDescription();
			ap.setPlatform(theAms.getDescriptionAction(null));
			er = new EventRecord(ap, localContainer);
			er.setWhen(ev.getTime());
			eventQueue.put(er);
		}
	}

	public void messageIn(MTPEvent ev) {
		// No AMS event corresponds to this MTPEvent --> Just do nothing
	}

	public void messageOut(MTPEvent ev) {
		// No AMS event corresponds to this MTPEvent --> Just do nothing
	}
}

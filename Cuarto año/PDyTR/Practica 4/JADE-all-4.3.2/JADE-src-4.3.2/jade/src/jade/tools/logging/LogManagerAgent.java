package jade.tools.logging;

//#ANDROID_EXCLUDE_FILE

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Map;

import jade.core.*;
import jade.domain.FIPAAgentManagement.APDescription;
import jade.domain.introspection.*;
import jade.content.lang.sl.SLCodec;
import jade.domain.JADEAgentManagement.JADEManagementOntology;
import jade.tools.logging.ontology.LogManagementOntology;
import jade.tools.logging.gui.LogManagerGUI;

/**
 * This tool agent supports local and remote management of logs in JADE containers.
 * 
 * @author Giovanni Caire - TILAB
 * @author Rosalba Bochicchio - TILAB
 */
public class LogManagerAgent extends Agent {
	private LogManagerGUI myGui;
	private APDescription myPlatformProfile;
	
	private AMSSubscriber myAMSSubscriber;
	
	protected void setup() {
		getContentManager().registerLanguage(new SLCodec());
		getContentManager().registerOntology(JADEManagementOntology.getInstance());
		getContentManager().registerOntology(LogManagementOntology.getInstance());
		
		myAMSSubscriber = new AMSSubscriber() {
			protected void installHandlers(Map handlersTable) {
				handlersTable.put(IntrospectionVocabulary.META_RESETEVENTS, new EventHandler() {
					public void handle(Event ev) {
						myGui.resetTree();
					}
				});
				
				handlersTable.put(IntrospectionVocabulary.ADDEDCONTAINER, new EventHandler() {
					public void handle(Event ev) {
						AddedContainer ac = (AddedContainer) ev;
						ContainerID cid = ac.getContainer();
						String name = cid.getName();
						String address = cid.getAddress();
						try {
							InetAddress addr = InetAddress.getByName(address);
							myGui.addContainer(name, addr);
						} catch (UnknownHostException uhe) {
							myGui.addContainer(name, null);
						}
					}
				});
				
				handlersTable.put(IntrospectionVocabulary.REMOVEDCONTAINER, new EventHandler() {
					public void handle(Event ev) {
						RemovedContainer rc = (RemovedContainer) ev;
						ContainerID cid = rc.getContainer();
						String name = cid.getName();
						myGui.removeContainer(name);
					}
				});
				
				//handle the APDescription provided by the AMS
				handlersTable.put(IntrospectionVocabulary.PLATFORMDESCRIPTION, new EventHandler() {
					public void handle(Event ev) {
						PlatformDescription pd = (PlatformDescription) ev;
						APDescription APdesc = pd.getPlatform();
						myPlatformProfile = APdesc;
						myGui.refreshLocalPlatformName(myPlatformProfile.getName());
					}
				});
				
			}
		};
		
		addBehaviour(myAMSSubscriber);
		
		myGui = new LogManagerGUI(this);
		myGui.showCorrect();	
	}
	
	protected void takeDown() {
		myGui.dispose();
		send(myAMSSubscriber.getCancel());
	}
}

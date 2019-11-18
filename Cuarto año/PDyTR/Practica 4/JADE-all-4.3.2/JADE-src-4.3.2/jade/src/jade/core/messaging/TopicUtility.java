package jade.core.messaging;

import jade.core.AID;

/**
 * This class embeds topic related utility methods that must be available in MIDP
 */
public class TopicUtility {
	public static final AID createTopic(String topicName) {
		return new AID(topicName+'@'+TopicManagementHelper.TOPIC_SUFFIX, AID.ISGUID);
	}
	
	public static final boolean isTopic(AID id) {
		// NOTE that checking the endsWith() condition is much faster as it does not involve extracting
		// the HAP from the AID --> Whenever an AID is not a topic we immediately detect that
		if (id.getName().endsWith(TopicManagementHelper.TOPIC_SUFFIX)) {
			return TopicManagementHelper.TOPIC_SUFFIX.equals(id.getHap());
		}
		else {
			return false;
		}
	}
}

package jade.core.faultRecovery;

import jade.core.ServiceException;
import jade.core.ServiceHelper;

public interface FaultRecoveryHelper extends ServiceHelper {
	public static final String SERVICE_NAME = "jade.core.faultRecovery.FaultRecovery";
	
	void reattach() throws ServiceException;

}

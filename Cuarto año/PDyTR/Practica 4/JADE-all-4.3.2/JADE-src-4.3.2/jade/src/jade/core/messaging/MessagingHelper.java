package jade.core.messaging;

import jade.core.AID;
import jade.core.IMTPException;
import jade.core.ServiceException;
import jade.core.ServiceHelper;

/**
 * ServiceHelper provided by the MessagingService to make additional messaging features
 * available to agents 
 */
public interface MessagingHelper extends ServiceHelper {
	/**
	 * The interface to be implemented by classes that need to be notified about 
	 * aliases creation/deletion 
	 */
	public static interface AliasListener {
		void handleNewAlias(AID alias, AID agent);
		void handleDeadAlias(AID alias, AID agent);
	}

	/**
	 * Create a new alias of the agent associated to this helper
	 * @param alias The alias to be created
	 */
	void createAlias(String alias) throws IMTPException, ServiceException;
	/**
	 * Delete an alias of the agent associated to this helper
	 * @param alias The alias to be deleted
	 */
	void deleteAlias(String alias) throws IMTPException, ServiceException;
	/**
	 * Register a listener that will be notified about alias creation/deletion.
	 * This methods can only be invoked in a Main Container. Invoking that method 
	 * in a peripheral container results in a ServiceException 
	 * @param l The listener to be registered 
	 */
	void registerAliasListener(AliasListener l) throws ServiceException;
	/**
	 * De-register a listener previously registered to be notified about alias creation/deletion.
	 * This methods can only be invoked in a Main Container. Invoking that method 
	 * in a peripheral container results in a ServiceException 
	 * @param l The listener to be de-registered 
	 */
	void deregisterAliasListener(AliasListener l) throws ServiceException;
}

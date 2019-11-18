/*****************************************************************
 JADE - Java Agent DEvelopment Framework is a framework to develop 
 multi-agent systems in compliance with the FIPA specifications.
 Copyright (C) 2000 CSELT S.p.A. 
 
 The updating of this file to JADE 2.0 has been partially supported by the IST-1999-10211 LEAP Project
 
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

package jade.domain.KBManagement;

//#J2ME_EXCLUDE_FILE

import jade.core.AID;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.NotUnderstoodException;
import jade.proto.SubscriptionResponder;
import jade.util.Logger;
import jade.util.leap.List;
import jade.util.leap.ArrayList;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Enumeration;
import java.util.NoSuchElementException;

/**
 * This class provides an abstract implementation of the 
 * <code>KB</code> interface where information are stored in
 * a database.
 * 
 * @author Roland Mungenast - Profactor
 */
public abstract class DBKB extends KB {
	
	/**
	 * Used database driver
	 */
	protected String driver = "sun.jdbc.odbc.JdbcOdbcDriver";
	
	/**
	 * This ThreadLocal is used to hold connections and associated additional information
	 * (such as prepared statements) currently used by each Thread
	 */
	private ThreadLocal connections = new ThreadLocal();
	
	private String url, username, password;
	
	/**
	 * Specifies whether the KB should delete all existing tables for the DF at startup
	 */
	protected boolean cleanTables;
	
	protected class ConnectionWrapper {
		private Connection conn;
		private Object info;
		
		public ConnectionWrapper(Connection conn) {
			this.conn = conn;
		}
		public Connection getConnection() {
			return conn;
		}
		public void setConnection(Connection conn) {
			this.conn = conn;
		}
		public Object getInfo() {
			return info;
		}
		public void setInfo(Object info) {
			this.info = info;
		}
	}
	
	/**
	 * Constructs a new <code>DFKB</code> and establishes a connection to the database
	 * at the given URL using the <code>sun.jdbc.odbc.JdbcOdbcDriver</code> driver.
	 * 
	 * @param maxResultLimit internal limit for the number of maximum search results.
	 * @param url database URL
	 * @param maxResultLimit JADE internal limit for the maximum number of search results
	 * @param cleanTables specifies whether the KB should delete all existing tables for the DF at startup
	 * @throws SQLException an error occured while opening a connection to the database
	 */
	public DBKB(String url, int maxResultLimit, boolean cleanTables) throws SQLException {
		this(null, url, maxResultLimit, cleanTables);
	}
	
	/**
	 * Constructs a new <code>DFKB</code> and establishes a connection to the database.
	 * 
	 * @param maxResultLimit internal limit for the number of maximum search results.
	 * @param drv database driver
	 * @param url database URL
	 * @param user database user name
	 * @param passwd database password
	 * @param maxResultLimit JADE internal limit for the maximum number of search results
	 * @param cleanTables specifies whether the KB should delete all existing tables for the DF at startup
	 * @throws SQLException an error occured while opening a connection to the database
	 */
	public DBKB(String drv, String url, int maxResultLimit, boolean cleanTables) throws SQLException {
		this(drv, url, null, null, maxResultLimit, cleanTables);
	}
	
	/**
	 * Constructs a new <code>DFKB</code> and establishes a connection to the database.
	 * 
	 * @param maxResultLimit internal limit for the number of maximum search results.
	 * @param drv database driver
	 * @param url database URL
	 * @param user database user name
	 * @param passwd database password
	 * @param maxResultLimit JADE internal limit for the maximum number of search results
	 * @param cleanTables specifies whether the KB should delete all existing tables for the DF at startup
	 * @throws SQLException an error occured while opening a connection to the database
	 */
	public DBKB(String drv, String url, String username, String password, int maxResultLimit, boolean cleanTables) throws SQLException {
		super(maxResultLimit);
		this.cleanTables = cleanTables;
		loadDBDriver(drv);
		
		// Store these value for later connection recreation 
		this.url = url;
		this.username = username;
		this.password = password;
		
		// Activate cursors when using a SQL Server database
		Connection conn = getConnectionWrapper().getConnection();
		DatabaseMetaData md = conn.getMetaData();
		String dbName = md.getDatabaseProductName();	
		if (dbName.toLowerCase().indexOf("sql server") != -1) {  
			if (url.toLowerCase().indexOf("selectmethod") == -1) {
				if (!url.endsWith(";"))
					url = url + ";";
				url = url + "SelectMethod=cursor";
				this.url = url;
				invalidateConnectionWrapper();
			}
		}
	}
	
	/**
	 * This method is called by the KB Factory and is a placeholder for implementation specific KB initializations. 
	 */
	abstract public void setup() throws SQLException;
	
	/**
	 * Loads an JDBC driver
	 * @param drv driver name or <code>null</code> </ br>
	 * if the default JDBC-ODBC driver should be used
	 * @throws SQLException if the driver cannot be loaded
	 */
	private void loadDBDriver(String drv) throws SQLException {
		//  Load DB driver
		try {
			if(drv != null) {
				if(!drv.equals("null"))
					driver = drv;
			}
			Class.forName(driver).newInstance();
		}
		catch(Exception e){
			throw new SQLException("Error loading driver "+driver+". "+e);
		}
	}
	
	protected Connection createDBConnection(String url, String username, String password) throws SQLException {
		if (username != null) {
			return DriverManager.getConnection(url, username, password);
		}
		else {
			return DriverManager.getConnection(url);
		}
	}
		
	protected final ConnectionWrapper getConnectionWrapper() throws SQLException {
		ConnectionWrapper wrapper = (ConnectionWrapper) connections.get();
		if (wrapper == null) {
			Connection conn = createDBConnection(url, username, password);
			wrapper = new ConnectionWrapper(conn);
			initConnectionWrapper(wrapper);
			connections.set(wrapper);
		}
		return wrapper;
	}
	
	/**
	 * Subclasses can redefine this method to provide implementation specific ConnectionWrapper initializations
	 */  
	protected void initConnectionWrapper(ConnectionWrapper wrapper) throws SQLException {	
	}
	
	private void invalidateConnectionWrapper() throws SQLException {
		ConnectionWrapper wrapper = (ConnectionWrapper) connections.get();
		if (wrapper != null) {
			try {wrapper.getConnection().close();} catch (Exception e) {}
			// FIXME: For JDK1.4 compatibility we can't use remove(). For our purposes there is no difference  
			connections.set(null);
		}
	}
	
	
	protected Object insert(Object name, Object fact) {
		try {
			return insertSingle(name, fact);
		}
		catch (SQLException sqle) {
			try {
				// Refresh the connection and retry.
				logger.log(Logger.WARNING, "Invalidating DB connection...");
				invalidateConnectionWrapper();
				//logger.log(Logger.INFO, "DB connection correctly refreshed");
				return insertSingle(name, fact);
			}
			catch (Exception e) {
				// Log the original error
				logger.log(Logger.SEVERE,"DB error inserting DFD for agent "+((DFAgentDescription) fact).getName().getName(), sqle); 
				try {invalidateConnectionWrapper();} catch(Exception e1) {}
			}
		}
		return null;
	}
	
	protected abstract Object insertSingle(Object name, Object fact) throws SQLException;
	
	protected Object remove(Object name) {
		try {
			return removeSingle(name);
		}
		catch (SQLException sqle) {
			try {
				// Refresh the connection and retry.
				logger.log(Logger.WARNING, "Invalidating DB connection...");
				invalidateConnectionWrapper();
				//logger.log(Logger.INFO, "DB connection correctly refreshed");
				return removeSingle(name);
			}
			catch (Exception e) {
				// Log the original error
				logger.log(Logger.SEVERE,"DB error removing DFD for agent "+((AID) name).getName(), sqle); 
				try {invalidateConnectionWrapper();} catch(Exception e1) {}
			}
		}
		return null;
	}

	protected abstract Object removeSingle(Object name) throws SQLException;
	
	public List search(Object template, int maxResult) {
		try {
			return searchSingle(template, maxResult);
		}
		catch (SQLException sqle) {
			try {
				// Refresh the connection and retry.
				logger.log(Logger.WARNING, "Invalidating DB connection...");
				invalidateConnectionWrapper();
				//logger.log(Logger.INFO, "DB connection correctly refreshed");
				return searchSingle(template, maxResult);
			}
			catch (Exception e) {
				// Log the original error
				logger.log(Logger.SEVERE,"DB error during search operation.", sqle); 
				try {invalidateConnectionWrapper();} catch(Exception e1) {}
			}
		}
		return new ArrayList();
	}

	protected abstract List searchSingle(Object template, int maxResult) throws SQLException;
	
	public KBIterator iterator(Object template) {
		try {
			return iteratorSingle(template);
		}
		catch (SQLException sqle) {
			try {
				// Refresh the connection and retry.
				logger.log(Logger.WARNING, "Invalidating DB connection...");
				invalidateConnectionWrapper();
				//logger.log(Logger.INFO, "DB connection correctly refreshed");
				return iteratorSingle(template);
			}
			catch (Exception e) {
				// Log the original error
				logger.log(Logger.SEVERE,"DB error during iterated search operation.", sqle); 
				try {invalidateConnectionWrapper();} catch(Exception e1) {}
			}
		}
		return new EmptyKBIterator();
	}

	protected abstract KBIterator iteratorSingle(Object template) throws SQLException;
	
	public void subscribe(Object template, SubscriptionResponder.Subscription s) throws NotUnderstoodException{
		try {
			subscribeSingle(template, s);
		}
		catch (SQLException sqle) {
			try {
				// Refresh the connection and retry.
				logger.log(Logger.WARNING, "Invalidating DB connection...");
				invalidateConnectionWrapper();
				//logger.log(Logger.INFO, "DB connection correctly refreshed");
				subscribeSingle(template, s);
			}
			catch (Exception e) {
				// Log the original error
				logger.log(Logger.SEVERE,"DB error during iterated search operation.", sqle); 
				try {invalidateConnectionWrapper();} catch(Exception e1) {}
			}
		}
	}

	protected abstract void subscribeSingle(Object template, SubscriptionResponder.Subscription s) throws SQLException, NotUnderstoodException;
	
	// Note that getSubscriptions() is only called just after a registration/deregistration/modification -->
	// The connection refresh process is useless in this case.
	public abstract Enumeration getSubscriptions();
	
	public void unsubscribe(SubscriptionResponder.Subscription s) {
		try {
			unsubscribeSingle(s);
		}
		catch (SQLException sqle) {
			try {
				// Refresh the connection and retry.
				logger.log(Logger.WARNING, "Invalidating DB connection...");
				invalidateConnectionWrapper();
				//logger.log(Logger.INFO, "DB connection correctly refreshed");
				unsubscribeSingle(s);
			}
			catch (Exception e) {
				// Log the original error
				logger.log(Logger.SEVERE,"DB error during iterated search operation.", sqle); 
				try {invalidateConnectionWrapper();} catch(Exception e1) {}
			}
		}
	}

	protected abstract void unsubscribeSingle(SubscriptionResponder.Subscription s) throws SQLException;
	
	
	/**
	 * Inner class EmptyKBIterator
	 */
	protected class EmptyKBIterator implements KBIterator {
		public boolean hasNext() {
			return false;
		}
		
		public Object next() {
			throw new NoSuchElementException("");
		}
		
		public void remove() {
		}
		
		public void close() {
		}		
	} // END of inner class EmptyKBIterator
}

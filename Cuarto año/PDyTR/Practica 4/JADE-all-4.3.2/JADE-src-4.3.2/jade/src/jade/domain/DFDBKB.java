/**
 * JADE - Java Agent DEvelopment Framework is a framework to develop
 * multi-agent systems in compliance with the FIPA specifications.
 * Copyright (C) 2000 CSELT S.p.A.
 *
 * GNU Lesser General Public License
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation,
 * version 2.1 of the License.
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the
 * Free Software Foundation, Inc., 59 Temple Place - Suite 330,
 * Boston, MA  02111-1307, USA.
 */
package jade.domain;

//#J2ME_EXCLUDE_FILE
//#APIDOC_EXCLUDE_FILE

import jade.core.AID;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.NotUnderstoodException;
import jade.domain.FIPAAgentManagement.Property;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.KBManagement.*;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.ACLCodec;
import jade.lang.acl.StringACLCodec;
import jade.proto.SubscriptionResponder;

import jade.util.leap.Collection;
import jade.util.leap.Iterator;
import jade.util.leap.ArrayList;
import jade.util.leap.List;
import jade.util.leap.Properties;
import jade.util.Logger;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.rmi.server.UID;
import java.security.MessageDigest;
import java.sql.*;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.Date;
import java.util.Vector;
import java.util.NoSuchElementException;

import org.apache.commons.codec.binary.Base64;


/**
 * This class implements a knowledge base used by the DF which stores its content
 * in an external database. 
 * 
 * @author Elisabetta Cortese - TILab
 * @author Roland Mungenast - Profactor
 */
public class DFDBKB extends DBKB {
	private static final int MAX_PRELOAD_CNT = 1000;
	private static final int MAX_REGISTER_WITHOUT_CLEAN = 100;
	private static final int MAX_PROP_LENGTH = 255;
	
	// Table names
	private static final String SUBSCRIPTION = "subscription";
	private static final String SERVICEPROTOCOL = "serviceprotocol";
	private static final String SERVICEONTOLOGY = "serviceontology";
	private static final String SERVICELANGUAGE = "servicelanguage";
	private static final String SERVICEPROPERTY = "serviceproperty";
	private static final String SERVICE = "service";
	private static final String LANGUAGE = "language";
	private static final String ONTOLOGY = "ontology";
	private static final String PROTOCOL = "protocol";
	private static final String AGENTUSERDEFSLOT = "agentuserdefslot";
	private static final String AGENTRESOLVER = "agentresolver";
	private static final String AGENTADDRESS = "agentaddress";
	private static final String DFAGENTDESCR = "dfagentdescr";
	
	// Number of registrations after the last lease-time-cleanup
	private int regsCnt = 0;
	
	private boolean tablesReady = false;
	
	/**
	 * Default data type for very long strings
	 */
	protected String DEFAULT_LONGVARCHAR_TYPE = "LONGVARCHAR";
	
	// This is used to generate unique IDs
	private String localIPAddress;
	
	private class PreparedStatements {
		// prepared SQL statements
		private PreparedStatement stm_selNrOfDescrForAID;
		private PreparedStatement stm_selAgentAddresses;
		private PreparedStatement stm_selAgentResolverAIDs;
		private PreparedStatement stm_selAgentUserDefSlot;
		private PreparedStatement stm_selLease;
		private PreparedStatement stm_selProtocols;
		private PreparedStatement stm_selLanguages;
		private PreparedStatement stm_selOntologies;
		private PreparedStatement stm_selServices;
		private PreparedStatement stm_selServiceProtocols;
		private PreparedStatement stm_selServiceLanguages;
		private PreparedStatement stm_selServiceOntologies;
		private PreparedStatement stm_selServiceProperties;
		private PreparedStatement stm_selExpiredDescr;
		private PreparedStatement stm_selSubscriptions;
		
		private PreparedStatement stm_selAllProtocols;
		private PreparedStatement stm_selCountAllProtocols;
		private PreparedStatement stm_selAllLanguages;
		private PreparedStatement stm_selCountAllLanguages;
		private PreparedStatement stm_selAllOntologies;
		private PreparedStatement stm_selCountAllOntologies;
		
		private PreparedStatement stm_insAgentDescr;
		private PreparedStatement stm_insAgentAddress;
		private PreparedStatement stm_insAgentUserDefSlot;
		private PreparedStatement stm_insAgentResolverAID;
		private PreparedStatement stm_insLanguage;
		private PreparedStatement stm_insOntology;
		private PreparedStatement stm_insProtocol;
		private PreparedStatement stm_insService;
		private PreparedStatement stm_insServiceProtocol;
		private PreparedStatement stm_insServiceOntology;
		private PreparedStatement stm_insServiceLanguage;
		private PreparedStatement stm_insServiceProperty;
		private PreparedStatement stm_insSubscription;
		private PreparedStatement stm_delAgentDescr;
		private PreparedStatement stm_delAgentUserDefSlot;
		private PreparedStatement stm_delAgentResolver;
		private PreparedStatement stm_delAgentAddress;
		
		private PreparedStatement stm_selDescrId; 
		private PreparedStatement stm_selServiceId;
		private PreparedStatement stm_delService;
		private PreparedStatement stm_delLanguage;
		private PreparedStatement stm_delProtocol;
		private PreparedStatement stm_delOntology;
		
		private PreparedStatement stm_delServiceLanguage;
		private PreparedStatement stm_delServiceOntology;
		private PreparedStatement stm_delServiceProtocol;
		private PreparedStatement stm_delServiceProperty;
		private PreparedStatement stm_delSubscription;

		private PreparedStatements(Connection conn) throws SQLException {
			// select statements
			stm_selNrOfDescrForAID = conn.prepareStatement("SELECT COUNT(*) FROM dfagentdescr WHERE aid = ?");
			stm_selAgentAddresses = conn.prepareStatement("SELECT address FROM agentaddress WHERE aid = ?");
			stm_selAgentResolverAIDs = conn.prepareStatement("SELECT resolveraid FROM agentresolver WHERE aid = ?");
			stm_selAgentUserDefSlot = conn.prepareStatement("SELECT slotkey, slotval FROM agentuserdefslot WHERE aid = ?");
			stm_selLease = conn.prepareStatement("SELECT id, lease FROM dfagentdescr WHERE aid = ?");
			stm_selProtocols = conn.prepareStatement("SELECT protocol FROM protocol WHERE descrid = ?");
			stm_selLanguages = conn.prepareStatement("SELECT language FROM language WHERE descrid = ?");
			stm_selOntologies = conn.prepareStatement("SELECT ontology FROM ontology WHERE descrid = ?");
			stm_selServices = conn.prepareStatement("SELECT id, sname, stype, sownership FROM service WHERE descrid = ?");
			stm_selServiceProtocols = conn.prepareStatement("SELECT protocol FROM serviceprotocol WHERE serviceid = ?");
			stm_selServiceLanguages = conn.prepareStatement("SELECT ontology FROM serviceontology WHERE serviceid = ?");
			stm_selServiceOntologies = conn.prepareStatement("SELECT language FROM servicelanguage WHERE serviceid = ?");
			stm_selServiceProperties = conn.prepareStatement("SELECT propkey, propval_str, propval_obj FROM serviceproperty WHERE serviceid = ?");
			stm_selDescrId = conn.prepareStatement("SELECT id FROM dfagentdescr WHERE aid = ?"); 
			stm_selServiceId = conn.prepareStatement("SELECT id FROM service WHERE descrid = ?");
			stm_selExpiredDescr = conn.prepareStatement("SELECT aid FROM dfagentdescr WHERE lease < ? AND lease <> '-1'");
			stm_selSubscriptions = conn.prepareStatement("SELECT * FROM subscription");

			stm_selAllProtocols = conn.prepareStatement("SELECT descrid, protocol FROM protocol ORDER BY descrid");
			stm_selCountAllProtocols = conn.prepareStatement("SELECT COUNT(*) FROM protocol");
			stm_selAllLanguages = conn.prepareStatement("SELECT descrid, language FROM language ORDER BY descrid");
			stm_selCountAllLanguages = conn.prepareStatement("SELECT COUNT(*) FROM language");
			stm_selAllOntologies = conn.prepareStatement("SELECT descrid, ontology FROM ontology ORDER BY descrid");
			stm_selCountAllOntologies = conn.prepareStatement("SELECT COUNT(*) FROM ontology");
			
			stm_insAgentDescr = conn.prepareStatement("INSERT INTO dfagentdescr VALUES (?, ?, ?)");
			stm_insAgentAddress = conn.prepareStatement("INSERT INTO agentaddress VALUES (?, ?, ?)");
			stm_insAgentUserDefSlot = conn.prepareStatement("INSERT INTO agentuserdefslot VALUES (?, ?, ?, ?)");
			stm_insAgentResolverAID = conn.prepareStatement("INSERT INTO agentresolver VALUES (?, ?, ?)");
			stm_insLanguage = conn.prepareStatement("INSERT INTO language VALUES (?, ?)");
			stm_insOntology = conn.prepareStatement("INSERT INTO ontology VALUES (?, ?)");
			stm_insProtocol = conn.prepareStatement("INSERT INTO protocol VALUES (?, ?)");
			stm_insService = conn.prepareStatement("INSERT INTO service VALUES (?, ?, ?, ?, ?)");
			stm_insServiceProtocol = conn.prepareStatement("INSERT INTO serviceprotocol VALUES (?, ?)");
			stm_insServiceOntology = conn.prepareStatement("INSERT INTO serviceontology VALUES (?, ?)");
			stm_insServiceLanguage = conn.prepareStatement("INSERT INTO servicelanguage VALUES (?, ?)");
			stm_insServiceProperty = conn.prepareStatement("INSERT INTO serviceproperty VALUES (?, ?, ?, ?, ?)");
			stm_insSubscription = conn.prepareStatement("INSERT INTO subscription VALUES (?, ?)");
			
			
			// delete statements
			stm_delAgentDescr = conn.prepareStatement("DELETE FROM dfagentdescr WHERE id = ?");
			stm_delAgentUserDefSlot = conn.prepareStatement("DELETE FROM agentuserdefslot WHERE aid = ?");
			stm_delAgentResolver = conn.prepareStatement("DELETE FROM agentresolver WHERE aid = ?");
			stm_delAgentAddress = conn.prepareStatement("DELETE FROM agentaddress WHERE aid = ?");
			stm_delLanguage = conn.prepareStatement("DELETE FROM language WHERE descrid = ?");
			stm_delProtocol = conn.prepareStatement("DELETE FROM protocol WHERE descrid = ?");
			stm_delOntology = conn.prepareStatement("DELETE FROM ontology WHERE descrid = ?");
			stm_delService = conn.prepareStatement("DELETE FROM service WHERE descrid = ?"); 
			stm_delServiceLanguage = conn.prepareStatement("DELETE FROM servicelanguage WHERE serviceid = ?");
			stm_delServiceOntology = conn.prepareStatement("DELETE FROM serviceontology WHERE serviceid = ?");
			stm_delServiceProtocol = conn.prepareStatement("DELETE FROM serviceprotocol WHERE serviceid = ?");
			stm_delServiceProperty = conn.prepareStatement("DELETE FROM serviceproperty WHERE serviceid = ?");
			stm_delSubscription = conn.prepareStatement("DELETE FROM subscription WHERE id = ?");
		}
	}
	
	/**
	 * Constructor
	 * @param maxResultLimit internal limit for the number of maximum search results.
	 * @param drv database driver
	 * @param url database URL
	 * @param user database user name
	 * @param passwd database password
	 * @param cleanTables specifies whether the KB should delete all existing tables for the DF at startup
	 * @throws SQLException an error occured while opening a connection to the database
	 */
	public DFDBKB(int maxResultLimit, String drv, String url, String user, String passwd, boolean cleanTables) throws SQLException {
		super(drv, url, user, passwd, maxResultLimit, cleanTables);
	}
	
	/**
	 * Initializes all used SQL statements, the DB tables and the logging
	 */
	public void setup() throws SQLException {
		logger = Logger.getMyLogger(this.getClass().getName());
		
		try {
			localIPAddress = InetAddress.getLocalHost().getHostAddress();
		} catch (Exception e) {
			localIPAddress = "localhost";
		} 
		
		ConnectionWrapper wrapper = getConnectionWrapper();
		Connection conn = wrapper.getConnection();
		try {
			conn.setAutoCommit(false); // deactivate auto commit for better performance
		} catch (Exception e) {
			if(logger.isLoggable(Logger.WARNING)) {
				logger.log(Logger.WARNING, "Disabling auto-commit failed.");
			}
		}
		
		if (cleanTables) {
			// Drop all existing tables for the DF if required
			dropDFTables();
		}
		createDFTables();
		tablesReady = true;
		PreparedStatements ps = new PreparedStatements(conn);
		wrapper.setInfo(ps);
		clean();
	}
	
	protected void initConnectionWrapper(ConnectionWrapper wrapper) throws SQLException {
		Connection conn = wrapper.getConnection();
		try {
			conn.setAutoCommit(false); // deactivate auto commit for better performance
		} catch (Exception e) {
			if(logger.isLoggable(Logger.WARNING)) {
				logger.log(Logger.WARNING, "Disabling auto-commit failed.");
			}
		}

		// We cannot initialize the prepared-statements if the tables are not there yet
		if (tablesReady) {
			PreparedStatements ps = new PreparedStatements(conn);
			wrapper.setInfo(ps);
		}
	}

	private PreparedStatements getPreparedStatements() throws SQLException {
		ConnectionWrapper wrapper = getConnectionWrapper();
		return (PreparedStatements)wrapper.getInfo();
	}

	/**
	 * Returns the name of the SQL type used in the
	 * database to represent very long strings.
	 */
	protected String getLongVarCharType() {
		String bestMatch = DEFAULT_LONGVARCHAR_TYPE;
		try {
			// get the datatype with the highest precision from the meta data information
			DatabaseMetaData md = getConnectionWrapper().getConnection().getMetaData();
			ResultSet typeInfo = md.getTypeInfo();
			long maxPrecision = -1;
			while (typeInfo.next()) {
				long jdbcType = Long.parseLong(typeInfo.getString("DATA_TYPE"));
				long precision = Long.parseLong(typeInfo.getString("PRECISION"));
				
				if (jdbcType == Types.LONGVARCHAR && precision > maxPrecision) {
					maxPrecision = precision;
					bestMatch = typeInfo.getString("TYPE_NAME");
				}
			}
		} catch (SQLException e) {
			// ignored --> default value will be returned
		}
		return bestMatch;
	}
		
	/**
	 * Returns a global unique identifier
	 */
	protected String getGUID() {
		UID uid = new UID();
		return localIPAddress + ":" + uid;
	}
	
	/**
	 * Drops a DB table or does nothing if the table doesn't exist.
	 */                
	protected void dropTable(Statement stmt, String tableName) {
		try {
			stmt.execute("DROP TABLE " + tableName + " CASCADE CONSTRAINTS"); 
			getConnectionWrapper().getConnection().commit();
		} catch (SQLException e) {
			// Check if the exception is because the table does not exist
			if (tableExists(tableName)) {
				logger.log(Logger.WARNING, "Cannot clean table "+tableName, e);
			}
		}
	}
	
	/**
	 * Drops all existing DB tables used by the DF.
	 */
	protected void dropDFTables() throws SQLException {    
		
		logger.log(Logger.INFO, "Cleaning DF tables...");
		
		Statement stmt = getConnectionWrapper().getConnection().createStatement();
		
		dropTable(stmt, SUBSCRIPTION); 
		dropTable(stmt, SERVICEPROTOCOL);
		dropTable(stmt, SERVICEONTOLOGY);
		dropTable(stmt, SERVICELANGUAGE);
		dropTable(stmt, SERVICEPROPERTY);
		dropTable(stmt, SERVICE);
		dropTable(stmt, LANGUAGE);
		dropTable(stmt, ONTOLOGY);
		dropTable(stmt, PROTOCOL);
		dropTable(stmt, AGENTUSERDEFSLOT);
		dropTable(stmt, AGENTRESOLVER);   
		dropTable(stmt, AGENTADDRESS);
		dropTable(stmt, DFAGENTDESCR); 
		
		stmt.close();
	}
	
	/**
	 * Check whether a database table already exists
	 * 
	 * @param name name of the table
	 */
	protected boolean tableExists(String name) {
		Statement stmt = null;
		
		try {
			stmt = getConnectionWrapper().getConnection().createStatement();
			stmt.execute("SELECT COUNT(*) FROM "+name);
			return true;
			
		} catch (SQLException e) {
			// table doesn't exist
			return false;
		} finally {
			if (stmt != null) {
				try{
					stmt.close();
				}
				catch(SQLException see) {
					see.printStackTrace();
				}
			}  
		}
	}
	
	/**
	 * Creates a new DB table
	 * 
	 * @param name name of the table
	 * @param entries array of column and constraint specifications
	 * @throws SQLException If the table cannot be created
	 */
	protected void createTable(String name, String[] entries) {
		if (!tableExists(name)) {
			Statement stmt = null;

			try {
				Connection conn = getConnectionWrapper().getConnection();
				stmt = conn.createStatement();
				
				String sql = "CREATE TABLE " + name + " (";
				for (int i = 0; i < entries.length; i++) {
					sql += entries[i];
					if (i < entries.length - 1)
						sql += ", ";
					else
						sql += ")";
				}
				stmt.executeUpdate(sql);
				conn.commit();
				
			} catch (SQLException e) {
				if(logger.isLoggable(Logger.SEVERE)) 
					logger.log(Logger.SEVERE, "Error creating table '"+name+"'", e);
				
			} finally {
				if (stmt != null) {
					try{
						stmt.close();
					}
					catch(SQLException see) {
						see.printStackTrace();
					}
				}
			}
		}
	}
	
	/**
	 * Adds explicit indices to the database to speed up queries
	 */
	protected void createIndices() {
		Statement stmt = null;
		
		try {
			Connection conn = getConnectionWrapper().getConnection();
			stmt = conn.createStatement();
			stmt.execute("CREATE INDEX dfagentDescrIdx ON dfagentdescr( aid )");
			stmt.execute("CREATE INDEX leaseIdx ON dfagentdescr( lease )");
			stmt.execute("CREATE INDEX agentAddressIdx ON agentaddress( aid )");
			stmt.execute("CREATE INDEX agentResolverIdx ON agentresolver( aid )");
			stmt.execute("CREATE INDEX agentUserdefslotIdx ON agentuserdefslot( aid )");
			stmt.execute("CREATE INDEX serviceLanguageIdx ON servicelanguage( serviceid )"); 
			stmt.execute("CREATE INDEX serviceProtocolIdx ON serviceprotocol( serviceid )"); 
			stmt.execute("CREATE INDEX serviceOntologyIdx ON serviceontology( serviceid )"); 
			stmt.execute("CREATE INDEX servicePropertyIdx ON serviceproperty( serviceid )");
			stmt.execute("CREATE INDEX ontologyIdx ON ontology( descrid )");
			stmt.execute("CREATE INDEX protocolIdx ON ontology( descrid )");
			stmt.execute("CREATE INDEX languageIdx ON ontology( descrid )");
			conn.commit();
			
		} catch (SQLException e) {
			if(logger.isLoggable(Logger.FINE)) 
				logger.log(Logger.FINE, "Indices for DF tables couldn't be created", e);
			
		} finally {
			if (stmt != null) {
				try{
					stmt.close();
				}
				catch(SQLException see) {
					see.printStackTrace();
				}
			}
		}
	}
	
	
	/**
	 * Creates the proper DB tables.
	 */
	protected void createDFTables() {
		String LONGVARCHAR_TYPE = getLongVarCharType();
		
		// Tables for DF registrations      
		createTable(DFAGENTDESCR, new String[] {
				"id VARCHAR(" + MAX_PROP_LENGTH + ")",  
				"aid VARCHAR(" + MAX_PROP_LENGTH + ")",  
				"lease VARCHAR(20)",   
				"PRIMARY KEY( id )"}); 
		
		createTable(AGENTADDRESS, new String[] {
				"id VARCHAR(" + MAX_PROP_LENGTH + ")", 
				"aid VARCHAR(" + MAX_PROP_LENGTH + ")",   
				"address VARCHAR(" + MAX_PROP_LENGTH + ")", 
				"PRIMARY KEY( id )"});
		
		createTable(AGENTRESOLVER, new String[] {
				"id VARCHAR(" + MAX_PROP_LENGTH + ")",
				"aid VARCHAR(" + MAX_PROP_LENGTH + ")",
				"resolveraid VARCHAR(" + MAX_PROP_LENGTH + ")",
				"PRIMARY KEY( id )"});	
		
		createTable(AGENTUSERDEFSLOT, new String[] {
				"id VARCHAR(" + MAX_PROP_LENGTH + ")",
				"aid	VARCHAR(" + MAX_PROP_LENGTH + ")",
				"slotkey	VARCHAR(" + MAX_PROP_LENGTH + ")",
				"slotval	" + LONGVARCHAR_TYPE,
				"PRIMARY KEY( id )"});	 		 	
		
		createTable(ONTOLOGY, new String[] {
				"descrid VARCHAR(" + MAX_PROP_LENGTH + ")",
				"ontology VARCHAR(64)",
				"PRIMARY KEY( descrid, ontology )",
				"FOREIGN KEY( descrid ) REFERENCES dfagentdescr( id )"
		});
		
		createTable(PROTOCOL, new String[] {
				"descrid VARCHAR(" + MAX_PROP_LENGTH + ")",
				"protocol VARCHAR(64)", 
				"PRIMARY KEY( descrid, protocol )", 
				"FOREIGN KEY( descrid ) REFERENCES dfagentdescr( id )"
		});
		
		createTable(LANGUAGE, new String[] {
				"descrid VARCHAR(" + MAX_PROP_LENGTH + ")",
				"language VARCHAR(32)",
				"PRIMARY KEY( descrid, language )",
				"FOREIGN KEY( descrid ) REFERENCES dfagentdescr( id )"
		});
		
		createTable(SERVICE, new String[] {
				"id VARCHAR(" + MAX_PROP_LENGTH + ")",
				"descrid VARCHAR(" + MAX_PROP_LENGTH + ")",
				"sname VARCHAR(" + MAX_PROP_LENGTH + ")",
				"stype VARCHAR(64)",
				"sownership VARCHAR(64)",
				"PRIMARY KEY( id )",
				"FOREIGN KEY( descrid ) REFERENCES dfagentdescr( id )"
		});
		
		createTable(SERVICEPROTOCOL, new String[] {
				"serviceid VARCHAR(" + MAX_PROP_LENGTH + ")",
				"protocol VARCHAR(64)",
				"PRIMARY KEY( serviceid, protocol )",
				"FOREIGN KEY( serviceid ) REFERENCES service( id )"});
		
		createTable(SERVICEONTOLOGY, new String[] {
				"serviceid VARCHAR(" + MAX_PROP_LENGTH + ")",
				"ontology VARCHAR(64)",
				"PRIMARY KEY( serviceid, ontology )",
				"FOREIGN KEY( serviceid ) REFERENCES service( id )"});
		
		createTable(SERVICELANGUAGE, new String[] {
				"serviceid VARCHAR(" + MAX_PROP_LENGTH + ")",
				"language VARCHAR(32)",
				"PRIMARY KEY( serviceid, language )",
				"FOREIGN KEY( serviceid ) REFERENCES service( id )"});
		
		createTable(SERVICEPROPERTY, new String[] {
				"serviceid VARCHAR(" + MAX_PROP_LENGTH + ")",
				"propkey VARCHAR(" + MAX_PROP_LENGTH + ")",
				"propval_obj " + LONGVARCHAR_TYPE,
				"propval_str VARCHAR(" + MAX_PROP_LENGTH + ")",
				"propvalhash VARCHAR(100)",
				"PRIMARY KEY( serviceid, propkey )",
				"FOREIGN KEY( serviceid ) REFERENCES service( id )"});
		
		// Tables for subscriptions
		createTable(SUBSCRIPTION, new String[] { 
				"id	 VARCHAR(" + MAX_PROP_LENGTH + ")"   ,
				"aclm " + LONGVARCHAR_TYPE,
				"PRIMARY KEY( id )"});
		
		createIndices();
		
		//DEBUG
		if(logger.isLoggable(Logger.FINE))
			logger.log(Logger.FINE,"Tables correctly created");
		
	}
	
	/**
	 * Builds an error message for a <code>BatchUpdateException</code>
	 */
	private String getBatchUpdateErroMsg(BatchUpdateException e) {
		StringBuffer msg = new StringBuffer("SQLException: " + e.getMessage() + "\n");
		msg.append("SQLState:  " + e.getSQLState() + "\n");
		msg.append("Message:  " + e.getMessage() + "\n");
		msg.append("Vendor:  " + e.getErrorCode() + "\n");
		msg.append("Update counts: ");
		
		int [] updateCounts = e.getUpdateCounts();
		for (int i = 0; i < updateCounts.length; i++) {
			msg.append(updateCounts[i] + "   ");
		}
		return msg.toString();
	}
	
	/**
	 * Stores the information of an AID of a resolver
	 */
	private void saveResolverAID(AID aid, AID resolverAid) throws SQLException {
		saveAID(resolverAid);
		PreparedStatements pss = getPreparedStatements();
		pss.stm_insAgentResolverAID.setString(1, getGUID());
		pss.stm_insAgentResolverAID.setString(2, aid.getName());
		pss.stm_insAgentResolverAID.setString(3, resolverAid.getName());
		pss.stm_insAgentResolverAID.addBatch(); 
	}
	
	/**
	 * Stores the information of an AID in the database
	 */
	private void saveAID(AID aid) throws SQLException {
		String name = aid.getName();
		PreparedStatements pss = getPreparedStatements();

		// Addresses
		Iterator iter = aid.getAllAddresses();
		if (iter.hasNext()) {
			pss.stm_insAgentAddress.clearBatch();
			while( iter.hasNext()){
				pss.stm_insAgentAddress.setString(1, getGUID());
				pss.stm_insAgentAddress.setString(2, name);
				pss.stm_insAgentAddress.setString(3, (String)iter.next());
				pss.stm_insAgentAddress.addBatch();
			}
			pss.stm_insAgentAddress.executeBatch();
		}
		
		
		// User defined slots
		Properties props = aid.getAllUserDefinedSlot();
		if (props.size() > 0) {
			pss.stm_insAgentUserDefSlot.clearBatch();
			java.util.Iterator pIter = props.entrySet().iterator();
			while (pIter.hasNext()) {
				Map.Entry entry = (Map.Entry)pIter.next();
				pss.stm_insAgentUserDefSlot.setString(1, getGUID());
				pss.stm_insAgentUserDefSlot.setString(2, name);
				pss.stm_insAgentUserDefSlot.setString(3, (String)entry.getKey());
				pss.stm_insAgentUserDefSlot.setString(4, (String)entry.getValue());
				pss.stm_insAgentUserDefSlot.addBatch();
			}
			pss.stm_insAgentUserDefSlot.executeBatch();
		}
		
		// Resolvers
		iter = aid.getAllResolvers();
		if (iter.hasNext()) {
			pss.stm_insAgentResolverAID.clearBatch();
			while(iter.hasNext()){
				AID resolverAID = (AID)iter.next();
				saveResolverAID(aid, resolverAID);
			}  
			pss.stm_insAgentResolverAID.executeBatch();
		}
	}
	
	/**
	 * Returns all resolver AIDs for the given AID
	 * @return <code>Collection</code> of aid strings
	 */
	private Collection getResolverAIDs(String aid) throws SQLException {
		ArrayList res = new ArrayList();
		PreparedStatements pss = getPreparedStatements();

		pss.stm_selAgentResolverAIDs.setString(1, aid);
		ResultSet rs = pss.stm_selAgentResolverAIDs.executeQuery();
		while(rs.next()){
			res.add(rs.getString(1));
		}
		return res;
	}
	
	/**
	 * Stores a collection of services for a specific description Id in the database
	 * @param descrId id of the DFD these services belong to
	 * @param iter iterator for a collection of <code>ServiceDescription</code> instances
	 * @throws SQLException
	 */
	private void saveServices(String descrId, Iterator iter) throws SQLException {
		if (iter.hasNext()) {
			PreparedStatements pss = getPreparedStatements();
			pss.stm_insService.clearBatch();
			pss.stm_insServiceOntology.clearBatch();
			pss.stm_insServiceOntology.clearBatch();
			pss.stm_insServiceLanguage.clearBatch();
			pss.stm_insServiceProperty.clearBatch();
			
			boolean executeProtocolsBatch = false;
			boolean executeOntologiesBatch = false;
			boolean executeLanguagesBatch = false;
			boolean executePropertiesBatch = false;
			
			while(iter.hasNext()){
				ServiceDescription service = (ServiceDescription)iter.next();
				String serviceId = getGUID();
				pss.stm_insService.clearParameters();
				pss.stm_insService.setString(1, serviceId);
				pss.stm_insService.setString(2, descrId);
				pss.stm_insService.setString(3, service.getName());
				pss.stm_insService.setString(4, service.getType());
				pss.stm_insService.setString(5, service.getOwnership());
				pss.stm_insService.addBatch();
				
				// Service - Protocols
				Iterator iterS = service.getAllProtocols();
				while(iterS.hasNext()){
					pss.stm_insServiceProtocol.setString(1, serviceId);
					pss.stm_insServiceProtocol.setString(2, (String)iterS.next());
					pss.stm_insServiceProtocol.addBatch();
					executeProtocolsBatch = true;
				}
				
				// Service - Ontologies
				iterS = service.getAllOntologies();
				while(iterS.hasNext()){
					pss.stm_insServiceOntology.setString(1, serviceId);
					pss.stm_insServiceOntology.setString(2, (String)iterS.next());
					pss.stm_insServiceOntology.addBatch();
					executeOntologiesBatch = true;
				}
				
				// Service - Languages
				iterS = service.getAllLanguages();
				while(iterS.hasNext()){
					pss.stm_insServiceLanguage.setString(1, serviceId);
					pss.stm_insServiceLanguage.setString(2, (String)iterS.next());
					pss.stm_insServiceLanguage.addBatch();
					executeLanguagesBatch = true;
				}
				
				// Service - Properties
				iterS = service.getAllProperties();
				while(iterS.hasNext()){
					
					Property prop = (Property)iterS.next();
					try {
						pss.stm_insServiceProperty.setString(1, serviceId);
						pss.stm_insServiceProperty.setString(2, prop.getName());
						
						// serialize value to a string and calcualte 
						// a hash map for later search operations
						Object value = prop.getValue();
						// store plain String object value directly
						// in 'propval_str' field otherwise store it in
						// 'propval_obj' field and fill hash field (this will be used in search phase to allow matching Serializable objects)
						if ( needSerialization(value) ) {
							//System.out.println("DF Handling Object property "+prop.getName()+": value = "+value);
							String valueStr = serializeObj(value);
							pss.stm_insServiceProperty.setString(3, valueStr);
							pss.stm_insServiceProperty.setString(4, null);
							String hashStr = getHashValue(value);
							pss.stm_insServiceProperty.setString(5, hashStr);
						}
						else {
							// set to NULL the serialized representation of the object and its hash
							//System.out.println("DF Handling String property "+prop.getName()+": value = "+value);
							pss.stm_insServiceProperty.setString(3, null);
							pss.stm_insServiceProperty.setString(4, (String) value);
							pss.stm_insServiceProperty.setString(5, null);
						};
						
						pss.stm_insServiceProperty.addBatch();
						executePropertiesBatch = true;            
					} catch (Exception e) {
						if(logger.isLoggable(Logger.SEVERE))
							logger.log(Logger.SEVERE,"Cannot serialize property '" + prop.getName() + 
									"' for service '" + service.getName() + "'", e);
					}
				}
			}
			pss.stm_insService.executeBatch();
			if (executeProtocolsBatch) {
				pss.stm_insServiceProtocol.executeBatch();
			}
			if (executeOntologiesBatch) {
				pss.stm_insServiceOntology.executeBatch();
			}
			if (executeLanguagesBatch) {
				pss.stm_insServiceLanguage.executeBatch();
			}
			if (executePropertiesBatch) {
				pss.stm_insServiceProperty.executeBatch();
			}
		}
	}
	
	private static final boolean needSerialization(Object value) {
		return !((value instanceof String) && ( ((String) value).length() <= MAX_PROP_LENGTH ));		
	}
	
	/**
	 *  Insert a new DFD object.
	 *  @return the previous DFD (if any) corresponding to the same AID
	 */
	protected Object insertSingle(Object name, Object fact) throws SQLException {
		DFAgentDescription dfd = (DFAgentDescription) fact;
		AID agentAID = dfd.getName();
		String agentName = agentAID.getName();
		DFAgentDescription dfdToReturn = null;
		String batchErrMsg = "";
		
		Connection conn = getConnectionWrapper().getConnection();
		PreparedStatements pss = getPreparedStatements();
		try {
			// -- Remove the previous DFD if any
			dfdToReturn = (DFAgentDescription) removeSingle(dfd.getName());

			// -- add new DFD

			// DF Agent Description
			Date leaseTime = dfd.getLeaseTime();
			long lt = (leaseTime != null ? leaseTime.getTime() : -1);
			String descrId = getGUID();

			pss.stm_insAgentDescr.setString(1, descrId);
			pss.stm_insAgentDescr.setString(2, agentName);
			pss.stm_insAgentDescr.setString(3, String.valueOf(lt));
			pss.stm_insAgentDescr.executeUpdate();
			
			
			// AID
			saveAID(agentAID);
			
			// Languages
			Iterator iter = dfd.getAllLanguages();
			if (iter.hasNext()) {
				pss.stm_insLanguage.clearBatch();
				while(iter.hasNext()){
					pss.stm_insLanguage.setString(1, descrId);
					pss.stm_insLanguage.setString(2, (String)iter.next());
					pss.stm_insLanguage.addBatch();
				}
				pss.stm_insLanguage.executeBatch();
			}
			
			// Ontologies
			iter = dfd.getAllOntologies();
			if (iter.hasNext()) {
				pss.stm_insOntology.clearBatch();
				while(iter.hasNext()){
					pss.stm_insOntology.setString(1, descrId);
					pss.stm_insOntology.setString(2, (String)iter.next());
					pss.stm_insOntology.addBatch();
				}
				pss.stm_insOntology.executeBatch();
			}
			
			// Protocols
			iter = dfd.getAllProtocols();
			if (iter.hasNext()) {
				pss.stm_insProtocol.clearBatch();
				while(iter.hasNext()){
					pss.stm_insProtocol.setString(1, descrId);
					pss.stm_insProtocol.setString(2, (String)iter.next());
					pss.stm_insProtocol.addBatch();
				}
				pss.stm_insProtocol.executeBatch();
			}
			
			// Services
			saveServices(descrId, dfd.getAllServices());    
			
			regsCnt++;
			// clear outdated entries after a certain number of new registrations
			if(regsCnt > MAX_REGISTER_WITHOUT_CLEAN){
				regsCnt = 0;
				clean();
			}
			
			conn.commit();		
		} catch (SQLException sqle) {
			// Rollback the transaction
			try {
				conn.rollback();
			} catch (SQLException se) {
				logger.log(Logger.SEVERE,"Rollback for incomplete insertion of DFD for agent "+dfd.getName() + " failed.", se);
			}
			// Re-throw the exception
			throw sqle;
		} 
		
		return dfdToReturn;
	}
	
	/**
	 * Remove the DFD object corresponding to the indicated AID.
	 * @return the removed DFD (if any) 
	 */
	protected Object removeSingle(Object name) throws SQLException {
		AID agentAID = (AID) name;
		String n = agentAID.getName();
		
		DFAgentDescription dfd = getDFD(n);
		if (dfd != null) {
			remove(n);
		}
		
		return dfd;
	}

	/**
	 * Retrieve the DFDs matching the given template
	 */
	protected List searchSingle(Object template, int maxResult) throws SQLException {
		List matchingAIDs = new ArrayList();
		
		// Get the names of all DFDs matching the template
		String select = null;
		ResultSet rs = null;
		Statement s = null;
		
		try {
			select = createSelect((DFAgentDescription) template);
					
			s = getConnectionWrapper().getConnection().createStatement();
			if (maxResult >= 0) {
				s.setMaxRows(maxResult);
				s.setFetchSize(maxResult);
			}
			rs = s.executeQuery(select);
			
			while(rs.next()) { 
				String aidS = rs.getString("aid");
				matchingAIDs.add(aidS);
			}			
		} 
		catch(SQLException sqle) {
			// Let it through
			throw sqle;
		}
		catch(Exception e) {
			logger.log(Logger.SEVERE, "Couldn't create the SQL SELECT statement.", e);
			throw new SQLException("Couldn't create the SQL SELECT statement. "+e.getMessage());
		}
		finally {
			closeResultSet(rs);
			closeStatement(s);
		}
		
		// For each matching AID reconstruct the complete DFD
		List dfds = new ArrayList(matchingAIDs.size());
		Iterator it = matchingAIDs.iterator();
		// FIXME: Define a proper constant and possibly a proper configuration option
		if (matchingAIDs.size() < 10) {
			while (it.hasNext()) {
				dfds.add(getDFD((String) it.next()));
			}
		}
		else {
			// If we found several matching agents we preload protocols languages and ontologies once for all 
			// instead of making several queries one per agent.
			PreparedStatements pss = getPreparedStatements();
			Map allLanguages = preloadIdValueTable(pss.stm_selCountAllLanguages, pss.stm_selAllLanguages);
			Map allOntologies = preloadIdValueTable(pss.stm_selCountAllOntologies, pss.stm_selAllOntologies);
			Map allProtocols = preloadIdValueTable(pss.stm_selCountAllProtocols, pss.stm_selAllProtocols);
			while (it.hasNext()) {
				dfds.add(getDFD((String) it.next(), allLanguages, allOntologies, allProtocols));
			}
		}
		
		return dfds;
	}
	
	private Map preloadIdValueTable(PreparedStatement cntStm, PreparedStatement stm) throws SQLException {
		Map m = null;
		ResultSet rs = cntStm.executeQuery();
		rs.next();
		long recordCount = rs.getLong(1);
		closeResultSet(rs);
		if (recordCount < MAX_PRELOAD_CNT) {
			// If there are more than MAX_PRELOAD_CNT elements return null. In fact it is more time consuming constructing the preload Map 
			// than performing all queries in the DB.
			rs = stm.executeQuery();
			
			if (true) {
				m = new HashMap();
				String currentId = null;
				List l = null;
				while(rs.next()){
					String id = rs.getString(1); // id (using the index is faster)
					if (!id.equals(currentId)) {
						l =  new ArrayList();
						m.put(id, l);
						currentId = id;
					}
					l.add(rs.getString(2)); // value (protocol, language, ontology ... depending on the passed statement)
				}
				closeResultSet(rs);
			}
		}
		return m;
	}


	/**
	 */
	protected KBIterator iteratorSingle(Object template) throws SQLException {
		String select = null;
		ResultSet rs = null;
		Statement s = null;
		
		try {
			select = createSelect((DFAgentDescription) template);
			
			s = getConnectionWrapper().getConnection().createStatement();
			rs = s.executeQuery(select);
			
			return new DFDBKBIterator(s, rs);
		} 
		catch(SQLException se){
			logger.log(Logger.SEVERE, "Error accessing DB: "+select, se);
			closeResultSet(rs);
			closeStatement(s);
			throw se;
		}
		catch(Exception e) {
			logger.log(Logger.SEVERE, "Error creating SQL SELECT statement.", e);
			throw new SQLException("Error creating SQL SELECT statement. "+e.getMessage());
		}
	}
	
	
	/**
	 Inner class DFDBKBIterator
	 */
	private class DFDBKBIterator implements KBIterator {
		private Statement s = null;
		private ResultSet rs = null;
		private boolean hasMoreElements = false;
		
		public DFDBKBIterator(Statement s, ResultSet rs) throws SQLException {
			this.s = s;
			this.rs = rs;
			if (rs != null) {
				// Move to the first row
				hasMoreElements = rs.next();
			}
		}
		
		public boolean hasNext() {
			return hasMoreElements;
		}
		
		public Object next() {
			if (hasMoreElements) {
				try {
					String name = rs.getString("aid");
					DFAgentDescription dfd = getDFD(name);
					hasMoreElements = rs.next();
					return dfd;
				}
				catch (SQLException sqle) {
					hasMoreElements = false;
					throw new NoSuchElementException("DB Error. "+sqle.getMessage());
				}
			}
			throw new NoSuchElementException("");
		}
		
		public void remove() {
			// Not implemented
		}
		
		public void close() {
			closeResultSet(rs);
			closeStatement(s);
		}
	} // END of inner class DFDBKBIterator
	
	
	/**
	 * Reconstructs an AID object corresponding to the given AID name
	 * @throws SQLException
	 */
	private AID getAID(String aidN) throws SQLException {
		
		ResultSet rs = null;
		AID id = new AID(aidN, AID.ISGUID);

		PreparedStatements pss = getPreparedStatements();
		// AID addresses
		pss.stm_selAgentAddresses.setString(1, aidN);
		rs = pss.stm_selAgentAddresses.executeQuery();
		while(rs.next()){
			id.addAddresses(rs.getString(1));
		}
		
		// AID resolvers 
		Collection resolvers = getResolverAIDs(aidN);
		Iterator iter = resolvers.iterator();
		while (iter.hasNext()) {
			id.addResolvers(getAID((String)iter.next()));
		}
		
		// AID User defined slots
		pss.stm_selAgentUserDefSlot.setString(1, aidN);
		rs = pss.stm_selAgentUserDefSlot.executeQuery();
		while(rs.next()) {
			String key = rs.getString("slotkey");
			String value = rs.getString("slotval");
			id.addUserDefinedSlot(key, value);
		}
		
		return id;
	}
	
	private DFAgentDescription getDFD(String aidN) throws SQLException {
		return getDFD(aidN, null, null, null);
	}
	
	/**
	 Reconstruct the DFD corresponding to the given AID name (if any)
	 */
	private DFAgentDescription getDFD(String aidN, Map allLanguages, Map allOntologies, Map allProtocols) throws SQLException {
		DFAgentDescription dfd = null;
		AID id = null;
		
		ResultSet rs = null;
		ResultSet rsS = null;
		String descrId = null;
		
		try{
			PreparedStatements pss = getPreparedStatements();
			// Check if there is a DFD corresponding to aidN and get lease time
			pss.stm_selLease.setString(1, aidN);
			rs = pss.stm_selLease.executeQuery();
			if (rs.next()) {
				dfd = new DFAgentDescription();
				id = getAID(aidN);
				dfd.setName(id);
				String sLease = rs.getString("lease");
				descrId = rs.getString("id");
				long lease = Long.parseLong(sLease);
				if (lease != -1) {
					dfd.setLeaseTime(new Date(lease));
				}
			}
			else {
				return null;
			}
			closeResultSet(rs);
			
			// Protocols
			loadProtocols(descrId, dfd, allProtocols);		
			
			// Languages
			loadLanguages(descrId, dfd, allLanguages);
			
			// Ontologies
			loadOntologies(descrId, dfd, allOntologies);
			
			// Services
			pss.stm_selServices.setString(1, descrId);
			rs = pss.stm_selServices.executeQuery();
			while(rs.next()) {
				ServiceDescription sd = new ServiceDescription();
				String serviceId = rs.getString("id");
				sd.setName(rs.getString("sname"));
				sd.setType(rs.getString("stype"));
				sd.setOwnership(rs.getString("sownership"));
				
				// Service protocols
				pss.stm_selServiceProtocols.setString(1, serviceId);
				rsS = pss.stm_selServiceProtocols.executeQuery();
				while(rsS.next()){
					sd.addProtocols(rsS.getString(PROTOCOL));
				}	
				closeResultSet(rsS);
				
				// Service languages
				pss.stm_selServiceLanguages.setString(1, serviceId);
				rsS = pss.stm_selServiceLanguages.executeQuery();
				while(rsS.next()){
					sd.addOntologies(rsS.getString(ONTOLOGY));
				}	
				closeResultSet(rsS);
				
				// Service ontologies
				pss.stm_selServiceOntologies.setString(1, serviceId);
				rsS = pss.stm_selServiceOntologies.executeQuery();
				while(rsS.next()){
					sd.addLanguages(rsS.getString(LANGUAGE));
				}
				closeResultSet(rsS);
				
				// Service properties
				pss.stm_selServiceProperties.setString(1, serviceId);
				rsS = pss.stm_selServiceProperties.executeQuery();
				while(rsS.next()){
					Property prop = new Property();
					prop.setName(rsS.getString("propkey"));
					String objStrVal = rsS.getString("propval_obj");
					String strStrVal = rsS.getString("propval_str");
					Object value = ( objStrVal == null )? strStrVal : deserializeObj(objStrVal);
					prop.setValue(value);
					sd.addProperties(prop);
				}
				
				dfd.addServices(sd);
			}
		}
		catch (SQLException sqle) {
			// Let it through
			throw sqle;
		}
		catch (Exception e) {
			logger.log(Logger.SEVERE, "Unexpected error retrieving DFD for agent "+aidN, e);
			throw new SQLException("Unexpected error retrieving DFD for agent "+aidN+". "+e.getMessage());
		}
		finally {
			closeResultSet(rs);
			closeResultSet(rsS);
		}
		return dfd;
	}
	
	
	private final void loadOntologies(String descrId, DFAgentDescription dfd, Map allOntologies) throws SQLException {
		if (allOntologies != null) {
			List ontos = (List) allOntologies.get(descrId);
			if (ontos != null) {
				Iterator it = ontos.iterator();
				while (it.hasNext()) {
					dfd.addOntologies((String) it.next());
				}
			}
		}
		else {
			PreparedStatements pss = getPreparedStatements();
			pss.stm_selOntologies.setString(1, descrId);
			ResultSet rs = pss.stm_selOntologies.executeQuery();
			while(rs.next()){
				dfd.addOntologies(rs.getString(ONTOLOGY));
			}
			closeResultSet(rs);
		}
	}

	private final void loadLanguages(String descrId, DFAgentDescription dfd, Map allLanguages) throws SQLException {
		if (allLanguages != null) {
			List langs = (List) allLanguages.get(descrId);
			if (langs != null) {
				Iterator it = langs.iterator();
				while (it.hasNext()) {
					dfd.addLanguages((String) it.next());
				}
			}
		}
		else {
			PreparedStatements pss = getPreparedStatements();
			pss.stm_selLanguages.setString(1, descrId);
			ResultSet rs = pss.stm_selLanguages.executeQuery();
			while(rs.next()){
				dfd.addLanguages(rs.getString(LANGUAGE));
			}
			closeResultSet(rs);
		}
	}

	private final void loadProtocols(String descrId, DFAgentDescription dfd, Map allProtocols) throws SQLException {
		if (allProtocols != null) {
			List protos = (List) allProtocols.get(descrId);
			if (protos != null) {
				Iterator it = protos.iterator();
				while (it.hasNext()) {
					dfd.addProtocols((String) it.next());
				}
			}
		}
		else {
			PreparedStatements pss = getPreparedStatements();
			pss.stm_selProtocols.setString(1, descrId);
			ResultSet rs = pss.stm_selProtocols.executeQuery();
			while(rs.next()){
				dfd.addProtocols(rs.getString(PROTOCOL));
			}
			closeResultSet(rs);
		}
	}

	/**
	 * Deletes the AID corresponding to the indicated agent name
	 * including all its resolver AIDs (if there are no df descriptions left for them)
	 */
	private void removeAID(String aid) throws SQLException {
		PreparedStatements pss = getPreparedStatements();
		// check whether there exists a DF description for the agent
		pss.stm_selNrOfDescrForAID.setString(1, aid);
		ResultSet rs = pss.stm_selNrOfDescrForAID.executeQuery();
		int found = 0;
		if (rs.next())
			found = Integer.parseInt(rs.getString(1));
		
		// no description found --> delete
		if (found == 0) {
			// user definded slots
			pss.stm_delAgentUserDefSlot.setString(1, aid);
			pss.stm_delAgentUserDefSlot.execute();
			
			// resolvers
			Collection resolverAIDs = getResolverAIDs(aid);
			Iterator iter = resolverAIDs.iterator();
			while (iter.hasNext()) {
				removeAID((String)iter.next());
			}
			
			pss.stm_delAgentResolver.setString(1, aid);
			pss.stm_delAgentResolver.execute();
			
			// address
			pss.stm_delAgentAddress.setString(1, aid);
			pss.stm_delAgentAddress.execute();
		}
		
		
	}
	
	/**
	 * Deletes all services corresponding to the indicated description ID
	 * @throws SQLException
	 */
	private void removeServices(String descrId) throws SQLException {
		ResultSet rs = null;
		PreparedStatements pss = getPreparedStatements();
		pss.stm_selServiceId.setString(1, descrId);
		rs = pss.stm_selServiceId.executeQuery();
		
		boolean executeBatch = false;
		while (rs.next()) {
			String serviceId = rs.getString("id");
			
			pss.stm_delServiceLanguage.setString(1, serviceId);
			pss.stm_delServiceLanguage.addBatch();
			
			pss.stm_delServiceOntology.setString(1, serviceId);
			pss.stm_delServiceOntology.addBatch();
			
			pss.stm_delServiceProtocol.setString(1, serviceId);
			pss.stm_delServiceProtocol.addBatch();
			
			pss.stm_delServiceProperty.setString(1, serviceId);
			pss.stm_delServiceProperty.addBatch();
			
			pss.stm_delService.setString(1, descrId);
			pss.stm_delService.addBatch();
			
			executeBatch = true;
		}
		rs.close();
		
		if (executeBatch) {
			pss.stm_delServiceLanguage.executeBatch();
			pss.stm_delServiceOntology.executeBatch();
			pss.stm_delServiceProtocol.executeBatch();
			pss.stm_delServiceProperty.executeBatch();
			pss.stm_delService.executeBatch();
		}
	}


	/**
	 *  Delete the DFD object corresponding to the indicated agent name.
	 */
	private void remove(String aid) throws SQLException {  		
		ResultSet rs = null;
		Connection conn = getConnectionWrapper().getConnection();
		
		try {   
			PreparedStatements pss = getPreparedStatements();
			// get description ID
			pss.stm_selDescrId.setString(1, aid);
			rs = pss.stm_selDescrId.executeQuery();
			
			if (rs.next()) {
				String descrId = rs.getString("id");
				closeResultSet(rs);
				
				// ontologies
				pss.stm_delOntology.setString(1, descrId);
				pss.stm_delOntology.execute();
				
				// protocols
				pss.stm_delProtocol.setString(1, descrId);
				pss.stm_delProtocol.execute();
				
				// languages
				pss.stm_delLanguage.setString(1, descrId);
				pss.stm_delLanguage.execute();
				
				// services
				removeServices(descrId);
				
				// agent description
				pss.stm_delAgentDescr.setString(1, descrId);
				pss.stm_delAgentDescr.execute();
				
				// AID
				removeAID(aid);
				conn.commit();
				
			} else {
				if(logger.isLoggable(Logger.FINE))
					logger.log(Logger.FINE,"No DF description found to remove for agent '"+aid+"'");
			}
		}
		catch(SQLException sqle){
			try {
				conn.rollback();
			} catch (SQLException se) {
				logger.log(Logger.SEVERE,"Rollback for incomplete remotion of DFD for agent "+aid + " failed.", se);
			}
			throw sqle;
		} finally {
			closeResultSet(rs);
		}
	}
	
	
	/**
	 * Convert a template DFAgentDescription into the SQL SELECT
	 * operation that returns the AID names whose DFAgentDescriptions
	 * match the given template.
	 */
	private String createSelect(DFAgentDescription dfdTemplate) throws Exception {
		
		StringBuffer select = new StringBuffer("SELECT dfagentdescr.aid FROM dfagentdescr");
		
		List lAs = new ArrayList();
		List lWhere = new ArrayList();
		
		// Name
		AID agentAID = dfdTemplate.getName();
		if(agentAID != null){
			lWhere.add(" dfagentdescr.aid = '"+agentAID.getName()+"'");
		}
		// Lease time
		Date lease = dfdTemplate.getLeaseTime();
		long currTime = System.currentTimeMillis();
		lWhere.add(" (dfagentdescr.lease = '-1' OR dfagentdescr.lease > '"+currTime+"')");
		
		// Languages
		Iterator iter = dfdTemplate.getAllLanguages();
		int i=0;
		while(iter.hasNext()){
			String tmp = LANGUAGE+i;
			lAs.add(", language "+tmp);
			lWhere.add(tmp+".language='"+(String)iter.next()+"'");
			lWhere.add(tmp+".descrid=dfagentdescr.id");
			i++;
		}
		// Ontologies
		iter = dfdTemplate.getAllOntologies();
		i = 0;
		while(iter.hasNext()){
			String tmp = ONTOLOGY+i;
			lAs.add(", ontology "+tmp);
			lWhere.add(tmp+".ontology='"+(String)iter.next()+"'");
			lWhere.add(tmp+".descrid=dfagentdescr.id");
			i++;
		}
		// Protocols
		iter = dfdTemplate.getAllProtocols();
		i = 0;
		while(iter.hasNext()){
			String tmp = PROTOCOL+i;
			lAs.add(", protocol "+tmp);
			lWhere.add(tmp+".protocol='"+(String)iter.next()+"'");
			lWhere.add(tmp+".descrid=dfagentdescr.id");
			i++;
		}
		// Services
		iter = dfdTemplate.getAllServices();
		i = 0;
		while(iter.hasNext()){
			ServiceDescription service = (ServiceDescription)iter.next();
			String serviceName = service.getName();
			String serviceType = service.getType();
			String serviceOwner = service.getOwnership();
			// Service name, type and ownership
			String tmp = SERVICE+i;
			lAs.add(", service "+tmp);
			if(serviceName != null){
				lWhere.add(tmp+".sname='"+serviceName+"'");
			}
			if(serviceType != null){
				lWhere.add(tmp+".stype='"+serviceType+"'");
			}
			if(serviceOwner != null){
				lWhere.add(tmp+".sownership='"+serviceOwner+"'");
			}
			lWhere.add(tmp+".descrid=dfagentdescr.id");
			i++;
			
			// Service languages
			Iterator iterS = service.getAllLanguages();
			int j = 0;
			while(iterS.hasNext()){
				String tmp1 = SERVICELANGUAGE+j;
				lAs.add(", servicelanguage "+tmp1);
				lWhere.add(tmp1+".language='"+(String)iterS.next()+"'");
				lWhere.add(tmp1+".serviceid="+tmp+".id");
				j++;
			}
			// Service ontologies
			iterS = service.getAllOntologies();
			j = 0;
			while(iterS.hasNext()){
				String tmp1 = SERVICEONTOLOGY+j;
				lAs.add(", serviceontology "+tmp1);
				lWhere.add(tmp1+".ontology='"+(String)iterS.next()+"'");
				lWhere.add(tmp1+".serviceid="+tmp+".id");
				j++;
			}
			// Service protocols
			iterS = service.getAllProtocols();
			j = 0;
			while(iterS.hasNext()){
				String tmp1 = SERVICEPROTOCOL+j;
				lAs.add(", serviceprotocol "+tmp1);
				lWhere.add(tmp1+".protocol='"+(String)iterS.next()+"'");
				lWhere.add(tmp1+".serviceid="+tmp+".id");
				j++;
			}
			// Service properties
			iterS = service.getAllProperties();
			j = 0;
			while(iterS.hasNext()){
				String tmp1 = SERVICEPROPERTY+j;
				lAs.add(", serviceproperty "+tmp1);
				Property prop = (Property) iterS.next();	
				
				if (prop.getName() != null)
					lWhere.add(tmp1+".propkey='"+prop.getName()+"'");
				
				Object value = prop.getValue();
				if (value != null) {
					if (needSerialization(value)) {
						String hashStr = getHashValue(prop.getValue());
						lWhere.add(tmp1+".propvalhash='"+ hashStr +"'");
					}
					else {
						lWhere.add(tmp1+".propval_str='"+ value +"'");
					}
				}
				lWhere.add(tmp1+".serviceid="+tmp+".id");  
				j++;
			}			
		}
		
		// Concatenate all the aliases
		iter = lAs.iterator();
		while (iter.hasNext()) {
			select.append((String) iter.next());
		}
		// Concatenate all WHERE
		if (lWhere.size() > 0) {
			select.append(" WHERE ");
		}
		iter = lWhere.iterator();
		i = 0;
		while (iter.hasNext()) {
			if(i > 0) {
				select.append(" and ");
			}
			select.append((String) iter.next());
			++i;
		}
		return select.toString();
	}
	
	////////////////////////////////////////
	// DB cleaning methods
	////////////////////////////////////////
	
	/**
	 Removes DF registrations and subscriptions whose lease time 
	 has expired.
	 This method is called at startup and each MAX_REGISTER_WITHOUT_CLEAN
	 registrations.
	 */
	private void clean(){
		cleanExpiredRegistrations();
		cleanExpiredSubscriptions();
	}
	
	/**
	 * Removes DF registrations whose lease time has expired.
	 */
	private void cleanExpiredRegistrations(){
		
		ResultSet rs = null;
		long currTime = System.currentTimeMillis();
		try{
			PreparedStatements pss = getPreparedStatements();
			pss.stm_selExpiredDescr.setString(1, String.valueOf(currTime));
			rs = pss.stm_selExpiredDescr.executeQuery();
			
			while(rs.next()){
				remove(rs.getString("aid"));
			}
		}
		catch(SQLException se){
			if(logger.isLoggable(Logger.WARNING))
				logger.log(Logger.WARNING, "Error cleaning expired DF registrations", se);
			
		} finally {
			closeResultSet(rs);
		}
	}
	
	/**
	 Removes DF subscriptions whose lease time has expired.
	 */
	private void cleanExpiredSubscriptions() {
		//FIXME: To be implemented
	}
	
	private StringACLCodec codec = new StringACLCodec();
	
	protected void subscribeSingle(Object dfd, SubscriptionResponder.Subscription s) throws SQLException, NotUnderstoodException{
		ACLMessage aclM = s.getMessage();
		String msgStr = aclM.toString();
		String convID = aclM.getConversationId();
		registerSubscription(convID, msgStr);
	}
	
	/**
	 * Add a subscription to the database
	 * @param convID conversation id (used as primary key)
	 * @param aclM ACL message for the subscription
	 */
	private void registerSubscription(String convID, String aclM) throws SQLException {
		Connection conn = getConnectionWrapper().getConnection();
		try {
			PreparedStatements pss = getPreparedStatements();
			String base64Str = new String(Base64.encodeBase64(aclM.getBytes("US-ASCII")), "US-ASCII"); 
			// --> convert string to Base64 encoding
			pss.stm_insSubscription.setString(1, convID);
			pss.stm_insSubscription.setString(2, base64Str);
			pss.stm_insSubscription.execute();
			conn.commit();			
		} 
		catch (SQLException sqle) {
			// Rollback the transaction
			try {
				conn.rollback();
			} catch (SQLException se) {
				logger.log(Logger.SEVERE,"Rollback for incomplete subscription failed.", se);
			}
			// Re-throw the exception
			throw sqle;
		}
		catch (Exception e) {
			logger.log(Logger.SEVERE, "Error encoding subscription message in Base64.", e);
			throw new SQLException("Error encoding subscription message in Base64. "+e.getMessage());
		}
	}
	
	/**
	 * Return all known subscriptions at the DF
	 * @return <code>Enumeration</code> with instances of the class 
	 * <code> jade.proto.SubscriptionResponder&Subscription</code>
	 */
	public Enumeration getSubscriptions(){
		Vector subscriptions = new Vector();
		StringACLCodec codec = new StringACLCodec();
		ResultSet rs = null;
		
		try {
			rs = getPreparedStatements().stm_selSubscriptions.executeQuery();
			while (rs.next()) {
				String base64Str = rs.getString("aclm");
				String aclmStr = new String(Base64.decodeBase64(base64Str.getBytes("US-ASCII")), "US-ASCII");
				ACLMessage aclm = codec.decode(aclmStr.getBytes(), ACLCodec.DEFAULT_CHARSET);
				subscriptions.add(sr.createSubscription(aclm));
			}
			
		} catch (Exception e) {
			if(logger.isLoggable(Logger.SEVERE))
				logger.log(Logger.SEVERE, "Error retrieving subscriptions from the database", e);
			
		} finally {
			closeResultSet(rs);
		}
		return subscriptions.elements();
	}
	
	
	protected void unsubscribeSingle(SubscriptionResponder.Subscription sub) throws SQLException {
		ACLMessage aclM = sub.getMessage();
		String convID = aclM.getConversationId();
		boolean deleted = deregisterSubscription(convID);
		if(!deleted)
			if(logger.isLoggable(Logger.WARNING))
				logger.log(Logger.WARNING,"No subscription to delete.");
	}
	
	
	/**
	 * Removes a registration from the database.
	 * @param convID id for the subscription
	 * @return <code>true</code> if an entry has been found and removed  
	 * - otherwise <code>false</code>
	 */
	private boolean deregisterSubscription(String convID) throws SQLException {

		Connection conn = getConnectionWrapper().getConnection();
		try {
			PreparedStatements pss = getPreparedStatements();
			pss.stm_delSubscription.setString(1, convID);
			int rowCount = pss.stm_delSubscription.executeUpdate();
			conn.commit();
			return (rowCount != 0);
		} catch (SQLException sqle) {
			// Rollback the transaction
			try {
				conn.rollback();
			} catch (SQLException se) {
				logger.log(Logger.SEVERE,"Rollback for incomplete un-subscription failed.", se);
			}
			// Re-throw the exception
			throw sqle;
		}
	}
	
	
	////////////////////////////////////////////////
	// Helper methods
	////////////////////////////////////////////////
	
	
	/**
	 * Closes an open result set and logs an appropriate error message
	 * when it fails
	 */
	private void closeResultSet(ResultSet rs) {
		try {
			if (rs != null) {
				rs.close();
				rs = null;
			}
		} catch (SQLException e) {
			// result set has already been closed. 
			//(depends party on the database driver)
		}
	}
	
	/**
	 * Closes an open SQL statement and logs an appropriate error message
	 * when it fails
	 */
	private void closeStatement(Statement s) {
		try {
			if (s != null) {
				s.close();
				s = null;
			}
		} catch (Exception e) {
			if(logger.isLoggable(Logger.WARNING))
				logger.log(Logger.WARNING,"Closing SQL statement failed.");
		}
	}
	
	/**
	 * Serializes any serializable object to a Base64 encoded string
	 * @param obj the object to serialize
	 * @throws IOException An error during the serialization
	 */
	private String serializeObj(Object obj) throws IOException {
		if (obj == null)
			return null;
		
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		ObjectOutputStream oos = new ObjectOutputStream(baos);
		oos.writeObject(obj);
		oos.close();
		byte[] data = baos.toByteArray(); 
		return new String(Base64.encodeBase64(data), "US-ASCII");
	}
	
	/**
	 * Deserializes any serializable object from a string
	 * @param str string which represents a serialized object
	 * @throws IOException An error during the serialization
	 * @throws ClassNotFoundException The deserialized java class is unknown
	 */
	private Object deserializeObj(String str) throws IOException, ClassNotFoundException {
		if (str == null)
			return null;
		byte[] data = Base64.decodeBase64(str.getBytes("US-ASCII"));
		ByteArrayInputStream bais = new ByteArrayInputStream(data);
		ObjectInputStream ois = new ObjectInputStream(bais);
		return ois.readObject();
	}
	
	/**
	 * Returns an MD5 hash value for an object
	 * @param obj Object to use for the hash value calcualtion
	 * @return an MD5 value encoded in ISO-8859-1
	 * @throws Exception The hash value couldn't be generated
	 */
	protected String getHashValue(Object obj) throws Exception {
		final String HASH_ALGORITHM = "MD5";
		
		if (obj == null)
			return "null";
		
		try {
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			ObjectOutputStream oos = new ObjectOutputStream(baos);
			oos.writeObject(obj);
			oos.close();
			byte[] data = baos.toByteArray();
			MessageDigest md = MessageDigest.getInstance(HASH_ALGORITHM);
			byte[] digest = md.digest(data);
			return new String(Base64.encodeBase64(digest), "US-ASCII");
			
		} catch (Exception e) {
			throw new Exception("Couldn't create " + HASH_ALGORITHM + " hash for given object.", e);
		}
	}
	
	/**
	 * This method must be used when inserting text values into a DB. It doubles all ' and " characters
	 * If the passed parameter is null, then an empty string is returned.
	 **/
	protected String prepDBStr(String s) {
		if (s == null){
			return "";
		}
		String result = replace(s,"'","''");
		result = replace(result,"\"","\"\"" );
		
		return result;
	}
	
	/**
	 * Replaces all occurences of a <code>pattern</code> in <code>str</code>
	 * with <code>replaceWith</code>
	 * @param str source string
	 * @param pattern pattern string to search for
	 * @param replaceWith new string
	 */
	protected String replace(String str, String pattern, String replaceWith) {
		int s = 0;
		int e = 0;
		StringBuffer result = new StringBuffer();
		
		while ((e = str.indexOf(pattern, s)) >= 0) {
			result.append(str.substring(s, e));
			if(replaceWith != null)
				result.append(replaceWith);
			s = e+pattern.length();
		}
		result.append(str.substring(s));
		return result.toString();
	}
}

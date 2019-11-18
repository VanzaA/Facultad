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

package jade.tools.logging;

import java.io.IOException;
import java.util.Enumeration;
import java.util.StringTokenizer;
import java.util.logging.FileHandler;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;
import jade.tools.logging.ontology.*;
import jade.util.leap.List;
import jade.util.leap.ArrayList;

public class JavaLoggingLogManagerImpl implements LogManager {
	
	public static final String JAVA_LOGGING_LOG_MANAGER_CLASS = "jade.tools.logging.JavaLoggingLogManagerImpl";
	private static final String DEFAULT_ROOT_LOGGER_NAME = "__ROOT_LOGGER";
	private static List levels = new ArrayList();
	
	static {
		levels.add(new LevelInfo(Level.ALL.getName() ,Level.ALL.intValue()));
		levels.add(new LevelInfo(Level.SEVERE.getName() ,Level.SEVERE.intValue()));
		levels.add(new LevelInfo(Level.WARNING.getName() ,Level.WARNING.intValue()));
		levels.add(new LevelInfo(Level.INFO.getName() ,Level.INFO.intValue()));
		levels.add(new LevelInfo(Level.CONFIG.getName() ,Level.CONFIG.intValue()));
		levels.add(new LevelInfo(Level.FINE.getName() ,Level.FINE.intValue()));
		levels.add(new LevelInfo(Level.FINER.getName() ,Level.FINER.intValue()));
		levels.add(new LevelInfo(Level.FINEST.getName() ,Level.FINEST.intValue()));
		levels.add(new LevelInfo(Level.OFF.getName() ,Level.OFF.intValue()));
	}
	
	
	private java.util.logging.LogManager logManager = java.util.logging.LogManager.getLogManager();
	private static final String LOGGER_FRIENDLY_NAME = "Java Util Logging";
	private List loggers = null;
	private java.util.ArrayList rootHandlers = null; //root handlers specified in configuration file.
	
	
	public String getName() {
		return LOGGER_FRIENDLY_NAME;
	}


	/**
	 * 
	 * @return a List of LogInfo
	 */
	public List getAllLogInfo(){
		boolean fhExists = false;
		//initilization of root handlers
		if(this.rootHandlers == null){
			String handlers = logManager.getProperty("handlers");
			if(handlers != null){
				int index = handlers.indexOf(",");
				String separator = ",";
				//handlers can be separated by comma or spaces.
				if(index == -1){
					separator = " ";
				}
				StringTokenizer st = new StringTokenizer(handlers, separator);
				while(st.hasMoreTokens()){
					if(this.rootHandlers == null)
						this.rootHandlers = new java.util.ArrayList();
					String handlerName = st.nextToken().trim();
					fhExists = (handlerName.indexOf("java.util.logging.FileHandler") > -1);
					this.rootHandlers.add(handlerName);
				}
			}
		}
		if(this.loggers == null){
			//istanzio la struttura e la popolo
			this.loggers = new ArrayList();
			for(Enumeration e = logManager.getLoggerNames();e.hasMoreElements();){
				String logName = (String)e.nextElement();
				try {
					Logger theLogger = this.logManager.getLogger(logName); 
					//retrieving the level
					Level level = getLevel(theLogger);
					int loggerLevel = level.intValue();
					//If the result is null, this logger's effective level will be inherited from its parent. 
					//The value is the one specified for the property level in the configuration file 			
					if (logName == null || logName.length() == 0) {
						// This is the ROOT Logger --> Use a non-empty predefined name
						logName = DEFAULT_ROOT_LOGGER_NAME;
					}
					LoggerInfo logInfoElem = new LoggerInfo(logName, loggerLevel);
					
					//if a FileHandler has been specified it's not possibile to retrieve the fileName 
					
					//retrieves all the handlers associated to the logger
					//root handlers are inherited by default
					List loggerHandlers = (this.rootHandlers == null ? new ArrayList() : new ArrayList(this.rootHandlers));
					//root logger handlers have been already set.
					if(!logName.equals("")){
						Handler[] handlers = theLogger.getHandlers();
						//if an handler has been specified at runtime it will have a format
						//i.e java.util.logging.FileHandler@1234556 so we remove the last part.
						//add the file handler specified by the user only if fileHandler is not a root handler
						for (int i=0;i<handlers.length;i++){
							String temp = handlers[i].toString();
							if (!fhExists){
				          String userHandler = (temp.indexOf('@') < 0 ? temp : temp.substring(0, temp.indexOf('@')));
				          loggerHandlers.add(userHandler);
							}
						}
					}
					logInfoElem.setHandlers(loggerHandlers);
					
					this.loggers.add(logInfoElem); //non sono in ordine alfabetico (dovrebbe essere la gui a mostrarle in quell'ordine.
				}
				catch (Exception ex) {
					ex.printStackTrace();
				}
			}
		}
		return this.loggers;
	}
	
	
	private Level getLevel(Logger logger) {
		if (logger != null) {
			Level l = logger.getLevel();
			if (l != null) {
				return l;
			}
			else {
				return getLevel(logger.getParent());
			}
		}
		else {
			return Level.INFO;
		}
	}


	public void setLogLevel(String name, int level){
		
		//update the LogInfo associated.
		for(int i=0; i<this.loggers.size(); i++){
			LoggerInfo lInfo = (LoggerInfo)this.loggers.get(i);
			if(lInfo.getName().equalsIgnoreCase(name)){
				lInfo.setLevel(level);
				break;
			}
		}	
		if(name.equals(DEFAULT_ROOT_LOGGER_NAME)) {
			name = "";
		}
		
		Logger logger = logManager.getLogger(name);
		Level  newLoggerLevel = Level.INFO;
		if(level == Level.ALL.intValue()){
			newLoggerLevel = Level.ALL;
		}else if(level == Level.SEVERE.intValue()){
			newLoggerLevel = Level.SEVERE;
		}else if(level == Level.WARNING.intValue()){
			newLoggerLevel = Level.WARNING;
		}else if(level == Level.INFO.intValue()){
			newLoggerLevel = Level.INFO;
		}else if(level == Level.CONFIG.intValue()){
			newLoggerLevel = Level.CONFIG;
		}else if(level == Level.FINE.intValue()){
			newLoggerLevel = Level.FINE;
		}else if(level == Level.FINER.intValue()){
			newLoggerLevel = Level.FINER;
		}else if(level == Level.FINEST.intValue()){
			newLoggerLevel = Level.FINEST;
		}else if(level == Level.OFF.intValue()){
			newLoggerLevel = Level.OFF;
		}
		logger.setLevel(newLoggerLevel);
		//Set level for handlers associated to logger
		if (logger.getParent() != null) {
			Handler[] pHandlers = logger.getParent().getHandlers();
			for (int i=0; i<pHandlers.length; i++){
				pHandlers[i].setLevel(newLoggerLevel);
			}
		}
		Handler[] handlers = logger.getHandlers();
		for (int j=0; j<handlers.length; j++){
			handlers[j].setLevel(newLoggerLevel);
		}
	}
		
	public void setFile(String name, String  fileHandler){
		try {
			//update the LogInfo associated.
			for(int i=0; i<this.loggers.size(); i++){
				LoggerInfo lInfo = (LoggerInfo)this.loggers.get(i);
				if(lInfo.getName().equalsIgnoreCase(name)){
					lInfo.setFile(fileHandler);
					break;
				}
			}
			if(name.equals(DEFAULT_ROOT_LOGGER_NAME)) {
				name = "";
			}
			Logger logger = logManager.getLogger(name);
			logger.addHandler(new FileHandler(fileHandler));
		} catch (IOException e) {
			e.printStackTrace();
		}	
	}
	
	/**
	 * Returns  a list of <code>LevelInfo</code> object each one describing a level valid for the selected logging system. 
	 * @return a list of <code>LevelInfo</code> object each one describing a level valid for the selected logging system.
	 */
	public List getLogLevels(){
		return levels;
	}
}

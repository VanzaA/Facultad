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

package jade.tools.logging.ontology;

//#J2ME_EXCLUDE_FILE

/**
 * This interface contains all the string constants for frame and slot
 * names of the <code>jLog-Management-Ontology</code> ontology.
 */
public interface LogManagementVocabulary {
	/**
	 * A symbolic constant, containing the name of thie Log Management ontology.
	 */
	public static final String NAME = "Log-Management-Ontology";

	public static final String LOGGER_INFO = "LOGGER-INFO";
	public static final String LOGGER_INFO_NAME = "name";
	public static final String LOGGER_INFO_LEVEL = "level";
	public static final String LOGGER_INFO_HANDLERS = "handlers";
	public static final String LOGGER_INFO_FILE = "file";

	public static final String GET_ALL_LOGGERS = "GET-ALL-LOGGERS";
	public static final String GET_ALL_LOGGERS_TYPE = "type";
	public static final String GET_ALL_LOGGERS_FILTER = "filter";

	public static final String SET_LEVEL = "SET-LEVEL";
	public static final String SET_LEVEL_LEVEL = "level";
	public static final String SET_LEVEL_LOGGER = "logger";

	public static final String SET_FILE = "SET-FILE";
	public static final String SET_FILE_FILE = "file";
	public static final String SET_FILE_LOGGER = "logger";
}

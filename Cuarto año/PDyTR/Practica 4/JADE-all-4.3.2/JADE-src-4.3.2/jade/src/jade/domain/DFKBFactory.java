/**
 * JADE - Java Agent DEvelopment Framework is a framework to develop
 * multi-agent systems in compliance with the FIPA specifications.
 * Copyright (C) 2004 CSELT S.p.A.
 *
 * GNU Lesser General Public License
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation,
 * version 2.1 of the License.
 *
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

import java.sql.SQLException;

import jade.domain.KBManagement.DBKB;
import jade.domain.KBManagement.MemKB;

/**
 * The <code>DFKBFactory</code> class creates 
 * knowledge base objects used by the DF. 
 * 
 * <p>
 * To run JADE with your own knowledge base implementation a new 
 * sub class of <code>DFKBFactory</code>, overriding the appropriate method(s), 
 * has to be implemented and specified by the command line parameter 
 * <code>-jade_domain_df_kb-factory</code>.
 * </p>
 * @author Roland Mungenast - Profactor
 * @since JADE 3.3
 */
public class DFKBFactory {

	/**
	 * Returns the memory based knowledge base which will be used by the DF
	 * @param maxResultLimit internal limit for the maximum number of search results
	 */
	protected MemKB getDFMemKB(int maxResultLimit) {
		return new DFMemKB(maxResultLimit);
	}

	/**
	 * Returns the database based knowledge base which will be used by the DF
	 * @param maxResultLimit JADE internal limit for the maximum number of search results
	 * @param driver database driver
	 * @param url database url
	 * @param user user for the database access
	 * @param passwd password for the database access
	 * @param cleanTables specifies whether the KB should delete all existing tables for the DF at startup
	 * @throws SQLException if the database cannot be initialized
	 */
	protected DBKB getDFDBKB(int maxResultLimit, String driver, String url, String user, String passwd, boolean cleanTables) throws SQLException {
		//#ANDROID_EXCLUDE_BEGIN
		DBKB kb = null;
		if (url == null)
			kb = new DFHSQLKB(maxResultLimit, cleanTables);
		else
			kb = new DFDBKB(maxResultLimit, driver, url, user, passwd, cleanTables);
		kb.setup();
		return kb;
		//#ANDROID_EXCLUDE_END

		/*#ANDROID_INCLUDE_BEGIN
		throw new SQLException("Unsupported SQL KB");
		#ANDROID_INCLUDE_END*/
	}
}



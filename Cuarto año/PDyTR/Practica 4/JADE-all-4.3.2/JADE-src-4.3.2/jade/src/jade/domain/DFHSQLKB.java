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

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;

/**
 * <code>DFHSQLKB</code> realizes a knowledge base used by the DF which stores its content
 * in an HSQL database, running in the same VM. The SQL commands are optimized for the HSQL database.
 * 
 * @author Roland Mungenast - Profactor
 */
public class DFHSQLKB extends DFDBKB {

  private static String db_driver = "org.hsqldb.jdbcDriver";
  private static String db_url    = "jdbc:hsqldb:file:dfdb";
  private static String db_user   = "sa";
  private static String db_passwd = "";
  
  protected final static String CACHE_SCALE      = "8"; 
  protected final static String CACHE_SIZE_SCALE = "8";
  protected final static String GC_INTERVAL      = "10000";
  
  
  /**
   * Constructor
   * @param maxResultLimit JADE internal limit for the number of maximum search results.
   * @param cleanTables specifies whether the KB should delete all existing tables for the DF at startup
   * @throws SQLException if a database access error occurs
   */
  public DFHSQLKB(int maxResultLimit, boolean cleanTables) throws SQLException {
    super(maxResultLimit, db_driver, db_url, db_user, db_passwd, cleanTables);
  }
  
 
  protected Connection createDBConnection(String url, String user, String passwd) throws SQLException {
    Properties props = new Properties();
    props.put("user", user);
    props.put("passwd", passwd);
    props.put("hsqldb.cache_scale", CACHE_SCALE);
    props.put("hsqldb.cache_size_scale", CACHE_SIZE_SCALE);
    props.put("hsqldb.gc_interval", GC_INTERVAL);
    
    return DriverManager.getConnection(url, props);
  }
 
  protected String getLongVarCharType() {
    return "LONGVARCHAR";
  }
  
  protected void createTable(Statement stmt, String name, String[] entries) throws SQLException {
    String sql = "CREATE CACHED TABLE " + name + " (";
    for (int i = 0; i < entries.length; i++) {
      sql += entries[i];
      if (i < entries.length - 1)
        sql += ", ";
      else
        sql += ")";
    }
    stmt.executeUpdate(sql);
    getConnectionWrapper().getConnection().commit();
  }
}
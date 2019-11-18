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

package jade.util;

//#J2ME_EXCLUDE_FILE

import jade.util.Logger;
import java.io.*;
import java.util.regex.*;

/**
 *
 *  An ACL object represents an Access Control List and 
 *  wraps a White list and a Black list, 
 *  Both lists make use of regular expressions for 
 *  allowing/denying access to a certain resource.
 * 
//#APIDOC_EXCLUDE_BEGIN
 * 
 * Example, a ACL object is initialized as follows:
 * <code>
 *     ACL myacl = new ACL();
 *     acl.setBlack( new File("black.txt") );
 *     acl.setWhite( new File("white.txt") );
 *     Properties client = new Property();
 *	   client.setProperty();
 *     boolean ok = acl.isAllowed();
 * </code>
 * 
 * and the two files look as follows: 
 * <strong>white.txt</strong>
 * <code>
 *  # this is a commant
 *  # the section 'user' (allowed usernames)
 * 	user:
 *     # all users are allowed
 *     .+?
 * 
 *  # the section 'msisdn' (allowed phone numbers)
 *  msisdn:
 * 	   # all business customers
 *     39335.+?
 * </code>
 * <strong>black.txt</strong>
 * <code>
 *  user:
 *     # specific users denied
 *     badboy
 *     goodgirl
 * 
 *  msisdn:
 *     # specific numbers denied
 *     3933555.+?
 *     39333444111
 * </code>
 * 
 * 
//#APIDOC_EXCLUDE_END
 * 
 * Into each file, multiple sections are allowed.
 *
 * A specific client is allowed if:
 * <ul><li>it is not present into the black list, AND </li>
 *     <li>it is present into the white list. </li>
 * </ul>
 *
 * More about regular expressions: <br>
 *  http://java.sun.com/j2se/1.4.2/docs/api/java/util/regex/Pattern.html
 *  http://java.sun.com/docs/books/tutorial/extra/regex/
 *
 *
 *
 * @author Giosue Vitaglione - Telecom Italia LAB
 *
 */
public class AccessControlList {


	private static Logger logger = Logger.getMyLogger(AccessControlList.class.getName());

	// constructor
	public AccessControlList(){
	} // end constructor


	// Set black and white list
	private String blackFileName=null;
	private String whiteFileName=null;
	public void setBlack( String blackFileName ){
		this.blackFileName=blackFileName;
		refresh_black();
	}
	public void setWhite( String whiteFileName ){
		this.whiteFileName=whiteFileName;
		refresh_white();
	}

	// internal representation (optimized for speed)
	private InternalACL black_iacl;
	private InternalACL white_iacl;



	/**
	 * A specific client is allowed if:
	 * <ul><li>it is not present into the black list, AND </li>
	 *     <li>it is present into the white list. </li>
	 * </ul>
	 *
	 * <code>isAllow("section", value)</code> returns is the client 
	 * having that specific value for that property is allowed.
	 *
	 */
	public boolean isAllowed(String section, String value){
		boolean retVal=false;
		if(logger.isLoggable(Logger.FINER)) {
			logger.log(Logger.FINER, 
					"Current dir: "+System.getProperty("user.dir")+"\n" +
					"\nChecking files:\n    black="+blackFileName+
					"\n    white="+whiteFileName+"\n"
			);}

		boolean isInBlack=false;
		boolean isInWhite=false;

		if (black_iacl!=null) // if loaded, check
			isInBlack = isInList( black_iacl, section, value);
		if (white_iacl!=null) 
			isInWhite = isInList( white_iacl, section, value);

		if(logger.isLoggable(Logger.FINE)) {
			logger.log(Logger.FINE, 
					" isInBlack="+isInBlack+ 
					" isInWhite="+isInWhite
			);}
		retVal = (!isInBlack) && (isInWhite);
		return retVal;
	}

	private boolean isInList( InternalACL iacl, String section, String value) {
		boolean retVal=false;
		// read the ACL file
		try {
			String currSection="";
			int pos = 0;
			while ( pos<iacl.size ) {
				pos++;

				//String p=null; if (iacl.pat[pos]!=null) p=iacl.pat[pos].pattern();
				//System.out.println( "+++ pos="+pos+"  pattern="+p+"  iacl.sectionName[pos]="+iacl.sectionName[pos] );

				// if encountered a section header
				// change the current section 
				if ( iacl.sectionName[pos]!=null) {
					currSection = iacl.sectionName[pos];
					logger.log(Logger.FINER, "Encountered section named: '"+currSection +"'");
					continue;
				}

				// is currSection the same as passed 'section' 
				// that we are searching for into?
				// If not, skip this line, looking for a new section
				if (! currSection.equals( section ) ) {
					continue;
				}

				if (iacl.pat[pos]==null) continue; // should not happen in healty acl;

				// prapare matcher (from the passed value)
				Matcher m = null;  // matcher is created from the pattern
				m = iacl.pat[pos].matcher( value );
				if(logger.isLoggable(Logger.FINER)) {
					logger.log(Logger.FINER, 
							"("+iacl.fileName+")  "+
							"  pattern="+iacl.pat[pos].pattern()+ 
							"  matcher="+value+ 
					"\n"); 
				}
				// check the matching
				boolean b = m.matches();
				if(logger.isLoggable(Logger.FINER)) 
					logger.log(Logger.FINER, "     " + value + "->" + b +"\n" );

				if (b) {
					retVal = true;
				}

			} // end while

		} catch (Throwable e) { 
			logger.log(Logger.WARNING, "Exception while checking "+iacl.fileName, e );
			retVal=false;
		}

		return retVal;
	} // end isAllowed


	/**
	 *   Update this AccessControlList object 
	 *   reading again the white and black files.
	 */
	public void refresh() {
		refresh_black();
		refresh_white();
	} // end refresh()

	private void refresh_black(){
		try {
			black_iacl = file2iacl( blackFileName );
		} catch (IOException e) { 
			logger.log(Logger.WARNING, "Exception while checking: "+blackFileName, e );
		}
	}
	private void refresh_white(){
		try {
			white_iacl = file2iacl( whiteFileName );
		} catch (IOException e) { 
			logger.log(Logger.WARNING, "Exception while checking: "+whiteFileName, e );
		}
	}



	private InternalACL file2iacl(String fileName) throws IOException {

		InternalACL iacl;

		// count number of lines 
		BufferedReader in = new BufferedReader(new FileReader(fileName));
		int n=0;
		while (( in.readLine()) != null) { n++; }
		in.close();
		// create properly dimensioned arrays
		iacl = new InternalACL( n+1 );

		iacl.fileName = fileName; // just used for debugging

		logger.log(Logger.FINER, "Opening acc.control list file:"+fileName+" ("+n+" lines)");
		in = new BufferedReader(new FileReader(fileName));
		String str;
		int pos=0;
		while ((str = in.readLine()) != null) {
			pos++;
			str=str.trim();
			if (str.startsWith("#")) {
				continue;
			}
			// if encountered a section header
			if (str.endsWith(":")) {
				iacl.sectionName[ pos ] = str.substring(0, str.length()-1);
				continue;
			}

			// prepare pattern (from the pattern at this line)
			try {
				iacl.pat[ pos ] = Pattern.compile(str);
			} catch (PatternSyntaxException pse) {
				logger.log(Logger.WARNING, "Error in expression acc.control list file:"+fileName+" (line:"+pos+")");
			}
		}
		in.close();

		return iacl;
	}// end file2iacl


	// internal representation of a acl file
	private class InternalACL {
		public Pattern[] pat; // compiled pattern at line i-th
		public String[] sectionName;  // name of the section header at line i-th
		public int size;
		public String fileName;
		public InternalACL(int dim){
			size=dim;
			pat=new Pattern[ dim+1 ];
			sectionName=new String[ dim+1 ];
		}
	}


	/*
	 *
	 * The following code is only for class-level testing.
	 *

public static void main(String args[]){
	createTestACL();
	AccessControlList acl = new AccessControlList();
	acl.setBlack( blackFilename );
	acl.setWhite( whiteFilename );
	acl.setLogLevel( Level.FINE );

	testAndPrint( acl, "user",     "goodboy"         , true);
	testAndPrint( acl, "user",     "sfogliatella9814", false);
	testAndPrint( acl, "section2", "forbiddenvalue"  , false);
	testAndPrint( acl, "section2", "goodvalue"       , true);
	testAndPrint( acl, "sectionX", "anyvalue"        , false);

}

private static void testAndPrint(AccessControlList acl, String section, String value, boolean expected){
	boolean ok = acl.isAllowed( section, value );
	System.out.println( 
		"section="+section+"  value="+value
		+"  ok="+(ok+" ").toUpperCase()
		+""+( (ok==expected) ? ":-)": "!!!  [:-( expected="+expected+" !!!")
		+"\n\n");
}

private static final String blackFilename="black.txt";
private static final String whiteFilename="white.txt";
private static void createTestACL(){
try {
 PrintWriter out;

 out = new PrintWriter(new BufferedWriter(new FileWriter(blackFilename)));
 out.println(
	"\n"
	+"user:\n"
	+"	sfogliatella.+?  \n"
	+"section2:\n"
	+"	forbiddenvalue.*  \n"
	+"section3:\n"
	+"	.+?  \n"
 );
 out.flush(); out.close();
 out = new PrintWriter(new BufferedWriter(new FileWriter(whiteFilename)));
 out.println(
	"\n"
	+"user:\n"
	+"	.+?  \n"
	+"section2:\n"
	+"	good.+?  \n"
	+"section3:\n"
	+"	.+?  \n"
 );
 out.flush(); out.close();

} catch (Exception e) { e.printStackTrace();}
}

private void setLogLevel(Level lev){
	logger.setLevel( lev );
	logger.getParent().setLevel( lev );

	//Set level for handlers associated to logger
	Handler[] pHandlers = logger.getParent().getHandlers();
	Handler[] handlers = logger.getHandlers();
	for (int i=0; i<pHandlers.length; i++){
		pHandlers[i].setLevel(lev);
	}
	for (int j=0; j<handlers.length; j++){
		handlers[j].setLevel(lev);
	}
}


	 */


}// end class
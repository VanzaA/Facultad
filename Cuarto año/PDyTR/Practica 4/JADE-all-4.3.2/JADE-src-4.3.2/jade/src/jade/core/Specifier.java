/**
 * ***************************************************************
 * JADE - Java Agent DEvelopment Framework is a framework to develop
 * multi-agent systems in compliance with the FIPA specifications.
 * Copyright (C) 2000 CSELT S.p.A.
 * GNU Lesser General Public License
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation,
 * version 2.1 of the License.
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the
 * Free Software Foundation, Inc., 59 Temple Place - Suite 330,
 * Boston, MA  02111-1307, USA.
 * **************************************************************
 */
package jade.core;

import java.util.Enumeration;
import java.util.Vector;

/**
 * This class represent a specifier and collects a name, a className,
 * and an array of arguments. Profile specifiers are used to describe
 * several kinds of complex information when configuring JADE
 * (e.g. MTPs to install, agents to start up, kernel services to
 * activate).
 *
 * The general string format for a specifier is
 * <p><code>name<b>:</b>className<b>(</b><i>separated arglist</i><b>)</b></code></p>
 * The separated argument list uses a separator character that can be configured when
 * invoking parsing-related utility methods. 
 *
 * @author LEAP
 */
public class Specifier {
	public static final char SPECIFIER_SEPARATOR = ';';
	public static final String NULL_SPECIFIER_LIST = "null";

	private String   name = null;
	private String   className = null;
	private Object[] args = null;

	/**
       Set the name for this specifier object.
       @param n The name to give to this specifier.
	 */
	public void setName(String n) {
		name = n;
	} 

	/**
       Retrieve the name for this specifier object.
       @return The name of the specifier, if one was set, or
       <code>null</code> otherwise.
	 */
	public String getName() {
		return name;
	} 

	/**
       Set the name of the class of this specifier.
       @param cn The class name to assign to the specifier object.
	 */
	public void setClassName(String cn) {
		className = cn;
	} 

	/**
       Retrieve the class name of this specifier.
       @return The class name of the specifier, if one was set, or
       <code>null</code> otherwise.
	 */
	public String getClassName() {
		return className;
	} 

	/**
       Set the argument list for this specifier object.
       @param a An object array containing the argument list for this
       specifier.
	 */
	public void setArgs(Object[] a) {
		args = a;
	} 

	/**
       Retrieve the argument list for this specifier.
       @return An object array containing the argument list, if one
       was set, or <code>null</code> otherwise.
	 */
	public Object[] getArgs() {
		return args;
	} 

	/**
	 * This method is used by Boot, ProfileImpl, and RMA in order
	 * to have a String representation of this Specifier according to the
	 * format <code>name:className(arg1 arg2 argn)</code>
	 *
	 * @return A string representation of this specifier, according to
	 * the format above.
	 **/
	public String toString() {
		// TAKE CARE: do not change this method otherwise Boot might fail
		StringBuffer tmp = new StringBuffer();
		if (name != null) {
			tmp.append(name);
			tmp.append(":");
		}
		if (className != null) {
			tmp.append(className);
		}
		if (args != null) {
			tmp.append("(");
			for (int i=0; i<args.length; i++) {
				tmp.append(args[i]);
				if (i<args.length-1) {
					//#ALL_EXCLUDE_BEGIN
					tmp.append(" ");
					//#ALL_EXCLUDE_END
					/*#ALL_INCLUDE_BEGIN
					tmp.append(",");
					//#ALL_INCLUDE_END*/
				}
			}
			tmp.append(")");
		}
		return tmp.toString();
	}

	/**
     This static utility method can parse the string representation of
     a list of specifiers. The general format of a specifier is used,
     with a comma as argument separator, i.e.:
     <p><code>name<b>:</b>className<b>(</b><i>comma-separated arglist</i><b>)</b></code></p>
     While comma is the separator character within a specifier
     arguments, the semicolon is used to separate the different
     specifiers in the list.

     @param specsLine The string containing the representation of the
     specifier list, according to the format above.
     @return A vector containing the parsed specifiers.
	 */
	public static Vector parseSpecifierList(String specsLine) throws Exception {
		Vector specs = parseList(specsLine, SPECIFIER_SEPARATOR);
		for (int i = 0; i < specs.size(); ++i) {
			String s = (String) specs.elementAt(i);
			if (s.length() > 0) {
				specs.setElementAt(parseSpecifier(s, ','), i);
			}
			else {
				specs.removeElementAt(i--);
			}
		}
		return specs;
	} 

	/**
	 * This static utility method produces a string representation of a list of Specifier objects.
	 */
	public static String encodeSpecifierList(Vector v){
		return encodeList(v, SPECIFIER_SEPARATOR);
	}

	public static final Vector parseList(String list, char delimiter) {
		Vector v = new Vector();

		if (list != null && !list.equals("") && !list.equals(NULL_SPECIFIER_LIST)) {
			// Copy the string with the specifiers into an array of char
			char[] specsChars = new char[list.length()];

			list.getChars(0, list.length(), specsChars, 0);

			// Create the StringBuffer to hold the first element
			StringBuffer sbElement = new StringBuffer();
			int          i = 0;

			while (i < specsChars.length) {
				char c = specsChars[i];

				if (c != delimiter) {
					sbElement.append(c);
				} 
				else {
					// The element is terminated.
					v.addElement(sbElement.toString().trim());
					// Create the StringBuffer to hold the next element
					sbElement = new StringBuffer();
				} 
				++i;
			} 

			// Append the last element
			v.addElement(sbElement.toString().trim());
		}
		return v;
	}

	public static String encodeList(Vector v, char delimiter){
		StringBuffer sb = new StringBuffer();
		Enumeration elements = v.elements();
		while(elements.hasMoreElements()){
			sb.append(elements.nextElement());
			if(elements.hasMoreElements()){
				sb.append(delimiter);
			}
		}
		return sb.toString();
	}

	/**
	 * Utility method that parses a stringified object specifier in the form
	 * <p><code>name<b>:</b>className<b>(</b><i>separated arglist</i><b>)</b></code></p>
	 * a Specifier object.
	 * Both the name and the list of arguments are optional.
	 * 
	 * @param specString A string containing the representation of the
	 * specifier, according to the format above.
	 * @param argsDelimiter The character to use as a delimiter within the argument list.
	 * @return A specifier object, built according to the parsed information.
	 */
	public static Specifier parseSpecifier(String specString, char argsDelimiter) throws Exception {
		Specifier s = new Specifier();

		// NAME
		int       index1 = specString.indexOf(':');
		int       index2 = specString.indexOf('(');

		if (index2 < 0) {
			index2 = 99999;
		} 

		if (index1 > 0 && index1 < index2) {

			// The name exists, colon exists, and is followed by the class name
			s.setName(specString.substring(0, index1));

			// Skip colon
			index1++;
		} 
		else {

			// No name specified
			index1 = 0;
		} 

		// CLASS
		index2 = specString.indexOf('(', index1);

		if (index2 < 0) {

			// No arguments --> just add the class name
			s.setClassName(specString.substring(index1));
		} 
		else {

			// There are arguments --> add the class name and then parse the args
			s.setClassName(specString.substring(index1, index2));

			// ARGUMENTS
			if (!specString.endsWith(")")) {
				throw new Exception("Incorrect specifier \""+specString+"\". Missing final parenthesis");
			} 

			// Get everything is in between '(' and ')'
			String args = specString.substring(index2+1, specString.length()-1);

			s.setArgs(parseArguments(args, argsDelimiter));
		} 

		return s;
	} 

	/**
	 */
	private static String[] parseArguments(String args, char argsDelimiter) {
		Vector argList = new Vector();
		int  argStart = 0;
		int  argEnd = args.indexOf(argsDelimiter);

		while (argEnd >= 0) {
			String arg = args.substring(argStart, argEnd);

			argList.addElement(arg.trim());

			argStart = argEnd+1;
			argEnd = args.indexOf(argsDelimiter, argStart);
		} 

		// Last argument
		String arg = args.substring(argStart, args.length());

		argList.addElement(arg.trim());

		// Convert the List into an Array
		String arguments[] = new String[argList.size()];
		int    i = 0;

		for (Enumeration e = argList.elements(); e.hasMoreElements(); arguments[i++] = (String) e.nextElement());

		return arguments;
	} 


}


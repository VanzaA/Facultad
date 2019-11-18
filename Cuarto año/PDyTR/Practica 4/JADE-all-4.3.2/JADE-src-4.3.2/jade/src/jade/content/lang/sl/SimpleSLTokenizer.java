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

package jade.content.lang.sl;

//#APIDOC_EXCLUDE_FILE

import jade.content.lang.Codec;

/**
   Simple utility class for hand-made SL parsing
   @author Giovanni Caire - TILAB
 */
public class SimpleSLTokenizer {
	private static final String msg = "Parse error: unexpected end of content at #";
	private String content;
	private int current = 0;

	/**
	   Construct a SimpleSLTokenizer that will act on the given String
	 */
	public SimpleSLTokenizer(String s) {
		content = s;
	}

	/**
	   Return the next SL token (i.e. '(', ')' or a generic element)
	   without advancing the pointer
	 */
	public String nextToken() throws Codec.CodecException {
		try {
			skipSpaces();
			String token = null;
			char c = content.charAt(current);
			if (c == ')' || c == '(') {
				token = String.valueOf(c);
			}
			else {
				int start = current;
				token = getElement(false);
				current = start;
			}
			return token;
		}
		catch (IndexOutOfBoundsException ioobe) {
			throw new Codec.CodecException(msg+current);
		}
	}
	
	// We need a dedicated method since just doing nextToken().equals("(") would fail in case 
	// there is a slot whose value is exactly "("
	public boolean isOpenBracket() throws Codec.CodecException {
		try {
			skipSpaces();
			char c = content.charAt(current);
			if (c == '(') {
				return true;
			}
			return false;
		}
		catch (IndexOutOfBoundsException ioobe) {
			throw new Codec.CodecException(msg+current);
		}
	}

	// We need a dedicated method since just doing nextToken().equals(")") would fail in case 
	// there is a slot whose value is exactly ")"
	public boolean isClosedBracket() throws Codec.CodecException {
		try {
			skipSpaces();
			char c = content.charAt(current);
			if (c == ')') {
				return true;
			}
			return false;
		}
		catch (IndexOutOfBoundsException ioobe) {
			throw new Codec.CodecException(msg+current);
		}
	}
	
	/**
	   Check that the next character (after eventual spaces) is
	   'c' and advance the pointer to the character just after
	 */
	public void consumeChar(char c) throws Codec.CodecException {
		try {
			skipSpaces();
			if (content.charAt(current++) != c) {
				throw new Codec.CodecException("Parse error: position "+(current-1)+", found "+content.charAt(current-1)+" while "+c+" was expected ["+content.substring(0, current)+"]");
			}
		}
		catch (IndexOutOfBoundsException ioobe) {
			throw new Codec.CodecException(msg+current);
		}
	}

	/**
	   Return the next SL element (i.e. a word or a generic sequence 
	   of char enclosed into "") and advance the pointer to the character
	   just after.
	   If the element starts with ':' this is automatically removed.
	   If the element is a sequence of char enclosed into "" the enclosing
	   " are removed and all \" are automatically transformed into "
	 */
	public String getElement() throws Codec.CodecException {
		return getElement(true);
	}

	private String getElement(boolean removeColon) throws Codec.CodecException {
		try {
			String el = null;
			skipSpaces();
			if (content.charAt(current) == '"') {
				current++;
				StringBuffer sb = new StringBuffer();
				while (content.charAt(current) != '"') {
					if (content.charAt(current) == '\\' && content.charAt(current+1) == '\"') {
						current++;
					}
					sb.append(content.charAt(current));
					current++;
				}
				current++;
				el = sb.toString();
			}
			else {
				el = getWord(removeColon);
			}
			return el;
		}
		catch (IndexOutOfBoundsException ioobe) {
			throw new Codec.CodecException(msg+current);
		}
	}

	private String getWord(boolean removeColon) {
		skipSpaces();
		int start = current;
		char c = content.charAt(current);
		// Automatically remove ':' in case this is a slot name.
		// Note that in SL slot values cannot start with ':'
		if (removeColon && c == ':') {
			start++;
		}
		while (!isSpace(c) && c != ')' && c != '(') {
			c = content.charAt(++current);
		}
		String s = content.substring(start, current);
		return s;
	}

	private void skipSpaces() {
		while (isSpace(content.charAt(current))) {
			current++;
		}
	}

	private boolean isSpace(char c) {
		return (c == ' ' || c == '\t' || c == '\n');
	}

	private static final String illegalFirstChar = "#0123456789:-?";
	//FIXME We might improve performance if we merged isAWord and quoteString into a single method
	// infact they both have to loop over the chars of the String.
	/**
	 * Test if the given string is a legal SL word using the FIPA XC00008D spec.
	 * In addition to FIPA's restrictions, place the additional restriction 
	 * that a Word can not contain a '\"', that would confuse the parser at
	 * the other end.
	 */
	public final static boolean isAWord( String s) {
		// This should permit strings of length 0 to be encoded.
		if( s==null || s.length()==0 ) {
			return false; // words must have at least one character
		}

		if ( illegalFirstChar.indexOf(s.charAt(0)) >= 0 ) {
			return false;
		}
		for( int i=0; i< s.length(); i++) {
			char c = s.charAt(i);
			if(c == '"' || c == '(' || c == ')' || c <= 0x20 || c >= 0x80 ) {
				return false;
			}
		}		
		return true;
	}

	/** 
	 * Take a java String and quote it to form a legal FIPA SL0 string.
	 * Add quotation marks to the beginning/end and escape any 
	 * quotation marks inside the string.
	 */
	public static String quoteString(String s) {
		// Make the stringBuffer a little larger than strictly
		// necessary in case we need to insert any additional
		// characters.  (If our size estimate is wrong, the
		// StringBuffer will automatically grow as needed).
		StringBuffer result = new StringBuffer(s.length()+20);
		result.append("\"");
		for( int i=0; i<s.length(); i++)
			if( s.charAt(i) == '"' ) 
				result.append("\\\"");
			else 
				result.append(s.charAt(i));
		result.append("\"");
		return result.toString();
	}
}

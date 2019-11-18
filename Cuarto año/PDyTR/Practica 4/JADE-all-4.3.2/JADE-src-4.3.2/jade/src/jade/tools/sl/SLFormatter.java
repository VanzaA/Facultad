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

/*

Contributed to JADE under GPL.

*/
package jade.tools.sl;

import java.io.*;

/**
 * Holder for static methods to format messages.
 * 
 *
 * @author Craig Sayers, Hewlett Packard Labs, Palo Alto.
 * @version 1.0
 */
public class SLFormatter {
    static String spacing = "\n                                                                   ";
    private static final int INDENT_EXPRESSION = 2;
    private static final int INDENT_ARGUMENT = 2;
    private static final int SHORT_EXPRESSION_LENGTH = 40;
    /**
     * Private class to iterate over a string.
     * This has a next() and hasNext() inspired by the standard Iterator 
     * interface.
     * (It might appear that java.text.StringCharacterIterator would
     * work, but its implementation of next() doesn't match the
     * standard iterator - it returns the value after incrementing
     * the index, and so always skips the first character).
     */ 
    private static class myStringIterator 
    { 
        public String string; 
        public int index; 
        myStringIterator(String s) {string = s; index=0; }
        char next() { return string.charAt(index++); }
        boolean hasNext() { return index < string.length();}
    }

    /**
     * Formats an ACL/SL string.
     * This takes an input string, formats it (adding new-lines 
     * and spaces in an attempt to improve readability), and returns 
     * the result.  
     * The formatted output is similar to that used for the examples
     * in the FIPA SL spec.  The formatter attempts to keep short
     * expressions on one line, while indenting longer expressions to
     * show the structure.  As an example, the result of:
     * <pre>
     *    System.out.println(SLFormatter.format("(vehicle :color red :max-speed 100 :owner (Person :name Luis :nationality Potuguese))"));
     * </pre>
     * is the output
     * <pre>
     *  (vehicle
     *    :color red
     *    :max-speed 100
     *    :owner
     *      (Person
     *        :name Luis
     *        :nationality Portuguese))
     * </pre>
     * Usually, the input string will come from a
     * call to ACLMessage.getContent(), for example if you have
     * an ACLMessage called msg, you can print formatted content:
     * <pre>
     *    System.out.println(SLFormatter.format(msg.getContent());
     * </pre>
     * Since text ACL messages follow an SL style, you can also
     * use this function to format entire messages by formatting
     * the return value from msg.toString().
     *
     * @param s ACL/SL string to be formatted
     *
     * @return formatted result string
     *
     */
    
    public static String format(String s) {
        return format(new myStringIterator(s),0);
    }

    private static String format(myStringIterator src, int indentation)
    {
        StringBuffer result = new StringBuffer();
        char current, previous=0;
        int indentStep=INDENT_EXPRESSION;
        boolean insideQuote=false;
        while( src.hasNext() )
        {
            current = src.next();
            if( insideQuote )
                result.append(current);
            else
                switch(current)
                {
                    case ':':
                        if( previous == ' ' || previous == '\n' || previous == '\r')
                        {
                            indentStep=INDENT_EXPRESSION+INDENT_ARGUMENT;
                            result.append(spacing.substring(0, indentation+INDENT_EXPRESSION));
                        }
                        result.append(current);
                        break;
                    case '(':
                        String expression = format(src, indentation+indentStep).trim();
                        String indentSpacing = "";
                        if( expression.length() > SHORT_EXPRESSION_LENGTH )
                            indentSpacing = spacing.substring(0, indentation+indentStep);
                        result.append(indentSpacing+'('+expression+')');
                        break;
                    case ')':
                        return result.toString();
                    case '\n':
                    case '\r':
                        break;
                    default:
                        result.append(current);
                        break;
                }
            if( current == '\"' && previous != '\\')
                insideQuote = !insideQuote;
            previous=current;
        }
        return result.toString();
    }

    /**
     * Main routine is helpful for debugging.  It asks
     * for an SL expression and then prints the result of calling
     * SLFormatter.format.
     * @author Fabio Bellifemine
     */
    public static void main(String[] args) {
	try {
	    BufferedReader buff = new BufferedReader(new InputStreamReader(System.in));
	    while (true) {
		System.out.println("\ninsert an SL0 expression to format: ");
		String inp = buff.readLine();
		System.out.println(format(inp));
	    }
	} catch (IOException e) {
	    e.printStackTrace();
	}
    }

    /**
       Default constructor.
    */
    public SLFormatter() {
    }

}

/**
 * 
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
 * 
 */
package jade;

//#APIDOC_EXCLUDE_FILE
//#J2ME_EXCLUDE_FILE


import java.util.Enumeration;    // J2ME CLDC OK
import java.util.Vector;         // J2ME CLDC OK
import java.util.Stack;          // J2ME CLDC OK
import jade.core.Specifier;
import jade.util.leap.List;
import jade.util.leap.ArrayList;

/**
 * This class provides a number of methods that help in parsing
 * the command line. It is also used by the StartNewAgent class of the RMA.
 * @version $Date: 2010-04-08 15:54:18 +0200 (gio, 08 apr 2010) $ $Revision: 6298 $
 **/
public class BootHelper {


    /** These constants are used by the mini-parser implemented by the method T2 */
    private static final int BETWEENTOKENS = 0;
    private static final int WORDTOKEN = 1;
    private static final int STRINGTOKEN = 2;
    private static final int ESCAPE = 3;

    /**
     * Reads the passed String and decompose it into a vector of Strings.
     * Each String is a token of type agentName: or className( or argument
     * This method is declared public because it is currently used by
     * <code>jade.tools.rma.StartNewAgentAction</code>
     *
     * @param s1 The source string.
     * @param keepquote when true no char is removed,
     * apart from blanks between tokens.
     * when false, quote chars are removed and also escaped quotes become
     * just quotes.
     *
     * @return The vector of strings.
     */
    public Vector T2(String s1, boolean keepquote) {
        Vector l = new Vector();
        if (s1 == null) {
            return l;
        }
        int state = BETWEENTOKENS;
        Stack returnState = new Stack();
        StringBuffer token = new StringBuffer();
        int i;

        for (i = 0; i < s1.length(); i++) {
            char ch = s1.charAt(i);

            switch (state) {
            case BETWEENTOKENS :
                if (ch != ' ') {
                    if (ch == '"') {
                        state = STRINGTOKEN;

                        if (keepquote) {
                            token.append(ch);
                        }

                        returnState.push(new Integer(BETWEENTOKENS));
                    } else if (ch == '\\') {
                        state = ESCAPE;

                        returnState.push(new Integer(BETWEENTOKENS));
                    } else {
                        token.append(ch);

                        state = WORDTOKEN;
                    }
                } else if (token.length() > 0) {
                    l.addElement(token.toString());

                    token = new StringBuffer();
                }
                break;

            case WORDTOKEN :
                if (ch == ' ') {
                    state = BETWEENTOKENS;

                    l.addElement(token.toString());

                    token = new StringBuffer();
                } else if (ch == '"') {
                    state = STRINGTOKEN;

                    if (keepquote) {
                        token.append(ch);
                    }

                    returnState.push(new Integer(WORDTOKEN));
                } else if (ch == '\\') {
                    state = ESCAPE;

                    returnState.push(new Integer(WORDTOKEN));
                } else {
                    token.append(ch);
                }
                break;

            case STRINGTOKEN :
                if (ch == '"') {
                    if (keepquote) {
                        token.append(ch);
                    }

                    state = ((Integer) returnState.pop()).intValue();
                } else if (ch == '\\') {
                    state = ESCAPE;

                    returnState.push(new Integer(STRINGTOKEN));
                } else {
                    token.append(ch);
                }
                break;

            case ESCAPE :
                if ((ch != '"') || (keepquote)) {
                    token.append('\\');
                }

                token.append(ch);

                state = ((Integer) returnState.pop()).intValue();
                break;
            }
        }

        if (token.length() > 0) {
            l.addElement(token.toString());
        }

        return l;
    }

    /**
     * parse an array of Strings and return an Iterator of
     * <code>Specifier</code>
     * This method is declared public because it is currently used by
     * <code>jade.tools.rma.StartNewAgentAction</code>
     * @param args is an array of string of agent specifiers of the
     * type "name:class(arguments)"
     * @return an Iterator over a List of <code>Specifier</code>
     */
    public Enumeration getCommandLineAgentSpecifiers(Vector args) {
        Vector all = new Vector();
        int i = 0;

        while (i < args.size()) {    //1
            String cur = (String) args.elementAt(i);

            // search for the agent name
            int index1 = cur.indexOf(':');

            if ((index1 > 0) && (index1 < (cur.length() - 1))) {    //2

                // in every cycle we generate a new object Specifier
                Specifier as = new Specifier();

                as.setName(cur.substring(0, index1));

                // set the agent class
                int index2 = cur.indexOf('(', index1);

                if (index2 < 0) {

                    // no arguments to this agent
                    as.setClassName(cur.substring(index1 + 1, cur.length()));
                } else {    //3
                    as.setClassName(cur.substring(index1 + 1, index2));

                    // having removed agentName,':',agentClass, and '(', 
                    // what remains is the firstArgument of the Agent 
                    // search for all the arguments up to the closed parenthesis
                    List asArgs = new ArrayList();
                    String nextArg =
                        cur.substring(index2 + 1);    //from the '(' to the end

                    while (!getAgentArgument(nextArg, asArgs)) {    //4
                        i++;

                        if (i >= args.size()) {    //5
                            System.err.println(
                                "Missing closed bracket to delimit agent arguments. The system cannot be launched");

                            //FIXME printUsageInfo = true;
                        }    //5

                        nextArg = (String) args.elementAt(i);
                    }    // 4 

                    Object agentArgs[] = new Object[asArgs.size()];

                    for (int i3 = 0; i3 < asArgs.size(); i3++) {
                        agentArgs[i3] = (String) asArgs.get(i3);
                    }

                    as.setArgs(agentArgs);
                }    // 3

                all.addElement(as);
            }    //2 

            i++;
        }    //1

        return all.elements();
    }    // 0 

    /**
     *  @param arg is the argument passed on the command line
     *  @param as is the List where arguments must be added
     *  @return true if this was the last argument, i.e. it found a closed bracket
     */
    public boolean getAgentArgument(String arg, List as) {
        boolean isLastArg = false;
        String realArg;

        // if the last char is a closed parenthesis, then this is the last argument
        if (arg.endsWith(")")) {
            if (arg.endsWith("\\)")) {
                isLastArg = false;
                realArg = arg;
            } else {
                isLastArg = true;
                realArg = arg.substring(0, arg.length()
                                        - 1);    //remove last parenthesis
            }
        } else {
            isLastArg = false;
            realArg = arg;
        }

        if (realArg.length() > 0) {

            // replace the escaped closed parenthesis with a simple parenthesis
            as.add(replace(realArg, "\\)", ")"));
        }

        return isLastArg;
    }

    private String replace(String arg, String oldStr, String newStr) {
        int index = arg.indexOf(oldStr);
        String tmp = arg;

        while (index >= 0) {
            tmp = tmp.substring(0, index) + newStr
                  + tmp.substring(index + oldStr.length());
            index = tmp.indexOf(oldStr);
        }

        return tmp;
    }


}

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

import java.util.Properties;

public class Toolkit {

	private static final String DELIM_START =  "{";
	private static final String DELIM_STOP = "}";
	
	public static String substituteParameters(String expression, Properties parameters) {
		return substituteParameters(expression, parameters, DELIM_START, DELIM_STOP);
	}
	
	public static String substituteParameters(String expression, Properties parameters, String startDelim, String stopDelim) {
		if (expression == null || parameters == null || parameters.isEmpty()) {
			return expression;
		}
		
		// Expression is != null and parameters is NOT empty --> Do the substitution
		StringBuffer sbuf = new StringBuffer();
		int position = 0;
		while (true) {
			int paramStart = expression.indexOf(startDelim, position);
			if (paramStart == -1) {
				// No more parameters
				if (position == 0) { 
					// The expression does not contain any parameter --> return it as it is
					return expression;
				} 
				else { 
					// Add the tail string which contains no parameters and return the result.
					sbuf.append(expression.substring(position, expression.length()));
					return sbuf.toString();
				}
			} 
			else {
				// Parameter found. Append from current position to the char before the param
				sbuf.append(expression.substring(position, paramStart));
				// Then manage param substitution
				int paramEnd = expression.indexOf(stopDelim, paramStart);
				if (paramEnd == -1) {
					throw new IllegalArgumentException('"' + expression + "\" has no closing brace. Opening brace at position " + paramStart + '.');
				} 
				else {
					String key = expression.substring(paramStart + startDelim.length(), paramEnd);
					String replacement = parameters.getProperty(key, null);
					if (replacement != null) {
						// Do parameter substitution on the replacement string
						// such that we can solve "Hello {x2}" as "Hello p1" also where 
						// x2={x1}
						// x1=p1
						String recursiveReplacement = substituteParameters(replacement, parameters, startDelim, stopDelim);
						sbuf.append(recursiveReplacement);
					}
					position = paramEnd + stopDelim.length();
				}
			}
		}
	}
}

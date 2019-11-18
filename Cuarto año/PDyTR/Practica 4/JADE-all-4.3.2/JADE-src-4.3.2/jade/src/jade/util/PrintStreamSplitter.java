/**
 * JADE - Java Agent DEvelopment Framework is a framework to develop
 * multi-agent systems in compliance with the FIPA specifications.
 * Copyright (C) 2000 CSELT S.p.A. 
 * Copyright (C) 2001,2002 TILab S.p.A. 
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

package jade.util;
 
//#J2ME_EXCLUDE_FILE
//#APIDOC_EXCLUDE_FILE

import java.io.*;

/**
   This class is an OutputStream whose output is duplicated and
   forwarded to two different output streams.
   @author Giovanni Caire - TILab 
 */
public class PrintStreamSplitter extends PrintStream {
	private PrintStream s1, s2;
	
    /**
       Create a new stream that feeds the output to the two given
       streams.
       @param s1 The first stream to write to.
       @param s2 The second stream to write to.
       @throws IOException If some stream operation fails.
    */
	public PrintStreamSplitter(PrintStream s1, PrintStream s2) throws IOException {
		super(new OutputStream() {
			public void write(int b) throws IOException {
			}
		} );

		this.s1 = s1;
		this.s2 = s2;
	}

    /**
       Print a Java object to the two streams.
       @param obj The Java object to print.
    */	
	public void print(Object obj) {
		s1.print(obj);
		s2.print(obj);
	}

    /**
       Print a string to the two streams.
       @param str The string to print.
    */	
	public void print(String str) {
		s1.print(str);
		s2.print(str);
	}

    /**
       Print a new line to the two streams.
    */	
	public void println() {
		s1.println();
		s2.println();
	}

    /**
       Print a Java object and a new line to the two streams.
       @param obj The Java object to print.
    */	
	public void println(Object obj) {
		s1.println(obj);
		s2.println(obj);
	}

    /**
       Print a string and a new line to the two streams.
       @param str The string to print.
    */	
	public void println(String str) {
		s1.println(str);
		s2.println(str);
	}
}

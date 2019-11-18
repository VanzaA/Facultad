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
import java.text.*;
import java.util.Date;

/**
   This class is an OutputStream whose output is stored in 
   in different files according to the day it is produced.
	 Existing files, if any, are not rewritten as output is appended
	 at the end of the file. 
   @author Giovanni Caire - TILab 
 */
public class PerDayFileLogger extends PrintStream {
  private final static long DAY = 24*60*60*1000;
  
	private String file;
	private long dayCnt;
	private static DateFormat df = new SimpleDateFormat("yyyyMMdd");

    /**
       Create a new day-based logger.
       @param file The name of the file to write logs to.
       @throws IOException If some filesystem operation fails.
    */
	public PerDayFileLogger(String file) throws IOException {
		super(new FileOutputStream(file+"."+df.format(new Date()), true), true);
		
		this.file = file;
		dayCnt = System.currentTimeMillis() / DAY;
	}
	
	private void checkDate() {
		long n = System.currentTimeMillis() / DAY;
		if (n > dayCnt) {
			dayCnt = n;
			try {
				out.close();
				out = new FileOutputStream(file+"."+df.format(new Date()), true);
			}
			catch (Exception e) {}
		}
	}

    /**
       Print a Java object to the proper file, replacing the file
       every new day.
       @param obj The Java object to print.
    */	
	public void print(Object obj) {
		checkDate();
		super.print(obj);
	}

    /**
       Print a string to the proper file, replacing the file every new
       day.
       @param str The string to print.
    */	
	public void print(String str) {
		checkDate();
		super.print(str);
	}
	
    /**
       Print a new line to the proper file, replacing the file every
       new day.
    */	
	public void println() {
		checkDate();
		super.println();
	}
	
    /**
       Print a Java object and a newline to the proper file, replacing
       the file every new day.
       @param obj The Java object to print.
    */	
	public void println(Object obj) {
		checkDate();
		super.println(obj);
	}
	
    /**
       Print a string and a new line to the proper file, replacing the
       file every new day.
       @param str THe string to print.
    */	
	public void println(String str) {
		checkDate();
		super.println(str);
	}
}

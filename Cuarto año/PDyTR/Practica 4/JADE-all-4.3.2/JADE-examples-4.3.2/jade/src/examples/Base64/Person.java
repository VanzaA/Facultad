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

package examples.Base64;

import java.io.*;
import java.util.Date;

/**
This is  just a support class for the object reader and writer agents.
Refer to them for any documentation.
@author Fabio Bellifemine - CSELT S.p.A
@version $Date: 2001-09-17 19:22:31 +0200 (lun, 17 set 2001) $ $Revision: 2685 $
*/

public class Person implements Serializable {

String name;
String surname;
Date   birthdate;
int    age;

  Person(String n, String s, Date d, int a) {
    name = n;
    surname = s;
    birthdate = d;
    age = a;
  }

  public String toString() {
    return(name+ " "+ surname +" born on "+birthdate.toString()+" age = "+age);
  }
  

}

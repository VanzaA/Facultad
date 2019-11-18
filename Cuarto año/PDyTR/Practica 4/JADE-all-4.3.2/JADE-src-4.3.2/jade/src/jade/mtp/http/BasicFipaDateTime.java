/**
 * Java Agent Message Router - JAMR ( http://liawww.epfl.ch/~cion/jamr ) 
 * FIPA compliant Message Transport Implementation
 *
 * Copyright (C) 2000, 2001, Laboratoire d'Intelligence Artificielle,
 * Echole Polytechnique Federale de Lausanne ( http://liawww.epfl.ch )
 *
 * This library is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published by
 * the Free Software foundation
 *
 *
 * This library is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Lesser
 * General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this library (file lesser.txt); if not, try downloading it
 * from http://www.gnu.org/copyleft/lesser.txt or write to the Free
 * Software Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA
 * 02111-1307  USA
 *
 *
 * BasicFipaDateTime.java
 *
 *
 * Created: Mon Aug 28 22:41:41 2000
 *
 * @author Ion Constantinescu ( ion.constantinescu@epfl.ch )
 * @version 0.72
 * @author Nicolas Lhuillier (Motorola Labs)
 * Corrected bug with Java Calendar class
 * @version 1.0
 */

package jade.mtp.http;

import java.util.*;
import java.text.*;

public class BasicFipaDateTime {
    
  
  Calendar cal = Calendar.getInstance();
  short year;
  short month;
  short day;
  short hour;
  short minutes;
  short seconds;
  short milliseconds;
  char typeDesignator='Z';
  
  public BasicFipaDateTime(Date date) {
    cal.setTime(date);
  }
  
  public BasicFipaDateTime(String str) {
    fromString(str);
  }
  
  /**
   * Get the value of year.
   * @return Value of year.
   */
  public short getYear() {
    return (short) cal.get(Calendar.YEAR);
  }
    
  /**
   * Set the value of year.
   * @param v  Value to assign to year.
   */
  public void setYear(short v) {
    cal.set(Calendar.YEAR,v);
  }
      
  /**
   * Get the value of month.
   * @return Value of month.
   */
  public short getMonth() {
    return (short)(cal.get(Calendar.MONTH)+1);
  }
  
  /**
   * Set the value of month.
   * @param v  Value to assign to month.
   */
  public void setMonth(short v) {
    cal.set(Calendar.MONTH,v-1);
  }
      
  /**
   * Get the value of day.
   * @return Value of day.
   */
  public short getDay() {
    return (short)cal.get(Calendar.DAY_OF_MONTH);
  }
    
  /**
   * Set the value of day.
   * @param v  Value to assign to day.
   */
  public void setDay(short  v) {
    cal.set(Calendar.DAY_OF_MONTH,v);
  }
     
  /**
   * Get the value of hour.
   * @return Value of hour.
   */
  public short getHour() {
    return (short)cal.get(Calendar.HOUR_OF_DAY);
  }
    
  /**
   * Set the value of hour.
   * @param v  Value to assign to hour.
   */
  public void setHour(short v) {
    cal.set(Calendar.HOUR_OF_DAY,v);
  }
    
  /**
   * Get the value of minutes.
   * @return Value of minutes.
   */
  public short getMinutes() {
    return (short)cal.get(Calendar.MINUTE);
  }
    
  /**
   * Set the value of minutes.
   * @param v  Value to assign to minutes.
   */
  public void setMinutes(short  v) {
    cal.set(Calendar.MINUTE,v);
  }
  
  /**
   * Get the value of seconds.
   * @return Value of seconds.
   */
  public short getSeconds() {
    return (short)cal.get(Calendar.SECOND);
  }
    
  /**
   * Set the value of seconds.
   * @param v  Value to assign to seconds.
   */
  public void setSeconds(short  v) {
    cal.set(Calendar.SECOND,v);
  }
      
  /**
   * Get the value of milliseconds.
   * @return Value of milliseconds.
   */
  public short getMilliseconds() {
    return (short)cal.get(Calendar.MILLISECOND);
  }
    
  /**
   * Set the value of milliseconds.
   * @param v  Value to assign to milliseconds.
   */
  public void setMilliseconds(short v) {
    cal.set(Calendar.MILLISECOND,v);
  }
      
  /**
   * Get the value of typeDesignator.
   * @return Value of typeDesignator.
   */
  public char getTypeDesignator() {
    return typeDesignator;
  }
    
  /**
   * Set the value of typeDesignator.
   * @param v  Value to assign to typeDesignator.
   */
  public void setTypeDesignator(char v) {
    this.typeDesignator = v;
  }

  public void fromString(String str) {
    if( str != null ) {
      cal.set(Calendar.YEAR,Integer.parseInt(str.substring(0,4)));
      cal.set(Calendar.MONTH,Integer.parseInt(str.substring(4,6))-1);
      cal.set(Calendar.DAY_OF_MONTH,Integer.parseInt(str.substring(6,8)));
      typeDesignator=str.charAt(8);
      cal.set(Calendar.HOUR_OF_DAY,Integer.parseInt(str.substring(9,11)));
      cal.set(Calendar.MINUTE,Integer.parseInt(str.substring(11,13)));
      cal.set(Calendar.SECOND,Integer.parseInt(str.substring(13,15)));
      cal.set(Calendar.MILLISECOND,Integer.parseInt(str.substring(15,18)));
    } 
  }
  
  private String paddedInt( int size, int val ) {
    String res = String.valueOf(val);
    while( res.length() < size ) {
	    res = 0 + res;
    }
    return res;
  }
  
  public String toString()  {
    String str_date = cal.get(Calendar.YEAR) +
      paddedInt(2,cal.get(Calendar.MONTH)+1) +
	    paddedInt(2,cal.get(Calendar.DAY_OF_MONTH))+"Z";
    if( cal.get(Calendar.AM_PM) == Calendar.PM ) { 
	    str_date=str_date+paddedInt(2,12+cal.get(Calendar.HOUR));
    } 
    else {
	    str_date=str_date+paddedInt(2,cal.get(Calendar.HOUR));
    }
    str_date=str_date+paddedInt(2,cal.get(Calendar.MINUTE))+
	    paddedInt(2,cal.get(Calendar.SECOND))+
	    paddedInt(3,cal.get(Calendar.MILLISECOND));
    
    return str_date;
  }
  
  public Date getTime() {
    return cal.getTime();
  }
  
  /*
    // For testing purposes only
    public static void main(String[] arg) {
    System.out.println("Initial date: 20030812Z171910154");
    BasicFipaDateTime bfdt = new BasicFipaDateTime("20030812Z171910154");
    System.out.println(bfdt.toString());
    System.out.println("Year:  "+bfdt.getYear());
    System.out.println("Month: "+bfdt.getMonth());
    System.out.println("Day:   "+bfdt.getDay());
    System.out.println("Hour:  "+bfdt.getHour());
    System.out.println("Min:   "+bfdt.getMinutes());
    System.out.println("Sec:   "+bfdt.getSeconds());
    System.out.println("Milli: "+bfdt.getMilliseconds());
    System.out.println(new BasicFipaDateTime(new BasicFipaDateTime("20030812Z171910154").getTime()));
    System.out.println();
    System.out.println("Current date: "+new BasicFipaDateTime(new Date()).toString());
    }
  */
    
} //  End of class BasicFipaDateTime

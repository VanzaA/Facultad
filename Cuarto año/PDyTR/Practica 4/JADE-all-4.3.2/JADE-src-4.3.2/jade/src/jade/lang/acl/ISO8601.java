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

package jade.lang.acl;

import jade.util.leap.*;
import java.util.Date;
import java.util.Calendar;
import java.util.TimeZone;

/**
 * This class contains a set of static methods that convert
 * to/from the Date Time format adopted by FIPA.
 * The FIPA format is based on ISO8601, with the addition of milliseconds.
 * Using the <code>java.text.SimpleDateFormat</code> notation, it is: 
 * <code>yyyyMMdd'T'HHmmssSSS'Z'</code>
 * , where the <code>'T'</code> serves to separate the Day from the Time, 
 * and the <code>'Z'</code> indicates that the time is in UTC.
 *
 * The FIPA specs permit either local or UTC time, however, they do 
 * express a preference for UTC time (this is particularly helpful when 
 * passing messages between agents running on machines in different timezones).
 * <UL>
 * <LI> Older versions of this code:
 *      <UL>
 *      <LI> read DateTime as local time
 *      <LI> wrote DateTime as local time
 *      </UL>
 * <LI> Current versions of this code:
 *      <UL>
 *      <LI> read DateTime in both local time and UTC time
 *      <LI> write DateTime as UTC time by default (can generate local time 
 *           if <code>toString(false)</code> is called). 
 *      </UL>
 * </UL> 
 *
 * @author Fabio Bellifemine - CSELT
 * @version $Date: 2003-11-20 11:55:37 +0100 (gio, 20 nov 2003) $ $Revision: 4572 $
 * Modified by:
 * @author Craig Sayers, HP Labs, Palo Alto, California
 */
public class ISO8601 {

    
    private static Calendar localCal = Calendar.getInstance();
    private static Calendar utcCal = Calendar.getInstance(TimeZone.getTimeZone("GMT"));

    /**
     * parse a date time token in UTC format (i.e. ending with a Z)
     **/
    private static Date parseutcDateFormat(String dateTimeToken) throws Exception {
	subparse(utcCal, dateTimeToken); 
	return utcCal.getTime();
    }
    
    /**
     * parse a date time token in local format (i.e. ending with a Z)
     **/
    private static Date parselocalDateFormat(String dateTimeToken) throws Exception {
	subparse(localCal, dateTimeToken);
	return localCal.getTime();
    }

    private static void subparse(Calendar cal, String dateTimeToken) {
	cal.set(Calendar.YEAR, 
		Integer.parseInt(dateTimeToken.substring(0,4)));
	cal.set(Calendar.MONTH, 
		Integer.parseInt(dateTimeToken.substring(4, 6)) - 1);
	cal.set(Calendar.DATE, 
		Integer.parseInt(dateTimeToken.substring(6, 8)));
	cal.set(Calendar.HOUR_OF_DAY, 
		Integer.parseInt(dateTimeToken.substring(9, 11)));
	cal.set(Calendar.MINUTE, 
		Integer.parseInt(dateTimeToken.substring(11, 13))); 
	cal.set(Calendar.SECOND, 
		Integer.parseInt(dateTimeToken.substring(13, 15)));
	cal.set(Calendar.MILLISECOND, 
		Integer.parseInt(dateTimeToken.substring(15, 18)));
    }

    private static String formatlocalDate(Date d) {
        // Initialize time
        localCal.setTime(d);
	return subFormatDate(localCal);
    }


    private static String formatutcDate(Date d) {
        utcCal.setTime(d);
	return subFormatDate(utcCal)+z;
    }
    
    private static String subFormatDate(Calendar cal) {
        // Format time
        StringBuffer formatedDate = new StringBuffer();

        formatedDate.append(zeroPaddingNumber(cal.get(Calendar.YEAR), 4));
        formatedDate.append(zeroPaddingNumber(cal.get(Calendar.MONTH) + 1, 2));
        formatedDate.append(zeroPaddingNumber(cal.get(Calendar.DATE), 2));
        formatedDate.append(t); 
        formatedDate.append(zeroPaddingNumber(cal.get(Calendar.HOUR_OF_DAY), 2));
        formatedDate.append(zeroPaddingNumber(cal.get(Calendar.MINUTE), 2));
        formatedDate.append(zeroPaddingNumber(cal.get(Calendar.SECOND), 2));
        formatedDate.append(zeroPaddingNumber(cal.get(Calendar.MILLISECOND), 3));
        return formatedDate.toString();
    }


    // set of constants used by the next method
    private static char plus = '+';
    private static char minus = '-';
    private static String z = "Z";
    private static char t = 'T';
    // 
    private final static long year = 365*24*60*60*1000L;
    private final static long month = 30*24*60*60*1000L;
    private final static long day = 24*60*60*1000;
    private final static long hour = 60*60*1000;
    private final static long minute = 60*1000;
    private final static long sec = 1000;

    /**
       Default constructor.
    */
    public ISO8601() {
    }

  /**
   * This method converts a FIPA DateTime token to a <code>java.util.Date</code>.  
   * It will accept both local and UTC time formats.
   * @throws an Exception if the String is not a valid dateTime
   * @return an absolute value of DateTime
   */
public synchronized static Date toDate(String dateTimeToken) throws Exception {
    if (dateTimeToken == null)
      return new Date();
    char sign = dateTimeToken.charAt(0);
    if ( (sign == plus) || (sign == minus) ) {
	// convert a relative time into an absolute time
	long millisec = Long.parseLong(dateTimeToken.substring(1, 5)) * year +
	  Long.parseLong(dateTimeToken.substring(5, 7))* month +
	  Long.parseLong(dateTimeToken.substring(7, 9))* day +
	  Long.parseLong(dateTimeToken.substring(10, 12)) * hour +
	  Long.parseLong(dateTimeToken.substring(12, 14)) * minute +
	  Long.parseLong(dateTimeToken.substring(14, 16)) * sec;
	System.out.println("sign="+sign+" millisec="+millisec);
	System.out.println(year+" "+month+" "+day+" "+hour);
	System.out.println("currentTime="+System.currentTimeMillis());
	millisec = System.currentTimeMillis() + (sign == plus ? millisec : (-millisec));
	return(new Date(millisec));
    }        
    else if( dateTimeToken.endsWith(z)) {
        // Preferred format is to pass UTC times, indicated by trailing 'Z'
        return parseutcDateFormat(dateTimeToken);
    }
    else {
        // Alternate format is to use local times - no trailing 'Z'
        return parselocalDateFormat(dateTimeToken);
    }
}

  /**
   * This method converts a <code>java.util.Date</code> into a FIPA DateTime token.
   *
   * Note: the current default behaviour is to generate dates in UTC time.
   * see <code>ISO8601.useUTCtime</code> for details.
   * @param useUTCtime controls the style used by <code>toString</code>,
   *  'true' generates tokens using UTC time, 'false' using local time.
   * If you need to send messages to agents compiled with older versions 
   * of Jade, then set this to <code>false</code>.
   * @return a String, e.g. "19640625T073000000Z" to represent 7:30AM on the
   * 25th of June of 1964, UTC time.
   */
public synchronized static String toString(Date d, boolean useUTCtime){
    if( useUTCtime ) {
        // perferred style is to generate UTC times, indicated by trailing 'Z'
        return formatutcDate(d);
    } else {
        // for backwards compatability, also support generating local times.
        return formatlocalDate(d);
    }
}


  /**
   * This method converts a <code>java.util.Date</code> into a FIPA DateTime 
   * token by using the UTC time.
   * @return a String, e.g. "19640625T073000000Z" to represent 7:30AM on the
   * 25th of June of 1964, UTC time.
   */
public static String toString(Date d){
    return toString(d, true);
}

  /**
   * this method converts into a string in ISO8601 format representing
   * relative time from the current time
   * @param millisec is the number of milliseconds from now
   * @return a String, e.g. "+00000000T010000000" to represent one hour
   * from now
   */
public static String toRelativeTimeString(long millisec) {
    StringBuffer str = new StringBuffer();

    if (millisec > 0)
	str.append(plus);
    else {
	str.append(minus);
	millisec = (-millisec); // get only the absolute value
    }

    long tmp = millisec/1000;
    long msec = millisec - tmp*1000;
    millisec = tmp;

    tmp = millisec/60;
    long sec = millisec - tmp*60;
    millisec = tmp;

    tmp = millisec/60;
    long min = millisec - tmp*60;
    millisec = tmp;

    tmp = millisec/24;
    long h = millisec - tmp*24;
    millisec = tmp;
    
    tmp = millisec/30;
    long day = millisec - tmp*30;
    millisec = tmp;

    tmp = millisec/12;
    long mon = millisec - tmp*12;
    millisec = tmp;

    long year = millisec;

    str.append(zeroPaddingNumber(year,4));
    str.append(zeroPaddingNumber(mon,2));
    str.append(zeroPaddingNumber(day,2));
    str.append(t);
    str.append(zeroPaddingNumber(h,2));
    str.append(zeroPaddingNumber(min,2));
    str.append(zeroPaddingNumber(sec,2));
    str.append(zeroPaddingNumber(msec,3));
    return str.toString();
}


private static String zeroPaddingNumber(long value, int digits) {
  String s = Long.toString(value);
  int n=digits-s.length();
  for (int i=0; i<n; i++)
      s="0"+s;
  return s;
}



//#MIDP_EXCLUDE_BEGIN
  /**
   * The main is here only for debugging.
   * You can test your conversion by executing the following command:
   * <p>
   * <code> java jade.lang.acl.ISO8601 <yourtoken> </code>
   */
public static void main(String argv[]) {

    System.out.println(localCal);

    System.out.println("USAGE: java ISO8601 DateTimetoken");
    System.out.println(argv[0]);
    try {
        System.out.println("Testing default behaviour (using UTC DateTime):");
        System.out.println("  ISO8601.toDate("+argv[0]+") returns:" + ISO8601.toDate(argv[0]));
        System.out.println("  converting that back to a string gives:" + ISO8601.toString(ISO8601.toDate(argv[0])));
        Date d1 = new Date();
        System.out.println("  ISO8601.toString( new Date() ) returns:" + ISO8601.toString(d1));
        System.out.println("  converting that back to a date gives:" + ISO8601.toDate(ISO8601.toString(d1)));
        
        System.out.println("Testing local time (for backwards compatability):");
        // ISO8601.useUTCtime = false;
        System.out.println("  ISO8601.toDate("+argv[0]+") returns:" + ISO8601.toDate(argv[0]));
        System.out.println("  converting that back to a string gives:" + ISO8601.toString(ISO8601.toDate(argv[0]), false));
        System.out.println("  ISO8601.toString( new Date(), false ) returns:" + ISO8601.toString(d1, false));
        System.out.println("  converting that back to a date gives:" + ISO8601.toDate(ISO8601.toString(d1, false)));
    } catch (Exception e) {
        e.printStackTrace();
    }
    
    try {
        System.out.println("ISO8601.toRelativeTimeString("+argv[0]+") returns:" + ISO8601.toRelativeTimeString(Long.parseLong(argv[0])));
        
        Date d = new Date(Integer.parseInt(argv[0]));
        System.out.println("ISO8601.toString("+d+", false) returns:" + ISO8601.toString(d, false));
    } catch (Exception e1) {
    }
    
}
//#MIDP_EXCLUDE_END
}

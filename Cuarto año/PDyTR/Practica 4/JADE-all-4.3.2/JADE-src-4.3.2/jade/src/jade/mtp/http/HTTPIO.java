/*****************************************************************
JADE - Java Agent DEvelopment Framework is a framework to develop
multi-agent systems in compliance with the FIPA specifications.
Copyright (C) 2000 CSELT S.p.A.

The updating of this file to JADE 2.0 has been partially supported by the
IST-1999-10211 LEAP Project

This file refers to parts of the FIPA 99/00 Agent Message Transport
Implementation Copyright (C) 2000, Laboratoire d'Intelligence
Artificielle, Ecole Polytechnique Federale de Lausanne

GNU Lesser General Public License

This library is free software; you can redistribute it sand/or
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

/**
 * HTTPIO.java
 *
 *
 * @author Jose Antonio Exposito
 * @author MARISM-A Development group ( marisma-info@ccd.uab.es )
 * @version 0.1
 * @author Nicolas Lhuillier (Motorola Labs)
 * @version 1.0
 */


package jade.mtp.http;

import jade.domain.FIPAAgentManagement.Envelope;
import jade.util.Logger;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.NoSuchElementException;
import java.util.StringTokenizer;

public class HTTPIO {
	
  // Response codes
  public static final String OK    = "200 OK";
  private static final String ERROR = "406 Not Acceptable";
  //private static final String UNAV  = "503 Service Unavailable";
  // HTTP constants
  private static final String HTTP1 = "HTTP/1.";
  private static final byte[] PROXY = {(byte) 'P',(byte) 'r',(byte) 'o',(byte) 'x',(byte) 'y',(byte) '-',(byte) 'C',(byte) 'o',(byte) 'n',(byte) 'n',(byte) 'e',(byte) 'c',(byte) 't',(byte) 'i',(byte) 'o',(byte) 'n',(byte) ':',(byte) ' '};
  private static final String PROXY_STR = "Proxy-Connection: ";
  private static final byte CR = (byte) '\r';
  private static final byte LF = (byte) '\n';
  private static final byte[] CRLF = {(byte) CR,(byte) LF};
  private static final byte[] POST = {(byte) 'P',(byte) 'O',(byte) 'S',(byte) 'T'};
  private static final String POST_STR = "POST";
  private static final byte[] CONTENT = {(byte) 'C',(byte) 'o',(byte) 'n',(byte) 't',(byte) 'e',(byte) 'n',(byte) 't',(byte) '-',(byte) 'T',(byte) 'y',(byte) 'p',(byte) 'e',(byte) ':',(byte) ' '};
  private static final String CONTENT_STR = "Content-Type: ";
  private static final byte[] CLENGTH = {(byte) 'C',(byte) 'o',(byte) 'n',(byte) 't',(byte) 'e',(byte) 'n',(byte) 't',(byte) '-',(byte) 'L',(byte) 'e',(byte) 'n',(byte) 'g',(byte) 't',(byte) 'h',(byte) ':',(byte) ' '};
  private static final byte[] MM = {(byte) 'm',(byte) 'u',(byte) 'l',(byte) 't',(byte) 'i',(byte) 'p',(byte) 'a',(byte) 'r',(byte) 't',(byte) '/',(byte) 'm',(byte) 'i',(byte) 'x',(byte) 'e',(byte) 'd'};
  private static final String MM_STR = "multipart/mixed";
  private static final byte[] BND = {(byte) 'b',(byte) 'o',(byte) 'u',(byte) 'n',(byte) 'd',(byte) 'a',(byte) 'r',(byte) 'y'};
  private static final String BND_STR = "boundary";
  private static final byte[] APPLI = {(byte) 'a',(byte) 'p',(byte) 'p',(byte) 'l',(byte) 'i',(byte) 'c',(byte) 'a',(byte) 't',(byte) 'i',(byte) 'o',(byte) 'n',(byte) '/'};
  private static final byte[] CONN = {(byte) 'C',(byte) 'o',(byte) 'n',(byte) 'n',(byte) 'e',(byte) 'c',(byte) 't',(byte) 'i',(byte) 'o',(byte) 'n',(byte) ':',(byte) ' '};
  private static final String CONN_STR = "Connection: ";
  public static final String CLOSE   = "close"; 
  public static final String KA      = "Keep-Alive"; 
  private static final byte[] HTTP = {(byte) 'H',(byte) 'T',(byte) 'T',(byte) 'P',(byte) '/',(byte) '1',(byte) '.',(byte) '1'};
  private static final byte[] CACHE =
	{(byte) 'C',(byte) 'a',(byte) 'c',(byte) 'h',(byte) 'e',(byte) '-',(byte) 'C',(byte) 'o',(byte) 'n',(byte) 't',(byte) 'r',(byte) 'o',(byte) 'l',(byte) ':',(byte) ' ',(byte) 'n',(byte) 'o',(byte) '-',(byte) 'c',(byte) 'a',(byte) 'c',(byte) 'h',(byte) 'e'};
  private static final byte[] MIME = {(byte) 'M',(byte) 'i',(byte) 'm',(byte) 'e',(byte) '-',(byte) 'V',(byte) 'e',(byte) 'r',(byte) 's',(byte) 'i',(byte) 'o',(byte) 'n',(byte) ':',(byte) ' ',(byte) '1',(byte) '.',(byte) '0'};
  private static final byte[] HOST = {(byte) 'H',(byte) 'o',(byte) 's',(byte) 't',(byte) ':',(byte) ' '};
  private static final String HOST_STR = "Host: ";
  private static final byte[] DL = {(byte) '-',(byte) '-'};
  private static final String DL_STR = "--";
  private static final String BLK     = "";
  private static final byte[] MIME_MULTI_PART_HEADER =
  	{(byte) 'T',(byte) 'h',(byte) 'i',(byte) 's',(byte) ' ',(byte) 'i',(byte) 's',(byte) ' ',(byte) 'n',(byte) 'o',(byte) 't',(byte) ' ',(byte) 'p',(byte) 'a',(byte) 'r',(byte) 't',(byte) ' ',(byte) 'o',(byte) 'f',(byte) ' ',(byte) 't',(byte) 'h',(byte) 'e',(byte) ' ',
  		(byte) 'M',(byte) 'I',(byte) 'M',(byte) 'E',(byte) ' ',(byte) 'm',(byte) 'u',(byte) 'l',(byte) 't',(byte) 'i',(byte) 'p',(byte) 'a',(byte) 'r',(byte) 't',(byte) ' ',(byte) 'e',(byte) 'n',(byte) 'c',(byte) 'o',(byte) 'd',(byte) 'e',(byte) 'd',(byte) ' ',
  		(byte) 'm',(byte) 'e',(byte) 's',(byte) 's',(byte) 'a',(byte) 'g',(byte) 'e',(byte) '.'};
  private static final byte[] XML = {(byte) 'x',(byte) 'm',(byte) 'l'};
  private static final byte[] CHARSET = {(byte) ';',(byte) ' ',(byte) 'c',(byte) 'h',(byte) 'a',(byte) 'r',(byte) 's',(byte) 'e',(byte) 't',(byte) '='};
  private static final byte[] TEXT = {(byte) 't',(byte) 'e',(byte) 'x',(byte) 't'};
  private static final byte[] TEXT_HTML = {(byte) 't',(byte) 'e',(byte) 'x',(byte) 't',(byte) '/',(byte) 'h',(byte) 't',(byte) 'm',(byte) 'l'};
  private static final byte[] HTML_BEGIN = {(byte) '<',(byte) 'h',(byte) 't',(byte) 'm',(byte) 'l',(byte) '>',(byte) '<',(byte) 'b',(byte) 'o',(byte) 'd',(byte) 'y',(byte) '>',(byte) '<',(byte) 'h',(byte) '1',(byte) '>'};
  private static final byte[] HTML_END = {(byte) '<',(byte) '/',(byte) 'h',(byte) '1',(byte) '>',(byte) '<',(byte) '/',(byte) 'b',(byte) 'o',(byte) 'd',(byte) 'y',(byte) '>',(byte) '<',(byte) '/',(byte) 'h',(byte) 't',(byte) 'm',(byte) 'l',(byte) '>'};
  
  private static Logger logger = Logger.getMyLogger(HTTPIO.class.getName());
  

  /* *********************************************** 
   *                 WRITE METHODS
   * ***********************************************/	
  
  
  /**
   * Write the message to the OutputStream associated to the Sender
   */
  public static void writeAll(OutputStream output, byte[] message) throws IOException {
    output.write(message);
    output.write(CRLF);
    output.flush();
  }
  
  /**
   * Create a generic message of HTTP with the input msgCode 
   * and type of connection (close or Keep-Alive)
   */
  public static byte[] createHTTPResponse(String msgCode, String type) {
    ByteArrayOutputStream message = new ByteArrayOutputStream(256);
    try {
      message.write(HTTP);
      message.write(' ');
      writeLowBytes(message,msgCode);
      message.write(CRLF);
      message.write(CONTENT);
      message.write(TEXT_HTML);
      message.write(CRLF);
      message.write(CACHE);
      message.write(CRLF);
      message.write(CONN);
      writeLowBytes(message,type);
      message.write(CRLF);
      message.write(CRLF);
      message.write(HTML_BEGIN);
      writeLowBytes(message,msgCode);
      message.write(HTML_END);
    } catch (IOException exception) {
      exception.printStackTrace();
    }
    return message.toByteArray();        
  }
	      
  /**
   * Prepare the HTML header
   */
  public static byte[] createHTTPHeader(HTTPAddress host, int length, String policy, byte[] boundary, boolean proxy) {
    //Put the header
    ByteArrayOutputStream header = new ByteArrayOutputStream(256);
    try {
      header.write(POST);
      header.write(' ');
      writeLowBytes(header,host.toString());
      header.write(' ');
      header.write(HTTP);
      header.write(CRLF);
      header.write(CACHE);
      header.write(CRLF);
      header.write(MIME);
      header.write(CRLF);
      header.write(HOST);
      writeLowBytes(header,host.getHost());
      header.write(':');
      writeLowBytes(header,host.getPort());
      header.write(CRLF);
      header.write(CONTENT);
      header.write(MM);
      header.write(' ');
      header.write(';');
      header.write(' ');
      header.write(BND);
      header.write('=');
      header.write('\"');
      header.write(boundary);
      header.write('\"');
      header.write(CRLF);
      //put the Content-Length
      header.write(CLENGTH);
      writeLowBytes(header,Integer.toString(length));
      header.write(CRLF);
      //put the Connection policy
      if (proxy) {
        header.write(PROXY);
        writeLowBytes(header,policy);
        header.write(CRLF);
      } else {
        header.write(CONN);
        writeLowBytes(header,policy);
        header.write(CRLF);
      }
      header.write(CRLF);
      header.flush();
    } catch (IOException exception) {
      exception.printStackTrace();
    }
    return header.toByteArray();
  }

  /**
   * Prepare the HTML body
   */
  public static byte[] createHTTPBody(Envelope env, byte[] boundary, byte[] payload) {
	ByteArrayOutputStream body = new ByteArrayOutputStream(payload.length + 100);
  	try {
      //PREPARE BODY
      body.write(MIME_MULTI_PART_HEADER);
      body.write(CRLF);
      body.write(DL);
      body.write(boundary);
      body.write(CRLF);
      //Insert The XML envelope
      // Put the Content-Type
      body.write(CONTENT);
      body.write(APPLI);
      body.write(XML);
      body.write(CRLF);
      body.write(CRLF); //A empty line
      env.setPayloadLength(new Long(payload.length));
      writeLowBytes(body,XMLCodec.encodeXML(env));
      body.write(CRLF);
      //Put the boundary delimit.
      body.write(DL);
      body.write(boundary);
      body.write(CRLF);
      //Insert the ACL message
      //Put the Content-Type  
      String payloadEncoding = env.getPayloadEncoding();
      if ((payloadEncoding != null) && (payloadEncoding.length() > 0)) {
        body.write(CONTENT);
        writeLowBytes(body,env.getAclRepresentation());
        body.write(CHARSET);
        writeLowBytes(body,payloadEncoding);
      } else {
        body.write(CONTENT);
        body.write(APPLI);
        body.write(TEXT);
      }
      body.write(CRLF);
      body.write(CRLF);
      //ACL part
      //Insert the ACL payload
      body.write(payload);
      body.write(CRLF);
      //Put the final boundary
      body.write(DL);
      body.write(boundary);
      body.write(DL);
      body.write(CRLF);
      body.flush();
  	} catch (IOException exception) {
      exception.printStackTrace();
  	}
    return body.toByteArray();
  }


  /* *********************************************** 
   *             READS METHODS
   * ***********************************************/     

  /**
   * Blocks on read until something is available on the stream 
   * or the stream is closed
   */
  /*
    public static String blockOnRead(BufferedReader br) throws IOException {
    //Skip empty lines
    String line = null;
    while(BLK.equals(line=br.readLine()));
    return line;
    }
  */
  
  /** 
   * Parse the input message, this message is received from the master server 
   * @param type return type of connection: close or Keep-Alive
   */  
  public static String readAll(InputStream input, StringBuffer xml, OutputStream acl, StringBuffer type) 
    throws IOException {
    //For the Control of sintaxis  
    String  host = null;
    //boolean foundMime       = false;
    boolean foundBoundary   = false;
    //boolean findContentType = false;
    String  boundary = null;
    //String  line = null;
    String  typeConnection = null;
    //Reset the Buffers
    // NL: Not supported on PJava
    /*
      if(xml.length()>0)
      xml.delete(0,xml.length());
      if(acl.length()>0)
      acl.delete(0,acl.length());	   
      if(connection.length()>0)
      connection.delete(0,connection.length());
    */
    //try {
    String line;
    while(BLK.equals(line=readLineFromInputStream(input))); // skip empty lines
    if(line==null) throw new IOException();
    StringTokenizer st = new StringTokenizer(line);
    try {

      if(!(st.nextToken()).equalsIgnoreCase(POST_STR) ) { 
        if(logger.isLoggable(Logger.WARNING))
        	logger.log(Logger.WARNING,"Malformed POST");

        type.append(CLOSE);
        return ERROR;
      }
      st.nextToken(); // Consumme a token
      if(!(st.nextToken().toUpperCase().startsWith("HTTP/1."))) { 
        if(logger.isLoggable(Logger.WARNING))
        	logger.log(Logger.WARNING,"Malformed HTTP/1.1 ");
        type.append(CLOSE);
        return ERROR;
      }
    }
    catch(NoSuchElementException nsee) {
      if(logger.isLoggable(Logger.WARNING))
      	logger.log(Logger.WARNING,"Malformed start line !: "+line);
      type.append(CLOSE);
      return ERROR;
    }
    //Process rest of header
    while (!BLK.equals(line=readLineFromInputStream(input))) {
      String lowerCaseLine = line.toLowerCase();
      if (lowerCaseLine.startsWith(HOST_STR.toLowerCase())) {
        host = processLine(line); //De momento solo controlamos que este
      }
      /* // NL do not test MIME version for interoperability with other MTP 
         if (line.toLowerCase().startsWith(MIME.toLowerCase())) {		  
         foundMime = true;
         }
      */
      if (lowerCaseLine.startsWith(CONN_STR.toLowerCase())) {
        typeConnection= processLine(line);
      }
      if (lowerCaseLine.startsWith(CONTENT_STR.toLowerCase())) {	
        //Process the left part

        if (!(processLine(line).toLowerCase().startsWith(MM_STR))) {
          if(logger.isLoggable(Logger.WARNING))
          	logger.log(Logger.WARNING,"MULTIPART/MIXED");

          type.append(CLOSE);
          return ERROR;
        }
        //Process the right part
        int pos = line.indexOf(BND_STR);
        if (pos == -1) {
          // Boundary on next line
          line=readLineFromInputStream(input);
          if ((pos = line.indexOf(BND_STR)) == -1) {
            // Bounday not found
            if(logger.isLoggable(Logger.WARNING))
            	logger.log(Logger.WARNING,"MIME boundary not found");
            type.append(CLOSE);
            return ERROR;
          }
        }
        line = line.substring(pos+BND_STR.length());
        pos = line.indexOf("\"")+1;
        boundary = DL_STR+line.substring(pos,line.indexOf("\"",pos));
        foundBoundary = true;
      }
    }//end while
    //if( !foundBoundary || !foundMime) {
    if(!foundBoundary) {
      if(logger.isLoggable(Logger.WARNING))
      	logger.log(Logger.WARNING,"Mime header error");
      type.append(CLOSE);
      return ERROR;
    }
    if (typeConnection == null) {
      type.append(KA); //Default Connection
    }	
    else {
      type.append(typeConnection); //Connection of request
    }
    //jump to first  "--Boundary" 
    while(BLK.equals(line=readLineFromInputStream(input))); // skip empty lines
    do {
      if (line.startsWith(boundary)) { 
        break;
      }
    }
    while(!BLK.equals(line=readLineFromInputStream(input)));
    while(BLK.equals(line=readLineFromInputStream(input))); // skip empty lines
    // Skip content-type
    do {    
      if(line.toLowerCase().startsWith(CONTENT_STR.toLowerCase())) { 
        break;
      }
    }
    while(!BLK.equals(line=readLineFromInputStream(input)));
    //Capture the XML part
    //Capture the message envelope
    while(!boundary.equals(line=readLineFromInputStream(input))) {
      if (! line.equals(BLK)) {
        xml.append(line); 
      }
    }
    //Capture the ACL part
    //JMP to ACLMessage
    while(BLK.equals(line=readLineFromInputStream(input))); // skip empty lines
    // Skip content-type
    do {    
      if(line.toLowerCase().startsWith(CONTENT_STR.toLowerCase())) { 
        break;
      }
    }
    while(!BLK.equals(line=readLineFromInputStream(input)));
    //Create last boundary for capture the ACLMessage
    ByteArrayOutputStream boundaryPattern = new ByteArrayOutputStream(boundary.length()+6);
    boundaryPattern.write(CRLF);
    boundaryPattern.write(boundary.getBytes("ISO-8859-1"));
    boundaryPattern.write(DL);
    //Capture the acl part.
    int character = -1;
    while(((character = input.read()) == CR ) || (character == LF)) {};  // Dirty hack: Skip leading blank lines.
    if (character >= 0) {
      acl.write(character);
      readBytesUpTo(input,acl,boundaryPattern.toByteArray());
    }
    return OK;
    /*
      }
      catch(NullPointerException npe) {
      // readLine returns null <--> EOF
      System.out.println("null pointer in readAll");
      //npe.printStackTrace();
      type.append(CLOSE);
      return ERROR;
      }
    */
  }
  
    
  /** 
   * Capture and return the code of response message, this message is received from client 
   */  
  public static int getResponseCode(InputStream input, StringBuffer type) 
    throws IOException {
    int responseCode = -1;
    try {
      String line = null; 
      //Capture and process the response message
      while (!(line=readLineFromInputStream(input)).startsWith(HTTP1));
      //capture the response code	     
      responseCode= Integer.parseInt(processLine(line));
      //Read all message
      while(((line=readLineFromInputStream(input))!=null)&&(!line.equals(BLK))) {
        if (line.toLowerCase().startsWith(CONN_STR.toLowerCase())) {
          type.append(processLine(line));
        }
        else if (line.toLowerCase().startsWith(PROXY_STR.toLowerCase())) {
          type.append(processLine(line));
        }
      }
      if (type.length() == 0) {
        type.append(KA); //Default Connection type  
      }
      return responseCode;
    }
    catch(Exception e) {
      // Connection has been closed before we receive confirmation.
      // We do cannot know if message has been received
      type.append(CLOSE);
      return responseCode; // NOT OK
    }
  }
  
   
  /** 
   * return the next information of search in the line
   */
  private static String processLine(String line) 
    throws IOException {  
    StringTokenizer st = new StringTokenizer(line);
    try {
      st.nextToken(); // Consumme first token
      return st.nextToken();
    }
    catch(NoSuchElementException nsee) {
      throw new IOException("Malformed line !: "+line);
    }
  }

  /**
   * Reads byte sequence from specified input stream into specified output stream up to specified
   * byte sequence pattern is occurred. The output byte sequence does not contains any bytes matched
   * with pattern. If the specified pattern was not found until the input stream reaches at end, output
   * all byte sequence up to end of input stream and returns false.
   *
   * @param input specified input stream.
   * @param output specified output stream.
   * @param pattern specified pattern byte seqence.
   * @return Whether the specified pattern was found or not.
   * @throws IOException  If an I/O error occurs.
   * @throws IllegalArgumentException If pattern is null or pattern is empty.
   * @author mminagawa
   */
  private static boolean readBytesUpTo(InputStream input, OutputStream output, byte[] pattern) throws IOException {
    if ((pattern == null) || (pattern.length == 0)) {
      throw new IllegalArgumentException("Specified pattern is null or empty.");
    }
    int patternIndex = 0;
    boolean matched = false;
    boolean atEnd = false;
    while ((!matched) && (!atEnd)) {
      int readByte = input.read();
      if (readByte < 0) {
        atEnd = true;
        if (patternIndex != 0) {
          output.write(pattern,0,patternIndex);
          patternIndex = 0;
        }
      } else {
        if (readByte == pattern[patternIndex]) {
          patternIndex++;
          if (patternIndex >= pattern.length) {
            matched = true;
          }
        } else {
          if (patternIndex != 0) {
			output.write(pattern,0,patternIndex);
			patternIndex = 0;
          }
          output.write(readByte);
        }
      }
    }
    return matched;
  }

  /**
   * Read a line of text from specified input stream.  A line is considered to be
   * terminated by a carriage return ('\r') followed immediately by a linefeed ('\n').
   *
   * @param input specified input stream to read from.
   * @return A String containing the contents of the line, not including any line-termination
   *          characters, or null if the end of the stream has been reached.
   * @throws IOException  If an I/O error occurs.
   * @author mminagawa
   */
  private static String readLineFromInputStream(InputStream input) throws IOException {
    StringBuffer buffer = new StringBuffer(256);
    int characterByte;
    boolean justBeforeCR = false;
    boolean terminated = false;
    boolean entered = false;
    while ((!terminated) && ((characterByte = input.read()) >= 0)) {
      entered = true;
      switch (characterByte) {
      case CR :
        if (justBeforeCR) {
          buffer.append((char)CR);
        } else {
          justBeforeCR = true;
        }
        break;
      case LF :
        if (justBeforeCR) {
          terminated = true;
        } else {
          buffer.append((char)LF);
        }
		justBeforeCR = false;
        break;
      default :
        if (justBeforeCR) { buffer.append((char)CR); }
        buffer.append((char)characterByte);
        justBeforeCR = false;
      }
    }
    if (!entered) { return null; }
    if ((!terminated) && (justBeforeCR)) {
      buffer.append((char)CR);
    }
    return buffer.toString();
  }

  /**
   * Write characters contained specified string to specified output stream.<br />
   * These characters must be 7-bit character, and stored only low-byte of each code.
   *
   * @param output specified output stream.
   * @param string specified string to output.
   * @throws IOException  If an I/O error occurs.
   * @author mminagawa
   */
  private static void writeLowBytes(OutputStream output, String string) throws IOException {
    for (int i = 0 ; i < string.length() ; i++ ) {
      output.write(string.charAt(i));
    }
  }

} // End of HTTPIO class 



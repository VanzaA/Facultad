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

/**
   Abstract interface for converting ACL messages back and forth
   between Java objects and raw byte sequences, according to a FIPA
   ACL message representation.

   @author Giovanni Rimassa - Universita` di Parma
   @version $Date: 2005-04-15 17:45:02 +0200 (ven, 15 apr 2005) $ $Revision: 5669 $

 */
public interface ACLCodec {

  //#DOTNET_EXCLUDE_BEGIN
  public static final String DEFAULT_CHARSET = "US-ASCII";
  //#DOTNET_EXCLUDE_END
  /*#DOTNET_INCLUDE_BEGIN
  public static final String DEFAULT_CHARSET = "ISO-8859-1";
  #DOTNET_INCLUDE_END*/

  /**
    This exception is thrown when some problem occurs in the concrete parsing
    subsystem accessed through this interface. If an exception is thrown by the
    underlying parser, it is wrapped with a <code>Codec.CodecException</code>,
    which is then rethrown.
  */
  public static class CodecException extends Exception {
    /**
    @serial
    */
    private Throwable nested;

    /**
      Construct a new <code>CodecException</code>
      @param msg The message for this exception.
      @param t The exception wrapped by this object.
    */
    public CodecException(String msg, Throwable t) {
      super(msg);
      nested = t;
      //this.fillInStackTrace();
    }

    /**
      Reads the exception wrapped by this object.
      @return the <code>Throwable</code> object that is the exception thrown by
      the concrete parsing subsystem.
    */
    public Throwable getNested() {
      return nested;
    }
   
      /**
	 Print the stack trace for this exception on the standard
	 output stream.
      */
    public void printStackTrace() {
      if (nested != null)
	nested.printStackTrace();
      super.printStackTrace();
    }
  }


  /**
     Encodes an <code>ACLMessage</code> object into a byte sequence,
     according to the specific message representation.
     @param msg The ACL message to encode.
     @param charset Charset encoding to use (e.g. US_ASCII, UTF-8, etc)
     @return a byte array, containing the encoded message.
  */
  byte[] encode(ACLMessage msg, String charset);

  /**
     Recovers an <code>ACLMessage</code> object back from raw data,
     using the specific message representation to interpret the byte
     sequence.
     @param data The byte sequence containing the encoded message.
     @param charset Charset encoding to use (e.g. US_ASCII, UTF-8, etc)
     @return A new <code>ACLMessage</code> object, built from the raw
     data.
     @exception CodecException If some kind of syntax error occurs.
   */
  ACLMessage decode(byte[] data, String charset) throws CodecException;

  /**
     Query the name of the message representation handled by this
     <code>Codec</code> object. The FIPA standard representations have
     a name starting with <code><b>"fipa.acl.rep."</b></code>.
     @return The name of the handled ACL message representation.
   */
  String getName();

}

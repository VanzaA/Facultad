/*--- formatted by Jindent 2.1, (www.c-lab.de/~jindent) ---*/
/**
 * ***************************************************************
 * The LEAP libraries, when combined with certain JADE platform components,
 * provide a run-time environment for enabling FIPA agents to execute on
 * lightweight devices running Java. LEAP and JADE teams have jointly
 * designed the API for ease of integration and hence to take advantage
 * of these dual developments and extensions so that users only see
 * one development platform and a
 * single homogeneous set of APIs. Enabling deployment to a wide range of
 * devices whilst still having access to the full development
 * environment and functionalities that JADE provides.
 * Copyright (C) 2001 Telecom Italia LAB S.p.A.
 * Copyright (C) 2001 Motorola.
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
 * **************************************************************
 */
package jade.imtp.leap.http;

//#MIDP_EXCLUDE_FILE
import jade.core.CaseInsensitiveString;
import java.io.*;

import java.util.Hashtable;
import java.util.Enumeration;

/**
 * @author Giovanni Caire - TILAB
 */
public class HTTPPacket {

    private static final CaseInsensitiveString CONTENT_LENGTH_KEY = new CaseInsensitiveString("content-length");
    private static final int CR = 13;
    private static final int LF = 10;
    private static final String DELIMITER = new String(new byte[]{CR, LF});
    protected String firstLine = null;
    protected String httpType = null;
    protected Hashtable fields = new Hashtable();
    protected byte[] payload = null;

    /**
     * Constructor declaration
     */
    protected HTTPPacket() {
    }

    public String getHttpType() {
        return httpType;
    }

    public String getField(String key) {
        return (String) fields.get(new CaseInsensitiveString(key));
    }

    public byte[] getPayload() {
        return payload;
    }

    public void setHttpType(String type) {
        httpType = type;
    }

    public void setField(String key, String value) {
        if (value != null) {
            fields.put(new CaseInsensitiveString(key), value);
        } else {
            fields.remove(new CaseInsensitiveString(key));
        }
    }

    public void setPayload(byte[] p) {
        payload = p;
        if (payload != null) {
            fields.put(CONTENT_LENGTH_KEY, String.valueOf(payload.length));
        } else {
            fields.remove(CONTENT_LENGTH_KEY);
        }
    }

    public void setPayload(byte[] bb, int first, int length) {
        payload = new byte[length];
        System.arraycopy(bb, first, payload, 0, length);
        fields.put(CONTENT_LENGTH_KEY, String.valueOf(payload.length));
    }

    /**
     * Method declaration
     * @see
     */
    protected void readFrom(InputStream is) throws IOException {
        // Read bytes until the payload marker (13-10-13-10) into a temporary buffer
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        int[] bb = new int[]{0, 0, 0};
        int pos = 0;
        //System.out.println("HTTP packet header -----------");
        do {
            bb[pos] = is.read();
            //System.out.print(bb[pos]+" ");
            if (bb[pos] == -1) {
                throw new EOFException("Unexpected EOF");
            }
            baos.write(bb[pos++]);
            if (pos >= 3) {
                pos = 0;
            }
        } while (!endOfHeader(bb, pos));
        //System.out.println("\n------------------------------");

        // Parse the Header from the temporary buffer
        ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
        BufferedReader inpReader = new BufferedReader(new InputStreamReader(bais));
        firstLine = inpReader.readLine();
        //System.out.println(firstLine);
        while (true) {
            String line = inpReader.readLine();
            //System.out.println(line);
            if (line == null || line.length() == 0) {
                // Header completed
                //System.out.println("------------------------------");
                break;
            }
            try {
                int colonIndex = line.indexOf(':');
                String key = line.substring(0, colonIndex);
                String value = (line.substring(colonIndex + 1, line.length())).trim();
                setField(key, value);
            } catch (Exception e) {
                throw new IOException("Header field format error. " + e);
            }
        }

        // Get the payload Payload
        try {
            int length = Integer.parseInt((String) fields.get(CONTENT_LENGTH_KEY));
            //System.out.println("Reading "+length+" bytes...");
            payload = new byte[length];
            int n = 0;
            while (n < length) {
                int k = is.read(payload, n, length - n);
                if (k < 0) {
                    throw new EOFException(String.valueOf(n));
                }
                n += k;
            }
            //System.out.println("Done");
        } catch (IOException ioe) {
            // Rethrow the exception
            throw ioe;
        } catch (Exception e) {
            payload = null;
        }
    }

    private static final boolean endOfHeader(int[] bb, int pos) {
        if (bb[pos] == LF) {
            if ((++pos) >= 3) {
                pos = 0;
            }
            if (bb[pos] == CR) {
                if ((++pos) >= 3) {
                    pos = 0;
                }
                return bb[pos] == LF;
            }
        }
        return false;
    }

    /**
     * Method declaration
     * @see
     */
    protected void writeTo(OutputStream os) throws IOException {
        // Write the Header into a temporary buffer
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        BufferedWriter outWriter = new BufferedWriter(new OutputStreamWriter(baos));
        outWriter.write(firstLine);
        outWriter.write(DELIMITER);
        Enumeration e = fields.keys();
        while (e.hasMoreElements()) {
            CaseInsensitiveString key = (CaseInsensitiveString) e.nextElement();
            String value = (String) fields.get(key);
            outWriter.write(key.toString() + ": " + value);
            outWriter.write(DELIMITER);
        }
        outWriter.write(DELIMITER);
        outWriter.flush();

        // Payload
        if (payload != null) {
            baos.write(payload);
        }

        os.write(baos.toByteArray());
        os.flush();
    }

    /**
     * Method declaration
     * @see
     */
    public String toString() {
        StringBuffer sb = new StringBuffer();
        Enumeration e = fields.keys();
        while (e.hasMoreElements()) {
            CaseInsensitiveString key = (CaseInsensitiveString) e.nextElement();
            String value = (String) fields.get(key);
            sb.append(key.toString() + "=" + value);
            sb.append("\n");
        }
        if (payload != null) {
            sb.append("-------------\nPayload size: " + payload.length + "\n");
        }
        return sb.toString();
    }
}


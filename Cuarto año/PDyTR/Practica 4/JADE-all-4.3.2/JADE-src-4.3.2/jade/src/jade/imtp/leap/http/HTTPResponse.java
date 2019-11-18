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
import java.io.*;

import java.util.StringTokenizer;

/**
 * @author Giovanni Caire - TILAB
 */
public class HTTPResponse extends HTTPPacket {

    protected String code = null;
    protected String msg = null;

    /**
     * Constructor declaration
     */
    public HTTPResponse() {
        super();
    }

    public String getCode() {
        return code;
    }

    public void setCode(String c) {
        code = c;
    }

    public String getMessage() {
        return msg;
    }

    public void setMessage(String m) {
        msg = m;
    }

    /**
     * Method declaration
     */
    protected void readFrom(InputStream is) throws IOException {
        super.readFrom(is);
        StringTokenizer st = new StringTokenizer(firstLine, " ");
        httpType = st.nextToken();
        code = st.nextToken();
        msg = st.nextToken();
    }

    /**
     * Method declaration
     * @see
     */
    public void writeTo(OutputStream os) throws IOException {
        firstLine = httpType + " " + code + " " + msg;
        super.writeTo(os);
    }

    /**
     * Method declaration
     * @see
     */
    public String toString() {
        StringBuffer sb = new StringBuffer();
        sb.append(httpType + " " + code + " " + msg);
        sb.append("\n");
        sb.append(super.toString());
        return sb.toString();
    }
}


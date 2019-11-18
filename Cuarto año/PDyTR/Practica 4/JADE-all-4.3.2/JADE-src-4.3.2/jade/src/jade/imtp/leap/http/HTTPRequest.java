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
public class HTTPRequest extends HTTPPacket {

    protected String method = null;
    protected String file = null;

    /**
     * Constructor declaration
     */
    public HTTPRequest() {
        super();
    }

    public String getMethod() {
        return method;
    }

    public void setMethod(String m) {
        method = m;
    }

    public String getFile() {
        return file;
    }

    public void setFile(String f) {
        file = f;
    }

    /**
     * Method declaration
     * @see
     */
    public void readFrom(InputStream is) throws IOException {
        super.readFrom(is);
        StringTokenizer st = new StringTokenizer(firstLine, " ");
        method = st.nextToken();
        file = st.nextToken();
        httpType = st.nextToken();
    }

    /**
     * Method declaration
     * @see
     */
    protected void writeTo(OutputStream os) throws IOException {
        firstLine = method + " " + file + " " + httpType;
        super.writeTo(os);
    }

    /**
     * Method declaration
     * @see
     */
    public String toString() {
        StringBuffer sb = new StringBuffer();
        sb.append(method + " " + file + " " + httpType);
        sb.append("\n");
        sb.append(super.toString());
        return sb.toString();
    }
}


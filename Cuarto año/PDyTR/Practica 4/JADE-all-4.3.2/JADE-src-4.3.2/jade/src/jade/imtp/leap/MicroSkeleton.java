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

package jade.imtp.leap;

/**
 * Deserialize a command, calls the corresponding method and
 * serialize the response.
 * @author Giovanni Caire - TILAB
 */
public abstract class MicroSkeleton {
	public byte[] handleCommand(byte[] cmd) {
		byte[] rsp = null;
		try {
			Command c = SerializationEngine.deserialize(cmd);
			Command r = executeCommand(c);
			rsp = SerializationEngine.serialize(r);
		} 
		catch (Throwable tr) {
			// These can be exceptions at the IMTP level (serialization or 
			// unsupported command) or unexpected exceptions at the upper level
			tr.printStackTrace();
			Command r = createErrorRsp(tr, false);
			try {
				rsp = SerializationEngine.serialize(r);
			}
			catch (LEAPSerializationException lse) {
				// This should never happen 
				lse.printStackTrace();
			}
		} 
		return rsp;
	}

	/**
	   Skeleton implementations must implement this method.
	 */
	abstract Command executeCommand(Command c) throws Throwable;

	protected Command createErrorRsp(Throwable tr, boolean expected) { 
		Command rsp = new Command(Command.ERROR);
		rsp.addParam(new Boolean(expected));
		rsp.addParam(tr.getClass().getName());
		rsp.addParam(tr.getMessage());
		return rsp;
	}
}


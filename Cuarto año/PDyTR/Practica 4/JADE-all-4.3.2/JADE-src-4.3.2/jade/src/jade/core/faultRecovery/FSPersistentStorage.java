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

package jade.core.faultRecovery;

//#J2ME_EXCLUDE_FILE

import jade.core.Profile;
import jade.util.Logger;

import java.util.Map;
import java.util.HashMap;
import java.io.*;

/**
   Default implementation of the PersistentStorage interface saving
   persistent information in the local file system.
   
   @author Giovanni Caire - TILAB
 */
class FSPersistentStorage implements PersistentStorage {
	public static final String LOCATION = "jade_core_faultRecovery_FSPersistentStorage_location";
	public static final String LOCATION_DEFAULT = ".";

	private static final String EXTENSION = ".fsps";
	private static final String CHILD_EXTENSION = ".fsps_c";
	private static final String UNREACHABLE_EXTENSION = ".unreachable";	
	private static final String PLATFORM_FILE_NAME = "platform";
	private static final String NODE_POSTFIX = "-node";
	
	private String fileSeparator;
	private File locationDir;
	
	private Logger myLogger = Logger.getMyLogger(getClass().getName());
	
	public void init(Profile p) throws Exception {
		fileSeparator = System.getProperty("file.separator");
		
		String locationName = p.getParameter(LOCATION, LOCATION_DEFAULT);
		locationDir = new File(locationName);
		if (!locationDir.exists()) {
			myLogger.log(Logger.CONFIG, "FSPS location directory "+locationName+" does not exists. Creating it ...");
			boolean success = locationDir.mkdirs();
			if (!success) {
				throw new IOException("Cannot create FSPS location directory "+locationName+".");
			}
		}
		else if (!locationDir.isDirectory()) {
			throw new IOException("FSPS location "+locationName+" is not a directory.");
		}		
	}
	
	public void close() {
		// Nothing to do in this file system based implementation
	}
	
	public void clear(final boolean clearPlatformInfo) throws Exception {
		//#DOTNET_EXCLUDE_BEGIN
		File[] ff = locationDir.listFiles(new FilenameFilter() {
			public boolean accept(File dir, String name) {
				if (!clearPlatformInfo && name.equals(PLATFORM_FILE_NAME+EXTENSION)) {
					return false;
				}
				return (name.endsWith(EXTENSION) || name.endsWith(CHILD_EXTENSION) || name.endsWith(UNREACHABLE_EXTENSION));
			}
		} );
		//#DOTNET_EXCLUDE_END
		
		/*#DOTNET_INCLUDE_BEGIN
		String[] ss = locationDir.list(new FilenameFilter() {
			public boolean accept(File dir, String name) {
				if (!clearPlatformInfo && name.equals(PLATFORM_FILE_NAME+EXTENSION)) {
					return false;
				}
				return (name.endsWith(EXTENSION) || name.endsWith(CHILD_EXTENSION) || name.endsWith(UNREACHABLE_EXTENSION));
			}
		} );

		File[] ff = new File[ss.length];
		for(int i=0; i < ss.length; i++)
		{
			ff[i] = new File( locationDir.getPath(), ss[i] );
		}
		#DOTNET_INCLUDE_END*/
		
		for (int i = 0; i < ff.length; ++i) {
			ff[i].delete();
		}
		if (myLogger.isLoggable(Logger.FINE)) {
			myLogger.log(Logger.FINE, "Removed "+ff.length+" files from persistent storage");
		}
	}
	
	public void storePlatformInfo(String platformName, String address) throws Exception {
		String tmp = platformName+'@'+address;
		File platformInfoFile = getFSPSFile(PLATFORM_FILE_NAME, EXTENSION);
		writeContent(platformInfoFile, tmp.getBytes());
		if (myLogger.isLoggable(Logger.FINE)) {
			myLogger.log(Logger.FINE, "Platform information (name = "+platformName+", address = "+address+" saved in persistent storage");
		}
	}
	
	public String[] getPlatformInfo() throws Exception {
		File addrFile = getFSPSFile(PLATFORM_FILE_NAME, EXTENSION);
		if (addrFile.exists()) {
			byte[] content = readContent(addrFile);
			String tmp = new String(content);
			int k = tmp.indexOf('@');
			if (k > 0) {
				String[] platformInfo = new String[2];
				platformInfo[0] = tmp.substring(0, k);
				platformInfo[1] = tmp.substring(k+1);
				return platformInfo;
			}
			else {
				// Wrong format
				return null;
			}
		}
		else {
			return null;
		}
	}
	
	public void storeNode(String name, boolean isChild, byte[] nn) throws Exception {
		File f = getFSPSFile(name+NODE_POSTFIX, (isChild ? CHILD_EXTENSION : EXTENSION));
		writeContent(f, nn);
		if (myLogger.isLoggable(Logger.FINE)) {
			myLogger.log(Logger.FINE, "Node "+name+" saved in persistent storage");
		}
		// If the newly stored node was already in the persistent storage as an unreachable node --> remove it
		File uf = getUnreachableFile(f);
		if (uf.exists()) {
			uf.delete();
		}
	}
	
	public void removeNode(String name) throws Exception {
		File f = getFSPSFile(name+NODE_POSTFIX, EXTENSION);
		if (!f.exists()) {
			f = getFSPSFile(name+NODE_POSTFIX, CHILD_EXTENSION);
		}
		if (f.exists()) {
			f.delete();
			if (myLogger.isLoggable(Logger.FINE)) {
				myLogger.log(Logger.FINE, "Node "+name+" removed from persistent storage");
			}
		}
	}
	
	public void setUnreachable(String name) throws Exception {
		File f = getFSPSFile(name+NODE_POSTFIX, EXTENSION);
		if (!f.exists()) {
			// Try as a child node
			f = getFSPSFile(name+NODE_POSTFIX, CHILD_EXTENSION);
		}
		if (f.exists()) {
			f.renameTo(getUnreachableFile(f));
			if (myLogger.isLoggable(Logger.FINE)) {
				myLogger.log(Logger.FINE, "Node "+name+" marked as unreachable");
			}
		}
	}
	
	public void resetUnreachable(String name) throws Exception {
		File f = getFSPSFile(name+NODE_POSTFIX, EXTENSION+UNREACHABLE_EXTENSION);
		if (!f.exists()) {
			// Try as a child node
			f = getFSPSFile(name+NODE_POSTFIX, CHILD_EXTENSION+UNREACHABLE_EXTENSION);
		}
		if (f.exists()) {
			f.renameTo(getReachableFile(f));
			if (myLogger.isLoggable(Logger.FINE)) {
				myLogger.log(Logger.FINE, "Node "+name+" restored as reachable");
			}
		}
	}
	
	public Map getAllNodes(boolean children) throws Exception {
		final String end = NODE_POSTFIX+(children ? CHILD_EXTENSION : EXTENSION);
		
		//#DOTNET_EXCLUDE_BEGIN
		File[] ff = locationDir.listFiles(new FilenameFilter() {
			public boolean accept(File dir, String name) {
				return (name.endsWith(end));
			}
		} );
		//#DOTNET_EXCLUDE_END
		
		/*#DOTNET_INCLUDE_BEGIN
		String[] ss = locationDir.list(new FilenameFilter() {
			public boolean accept(File dir, String name) {
				return (name.endsWith(end));
			}
		} );

		File[] ff = new File[ss.length];
		for(int i=0; i < ss.length; i++)
		{
			ff[i] = new File( locationDir.getPath(), ss[i] );
		}
		#DOTNET_INCLUDE_END*/
		
		Map nodes = new HashMap(ff.length);
		for (int i = 0; i < ff.length; ++i) {
			nodes.put(getNodeName(ff[i].getName()), readContent(ff[i]));
		}
		return nodes;
	}
	
	public byte[] getUnreachableNode(String name) throws Exception {
		File f = getFSPSFile(name+NODE_POSTFIX, EXTENSION+UNREACHABLE_EXTENSION);
		if (!f.exists()) {
			// Try as an unreachable child node
			f = getFSPSFile(name+NODE_POSTFIX, CHILD_EXTENSION+UNREACHABLE_EXTENSION);
		}
		if (f.exists()) {
			return readContent(f);
		}
		return null;
	}
	
	private String getNodeName(String filename) {
		int index = filename.indexOf(EXTENSION);
		int length = index - 5; // 5 is the length of "-node"
		return filename.substring(0, length);
	}
	
	private File getFSPSFile(String name, String ext) {
		String fileName = locationDir.getPath()+fileSeparator+name+ext;
		return new File(fileName);
	}
	
	private File getUnreachableFile(File f) {
		String unreachableName = locationDir.getPath()+fileSeparator+f.getName()+UNREACHABLE_EXTENSION;
		return new File(unreachableName);
	}

	private File getReachableFile(File f) {
		int length = f.getName().length() - 12; // 12 is the length of ".unreachable"
		String reachableName = locationDir.getPath()+fileSeparator+f.getName().substring(0, length);
		return new File(reachableName);
	}

	private void writeContent(File file, byte [] content) throws Exception {
		//#DOTNET_EXCLUDE_BEGIN
		file.createNewFile();
		//#DOTNET_EXCLUDE_END
		
		FileOutputStream fos = null;
		try {
			fos = new FileOutputStream(file);
			fos.write(content);
		} 
		finally {
			if (fos != null) {
				fos.close();
			}
		}
	}

	private byte[] readContent(File file) throws IOException {
		FileInputStream fis = null;
		int length = (int) file.length();
		if (length > 0) {
			byte[] content = new byte[length];
			try {
				fis = new FileInputStream(file);
				int cnt = 0;
				int n;
				do {
					n = fis.read(content, cnt, length-cnt);
					if (n == -1) {
						throw new EOFException("EOF reading packet data");
					} 
					cnt += n;
				} 
				while (cnt < length);
				return content;
			}
			finally {
				if (fis != null) {
					fis.close();
				}
			}
		}
		else {
			return null;
		}
	}
}

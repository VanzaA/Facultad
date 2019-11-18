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
package jade.core.resource;

//#J2ME_EXCLUDE_FILE
//#APIDOC_EXCLUDE_FILE

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;

import jade.core.Agent;
import jade.core.AgentContainer;
import jade.core.BaseService;
import jade.core.HorizontalCommand;
import jade.core.IMTPException;
import jade.core.Node;
import jade.core.NotFoundException;
import jade.core.Profile;
import jade.core.ProfileException;
import jade.core.Service;
import jade.core.ServiceException;
import jade.core.ServiceHelper;
import jade.core.VerticalCommand;
import jade.util.Logger;

/**
 * ResourceManagement service main class
 */
public class ResourceManagementService extends BaseService {
	public static final String NAME = ResourceManagementHelper.SERVICE_NAME;

	private ServiceComponent localSlice;
	private ResourceManagementHelper helper;

	@Override
	public void init(AgentContainer ac, Profile p) throws ProfileException {
		super.init(ac, p);
		
		localSlice = new ServiceComponent();
	}

	@Override
	public void boot(Profile p) throws ServiceException {
		super.boot(p);

		helper = new ResourceManagementHelperImpl();
		SingletonResourceManagementHelper.getInstance().addHelper(helper);
	}

	@Override
	public void shutdown() {
		SingletonResourceManagementHelper.getInstance().removeHelper(helper);
		helper = null;
	}
	
	public String getName() {
		return NAME;
	}

	@Override
	public Class getHorizontalInterface() {
		return ResourceManagementSlice.class;
	}
	
	@Override
	public Service.Slice getLocalSlice() {
		return localSlice;
	}
	
	@Override
	public ServiceHelper getHelper(Agent a) {
		return helper;
	}
	
	public static ResourceManagementHelper getHelper() {
		return SingletonResourceManagementHelper.getInstance().getHelper();
	}
	
	
	/**
	 * Inner class ServiceComponent
	 */
	private class ServiceComponent implements Service.Slice {

		public Node getNode() throws ServiceException {
			try {
				return ResourceManagementService.this.getLocalNode();
			}
			catch(IMTPException imtpe) {
				throw new ServiceException("Error retrieving local node", imtpe);
			}
		}

		public Service getService() {
			return ResourceManagementService.this;
		}

		public VerticalCommand serve(HorizontalCommand cmd) {
			try {
				String cmdName = cmd.getName();
				Object[] params = cmd.getParams();
				
				if (cmdName.equals(ResourceManagementSlice.H_GETRESOURCE)) {
					String name = (String) params[0];
					int fetchMode = (Integer) params[1];
					
					if (myLogger.isLoggable(Logger.FINE)) {
						myLogger.log(Logger.FINE, "Serve getResource "+name+" with fetchMode "+fetchMode);
					}					
					
					byte[] resource = getResource(name, fetchMode);
					cmd.setReturnValue(resource);
				}
			}
			catch (Throwable t) {
				cmd.setReturnValue(t);
			}
			return null;
		}

		private byte[] getResource(String name, int fetchMode) throws IOException, NotFoundException {

			// Search in classpath
			if (fetchMode == ResourceManagementHelper.CLASSPATH_RESOURCES) {
				return getResourceFromClasspath(name);
			}
			
			// Search in file system
			if (fetchMode == ResourceManagementHelper.FILE_SYSTEM_RESOURCES) {
				return getResourceFromFileSystem(name);
			}
			
			// Search in shared resources
			if (fetchMode == ResourceManagementHelper.SHARED_RESOURCES) {
				return getResourceFromShared(name);
			}
			
			// Search in all (shared->classpath->filesystem)
			if (fetchMode == ResourceManagementHelper.ALL_RESOURCES) {
				byte[] res = null; 
				try {
					res = getResourceFromShared(name);
				} catch(NotFoundException nfe1) {
					try {
						res = getResourceFromClasspath(name);
					} catch(NotFoundException nfe2) {
						res = getResourceFromFileSystem(name);
					}
				}
				return res;
			}
			
			// Wrong fetch mode
			myLogger.log(Logger.WARNING, "Received getResource "+name+" with unsupported fetchMode "+fetchMode);
			throw new IllegalArgumentException("getResource "+name+" with unsupported fetchMode "+fetchMode);
		}

		private byte[] getResourceFromShared(String name) throws IOException, NotFoundException {
			// Get shared folder
			String sharedFolder = System.getProperty(ResourceManagementHelper.SHARED_RESOURCES_FOLDER_KEY);
			if (sharedFolder == null) {
				sharedFolder = "";
			} else {
				sharedFolder += File.separator;
			}
			
			return getResourceFromFileSystem(sharedFolder+name);
		}

		private byte[] getResourceFromFileSystem(String name) throws IOException, NotFoundException {
			return getResourceFromFile(new File(name));
		}
		
		private byte[] getResourceFromClasspath(String name) throws IOException, NotFoundException {
			// Get resource URL
			URL resourceUrl = getClass().getClassLoader().getResource(name);
			if (resourceUrl == null) {
				throw new NotFoundException("Resource " + name + " not found in class-path");
			}

			// Get input stream and read the bytes
			URLConnection uc = (URLConnection)resourceUrl.openConnection();
			InputStream is = uc.getInputStream();
			byte[] resource = getResourceFromStream(is);
			
			return resource;
		}

		private byte[] getResourceFromFile(File file) throws IOException, NotFoundException {
			if (!file.exists()) {
				throw new NotFoundException("Resource " + file.getName() + " not found");
			}
			
			return getResourceFromStream(new FileInputStream(file));
		}
		
		private byte[] getResourceFromStream(InputStream is) throws IOException {
	        long length = is.available();
	        byte[] bytes = new byte[(int)length];
	    
	        int offset = 0;
	        int numRead = 0;
	        while (offset < bytes.length
	        		&& (numRead = is.read(bytes, offset, Math.min(bytes.length - offset, 512*1024))) >= 0) { 
	            offset += numRead;
	        }
	    
	        if (offset < bytes.length) { 
	        	throw new IOException("Could not completely read the resource"); 
	        }
	        
	        is.close();
	        return bytes;			
		}
	} // END of inner class ServiceComponent
	
	
	/**
	 * Inner class ResourceManagementHelperImpl
	 */	
	private class ResourceManagementHelperImpl implements ResourceManagementHelper {

		public void init(Agent a) {
		}

		public byte[] getResource(String name) throws ServiceException, NotFoundException {
			return getResource(name, ResourceManagementHelper.ALL_RESOURCES, null);
		}

		public byte[] getResource(String name, int fetchMode) throws ServiceException, NotFoundException {
			return getResource(name, fetchMode, null);
		}

		public byte[] getResource(String name, String containerName) throws ServiceException, NotFoundException {
			return getResource(name, ResourceManagementHelper.ALL_RESOURCES, containerName);
		}

		public byte[] getResource(String name, int fetchMode, String containerName) throws ServiceException, NotFoundException {
			// If not specified send request to Main-Container
			if (containerName == null) {
				containerName = MAIN_SLICE;
			}
			
			// Get slice for specific container
			ResourceManagementSlice slice = (ResourceManagementSlice)getSlice(containerName);
			if (slice == null) {
				throw new ServiceException("getResource called with a wrong container name (" + containerName + ")");
			}
			
			// Get resource
			byte[] resource;
			try {
				try {
					resource = slice.getResource(name, fetchMode);
				} 
				catch (IMTPException imtpe) {
					// Try to get a newer slice and repeat...
					slice = (ResourceManagementSlice) getFreshSlice(containerName);
					resource = slice.getResource(name, fetchMode);
				}
			} catch (NotFoundException nfe) {
				throw nfe;
			} catch (ServiceException se) {
				throw se;
			} catch (Throwable t) {
				throw new ServiceException("Error accessing resource " + name + " from " + slice.getNode().getName() + " with fetch-mode " + fetchMode, t);
			}
			
			return resource;
		}
	} // END of inner class ResourceManagementHelperImpl

}

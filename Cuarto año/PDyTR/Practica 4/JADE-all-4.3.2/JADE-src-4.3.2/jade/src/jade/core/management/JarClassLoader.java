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

package jade.core.management;

//#J2ME_EXCLUDE_FILE

import jade.util.Logger;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.IOException;
import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.jar.JarFile;
import java.util.zip.ZipEntry;

/**
 * This ClassLoader is intended to be used to load agent classes
 * packed within a jar file. If the specified jar does not exist this ClassLoader
 * will attempt to load classes from the system ClassLoader.
 * 
 * @author <a href="mailto:jcucurull@deic.uab.cat">Jordi Cucurull Juan</a>
 * @author <a href="mailto:Joan.Ametller@gmail.com">Joan Ametller Esquerra </a>
 * 
 * @version 1.2
 */
public class JarClassLoader extends ClassLoader {

	public static final int BUFFER_SIZE = 1024;
	
	private Logger myLogger = Logger.getMyLogger(getClass().getName());

	//private JarFile _jarFile = null;
	//private File _file = null;
	
	private JarFile[] jarFiles;
	private File[] files;

	/**
	 * @param Path and name of the JAR file
	 * @throws IOException If there are problems opening the file
	 */
	public JarClassLoader(File f, ClassLoader parent) throws IOException {
		//super(parent);
		//_file = f;
		//_jarFile = new JarFile(_file);
		this(new File[]{f}, parent);
	}
	
	public JarClassLoader(File[] ff, ClassLoader parent) throws IOException {
		super(parent);
		files = (ff != null ? ff : new File[0]);
		jarFiles = new JarFile[files.length];
		for (int i = 0; i < files.length; ++i) {
			jarFiles[i] = new JarFile(files[i]);
		}
	}
	/**
	 * Method which close the JAR file.
	 */
	public void close() {
		for (int i = 0; i < files.length; ++i) {
			try {
				jarFiles[i].close();
			} catch (IOException ioe) {
				myLogger.log(Logger.WARNING, "Error closing Jar file: " + jarFiles[i].getName());
			}
		}
		//try {
		//	_jarFile.close();
		//} catch (IOException ioe) {
		//	System.out.println("JarClassLoader: Error closing Jar file: " + _jarFile.getName());
		//}
		
	}
	
	/**
	 * Get the Jar filename.
	 * @return String - Jar absolute path.
	 */
	public String getJarFileName() {
		if (files != null && files.length > 0) {
			return files[0].getAbsolutePath();
		}
		else {
			return null;
		}
		//return _file.getAbsolutePath();
	}

	/**
	 * Get a class from within the JAR file used in this classloader.
	 */
	protected Class findClass(String className) throws ClassNotFoundException {

		String resourceName = className.replace('.', '/') + ".class";
		
		InputStream is = getResourceAsStream(resourceName);
				
		if (is != null) {
			try {
				byte[] rawClass = readFully(is);
				is.close();
				return defineClass(className, rawClass, 0, rawClass.length);
			} catch (IOException ioe) {
				throw new ClassNotFoundException("Error getting class " + className + " from JAR file " + getJarFileName() + " " + ioe);
			}
		} else {
			throw new ClassNotFoundException("Class " + className + " not found in JAR file: " + getJarFileName());
		}
	}

	/*protected URL findResource(String name) {
		try {
			String fileUrl = _file.toURL().toString();
			System.out.println("%%%%%%%%%%%% FILE URL is "+fileUrl);
			return new URL("jar:"+fileUrl+"!/"+name);
		}
		catch (MalformedURLException mue) {
			// Should never happen
			mue.printStackTrace();
			return null;
		}
	}*/
	
	/**
	 * Get a resource from within this classloader.
	 *
	public InputStream getResourceAsStream(String name) {
		if (_jarFile != null) {
			
			ZipEntry zEntry = _jarFile.getEntry(name);
			
			try {
				if (zEntry != null) {
					return _jarFile.getInputStream(zEntry);
				} else return null;
			} catch (IOException ioe) {
				return null;
			}
			
		} else {
			return null;
		}
	}*/
	
    public URL findResource(String name) {
        String entryName=(name.startsWith("/"))?name.substring(1):name;
        
    	for (int i = 0; i < jarFiles.length; ++i) {
            ZipEntry zEntry = jarFiles[i].getEntry(name);
            if (zEntry != null) {
                try {
                    return new URL("jar:file:"+jarFiles[i].getName()+"!/"+entryName);
                } 
                catch (MalformedURLException murle) {
                    return null;
                }
            }
    	}
    	return null;
    }

	private byte[] readFully(InputStream is) throws IOException {
		byte[] buffer = new byte[BUFFER_SIZE];
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		int read = 0;

		while ((read = is.read(buffer)) >= 0)
			baos.write(buffer, 0, read);

		return baos.toByteArray();
	}

	/**
	 * Clean up the JarClassLoader. This means closing the JAR file
	 * if it has not explicitly done before by using the provided 
	 * close() method.
	 */
	protected void finalize() throws Throwable {
		
		close();
		// Close the JarFile.
		//_jarFile.close();
		
		super.finalize();
	}

}

package classLoader;

import java.io.File;
import java.net.URL;
import java.net.URLClassLoader;
import java.net.URLStreamHandler;
import java.util.ArrayList;

public class FileClassLoader {
	
	public ArrayList<Class<?>> getClass(String classPath, String[] classNames) throws Exception {
		ArrayList<Class<?>> out = new ArrayList<Class<?>>();
		
		ArrayList<URL> urls = new ArrayList<URL>();
		URLStreamHandler urlStHandler = null;
		File classPathFile = new File(classPath);
		
		urls.add(new URL(null, "file:" + classPathFile.getCanonicalPath() + File.separator, urlStHandler));
		
		try (URLClassLoader urlLoader = new URLClassLoader((URL[])urls.toArray(new URL[urls.size()]))) {
			for (String className : classNames) {
				Class<?> clazz = urlLoader.loadClass(className);
				out.add(clazz);
			}
		}
		return out;
	}
}

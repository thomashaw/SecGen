/*
 * Created on 2003-jul-03
 *
 */
package se.bluefish.blueblog.repository;

import java.util.HashMap;
import java.util.Map;

import se.bluefish.blueblog.formatters.Formatter;

/**
 * Placeholder for information to be sent to 
 * RepositoryManagers. It can probably be used for
 * more than it is at the moment...
 * 
 * @author Robert Burén
 */
public class RepositoryContext {
	private Map formatters = new HashMap();

	/**
	 * Register a Formatter instance by name.
	 */
	void addFormatter(Formatter f) {
		formatters.put(f.getName(), f);
	}
	
	/**
	 * Gets a Formatter if one with the same name has been
	 * registered.
	 */
	public Formatter getFormatter(String name) {
		return (Formatter)formatters.get(name);
	}
}

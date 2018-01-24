/*
 * Created on Jul 3, 2003
 *
 * To change the template for this generated file go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
package net.eyde.personalblog.beans;

/**
 * @author NEyde
 *
 * To change the template for this generated type comment go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
public class BlogProperty {

	private String name;
	private String value;
	private String description;
	private int version; // the Hibernate version number

	/**
	 * @return
	 */
	public String getName() {
		return name;
	}

	/**
	 * @return
	 */
	public String getValue() {
		return value;
	}

	/**
	 * @param string
	 */
	public void setName(String string) {
		name = string;
	}

	/**
	 * @param string
	 */
	public void setValue(String string) {
		value = string;
	}

	/**
	 * @return
	 */
	public String getDescription() {
		return description;
	}

	/**
	 * @param string
	 */
	public void setDescription(String string) {
		description = string;
	}

	/**
	 * @return
	 */
	public int getVersion() {
		return version;
	}

	/**
	 * @param i
	 */
	public void setVersion(int i) {
		version = i;
	}

}

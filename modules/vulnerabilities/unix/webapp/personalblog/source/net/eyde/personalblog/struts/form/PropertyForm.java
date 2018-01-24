package net.eyde.personalblog.struts.form;

import net.eyde.personalblog.beans.*;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.apache.struts.action.ActionForm;

import java.util.List;


/**
 * @author NEyde
 *
 * To change the template for this generated type comment go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
public final class PropertyForm extends ActionForm {
    private static Log log = LogFactory.getLog(PropertyForm.class);
    private List BlogProperties = null;

    public PropertyForm() {
        log.debug("Property Form Constructor");
    }

    /**
     * @return
     */
    public List getBlogProperties() {
        log.debug("retrieving properties");

        return BlogProperties;
    }

    /**
     * @param list
     */
    public void setBlogProperties(List list) {
        log.debug("setting properties");

        BlogProperties = list;
    }

    /**
     * @return
     */
    public String getDescriptionIndexed(int index) {
        log.debug("getting property for index: " + index);

        BlogProperty prop = (BlogProperty) BlogProperties.get(index);

        log.debug("returning property name: " + prop.getDescription());

        return (prop.getDescription());
    }

    /**
     * @return
     */
    public String getNameIndexed(int index) {
        log.debug("getting property for index: " + index);

        BlogProperty prop = (BlogProperty) BlogProperties.get(index);

        log.debug("returning property name: " + prop.getName());

        return (prop.getName());
    }

    /**
     * @return
     */
    public String getValueIndexed(int index) {
        log.debug("getting property for index: " + index);

        BlogProperty prop = (BlogProperty) BlogProperties.get(index);

        log.debug("returning property value: " + prop.getValue());

        return (prop.getValue());
    }

    /**
     * @param string
     */
    public void setDescriptionIndexed(int index, String value) {
        BlogProperty prop = (BlogProperty) BlogProperties.get(index);
        prop.setDescription(value);
        BlogProperties.set(index, prop);
    }

    /**
     * @param string
     */
    public void setNameIndexed(int index, String value) {
        BlogProperty prop = (BlogProperty) BlogProperties.get(index);
        prop.setName(value);
        BlogProperties.set(index, prop);
    }

    /**
     * @param string
     */
    public void setValueIndexed(int index, String value) {
        BlogProperty prop = (BlogProperty) BlogProperties.get(index);
        prop.setValue(value);
        BlogProperties.set(index, prop);
    }
}

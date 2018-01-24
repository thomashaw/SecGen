/* Vocabulary Class generated by Jena vocabulary generator
 *
 * Version $Id: LABEL.java,v 1.1 2003/08/12 16:59:44 leo Exp $
 * On: Mon Jul 14 03:27:07 CEST 2003
 */
package org.snipsnap.serialization.rdf.vocabulary;

import com.hp.hpl.mesa.rdf.jena.common.ErrorHelper;
import com.hp.hpl.mesa.rdf.jena.common.PropertyImpl;
import com.hp.hpl.mesa.rdf.jena.common.ResourceImpl;
import com.hp.hpl.mesa.rdf.jena.model.Model;
import com.hp.hpl.mesa.rdf.jena.model.Resource;
import com.hp.hpl.mesa.rdf.jena.model.Property;
import com.hp.hpl.mesa.rdf.jena.model.RDFException;

/** LABEL vocabulary class for namespace http://snipsnap.org/rdf/label-schema#
 */
public class LABEL {

    protected static final String uri ="http://snipsnap.org/rdf/label-schema#";

    /** returns the URI for this schema
     * @return the URI for this schema
     */
    public static String getURI() {
          return uri;
    }
           static String nSnipLabel = "SnipLabel";
    public static Resource SnipLabel;
           static String nTypeLabel = "TypeLabel";
    public static Resource TypeLabel;
           static String nsnipLabels = "snipLabels";
    public static Property snipLabels;

    static {
        try {
            SnipLabel = new ResourceImpl(uri, nSnipLabel);
            TypeLabel = new ResourceImpl(uri, nTypeLabel);
            snipLabels = new PropertyImpl(uri, nsnipLabels);
        } catch (Exception e) {
            ErrorHelper.logInternalError("RDF", 1, e);
        }
    }
}

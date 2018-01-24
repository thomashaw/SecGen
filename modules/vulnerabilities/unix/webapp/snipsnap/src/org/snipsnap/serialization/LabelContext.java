package org.snipsnap.serialization;

import org.snipsnap.snip.Snip;
import org.snipsnap.snip.label.Label;

/**
 * A class holding context information about a Label. Subclasses can add all sorts of things so that
 * LabelSerializers can make use of it.
 *
 * @author gis
 */

public class LabelContext {
  public Label label;
  public Snip snip;

  public LabelContext() {
  }

  public LabelContext(Snip snip, Label label) {
    this.snip = snip;
    this.label = label;
  }

  public Snip getSnip() {
    return snip;
  }

  public Label getLabel() {
    return label;
  }
}

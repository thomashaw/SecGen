package pebble.blog;

/**
 * An interface implemented by any object that is permalinkable (i.e. can
 * be reached by a permalink).
 *
 * @author Simon Brown
 */
public interface Permalinkable {

  /**
   * Gets the permalink for this object.
   *
   * @return  a URL as a String
   */
  public String getPermalink();
  
}

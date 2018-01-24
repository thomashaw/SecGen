package pebble.decorator;

import pebble.blog.BlogEntry;

/**
 * Disables comments and TrackBacks for the blog entry.
 * 
 * @author Simon Brown
 */
public class DisableResponseDecorator extends BlogEntryDecoratorSupport {

  /**
   * Executes the logic associated with this decorator.
   *
   * @param chain   the chain of BlogEntryDecorators to apply
   * @param context     the context in which the decoration is running
   * @throws BlogEntryDecoratorException
   *          if something goes wrong when running the decorator
   */
  public void decorate(BlogEntryDecoratorChain chain, BlogEntryDecoratorContext context)
      throws BlogEntryDecoratorException {
    BlogEntry blogEntry = context.getBlogEntry();
    blogEntry.setCommentsEnabled(false);
    blogEntry.setTrackBacksEnabled(false);
    
    chain.decorate(context);
  }

  /**
   * Gets the name of this decorator.
   *
   * @return the name of this decorator as a String
   */
  public String getName() {
    return "Disable response decorator";
  }

}

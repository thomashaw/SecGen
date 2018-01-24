package pebble.decorator;

import java.util.Iterator;

import pebble.blog.BlogEntry;
import pebble.blog.Comment;
import pebble.blog.TrackBack;
import pebble.util.StringUtils;

/**
 * Decorates blog entries and comments by rendering them as HTML.
 * 
 * @author Simon Brown
 */
public class HtmlDecorator extends BlogEntryDecoratorSupport {

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

    // render comments if in detail mode
    if (context.getView() == BlogEntryDecoratorContext.DETAIL_VIEW) {
      Iterator it = blogEntry.getComments().iterator();
      while (it.hasNext()) {
        Comment comment = (Comment)it.next();
        comment.setBody(StringUtils.transformToHTMLSubset(StringUtils.transformHTML(comment.getBody())));
      }

      it = blogEntry.getTrackBacks().iterator();
      while (it.hasNext()) {
        TrackBack trackBack = (TrackBack)it.next();
        trackBack.setExcerpt(StringUtils.transformToHTMLSubset(StringUtils.transformHTML(trackBack.getExcerpt())));
      }
    }

    chain.decorate(context);
  }

  /**
   * Gets the name of this decorator.
   *
   * @return the name of this decorator as a String
   */
  public String getName() {
    return "HTML decorator";
  }

}

package pebble.decorator;

import pebble.blog.Blog;
import pebble.blog.BlogEntry;

/**
 * Translates relative URIs in the blog entry body and excerpt into
 * absolute URLs.
 * 
 * @author Simon Brown
 */
public class RelativeUriDecorator extends BlogEntryDecoratorSupport {

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
    if (context.getMedia() != BlogEntryDecoratorContext.DESKTOP_UI) {
      BlogEntry blogEntry = context.getBlogEntry();
      Blog blog = blogEntry.getRootBlog();
      String body = blogEntry.getBody();
      body = body.replaceAll("\\./images/", blog.getUrl() + "images/");
      body = body.replaceAll("\\./files/", blog.getUrl() + "files/");
      blogEntry.setBody(body);

      String excerpt = blogEntry.getExcerpt();
      excerpt = excerpt.replaceAll("\\./images/", blog.getUrl() + "images/");
      excerpt = excerpt.replaceAll("\\./files/", blog.getUrl() + "files/");
      blogEntry.setExcerpt(excerpt);
    }

    chain.decorate(context);
  }

  /**
   * Gets the name of this decorator.
   *
   * @return the name of this decorator as a String
   */
  public String getName() {
    return "Relative URI decorator";
  }

}

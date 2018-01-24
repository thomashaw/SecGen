package pebble.decorator;

import java.util.Iterator;

import org.radeox.api.engine.RenderEngine;
import org.radeox.api.engine.context.InitialRenderContext;
import org.radeox.api.engine.context.RenderContext;
import org.radeox.engine.BaseRenderEngine;
import org.radeox.engine.context.BaseInitialRenderContext;
import pebble.blog.BlogEntry;
import pebble.blog.Comment;

/**
 * Decorates blog entries and comments by rendering them with Radeox.
 *
 * @author    Simon Brown
 */
public class RadeoxDecorator extends BlogEntryDecoratorSupport {

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
    InitialRenderContext initialContext = new BaseInitialRenderContext();
    initialContext.set(RenderContext.INPUT_LOCALE, getBlog().getLocale());
    RenderEngine engineWithContext = new BaseRenderEngine(initialContext);
    String result = engineWithContext.render(blogEntry.getBody(), initialContext);
    blogEntry.setBody(result);

    // now also render comments if in detail mode
    if (context.getView() == BlogEntryDecoratorContext.DETAIL_VIEW) {
      Iterator it = blogEntry.getComments().iterator();
      while (it.hasNext()) {
        Comment comment = (Comment)it.next();
        result = engineWithContext.render(comment.getBody(), initialContext);
        comment.setBody(result);
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
    return "Radeox decorator";
  }

}

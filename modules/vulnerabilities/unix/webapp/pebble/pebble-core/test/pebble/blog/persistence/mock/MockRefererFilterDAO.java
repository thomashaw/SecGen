package pebble.blog.persistence.mock;

import java.util.Collection;
import java.util.HashSet;

import pebble.blog.Blog;
import pebble.blog.RefererFilter;
import pebble.blog.persistence.PersistenceException;
import pebble.blog.persistence.RefererFilterDAO;

/**
 * A mock implementation of the CategoryDAO interface that does nothing. This
 * is used when performing unit tests.
 *
 * @author    Simon Brown
 */
public class MockRefererFilterDAO implements RefererFilterDAO {

  /**
   * Loads the referer filters.
   *
   * @param rootBlog    the owning Blog instance
   * @return  a Collection of RefererFilter instances
   * @throws  PersistenceException    if filters cannot be loaded
   */
  public Collection getRefererFilters(Blog rootBlog) throws PersistenceException {
    return new HashSet();
  }

  /**
   * Adds the specified referer filter.
   *
   * @param filter    the RefererFilter instance to be added
   * @param rootBlog    the owning Blog instance
   * @throws PersistenceException   if something goes wrong storing the filters
   */
  public void addRefererFilter(RefererFilter filter, Blog rootBlog) throws PersistenceException {
  }

  /**
   * Removes the specified referer filter.
   *
   * @param filter    the RefererFilter instance to be removed
   * @param rootBlog    the owning Blog instance
   * @throws PersistenceException   if something goes wrong removing the filter
   */
  public void deleteRefererFilter(RefererFilter filter, Blog rootBlog) throws PersistenceException {
  }

}

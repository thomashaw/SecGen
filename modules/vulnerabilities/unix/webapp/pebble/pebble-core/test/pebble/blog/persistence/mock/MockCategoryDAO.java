package pebble.blog.persistence.mock;

import java.util.Collection;
import java.util.HashSet;

import pebble.blog.Blog;
import pebble.blog.Category;
import pebble.blog.persistence.CategoryDAO;
import pebble.blog.persistence.PersistenceException;

/**
 * A mock implementation of the CategoryDAO interface that does nothing. This
 * is used when performing unit tests.
 *
 * @author    Simon Brown
 */
public class MockCategoryDAO implements CategoryDAO {

  /**
   * Gets the categories for a particular blog.
   *
   * @param rootBlog    the owning Blog instance
   * @return  a Collection of Category instances
   * @throws  PersistenceException    if categories cannot be loaded
   */
  public Collection getCategories(Blog rootBlog) throws PersistenceException {
    return new HashSet();
  }

  /**
   * Adds the specified category.
   *
   * @param category    the Category instance to be added
   * @param rootBlog    the owning Blog instance
   * @throws PersistenceException   if something goes wrong storing the category
   */
  public void addCategory(Category category, Blog rootBlog) throws PersistenceException {
  }

  /**
   * Updates the specified category.
   *
   * @param updatedCategory   the Category instance to be updated
   * @param rootBlog    the owning Blog instance
   * @throws PersistenceException   if something goes wrong storing the category
   */
  public void updateCategory(Category updatedCategory, Blog rootBlog) throws PersistenceException {
  }

  /**
   * Removes the specified category.
   *
   * @param category    the Category instance to be removed
   * @param rootBlog    the owning Blog instance
   * @throws PersistenceException   if something goes wrong removing the category
   */
  public void deleteCategory(Category category, Blog rootBlog) throws PersistenceException {
  }

}

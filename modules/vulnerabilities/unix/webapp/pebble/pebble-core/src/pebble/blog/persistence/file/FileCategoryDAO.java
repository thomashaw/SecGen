package pebble.blog.persistence.file;

import java.io.*;
import java.util.*;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import pebble.blog.Blog;
import pebble.blog.Category;
import pebble.blog.persistence.CategoryDAO;
import pebble.blog.persistence.PersistenceException;

public class FileCategoryDAO implements CategoryDAO {

  /** the name of the file containing the category information */
  private static final String CATEGORIES_FILE = "blog.categories";

  /** the log used by this class */
  private static Log log = LogFactory.getLog(FileCategoryDAO.class);

  private Properties categories;

  /**
   * Gets the categories for a particular blog.
   *
   * @param rootBlog    the owning Blog instance
   * @return  a Collection of Category instances
   * @throws  PersistenceException    if categories cannot be loaded
   */
  public Collection getCategories(Blog rootBlog) throws PersistenceException {
    Collection coll = new HashSet();

    this.categories = new Properties();
    String root = rootBlog.getRoot();
    try {
      File blogCategoriesFile = new File(root, CATEGORIES_FILE);
      if (!blogCategoriesFile.exists()) {
        return coll;
      }

      FileInputStream fin = new FileInputStream(blogCategoriesFile);
      categories.load(fin);
      fin.close();
    } catch (IOException ioe) {
      log.error("A " + CATEGORIES_FILE + " file at " + root + " cannot be loaded", ioe);
      return coll;
    }

    Enumeration enum = categories.propertyNames();
    while (enum.hasMoreElements()) {
      Category category;
      String id = (String)enum.nextElement();
      String name = categories.getProperty(id);

      category = new Category(id, name);
      coll.add(category);
    }

    return coll;
  }

  /**
   * Adds the specified category.
   *
   * @param category    the Category instance to be added
   * @param rootBlog    the owning Blog instance
   * @throws PersistenceException   if something goes wrong storing the category
   */
  public void addCategory(Category category, Blog rootBlog) throws PersistenceException {
    Collection categories = getCategories(rootBlog);
    categories.add(category);
    store(categories, rootBlog);
  }

  /**
   * Updates the specified category.
   *
   * @param updatedCategory   the Category instance to be updated
   * @param rootBlog    the owning Blog instance
   * @throws PersistenceException   if something goes wrong storing the category
   */
  public void updateCategory(Category updatedCategory, Blog rootBlog) throws PersistenceException {
    Collection categories = getCategories(rootBlog);
    Category category;
    Iterator it = categories.iterator();
    while (it.hasNext()) {
      category = (Category)it.next();

      if (category.getId().equals(updatedCategory.getId())) {
        category.setName(updatedCategory.getName());
      }
    }

    store(categories, rootBlog);
  }

  /**
   * Removes the specified category.
   *
   * @param category    the Category instance to be removed
   * @param rootBlog    the owning Blog instance
   * @throws PersistenceException   if something goes wrong removing the category
   */
  public void deleteCategory(Category category, Blog rootBlog) throws PersistenceException {
    Collection categories = getCategories(rootBlog);
    categories.remove(category);
    store(categories, rootBlog);
  }

  /**
   * Helper method to store all categories for a given blog.
   *
   * @param categories    the Collection of Category instances to store
   * @param rootBlog      the blog to which the categories belong
   */
  private void store(Collection categories, Blog rootBlog) throws PersistenceException {
    try {
      String root = rootBlog.getRoot();
      Properties props = new Properties();
      Iterator it = categories.iterator();
      while (it.hasNext()) {
        Category cat = (Category)it.next();
        props.setProperty(cat.getId(), cat.getName());
      }

      FileOutputStream fout = new FileOutputStream(new File(root, CATEGORIES_FILE));
      props.store(fout, "Categories for " + rootBlog.getName());
      fout.flush();
      fout.close();
    } catch (IOException ioe) {
      log.error(ioe);
      throw new PersistenceException("Categories could not be saved : " + ioe.getMessage());
    }
  }

}

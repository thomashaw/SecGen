package pebble.desktop;

import java.util.List;
import java.util.Vector;
import java.text.DateFormat;

import javax.swing.table.TableModel;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.AbstractTableModel;

import pebble.blog.BlogEntry;

/**
 * ...
 *
 * @author Simon Brown
 */
public class BlogEntryTableModel extends AbstractTableModel {

  private static final String[] COLUMN_NAMES;

  static {
    COLUMN_NAMES = new String[] { "Title", "Date" };
  }

  private List blogEntries;

  public BlogEntryTableModel(List blogEntries) {
    setBlogEntries(blogEntries);
  }

  public void setBlogEntries(List blogEntries) {
    this.blogEntries = blogEntries;
    this.fireTableDataChanged();
  }

  public BlogEntry getBlogEntry(int index) {
    if (index >= 0 && index < blogEntries.size()) {
      return (BlogEntry)blogEntries.get(index);
    } else {
      return null;
    }
  }

  /**
   * Returns the number of rows in this data table.
   *
   * @return the number of rows in the model
   */
  public int getRowCount() {
    return blogEntries.size();
  }

  /**
   * Returns an attribute value for the cell at <code>row</code>
   * and <code>column</code>.
   *
   * @param row    the row whose value is to be queried
   * @param column the column whose value is to be queried
   * @return the value Object at the specified cell
   * @throws ArrayIndexOutOfBoundsException if an invalid row or
   *                                        column was given
   */
  public Object getValueAt(int row, int column) {
    BlogEntry blogEntry = getBlogEntry(row);
    switch (column) {
      case 0 : return blogEntry.getTitle();
      case 1 : return blogEntry.getDate();
      default : return "";
    }
  }

  /**
   * Returns the number of columns in the model. A
   * <code>JTable</code> uses this method to determine how many columns it
   * should create and display by default.
   *
   * @return the number of columns in the model
   * @see #getRowCount
   */
  public int getColumnCount() {
    return COLUMN_NAMES.length;
  }

  /**
   * Returns a default name for the column using spreadsheet conventions:
   * A, B, C, ... Z, AA, AB, etc.  If <code>column</code> cannot be found,
   * returns an empty string.
   *
   * @param column the column being queried
   * @return a string containing the default name of <code>column</code>
   */
  public String getColumnName(int column) {
    return COLUMN_NAMES[column];
  }

}

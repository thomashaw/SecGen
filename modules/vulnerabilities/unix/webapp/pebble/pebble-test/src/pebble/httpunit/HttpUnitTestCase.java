package pebble.httpunit;

import java.io.File;

import junit.framework.TestCase;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.logging.Log;

/**
 * Created by IntelliJ IDEA.
 * User: simon
 * Date: Apr 16, 2004
 * Time: 6:24:57 PM
 * To change this template use File | Settings | File Templates.
 */
public class HttpUnitTestCase extends TestCase {

  private static final Log log = LogFactory.getLog(HttpUnitTestCase.class);

  static {
    // create the "test" blog directory
    File directory = new File("/Users/sandbox/pebble/blogs", "test");
    log.debug("Creating " + directory.getAbsolutePath());
    directory.mkdir();

    Runtime.getRuntime().addShutdownHook(new HttpUnitShutdownHook());
  }

}

class HttpUnitShutdownHook extends Thread {

  private static final Log log = LogFactory.getLog(HttpUnitShutdownHook.class);

  public void run() {
   // create the "test" blog directory
   File directory = new File("/Users/sandbox/pebble/blogs", "test");
   deleteDirectory(directory);
  }

  private void deleteDirectory(File directory) {
    File files[] = directory.listFiles();
    for (int i = 0; i < files.length; i++) {
      if (files[i].isDirectory()) {
        deleteDirectory(files[i]);
      }

      log.debug("Deleting " + files[i].getAbsolutePath());
      files[i].delete();
    }
  }

}

package pebble.httpunit;

import junit.framework.TestCase;
import com.meterware.httpunit.GetMethodWebRequest;
import com.meterware.httpunit.WebRequest;
import com.meterware.httpunit.WebConversation;
import com.meterware.httpunit.WebResponse;

/**
 * Created by IntelliJ IDEA.
 * User: simon
 * Date: Apr 16, 2004
 * Time: 5:39:33 PM
 * To change this template use File | Settings | File Templates.
 */
public class BlogHomePageTest extends HttpUnitTestCase {

  public void testPageAccessible() throws Exception {
    WebConversation wc = new WebConversation();
    WebResponse res = wc.getResponse("http://localhost:8080/blog/blog/");
    assertEquals(200, res.getResponseCode());
  }

}

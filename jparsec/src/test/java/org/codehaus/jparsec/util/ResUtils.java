package org.codehaus.jparsec.util;

import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.io.InputStream;

/**
 * Loading resources.
 *
 * @author Winter Young
 */
public class ResUtils {
  private ResUtils() {}

  public static String loadRes(String classpath) {
    InputStream stream = ResUtils.class.getResourceAsStream(classpath);
    try {
      String str = IOUtils.toString(stream);
      return Strings.replace(str, "\r\n", "\n");
    } catch (IOException e) {
      throw new RuntimeException(e);
    } finally {
      IOUtils.closeQuietly(stream);
    }
  }
}

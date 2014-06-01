/*
 * Created on Jan 3, 2005
 *
 * Author Ben Yu
 */
package org.codehaus.jparsec.util;

import java.io.FileReader;
import java.io.IOException;

/**
 * @author Ben Yu
 *
 * Jan 3, 2005
 */
public final class Utils {

  public static String readFile(final String name) {
    try{
      final FileReader fr = new FileReader(name);
      try{
        final StringBuffer dest = new StringBuffer();
        final char[] buf = new char[4000];
        for(;;) {
          final int n = fr.read(buf);
          if (n > 0) {
            dest.append(buf, 0, n);
          }
          if (n < 0) break;
        }
        return dest.toString();
      }
      finally{
        fr.close();
      }
    }
    catch(IOException e) {
      throw new IllegalStateException(e.getMessage());
    }
  }
}

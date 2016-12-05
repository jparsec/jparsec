/*****************************************************************************
 * Copyright (C) jparsec.org                                                *
 * ------------------------------------------------------------------------- *
 * Licensed under the Apache License, Version 2.0 (the "License");           *
 * you may not use this file except in compliance with the License.          *
 * You may obtain a copy of the License at                                   *
 *                                                                           *
 * http://www.apache.org/licenses/LICENSE-2.0                                *
 *                                                                           *
 * Unless required by applicable law or agreed to in writing, software       *
 * distributed under the License is distributed on an "AS IS" BASIS,         *
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  *
 * See the License for the specific language governing permissions and       *
 * limitations under the License.                                            *
 *****************************************************************************/

package org.jparsec.examples.common;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.CharBuffer;
import java.nio.charset.Charset;

/**
 * Common utilities for working with io.
 * 
 * @author Ben Yu
 */
public final class IoUtils {
  
  public static String read(URL url) throws IOException {
    InputStream in = url.openStream();
    try {
      return read(new InputStreamReader(in, Charset.forName("UTF-8")));
    } finally {
      in.close();
    }
  }
  
  /** Reads all characters from {@code readable}. */
  public static String read(Readable readable) throws IOException {
    StringBuilder builder = new StringBuilder();
    copy(readable, builder);
    return builder.toString();
  }
  
  /** Copies all content from {@code from} to {@code to}. */
  public static void copy(Readable from, Appendable to) throws IOException {
     CharBuffer buf = CharBuffer.allocate(2048);
     for (;;) {
       int r = from.read(buf);
       if (r == -1) break;
       buf.flip();
       to.append(buf, 0, r);
     }
  }
}

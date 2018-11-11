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
package org.jparsec.internal.util;

import static java.util.Arrays.asList;


/**
 * Internal utility for {@link String} operation.
 * 
 * @author Ben Yu
 */
public final class Strings {

  /** Joins {@code objects} with {@code delim} as the delimiter. */
  public static String join(String delim, Object[] objects) {
    // Do not use varargs to prevent some silly compiler warnings.
    if (objects.length == 0) return "";
    return join(new StringBuilder(), delim, objects).toString();
  }

  /** Joins {@code objects} with {@code delim} as the delimiter. */
  public static StringBuilder join(StringBuilder builder, String delim, Object[] objects) {
    return join(builder, delim, asList(objects));
  }

  /** Joins {@code objects} with {@code delim} as the delimiter. */
  public static StringBuilder join(StringBuilder builder, String delim, Iterable<?> objects) {
    int i = 0;
    for (Object obj : objects) {
      if (i++ > 0) builder.append(delim);
      builder.append(obj);
    }
    return builder;
  }
}

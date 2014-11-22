/*****************************************************************************
 * Copyright (C) Codehaus.org                                                *
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
package org.codehaus.jparsec.util;

/**
 * Internal utility for {@link String} operation.
 * 
 * @author Ben Yu
 */
public final class Strings {
  /**
   * Represents a failed index search.
   */
  public static final int INDEX_NOT_FOUND = -1;

  /** Joins {@code objects} with {@code delim} as the delimiter. */
  public static String join(String delim, Object[] objects) {
    // Do not use varargs to prevent some silly compiler warnings.
    if (objects.length == 0) return "";
    return join(new StringBuilder(), delim, objects).toString();
  }

  /** Joins {@code objects} with {@code delim} as the delimiter. */
  public static StringBuilder join(StringBuilder builder, String delim, Object[] objects) {
    int i = 0;
    for (Object obj : objects) {
      if (i++ > 0) builder.append(delim);
      builder.append(obj);
    }
    return builder;
  }

  /**
   * <p>Replaces all occurrences of a String within another String.</p>
   *
   * <p>A {@code null} reference passed to this method is a no-op.</p>
   *
   * <pre>
   * StringUtils.replace(null, *, *)        = null
   * StringUtils.replace("", *, *)          = ""
   * StringUtils.replace("any", null, *)    = "any"
   * StringUtils.replace("any", *, null)    = "any"
   * StringUtils.replace("any", "", *)      = "any"
   * StringUtils.replace("aba", "a", null)  = "aba"
   * StringUtils.replace("aba", "a", "")    = "b"
   * StringUtils.replace("aba", "a", "z")   = "zbz"
   * </pre>
   *
   * @see #replace(String text, String searchString, String replacement, int max)
   * @param text  text to search and replace in, may be null
   * @param searchString  the String to search for, may be null
   * @param replacement  the String to replace it with, may be null
   * @return the text with any replacements processed,
   *  {@code null} if null String input
   */
  public static String replace(final String text, final String searchString, final String replacement) {
    return replace(text, searchString, replacement, -1);
  }


  /**
   * <p>Replaces a String with another String inside a larger String,
   * for the first {@code max} values of the search String.</p>
   *
   * <p>A {@code null} reference passed to this method is a no-op.</p>
   *
   * <pre>
   * StringUtils.replace(null, *, *, *)         = null
   * StringUtils.replace("", *, *, *)           = ""
   * StringUtils.replace("any", null, *, *)     = "any"
   * StringUtils.replace("any", *, null, *)     = "any"
   * StringUtils.replace("any", "", *, *)       = "any"
   * StringUtils.replace("any", *, *, 0)        = "any"
   * StringUtils.replace("abaa", "a", null, -1) = "abaa"
   * StringUtils.replace("abaa", "a", "", -1)   = "b"
   * StringUtils.replace("abaa", "a", "z", 0)   = "abaa"
   * StringUtils.replace("abaa", "a", "z", 1)   = "zbaa"
   * StringUtils.replace("abaa", "a", "z", 2)   = "zbza"
   * StringUtils.replace("abaa", "a", "z", -1)  = "zbzz"
   * </pre>
   *
   * @param text  text to search and replace in, may be null
   * @param searchString  the String to search for, may be null
   * @param replacement  the String to replace it with, may be null
   * @param max  maximum number of values to replace, or {@code -1} if no maximum
   * @return the text with any replacements processed,
   *  {@code null} if null String input
   */
  public static String replace(final String text, final String searchString, final String replacement, int max) {
    if (isEmpty(text) || isEmpty(searchString) || replacement == null || max == 0) {
      return text;
    }
    int start = 0;
    int end = text.indexOf(searchString, start);
    if (end == INDEX_NOT_FOUND) {
      return text;
    }
    final int replLength = searchString.length();
    int increase = replacement.length() - replLength;
    increase = increase < 0 ? 0 : increase;
    increase *= max < 0 ? 16 : max > 64 ? 64 : max;
    final StringBuilder buf = new StringBuilder(text.length() + increase);
    while (end != INDEX_NOT_FOUND) {
      buf.append(text.substring(start, end)).append(replacement);
      start = end + replLength;
      if (--max == 0) {
        break;
      }
      end = text.indexOf(searchString, start);
    }
    buf.append(text.substring(start));
    return buf.toString();
  }

  /**
   * <p>Checks if a CharSequence is empty ("") or null.</p>
   *
   * <pre>
   * StringUtils.isEmpty(null)      = true
   * StringUtils.isEmpty("")        = true
   * StringUtils.isEmpty(" ")       = false
   * StringUtils.isEmpty("bob")     = false
   * StringUtils.isEmpty("  bob  ") = false
   * </pre>
   *
   * <p>NOTE: This method changed in Lang version 2.0.
   * It no longer trims the CharSequence.
   * That functionality is available in isBlank().</p>
   *
   * @param cs  the CharSequence to check, may be null
   * @return {@code true} if the CharSequence is empty or null
   */
  public static boolean isEmpty(final CharSequence cs) {
    return cs == null || cs.length() == 0;
  }
}

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
package org.jparsec.error;

import java.util.LinkedHashSet;
import java.util.List;

import org.jparsec.internal.annotations.Private;

/**
 * Reports parser errors in human-readable format. 
 * 
 * @author Ben Yu
 */
final class ErrorReporter {
  
  static String toString(ParseErrorDetails details, Location location) {
    StringBuilder buf = new StringBuilder();
    if (location != null) {
      buf.append("line " + location.line + ", column " + location.column);
    }
    if (details != null) {
      buf.append(":\n");
      if (details.getFailureMessage() != null) {
        buf.append(details.getFailureMessage());
      }
      else if (!details.getExpected().isEmpty()) {
        reportList(buf, details.getExpected());
        buf.append(" expected, ");
        buf.append(details.getEncountered()).append(" encountered.");
      }
      else if (details.getUnexpected() != null) {
        buf.append("unexpected ").append(details.getUnexpected()).append('.');
      }
    }
    return buf.toString();
  }
  
  @Private static void reportList(StringBuilder builder, List<String> messages) {
    if (messages.isEmpty()) return;
    LinkedHashSet<String> set = new LinkedHashSet<String>(messages);
    int size = set.size();
    int i = 0;
    for (String message : set) {
      if (i++ > 0) {
        if (i == size) { // last one
          builder.append(" or ");
        } else {
          builder.append(", ");
        }
      }
      builder.append(message);
    }
  }
}

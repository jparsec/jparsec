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

package org.jparsec;

import java.util.Collections;
import java.util.List;

import org.jparsec.error.ParseErrorDetails;

/**
 * Empty implementation of {@link ParseErrorDetails} for subclasses to override.
 * 
 * @author benyu
 */
class EmptyParseError implements ParseErrorDetails {
  
  private final int index;
  private final String encountered;
  
  EmptyParseError(int index, String encountered) {
    this.index = index;
    this.encountered = encountered;
  }
  
  @Override public final String getEncountered() {
    return encountered;
  }

  @Override public List<String> getExpected() {
    return Collections.emptyList();
  }

  @Override public String getFailureMessage() {
    return null;
  }

  @Override public final int getIndex() {
    return index;
  }

  @Override public String getUnexpected() {
    return null;
  }
}

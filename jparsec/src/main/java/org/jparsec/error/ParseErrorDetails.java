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

import org.jparsec.Parsers;

import java.util.List;

/**
 * Describes details of a parsing error to support fine-grained error handling.
 * 
 * @author Ben Yu
 */
public interface ParseErrorDetails {
  
  /** Returns the 0-based index in the source where the error happened. */
  int getIndex();
  
  /** Returns the physical input encountered when the error happened. */
  String getEncountered();
  
  /** Returns all that are logically expected. */
  List<String> getExpected();
  
  /** Returns what is logically unexpected, or {@code null} if none. */
  String getUnexpected();
  
  /**
   * Returns the error message incurred by {@link Parsers#fail(String)},
   * or {@code null} if none.
   */
  String getFailureMessage();
}

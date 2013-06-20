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

package org.codehaus.jparsec;

/**
 * Implemented by {@link Parser} results that need to be annotated with source location.
 * <p>
 *   This interface works in conjunction with the method {@link org.codehaus.jparsec.Parser#locate()}. Implement this 
 *   interface in the result type of a Parser, then call <code>locate()</code> on the parser: When successful the 
 *   result will get populated with source and location information.
 * </p>
 */
public interface Locatable {

  /**
   * 
   * @param source the parsed string producing this result.
   */
  void setSource(String source);

  /**
   * 
   * @param beginIndex beginning index in the parsed input for this parsed object.
   * @param endIndex end index in the parsed input.
   */
  void setLocation(int beginIndex, int endIndex);
}

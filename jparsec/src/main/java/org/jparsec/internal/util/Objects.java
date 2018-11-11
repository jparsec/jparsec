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

/**
 * Utility functions for any object.
 *
 * @author Ben Yu
 */
public final class Objects {
  /** Gets the has hcode for {@code obj}. 0 is returned if obj is null. */
  public static int hashCode(Object obj) {
    return obj == null ? 0 : obj.hashCode();
  }
  
  /**
   * Compares {@code o1} and {@code o2} for equality. Returns true if both are {@code null} or
   * {@code o1.equals(o2)}.
   */
  public static boolean equals(Object o1, Object o2) {
    return o1 == null ? o2 == null : o1.equals(o2);
  }
  
  /** Checks whether {@code obj} is one of the elements of {@code array}. */
  public static boolean in(Object obj, Object... array) {
    for (Object expected : array) {
      if (obj == expected) {
        return true;
      }
    }
    return false;
  }
}

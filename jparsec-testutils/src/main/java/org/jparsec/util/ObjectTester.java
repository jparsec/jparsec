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
package org.jparsec.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

/**
 * Tests any {@link Object} for {@link Object#equals(Object)} and {@link Object#hashCode()}.
 * 
 * @author Ben Yu
 */
public final class ObjectTester {
  private static final class AnotherType {}
  
  public static void assertEqual(Object obj, Object... values) {
    for (Object value : values) {
      assertEquals(value, obj);
      assertEquals(obj.hashCode(), value.hashCode());
    }
  }
  
  public static void assertNotEqual(Object obj, Object... values) {
    assertFalse(obj.equals(new AnotherType()));
    assertFalse(obj.equals(null));
    for (Object value : values) {
      assertFalse(obj.equals(value));
    }
  }
}

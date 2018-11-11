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

import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * Unit test for {@link Strings}.
 * 
 * @author Ben Yu
 */
public class StringsTest {

  @Test
  public void testJoin() {
    assertEquals("", Strings.join(", ", new Object[0]));
    assertEquals("1", Strings.join(", ", new Object[]{1}));
    assertEquals("1, 2", Strings.join(", ", new Object[]{1, 2}));
  }

  @Test
  public void testJoin_withStringBuilder() {
    assertEquals("", Strings.join(new StringBuilder(), ", ", new Object[0]).toString());
    assertEquals("1", Strings.join(new StringBuilder(), ", ", new Object[]{1}).toString());
    assertEquals("1, 2", Strings.join(new StringBuilder(), ", ", new Object[]{1, 2}).toString());
  }
}

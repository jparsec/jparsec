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

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

/**
 * Unit test for {@link EmptyParseError}.
 * 
 * @author benyu
 */
public class EmptyParseErrorTest {

  @Test
  public void testEmptyParseError() {
    EmptyParseError error = new EmptyParseError(1, "foo");
    assertEquals(1, error.getIndex());
    assertEquals("foo", error.getEncountered());
    assertNull(error.getUnexpected());
    assertNull(error.getFailureMessage());
    assertEquals(0, error.getExpected().size());
  }

}

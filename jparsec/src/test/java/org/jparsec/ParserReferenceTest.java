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
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.Arrays;
import java.util.Collection;

import static org.jparsec.Asserts.assertFailure;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

/**
 * Unit test for {@link Parser.Reference}.
 * 
 * @author Ben Yu
 */
@RunWith(Parameterized.class)
public class ParserReferenceTest {

  @Parameterized.Parameters
  public static Collection<Object[]> data() {
    return Arrays.asList(new Object[] {Parser.Mode.PRODUCTION}, new Object[] {Parser.Mode.DEBUG});
  }

  private final Parser.Mode mode;

  public ParserReferenceTest(Parser.Mode mode) {
    this.mode = mode;
  }

  @Test
  public void testLazy() {
    Parser.Reference<String> ref = Parser.newReference();
    assertNull(ref.get());
    Parser<String> lazyParser = ref.lazy();
    assertEquals("lazy", lazyParser.toString());
    ref.set(Parsers.constant("foo"));
    assertEquals("foo", lazyParser.parse(""));
    ref.set(Parsers.constant("bar"));
    assertEquals("bar", lazyParser.parse(""));
  }

  @Test
  public void testUninitializedLazy() {
    Parser.Reference<String> ref = Parser.newReference();
    assertNull(ref.get());
    assertFailure(mode, ref.lazy(), "", 1, 1, "Uninitialized lazy parser reference");
  }

}

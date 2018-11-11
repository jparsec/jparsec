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

import org.jparsec.util.ObjectTester;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * Unit test for {@link Tokens}.
 * 
 * @author Ben Yu
 */
public class TokensTest {

  @Test
  public void testFragment() {
    Tokens.Fragment fragment = Tokens.fragment("foo", 1);
    assertEquals("foo", fragment.toString());
    assertFragment(1, "foo", fragment);
    ObjectTester.assertEqual(fragment, fragment, Tokens.fragment("foo", 1));
    ObjectTester.assertNotEqual(fragment,
        Tokens.fragment("foo", 2), Tokens.fragment("foo", "1"), Tokens.fragment("bar", 1),
        "foo", 1);
  }

  @Test
  public void testReserved() {
    assertFragment(Tokens.Tag.RESERVED, "foo", Tokens.reserved("foo"));
  }

  @Test
  public void testWord() {
    assertFragment(Tokens.Tag.IDENTIFIER, "word", Tokens.identifier("word"));
  }

  @Test
  public void testDecimal() {
    assertFragment(Tokens.Tag.DECIMAL, "123", Tokens.decimalLiteral("123"));
  }

  @Test
  public void testInteger() {
    assertFragment(Tokens.Tag.INTEGER, "123", Tokens.integerLiteral("123"));
  }

  @Test
  public void testScientificNumber() {
    Tokens.ScientificNotation number = Tokens.scientificNotation("1.0", "2");
    assertEquals("1.0E2", number.toString());
    assertEquals("1.0", number.significand);
    assertEquals("2", number.exponent);
    ObjectTester.assertEqual(number, number, Tokens.scientificNotation("1.0", "2"));
    ObjectTester.assertNotEqual(number,
        Tokens.scientificNotation("1.0", "-2"), Tokens.scientificNotation("2", "2"));
  }
  
  private static void assertFragment(Object tag, String text, Tokens.Fragment fragment) {
    assertEquals(text, fragment.text());
    assertEquals(tag, fragment.tag());
  }
}

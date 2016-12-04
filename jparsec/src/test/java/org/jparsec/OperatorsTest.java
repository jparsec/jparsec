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

import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;

/**
 * Unit test for {@link Operators}.
 * 
 * @author Ben Yu
 */
public class OperatorsTest {

  @Test
  public void testSort() {
    Asserts.assertArrayEquals(Operators.sort());
    Asserts.assertArrayEquals(Operators.sort("="), "=");
    Asserts.assertArrayEquals(Operators.sort("+", "+=", "+", ""), "+=", "+");
    Asserts.assertArrayEquals(Operators.sort("+", "+=", "+++", "-"), "-", "+=", "+++", "+");
    Asserts.assertArrayEquals(Operators.sort("+", "+=", "+++", "-", "-="),
        "-=", "-", "+=", "+++", "+");
  }

  @Test
  public void testLexicon() {
    List<String> ops = Arrays.asList(
        "++", "--", "+", "-", "+=", "-=", "+++",
        "=", "==", "===", "!", "!=", "<", "<=", ">", ">=", "<>");
    Lexicon lexicon = Operators.lexicon(ops);
    for (String op : ops) {
      assertEquals(Tokens.reserved(op), lexicon.word(op));
    }
    for (String op : ops) {
      assertEquals(Tokens.reserved(op), lexicon.tokenizer.parse(op));
    }
  }

}

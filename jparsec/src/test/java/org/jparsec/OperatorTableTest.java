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

import java.util.function.BiFunction;
import java.util.function.Function;

import static org.junit.Assert.*;

/**
 * Unit test for {@link OperatorTable}.
 * 
 * @author Ben Yu
 */
public class OperatorTableTest {
  private static final Parser<?> OP = Parsers.never();
  private static final Parser<Function<Integer, Integer>> UNARY_OP = Parsers.never();
  private static final Parser<BiFunction<Integer, Integer, Integer>> BINARY_OP = Parsers.never();

  @Test
  public void testAssociativityOrder() {
    assertTotalOrder(OperatorTable.Associativity.PREFIX, OperatorTable.Associativity.POSTFIX, OperatorTable.Associativity.LASSOC, OperatorTable.Associativity.NASSOC, OperatorTable.Associativity.RASSOC);
  }

  @Test
  public void testOperatorOrder() {
    assertTotalOrder(
        operator(2, OperatorTable.Associativity.PREFIX), operator(2, OperatorTable.Associativity.POSTFIX),
        operator(2, OperatorTable.Associativity.LASSOC), operator(2, OperatorTable.Associativity.NASSOC), operator(2, OperatorTable.Associativity.RASSOC), 
        operator(1, OperatorTable.Associativity.PREFIX), operator(1, OperatorTable.Associativity.POSTFIX),
        operator(1, OperatorTable.Associativity.LASSOC), operator(1, OperatorTable.Associativity.NASSOC), operator(1, OperatorTable.Associativity.RASSOC));
  }

  @Test
  public void testGetOperators() {
    OperatorTable<Integer> table = new OperatorTable<Integer>()
       .infixl(BINARY_OP, 2)
       .infixr(BINARY_OP, 1)
       .prefix(UNARY_OP, 4)
       .postfix(UNARY_OP, 3)
       .postfix(UNARY_OP, 3)
       .infixn(BINARY_OP, 5);
    assertNotNull(table);
    OperatorTable.Operator[] operators = table.operators();
    assertEquals(6, operators.length);
    assertEquals(5, operators[0].precedence);
    assertEquals(4, operators[1].precedence);
    assertEquals(3, operators[2].precedence);
    assertEquals(3, operators[3].precedence);
    assertEquals(2, operators[4].precedence);
    assertEquals(1, operators[5].precedence);
  }

  private static <T extends Comparable<T>> void assertTotalOrder(T... objects) {
    for (int i = 0; i < objects.length; i++) {
      assertSameOrder(objects[i]);
      for (int j = i + 1; j < objects.length; j++) {
        assertOrder(objects[i], objects[j]);
      }
    }
  }
  
  private static <T extends Comparable<T>> void assertOrder(T obj1, T obj2) {
    assertTrue(obj1 + " should be before " + obj2, obj1.compareTo(obj2) < 0);
    assertTrue(obj2 + " should be after " + obj1, obj2.compareTo(obj1) > 0);
  }
  
  private static <T extends Comparable<T>> void assertSameOrder(T obj) {
    assertEquals(obj + " should be equal to itself", 0, obj.compareTo(obj));
  }
  
  private static OperatorTable.Operator operator(int precedence, OperatorTable.Associativity associativity) {
    return new OperatorTable.Operator(OP, precedence, associativity);
  }
}

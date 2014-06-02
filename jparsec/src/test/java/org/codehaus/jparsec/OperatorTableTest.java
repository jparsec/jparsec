package org.codehaus.jparsec;

import static org.codehaus.jparsec.OperatorTable.Associativity.*;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.codehaus.jparsec.OperatorTable.Associativity;
import org.codehaus.jparsec.OperatorTable.Operator;
import org.codehaus.jparsec.functors.Map;
import org.codehaus.jparsec.functors.Map2;
import org.junit.Test;

/**
 * Unit test for {@link OperatorTable}.
 * 
 * @author Ben Yu
 */
public class OperatorTableTest {
  private static final Parser<?> OP = Parsers.never();
  private static final Parser<Map<Integer, Integer>> UNARY_OP = Parsers.never();
  private static final Parser<Map2<Integer, Integer, Integer>> BINARY_OP = Parsers.never();

  @Test
  public void testAssociativityOrder() {
    assertTotalOrder(PREFIX, POSTFIX, LASSOC, NASSOC, RASSOC);
  }

  @Test
  public void testOperatorOrder() {
    assertTotalOrder(
        operator(2, PREFIX), operator(2, POSTFIX),
        operator(2, LASSOC), operator(2, NASSOC), operator(2, RASSOC), 
        operator(1, PREFIX), operator(1, POSTFIX),
        operator(1, LASSOC), operator(1, NASSOC), operator(1, RASSOC));
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
    Operator[] operators = table.operators();
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
  
  private static Operator operator(int precedence, Associativity associativity) {
    return new Operator(OP, precedence, associativity);
  }
}

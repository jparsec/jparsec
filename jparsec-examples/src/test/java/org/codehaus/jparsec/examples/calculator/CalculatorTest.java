package org.codehaus.jparsec.examples.calculator;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * Unit test for {@link Calculator}.
 * 
 * @author Ben Yu
 */
public class CalculatorTest {

  @Test
  public void testEvaluate() {
    assertResult(1, "1");
    assertResult(1, "(1)");
    assertResult(3, "1+2");
    assertResult(-5, "1+2*-3");
    assertResult(1, "((1-2)/-1)");
  }

  private static void assertResult(int expected, String source) {
    assertEquals(expected, Calculator.evaluate(source));
  }
}

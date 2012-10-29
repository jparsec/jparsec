package org.codehaus.jparsec.examples.calculator;

import org.codehaus.jparsec.examples.calculator.Calculator;

import junit.framework.TestCase;

/**
 * Unit test for {@link Calculator}.
 * 
 * @author Ben Yu
 */
public class CalculatorTest extends TestCase {
  public void testEvaluate() {
    assertResult(1, "1");
    assertResult(1, "(1)");
    assertResult(3, "1+2");
    assertResult(-5, "1+2*-3");
    assertResult(1, "((1-2)/-1)");
  }

  private void assertResult(int expected, String source) {
    assertEquals(expected, Calculator.evaluate(source));
  }
}

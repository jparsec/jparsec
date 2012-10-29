package org.codehaus.jparsec;


import junit.framework.TestCase;

/**
 * Unit test for {@link Operators}.
 * 
 * @author Ben Yu
 */
public class OperatorsTest extends TestCase {
  
  public void testSort() {
    Asserts.assertArrayEquals(Operators.sort());
    Asserts.assertArrayEquals(Operators.sort("="), "=");
    Asserts.assertArrayEquals(Operators.sort("+", "+=", "+", ""), "+=", "+");
    Asserts.assertArrayEquals(Operators.sort("+", "+=", "+++", "-"), "-", "+=", "+++", "+");
    Asserts.assertArrayEquals(Operators.sort("+", "+=", "+++", "-", "-="),
        "-=", "-", "+=", "+++", "+");
  }
  
  public void testLexicon() {
    String[] ops = {"++", "--", "+", "-", "+=", "-=", "+++",
        "=", "==", "===", "!", "!=", "<", "<=", ">", ">=", "<>"};
    Lexicon lexicon = Operators.lexicon(ops);
    for (String op : ops) {
      assertEquals(Tokens.reserved(op), lexicon.word(op));
    }
    for (String op : ops) {
      Asserts.assertParser(lexicon.tokenizer, op, Tokens.reserved(op));
    }
  }
}

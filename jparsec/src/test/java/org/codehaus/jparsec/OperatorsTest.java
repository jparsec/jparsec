package org.codehaus.jparsec;

import java.util.Arrays;
import java.util.List;

import org.junit.Test;

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

package org.codehaus.jparsec;

import static org.codehaus.jparsec.Asserts.assertFailure;
import static org.codehaus.jparsec.Asserts.assertParser;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import org.junit.Test;

/**
 * Unit test for {@link Parser.Reference}.
 * 
 * @author Ben Yu
 */
public class ParserReferenceTest {

  @Test
  public void testLazy() {
    Parser.Reference<String> ref = Parser.newReference();
    assertNull(ref.get());
    Parser<String> lazyParser = ref.lazy();
    assertEquals("lazy", lazyParser.toString());
    ref.set(Parsers.constant("foo"));
    assertParser(lazyParser, "", "foo");
    ref.set(Parsers.constant("bar"));
    assertParser(lazyParser, "", "bar");
  }

  @Test
  public void testUninitializedLazy() {
    Parser.Reference<String> ref = Parser.newReference();
    assertNull(ref.get());
    assertFailure(ref.lazy(), "", 1, 1, "Uninitialized lazy parser reference");
  }

}

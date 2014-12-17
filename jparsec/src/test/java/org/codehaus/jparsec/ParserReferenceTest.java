package org.codehaus.jparsec;

import static org.codehaus.jparsec.Asserts.assertFailure;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.util.Arrays;
import java.util.Collection;

import org.codehaus.jparsec.Parser.Mode;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

/**
 * Unit test for {@link Parser.Reference}.
 * 
 * @author Ben Yu
 */
@RunWith(Parameterized.class)
public class ParserReferenceTest {

  @Parameterized.Parameters
  public static Collection<Object[]> data() {
    return Arrays.asList(new Object[] {Mode.PRODUCTION}, new Object[] {Mode.DEBUG});
  }

  private final Mode mode;

  public ParserReferenceTest(Mode mode) {
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

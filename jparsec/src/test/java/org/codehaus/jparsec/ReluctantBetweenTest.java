package org.codehaus.jparsec;

import static org.codehaus.jparsec.Scanners.isChar;
import static org.junit.Assert.assertEquals;

import java.util.Arrays;
import java.util.Collection;

import org.codehaus.jparsec.Parser.Mode;
import org.codehaus.jparsec.functors.Pair;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

/**
 *
 * @author michael
 */
@RunWith(Parameterized.class)
@SuppressWarnings("deprecation")
public class ReluctantBetweenTest {

  @Parameterized.Parameters
  public static Collection<Object[]> data() {
          return Arrays.asList(new Object[] {Mode.PRODUCTION}, new Object[] {Mode.DEBUG});
  }

  private final Mode mode;

  public ReluctantBetweenTest(Mode mode) {
    this.mode = mode;
  }

  @Test
	public void parsing_input_with_delimiting_character_inside_delimiters () {
    Parser<?> relunctant = Parsers.tuple(
        Scanners.IDENTIFIER.followedBy(Scanners.isChar(':')),
        Scanners.ANY_CHAR.many().source());
    assertEquals(new Pair<String,String>("hello","world)"),
        relunctant.reluctantBetween(Scanners.isChar('('), Scanners.isChar(')')).parse("(hello:world))", mode));
	}

  @Test
	public void parsing_simple_input() {
      assertEquals("hello",
          Scanners.IDENTIFIER.many().source().reluctantBetween(isChar('('), isChar(')'))
          .followedBy(Scanners.ANY_CHAR.skipMany()).parse("(hello)and the rest", mode));
		assertEquals("hello",
		    Scanners.IDENTIFIER.many().source().reluctantBetween(isChar('('), isChar(')')).parse("(hello)", mode));
		assertEquals("hello",
		    Scanners.IDENTIFIER.many().source().reluctantBetween(isChar('('), isChar(')').optional()).parse("(hello", mode));
		assertEquals("",
		    Scanners.IDENTIFIER.many().source().reluctantBetween(isChar('('), isChar(')')).parse("()", mode));
	}

  @Test
	public void parsing_incorrect_input() {
		Asserts.assertFailure(mode,
		    Scanners.IDENTIFIER.many().source().reluctantBetween(isChar('('), isChar(')')),
				"(hello", 1,7);
	}
}

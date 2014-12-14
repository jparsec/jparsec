package org.codehaus.jparsec;

import static org.codehaus.jparsec.Scanners.isChar;

import org.codehaus.jparsec.functors.Pair;
import org.junit.Test;

/**
 *
 * @author michael
 */
@SuppressWarnings("deprecation")
public class ReluctantBetweenTest {

  @Test
	public void parsing_input_with_delimiting_character_inside_delimiters () {
    Parser<?> relunctant = Parsers.tuple(
        Scanners.IDENTIFIER.followedBy(Scanners.isChar(':')),
        Scanners.ANY_CHAR.many().source());
    Asserts.assertParser(relunctant.reluctantBetween(Scanners.isChar('('), Scanners.isChar(')')),
        "(hello:world))", new Pair<String,String>("hello","world)"));
	}

  @Test
	public void parsing_simple_input() {
      Asserts.assertParser(
          Scanners.IDENTIFIER.many().source().reluctantBetween(isChar('('), isChar(')'))
              .followedBy(Scanners.ANY_CHAR.skipMany()),
          "(hello)and the rest", "hello");
		Asserts.assertParser(
		    Scanners.IDENTIFIER.many().source().reluctantBetween(isChar('('), isChar(')')),
				"(hello)", "hello");
		Asserts.assertParser(
		    Scanners.IDENTIFIER.many().source().reluctantBetween(isChar('('), isChar(')').optional()),
				"(hello", "hello");
		Asserts.assertParser(
		    Scanners.IDENTIFIER.many().source().reluctantBetween(isChar('('), isChar(')')),
				"()", "");
	}

  @Test
	public void parsing_incorrect_input() {
		Asserts.assertFailure(
		    Scanners.IDENTIFIER.many().source().reluctantBetween(isChar('('), isChar(')')),
				"(hello", 1,7);
	}
}

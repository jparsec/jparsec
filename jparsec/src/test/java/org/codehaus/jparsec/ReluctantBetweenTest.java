package org.codehaus.jparsec;

import static org.codehaus.jparsec.Scanners.isChar;
import org.codehaus.jparsec.functors.Pair;
import org.junit.Test;

/**
 *
 * @author michael
 */
public class ReluctantBetweenTest {

    @Test
	public void parsing_input_with_delimiting_character_inside_delimiters () {
		Parser<Pair<String,String>> sut = Parsers.tuple(Scanners.IDENTIFIER.followedBy(Scanners.among(":")),
				Scanners.ANY_CHAR.many().source()
				).reluctantBetween(Scanners.isChar('('), Scanners.isChar(')'));
        Asserts.assertParser(sut, "(hello:world))", new Pair<String,String>("hello","world)"));
	}

    @Test
	public void parsing_simple_input() {
		Asserts.assertParser( Scanners.IDENTIFIER.many().source().reluctantBetween(isChar('('), isChar(')')),
				"(hello)", "hello");
		Asserts.assertParser(Scanners.IDENTIFIER.many().source().reluctantBetween(isChar('('), isChar(')').optional()),
				"(hello", "hello");
		Asserts.assertParser( Scanners.IDENTIFIER.many().source().reluctantBetween(isChar('('), isChar(')')),
				"()", "");
	}

    @Test
	public void parsing_incorrect_input() {
		Asserts.assertFailure(Scanners.IDENTIFIER.many().source().reluctantBetween(isChar('('), isChar(')')),
				"(hello", 1,7);
	}

}

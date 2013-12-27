package org.codehaus.jparsec;

import static junit.framework.Assert.assertEquals;
import static org.codehaus.jparsec.Scanners.isChar;
import org.codehaus.jparsec.functors.Pair;

/**
 *
 * @author michael
 */
public class ReluctantBetweenTest {

	public void testReluctantBetween() {
		Parser<Pair<String,String>> sut = Parsers.tuple(Scanners.IDENTIFIER.followedBy(Scanners.among(":")),
				Scanners.ANY_CHAR.many().source()
				).reluctantBetween(Scanners.isChar('('), Scanners.isChar(')'));
		assertEquals( new Pair<String,String>("hello","world)"),
				      sut.parse("(hello:world))") 
		             );
	}
	
	public void testReluctantBetween_simple() {
		Asserts.assertParser( Scanners.IDENTIFIER.many().source().reluctantBetween(isChar('('), isChar(')')),
				"(hello)", "hello");
		Asserts.assertParser( Scanners.IDENTIFIER.many().source().reluctantBetween(isChar('('), isChar(')')),
				"()", "");
	}
	
	public void testReluctantBetween_fail() {
		Asserts.assertFailure(Scanners.IDENTIFIER.many().source().reluctantBetween(isChar('('), isChar(')')),
				"(hello", 1,6);
	}

}

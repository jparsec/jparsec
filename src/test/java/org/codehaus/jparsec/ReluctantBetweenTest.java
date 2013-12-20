package org.codehaus.jparsec;

import static junit.framework.Assert.assertEquals;
import static org.codehaus.jparsec.Scanners.isChar;
import org.codehaus.jparsec.functors.Pair;
import org.junit.Test;

/**
 *
 * @author michael
 */
public class ReluctantBetweenTest {

	@Test
	public void testReluctantBetween() {
		Parser<Pair<String,String>> sut = Parsers.tuple(Scanners.IDENTIFIER.followedBy(Scanners.among(":")),
				Scanners.ANY_CHAR.many().source()
				).reluctantBetween(Scanners.isChar('('), Scanners.isChar(')'));
		assertEquals( new Pair<String,String>("hello","world)"),
				      sut.parse("(hello:world))") 
		             );
	}
	
	@Test
	public void testReluctantBetween_simple() {
		Asserts.assertParser( Scanners.IDENTIFIER.many().source().reluctantBetween(isChar('('), isChar(')')),
				"(hello)", "hello");
		Asserts.assertParser( Scanners.IDENTIFIER.many().source().reluctantBetween(isChar('('), isChar(')')),
				"()", "");
	}
	
	@Test
	public void testReluctantBetween_fail() {
		Asserts.assertFailure(Scanners.IDENTIFIER.many().source().reluctantBetween(isChar('('), isChar(')')),
				"(hello", 1,6);
	}

}

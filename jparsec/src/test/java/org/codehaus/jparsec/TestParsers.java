package org.codehaus.jparsec;

/**
 * {@link Parser}s for testing purpose.
 * 
 * @author Ben Yu
 */
final class TestParsers {
  
  static Parser<Character> isChar(char c) {
    return Scanners.isChar(c).retn(c);
  }
  
  static Parser<Character> areChars(String chars) {
    Parser<Character> parser = Parsers.constant(null);
    for (int i = 0; i < chars.length(); i++) {
      parser = parser.next(isChar(chars.charAt(i)));
    }
    return parser;
  }
}

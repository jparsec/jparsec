package org.codehaus.jparsec;

/**
 * Converts the current return value as a {@link Token} with starting index and length.
 * 
 * @author Ben Yu
 */
final class ToTokenParser extends Parser<Token> {
  private final Parser<?> parser;
  
  ToTokenParser(Parser<?> parser) {
    this.parser = parser;
  }

  @Override boolean apply(ParseContext ctxt) {
    int begin = ctxt.getIndex();
    if (!parser.apply(ctxt)) {
      return false;
    }
    int len = ctxt.getIndex() - begin;
    Token token = new Token(begin, len, ctxt.result);
    ctxt.result = token;
    return true;
  }
  
  @Override public String toString() {
    return parser.toString();
  }
}

package org.codehaus.jparsec.pattern;

class NotStringPattern extends Pattern {

  private final String string;

  public NotStringPattern(String string) {
    this.string = string;
  }

  @Override
  public Pattern derive(char c) {
    if (string.length() > 0 && string.charAt(0) != c)
      return Patterns.notString(string.substring(1));

    return Patterns.NEVER;
  }

  @Override
  public int match(CharSequence src, int begin, int end) {
    if (begin >= end)
      return MISMATCH;
    int matchedLength = StringPattern.matchString(string, src, begin, end);
    if ((matchedLength == Pattern.MISMATCH) || (matchedLength < string.length()))
      return 1;
    else
      return MISMATCH;
  }

  @Override
  public String toString() {
    return "!(" + string + ")";
  }
}

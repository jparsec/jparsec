package org.codehaus.jparsec.pattern;

class PeekPattern extends Pattern {

  private final Pattern pp;

  public PeekPattern(Pattern pp) {
    this.pp = pp;
  }

  @Override public int match(CharSequence src, int begin, int end) {
    if (pp.match(src, begin, end) == Pattern.MISMATCH)
      return Pattern.MISMATCH;
    else return 0;
  }

  @Override public String toString() {
    return "(?:" + pp.toString() + ")";
  }
}

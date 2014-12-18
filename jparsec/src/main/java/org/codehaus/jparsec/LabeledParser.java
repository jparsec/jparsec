package org.codehaus.jparsec;

final class LabeledParser<T> extends Parser<T> {

  private final Parser<T> parser;
  private final String name;
  
  LabeledParser(Parser<T> paser, String name) {
    this.parser = paser;
    this.name = name;
  }

  @Override public Parser<T> label(String overrideName) {
    return new LabeledParser<T>(parser, overrideName);
  }

  @Override boolean apply(ParseContext ctxt) {
    int at = ctxt.at;
    int step = ctxt.step;
    ctxt.getTrace().push(name);
    if (parser.apply(ctxt)) {
      ctxt.traceCurrentResult();
      ctxt.getTrace().pop();
      return true;
    }
    if (ctxt.stillThere(at, step)) ctxt.expected(name);
    ctxt.getTrace().pop();
    return false;
  }

  @Override public String toString() {
    return name;
  }
}

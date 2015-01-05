package org.codehaus.jparsec.parameters;

import org.codehaus.jparsec.Token;

public class ParseLevelState {
  private Token first;
  private Token last;
  private Parameters params;

  public ParseLevelState(Token first, Token last, Parameters params) {
    this.first = first;
    this.last = last;
    this.params = params;
  }
  
  public Token getFirstToken() {
    return first;
  }

  public Token getLastToken() {
    return last;
  }
  
  public Parameters getParams() {
    return params;
  }

}

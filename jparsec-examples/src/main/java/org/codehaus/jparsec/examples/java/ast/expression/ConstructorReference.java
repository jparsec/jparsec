package org.codehaus.jparsec.examples.java.ast.expression;

import org.codehaus.jparsec.examples.common.ValueObject;

public class ConstructorReference extends ValueObject implements Expression {
  public final Expression owner;

  public ConstructorReference(Expression owner) {
    this.owner = owner;
  }
  
  @Override public String toString() {
    return owner + "::new";
  }
}

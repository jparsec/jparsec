package org.codehaus.jparsec.examples.java.ast.expression;

import java.util.List;

import org.codehaus.jparsec.examples.common.Strings;
import org.codehaus.jparsec.examples.common.ValueObject;
import org.codehaus.jparsec.examples.java.ast.type.TypeLiteral;

/**
 * Represents expressions like {@code obj::f}.
 */
public class MethodReference extends ValueObject implements Expression {
  public final Expression owner;
  public final List<TypeLiteral> typeParameters;
  public final String name;

  public MethodReference(Expression owner, List<TypeLiteral> typeParameters, String name) {
    this.owner = owner;
    this.typeParameters = typeParameters;
    this.name = name;
  }
  
  @Override public String toString() {
    return owner + "::"
      + (typeParameters.isEmpty() ? "" : "<" + Strings.join(", ", typeParameters) + ">") + name;
  }
}

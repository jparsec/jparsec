package org.codehaus.jparsec.examples.java.ast.expression;

import java.util.List;
import java.util.Optional;

import org.codehaus.jparsec.examples.common.Strings;
import org.codehaus.jparsec.examples.common.ValueObject;
import org.codehaus.jparsec.examples.java.ast.statement.Statement;
import org.codehaus.jparsec.examples.java.ast.type.TypeLiteral;

/**
 * Represents lambda expressions.
 * 
 * @author Ben Yu
 */
public class LambdaExpression extends ValueObject implements Expression {

  public final List<Parameter> parameters;
  public final Statement body;

  public LambdaExpression(List<Parameter> parameters, Statement body) {
    this.parameters = parameters;
    this.body = body;
  }

  public static final class Parameter extends ValueObject {
    public final Optional<TypeLiteral> type;
    public final String name;
 
    public Parameter(TypeLiteral type, String name) {
      this.type = Optional.of(type);
      this.name = name;
    }
 
    public Parameter(String name) {
      this.type = Optional.empty();
      this.name = name;
    }

    @Override public String toString() {
      return type.map(t -> t + " ").orElse("") + name;
    }
  }

  @Override public String toString() {
    return "(" + Strings.join(", ", parameters) + ") -> " + body;
  }
}

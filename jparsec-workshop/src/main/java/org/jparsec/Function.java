package org.jparsec;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;

import java.util.Collections;
import java.util.List;
import java.util.Objects;


/**
 */
public class Function implements Expr {
  private final Identifier name;
  private final List<Expr> arguments;

  public Function(Identifier name) {
    this.name = name;
    this.arguments = Collections.emptyList();
  }

  public Function(Identifier name, Expr argument, Expr... moreArguments) {
    this.name = name;
    this.arguments = Lists.asList(argument, moreArguments);
  }

  public Function(Identifier name, List<Expr> arguments) {
    this.name = name;
    this.arguments = ImmutableList.copyOf(arguments);
  }

  @Override
  public int hashCode() {
    return Objects.hash(name, arguments);
  }

  @Override
  public boolean equals(Object obj) {

    if (this == obj) {
      return true;
    }

    if ((obj == null) || (getClass() != obj.getClass())) {
      return false;
    }

    final Function other = (Function) obj;

    return Objects.equals(this.name, other.name) && Objects.equals(this.arguments, other.arguments);
  }

  @Override
  public String toString() {
    return "Function{" + "name=" + name + ", arguments=" + arguments + '}';
  }
}

package org.jparsec;

import java.util.Objects;


/**
 */
public class Identifier implements Expr {
  private final String label;

  public Identifier(CharSequence label) {
    this.label = label.toString();
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(label);
  }

  @Override
  public boolean equals(Object obj) {

    if (this == obj) {
      return true;
    }

    if ((obj == null) || (getClass() != obj.getClass())) {
      return false;
    }

    final Identifier other = (Identifier) obj;

    return Objects.equals(this.label, other.label);
  }

  @Override
  public String toString() {
    return "Identifier{" + "label='" + label + '\'' + '}';
  }
}

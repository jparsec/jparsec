package org.codehaus.jparsec.lambda;

public class Abs implements Expr {
  private final String binding;
  private final Expr body;

  public Abs(String binding, Expr body) {
    this.binding = binding;
    this.body = body;
  }


  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    Abs abs = (Abs) o;

    if (!binding.equals(abs.binding)) return false;
    if (!body.equals(abs.body)) return false;

    return true;
  }

  @Override
  public int hashCode() {
    int result = binding.hashCode();
    result = 31 * result + body.hashCode();
    return result;
  }


  @Override
  public String toString() {
    return "λ"+binding+"."+body;
  }
}

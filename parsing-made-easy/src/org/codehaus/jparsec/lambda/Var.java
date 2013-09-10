package org.codehaus.jparsec.lambda;

public class Var implements Expr {
  private final String symbol;

  private Var(String symbol) {
    this.symbol = symbol;
  }

  public static Var var(String symbol) {
    return new Var(symbol);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    Var var = (Var) o;

    if (!symbol.equals(var.symbol)) return false;

    return true;
  }

  @Override
  public int hashCode() {
    return symbol.hashCode();
  }
}

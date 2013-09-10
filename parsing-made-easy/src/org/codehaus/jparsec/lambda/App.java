package org.codehaus.jparsec.lambda;

public class App implements Expr {
  private final Var left;
  private final Var right;

  public App(Var left, Var right) {
    this.left = left;
    this.right = right;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    App app = (App) o;

    if (!left.equals(app.left)) return false;
    if (!right.equals(app.right)) return false;

    return true;
  }

  @Override
  public int hashCode() {
    int result = left.hashCode();
    result = 31 * result + right.hashCode();
    return result;
  }
}

package org.codehaus.jparsec;

/**
 * @author Stepan Koltsov
 */
public class WithSource<T> {
  private final T value;
  private final String source;

  public WithSource(T value, String source) {
    this.value = value;
    this.source = source;
  }

  public T getValue() {
    return value;
  }

  public String getSource() {
    return source;
  }

  /** Returns the string representation of the token value. */
  @Override public String toString() {
    return String.valueOf(value);
  }

  @Override public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    WithSource<?> that = (WithSource<?>) o;

    if (value != null ? !value.equals(that.value) : that.value != null) return false;
    if (source != null ? !source.equals(that.source) : that.source != null) return false;

    return true;
  }

  @Override public int hashCode() {
    int result = value != null ? value.hashCode() : 0;
    result = 31 * result + (source != null ? source.hashCode() : 0);
    return result;
  }
}

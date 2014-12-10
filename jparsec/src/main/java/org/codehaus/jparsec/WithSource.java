package org.codehaus.jparsec;

import static org.codehaus.jparsec.internal.util.Checks.checkNotNull;

import org.codehaus.jparsec.internal.util.Objects;

/**
 * Parsed result with the matched source text.
 *
 * @author Stepan Koltsov
 */
public final class WithSource<T> {
  private final T value;
  private final String source;

  public WithSource(T value, String source) {
    this.value = value;
    this.source = checkNotNull(source);
  }

  /** Returns the parsed result. */
  public T getValue() {
    return value;
  }

  /** Returns the underlying source text. Never null. */
  public String getSource() {
    return source;
  }

  /** Returns the underlying source text. */
  @Override public String toString() {
    return source;
  }

  @Override public boolean equals(Object o) {
    if (o instanceof WithSource<?>) {
      WithSource<?> that = (WithSource<?>) o;
      return Objects.equals(value, that.value)
          && source.equals(that.source);
    }
    return false;
  }

  @Override public int hashCode() {
    return Objects.hashCode(value) * 31 + source.hashCode();
  }
}

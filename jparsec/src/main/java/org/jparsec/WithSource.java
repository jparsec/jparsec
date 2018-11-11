/*****************************************************************************
 * Copyright (C) jparsec.org                                                *
 * ------------------------------------------------------------------------- *
 * Licensed under the Apache License, Version 2.0 (the "License");           *
 * you may not use this file except in compliance with the License.          *
 * You may obtain a copy of the License at                                   *
 *                                                                           *
 * http://www.apache.org/licenses/LICENSE-2.0                                *
 *                                                                           *
 * Unless required by applicable law or agreed to in writing, software       *
 * distributed under the License is distributed on an "AS IS" BASIS,         *
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  *
 * See the License for the specific language governing permissions and       *
 * limitations under the License.                                            *
 *****************************************************************************/
package org.jparsec;

import org.jparsec.internal.util.Objects;
import org.jparsec.internal.util.Checks;

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
    this.source = Checks.checkNotNull(source);
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

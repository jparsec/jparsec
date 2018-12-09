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
package org.jparsec.error;

/**
 * Represents a line and column number of a character or token in the source.
 * 
 * @deprecated Prefer to use {@link org.jparsec.SourceLocation} instead.
 * @author Ben Yu
 */
@Deprecated
public final class Location {
  
  /** 1-based line number. */
  public final int line;
  
  /** 1-based column number. */
  public final int column;
  
  /**
   * Creates a {@link Location} instance.
   * 
   * @param line line number
   * @param column column number
   */
  public Location(int line, int column) {
    this.line = line;
    this.column = column;
  }
  
  @Override public boolean equals(Object obj) {
    if (obj instanceof Location) {
      Location other = (Location) obj;
      return line == other.line && column == other.column;
    }
    return false;
  }

  @Override public int hashCode() {
    return line * 31 + column;
  }

  @Override public String toString() {
    return "line " + line + " column "+column;
  }
}

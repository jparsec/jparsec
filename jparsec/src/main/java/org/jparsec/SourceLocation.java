package org.jparsec;

import org.jparsec.error.Location;

/**
 * Represents a location inside the source.
 *
 * <em>Not thread safe</em>.
 *
 * @since 3.1
 */
public class SourceLocation {

  private final int index;
  private final SourceLocator locator;
  private Location location;
  
  SourceLocation(int index, SourceLocator locator) {
    this.index = index;
    this.locator = locator;
  }

  /** Returns the 0-based index within the source. */
  public int getIndex() {
    return index;
  }

  /**
   * Returns the line number of this location. Because this method takes amortized {@code log(n)} time,
   * it's typically a good idea to avoid calling it until the entire source has been successfully parsed.
   */
  public int getLine() {
    return getLocation().line;
  }

  /**
   * Returns the column number of this location. Because this method takes amortized {@code log(n)} time,
   * it's typically a good idea to avoid calling it until the entire source has been successfully parsed.
   */
  public int getColumn() {
    return getLocation().column;
  }
  
  private Location getLocation() {
    if (location == null) {
      location = locator.locate(index);
    }
    return location;
  }
}

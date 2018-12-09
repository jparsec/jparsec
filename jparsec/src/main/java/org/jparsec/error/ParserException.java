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

import org.jparsec.ParseTree;
import org.jparsec.Parser;

/**
 * Is thrown when any grammar error happens or any exception is thrown during parsing.
 * 
 * @author Ben Yu
 */
@SuppressWarnings("serial")
public class ParserException extends RuntimeException {
  
  private final ParseErrorDetails error;
  private final Location location;
  private ParseTree parseTree = null;
  @Deprecated private final String module;
  
  /**
   * Creates a {@link ParserException} object.
   * 
   * @param details the {@link ParseErrorDetails} that describes the error details.
   * @param location the error location.
   */
  public ParserException(ParseErrorDetails details,  Location location) {
    this(details, null, location);
  }
  
  /**
   * Creates a {@link ParserException} object.
   * 
   * @param details the {@link ParseErrorDetails} that describes the error details.
   * @param moduleName the module name.
   * @param location the error location.
   */
  @Deprecated
  public ParserException(ParseErrorDetails details,  String moduleName, Location location) {
    super(toErrorMessage(null, moduleName, details, location));
    this.error = details;
    this.module = moduleName;
    this.location = location;
  }

  /**
   * Creates a {@link ParserException} object.
   * 
   * @param cause the exception that causes this.
   * @param details the {@link ParseErrorDetails} that describes the error details.
   * @param moduleName the module name.
   * @param location the location.
   */
  @Deprecated
  public ParserException(
      Throwable cause, ParseErrorDetails details, String moduleName, Location location) {
    super(toErrorMessage(cause.getMessage(), moduleName, details, location), cause);
    this.error = details;
    this.location = location;
    this.module = moduleName;
  }

  /**
   * Returns the detailed description of the error, or {@code null} if none.
   */
  public ParseErrorDetails getErrorDetails() {
    return error;
  }

  /**
   * Returns the parse tree until the parse error happened, when 
   * {@link Parser#parseTree parseTree()} was invoked.
   * {@code null} if absent.
   *
   * @since 2.3
   */
  public ParseTree getParseTree() {
    return parseTree;
  }

  /** @since 2.3 */
  public void setParseTree(ParseTree parseTree) {
    this.parseTree = parseTree;
  }
  
  private static String toErrorMessage(
      String message, String module, ParseErrorDetails details, Location location) {
    StringBuilder buf = new StringBuilder();
    if (message != null && message.length() > 0) {
      buf.append(message).append('\n');
    }
    if (module != null) {
      buf.append('(').append(module).append(") ");
    }
    buf.append(ErrorReporter.toString(details, location));
    return buf.toString();
  }
  
  /** Returns the module name, or {@code null} if none. */
  @Deprecated
  public String getModuleName() {
    return module;
  }
  
  /**
   * Returns the location of the error.
   *
   * @deprecated Use {@link #getLine} and {@link #getColumn} instead.
   */
  @Deprecated
  public Location getLocation() {
    return location;
  }

  /**
   * Returns the line where the error occurred.
   *
   * @since 3.1
   */
  public final int getLine() {
    return location.line;
  }


  /**
   * Returns the column where the error occurred.
   *
   * @since 3.1
   */
  public final int getColumn() {
    return location.column;
  }
}

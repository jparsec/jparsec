/*****************************************************************************
 * Copyright (C) Codehaus.org                                                *
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
package org.codehaus.jparsec;

import org.codehaus.jparsec.error.ParserException;

/**
 * Represents a parse result that possibly contains some error result.
 *
 * @author Winter Young
 * @since 3.0
 */
public class ParseResult<T> {
  /**
   * The result on a successful parse.
   */
  private T result;
  /**
   * The exception on a failed parse.
   */
  private ParserException exception;

  public T getResult() {
    return result;
  }

  public void setResult(T result) {
    this.result = result;
  }

  public ParserException getException() {
    return exception;
  }

  public void setException(ParserException exception) {
    this.exception = exception;
  }

  @Override
  public String toString() {
    return "ParseResult{" +
        "result=" + result +
        ", exception=" + exception +
        '}';
  }

  /**
   * Create an instance of <code>ParseResult</code>.
   */
  public static <T> ParseResult<T> create() {
    return new ParseResult<T>();
  }
}

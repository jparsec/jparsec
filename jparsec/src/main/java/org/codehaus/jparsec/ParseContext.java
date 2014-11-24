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

import org.codehaus.jparsec.error.ParseErrorDetails;

import java.util.List;

/**
 * Represents the context state during parsing.
 *
 * @author Winter Young
 */
interface ParseContext {
  String EOF = "EOF";

  int getAt();

  void setAt(int at);

  int getPartialMatchedEnd();

  void setPartialMatchedEnd(int partialMatchedEnd);

  int getStep();

  void setStep(int step);

  Object getResult();

  void setResult(Object result);

  String getModule();

  CharSequence getSource();

  SourceLocator getLocator();

  boolean suppressError(boolean value);

  int errorIndex();

  ErrorType errorType();

  List<Object> errors();

  ParseErrorDetails renderError();

  String getEncountered();

  void setEncountered(String encountered);

  /** Returns the string representation of the current input (character or token). */
  String getInputName(int pos);

  boolean isEof();

  int getIndex();

  /** Returns the current token. Only applicable to token level parser. */
  Token getToken();

  /** Peeks the current character. Only applicable to character level parser. */
  char peekChar();

  /** Translates the logical position to physical index in the original source. */
  int toIndex(int pos);

  void trap();

  void fail(String message);

  void expected(Object what);

  void unexpected(String what);

  void set(int step, int at, Object ret);

  void setErrorState(
      int errorAt, int errorIndex, ErrorType errorType, List<Object> errors);

  void setAt(int step, int at);

  void next();

  void next(int n);

  /** Reads the characters as input. Only applicable to character level parsers. */
  CharSequence characters();

  public enum ErrorType {
    /** Default value, no error. */
    NONE(false),

    /** When {@link org.codehaus.jparsec.Parsers#never()} is called. Only for creating a trap programmatically. */
    TRAP(false),

    /** When {@link org.codehaus.jparsec.Parser#not()} is called. Signals that something isn't expected. */
    UNEXPECTED(false),

    /** When any expected input isn't found. */
    EXPECTED(true),

    /** When {@link org.codehaus.jparsec.Parsers#fail(String)} is called. Signals a serious problem. */
    FAILURE(false),

    /** When {@link org.codehaus.jparsec.Parsers#expect(String)} or {@link org.codehaus.jparsec.Parser#label(String)} is called. */
    EXPECT(true);

    ErrorType(boolean mergeable) {
      this.mergeable = mergeable;
    }

    final boolean mergeable;
  }
}

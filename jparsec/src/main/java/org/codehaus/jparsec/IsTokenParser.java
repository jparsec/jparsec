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

final class IsTokenParser<T> extends Parser<T> {
  private final TokenMap<? extends T> fromToken;

  IsTokenParser(TokenMap<? extends T> fromToken) {
    this.fromToken = fromToken;
  }

  @Override boolean apply(final ParseContext ctxt) {
    if (ctxt.isEof()) {
      ctxt.expected(fromToken);
      return false;
    }
    Token token = ctxt.getToken();
    Object v = fromToken.map(token);
    if (v == null) {
      ctxt.expected(fromToken);
      return false;
    }
    ctxt.result = v;
    ctxt.next();
    return true;
  }
  
  @Override public String toString() {
    return fromToken.toString();
  }
}
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

import static org.assertj.core.api.Assertions.assertThat;

import org.codehaus.jparsec.error.ParserException;

import org.junit.Test;


public class CypherParserTest {

  private final CypherParser parser = new CypherParser();

  @Test
  public void parsesIdentifier() throws Exception {
    assertThat(parser.parse("joe")).isEqualTo(new Identifier("joe"));
    assertThat(parser.parse("joe67_")).isEqualTo(new Identifier("joe67_"));
  }

  @Test(expected = ParserException.class)
  public void rejectsIdentifierStartingWithANumber() throws Exception {
    parser.parse("67joe");
  }


  @Test
  public void parsesFunctionCall() throws Exception {
    assertThat(parser.parse("foo()")).isEqualTo(new Function(new Identifier("foo")));
    assertThat(parser.parse("foo(bar)")).isEqualTo(new Function(new Identifier("foo"), new Identifier("bar")));
    assertThat(parser.parse("foo(bar,baz,qix)")).isEqualTo(new Function(new Identifier("foo"), new Identifier("bar"),
        new Identifier("baz"), new Identifier("qix")));
    assertThat(parser.parse("foo(bar,baz(qix))")).isEqualTo(new Function(new Identifier("foo"), new Identifier("bar"),
        new Function(new Identifier("baz"), new Identifier("qix"))));
  }

}

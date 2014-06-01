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

import org.junit.Test;

import static org.fest.assertions.Assertions.assertThat;

public class IncrementalParserTest {

  @Test
  public void is_not_done_given_single_char_parser_when_fed_empty_input() throws Exception {
    assertThat(Scanners.isChar('a').incrementally().parse("").isDone()).isFalse();
  }
  
  @Test
  public void is_done_given_single_char_parser_when_fed_expected_char() throws Exception {
    assertThat(Scanners.isChar('a').incrementally().parse("a").isDone()).isTrue();
  }

  @Test
  public void is_failed_given_single_char_parser_when_fed_unexpected_char() throws Exception {
    Parser.Incremental<Void> parsed = Scanners.isChar('a').incrementally().parse("b");
    
    assertThat(parsed.isDone()).isTrue();
    assertThat(parsed.isFailed()).isTrue();
  }

}

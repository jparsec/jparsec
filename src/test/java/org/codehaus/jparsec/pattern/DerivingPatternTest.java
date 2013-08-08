/*****************************************************************************
 * Copyright 2013 (C) Codehaus.org                                                *
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
package org.codehaus.jparsec.pattern;

import org.junit.Test;

import static org.codehaus.jparsec.pattern.Pattern.MISMATCH;
import static org.codehaus.jparsec.pattern.Patterns.*;
import static org.fest.assertions.Assertions.assertThat;


public class DerivingPatternTest {

  @Test
  public void single_char_derives_to_empty_string_if_match() throws Exception {
    assertThat(isChar('a').derive('a').match("", 0, 0)).isEqualTo(0);
    assertThat(isChar('a').derive('b').match("", 0, 0)).isEqualTo(MISMATCH);
  }

  @Test
  public void char_predicate_derives_to_empty_string_if_match() throws Exception {
    assertThat(isChar(CharPredicates.IS_ALPHA).derive('a').match("", 0, 0)).isEqualTo(0);
    assertThat(isChar(CharPredicates.IS_ALPHA).derive('-').match("", 0, 0)).isEqualTo(MISMATCH);
  }

  @Test
  public void string_derives_to_one_char_shorter_string_if_match() throws Exception {
    assertThat(string("abc").derive('a').match("bc", 0, 2)).isEqualTo(2);
    assertThat(string("abc").derive('b').match("bc", 0, 2)).isEqualTo(MISMATCH);
  }

  @Test
  public void case_insensitive_string_derives_to_one_char_shorter_if_char_matches_without_case() throws Exception {
    assertThat(stringCaseInsensitive("abc").derive('A').match("bc", 0, 2)).isEqualTo(2);
    assertThat(stringCaseInsensitive("abc").derive('B').match("bc", 0, 2)).isEqualTo(MISMATCH);
  }

  @Test
  public void empty_string_always_derives_to_empty_pattern() throws Exception {
    assertThat(string("").derive('a').match("", 0, 0)).isEqualTo(MISMATCH);
  }

  @Test
  public void not_string_derives_to_one_char_shorter_if_char_does_not_match() throws Exception {
    assertThat(notString("abc").derive('b').match("cb", 0, 2)).isEqualTo(1);
    assertThat(notString("abc").derive('a').match("cb", 0, 2)).isEqualTo(MISMATCH);
    assertThat(notString("").derive('a')).isEqualTo(NEVER);
  }

  @Test
  public void case_insensitive_not_string_derives_to_one_char_shorter_if_char_does_not_match() throws Exception {
    assertThat(notStringCaseInsensitive("abc").derive('B').match("cb", 0, 2)).isEqualTo(1);
    assertThat(notStringCaseInsensitive("abc").derive('A').match("cb", 0, 2)).isEqualTo(MISMATCH);
    assertThat(notStringCaseInsensitive("").derive('A')).isEqualTo(NEVER);
  }

  @Test
  public void eof_derives_to_empty_pattern() throws Exception {
    assertThat(Patterns.EOF.derive('a').match("", 0, 0)).isEqualTo(MISMATCH);
  }

  @Test
  public void always_derives_to_empty_pattern() throws Exception {
    assertThat(Patterns.ALWAYS.derive('a').match("", 0, 0)).isEqualTo(MISMATCH);
  }

  @Test
  public void or_combines_derivative_of_combined_parsers() throws Exception {
    Pattern abOrAcDerivedFromA = string("ab").or(string("ac")).derive('a');
    assertThat(abOrAcDerivedFromA.match("b", 0, 1)).isEqualTo(1);
    assertThat(abOrAcDerivedFromA.match("c", 0, 1)).isEqualTo(1);

    Pattern abOrBcDerivedFromA = string("ab").or(string("bc")).derive('a');
    assertThat(abOrBcDerivedFromA.match("b", 0, 1)).isEqualTo(1);
    assertThat(abOrBcDerivedFromA.match("c", 0, 1)).isEqualTo(MISMATCH);

    Pattern abOrBcDerivedFromB = string("ab").or(string("bc")).derive('b');
    assertThat(abOrBcDerivedFromB.match("b", 0, 1)).isEqualTo(MISMATCH);
    assertThat(abOrBcDerivedFromB.match("c", 0, 1)).isEqualTo(1);
  }

  @Test
  public void nullable_derives_to_empty_pattern() throws Exception {
    assertThat(Patterns.nullable(string("")).derive('a').match("", 0, 0)).isEqualTo(MISMATCH);
  }

  @Test
  public void sequence_of_string_derives_union_of_first_derivative_and_second_derivative_with_nullable() throws Exception {
    Pattern abcd = string("ab").next(string("cd"));
    assertThat(abcd.derive('a').match("bcd", 0, 3)).isEqualTo(3);
    assertThat(abcd.derive('a').derive('b').match("cd", 0, 2)).isEqualTo(2);
    assertThat(abcd.derive('a').derive('b').derive('c').match("d", 0, 1)).isEqualTo(1);
  }

  @Test
  public void optional_derives_to_empty_string_if_not_match() throws Exception {
    Pattern optionalAb = string("ab").optional();
    assertThat(optionalAb.derive('a').match("b", 0, 1)).isEqualTo(1);
    assertThat(optionalAb.derive('b').match("", 0, 0)).isEqualTo(0);
  }

  @Test
  public void has_exact_derives_to_number_of_expected_chars_minus_1() throws Exception {
    assertThat(Patterns.hasExact(2).derive((char) 0).match("a", 0, 1)).isEqualTo(1);
    assertThat(Patterns.hasExact(1).derive((char) 0).match("a", 0, 1)).isEqualTo(MISMATCH);
    assertThat(Patterns.hasExact(0).derive((char) 0)).isEqualTo(NEVER);
  }

  @Test
  public void has_at_least_derives_to_number_of_expected_chars_minus_1() throws Exception {
    assertThat(Patterns.hasAtLeast(2).derive((char) 0).match("abc", 0, 3)).isEqualTo(1);
    assertThat(Patterns.hasAtLeast(3).derive((char) 0).match("c", 0, 1)).isEqualTo(MISMATCH);
  }

  @Test
  public void sequence_with_optional_first_derives_second_and_first() throws Exception {
    Pattern optionalAbcd = string("ab").optional().next(string("cd"));
    assertThat(optionalAbcd.derive('c').match("d", 0, 1)).isEqualTo(1);
    assertThat(optionalAbcd.derive('a').match("bcd", 0, 3)).isEqualTo(3);
  }

  @Test
  public void many_pattern_derives_to_derived_element_followed_by_many() throws Exception {
    Pattern abStar = Patterns.many(string("ab"));
    assertThat(abStar.derive('a').match("babab", 0, 5)).isEqualTo(5);
    assertThat(abStar.derive('b').match("babab", 0, 5)).isEqualTo(MISMATCH);
    assertThat(abStar.derive('a').derive('b').match("", 0, 0)).isEqualTo(0);
  }

  @Test
  public void many_char_predicates_derives_to_derived_element_followed_by_many() throws Exception {
    Pattern manyDigits = Patterns.many(CharPredicates.IS_DIGIT);
    assertThat(manyDigits.derive('0').match("12345", 0, 5)).isEqualTo(5);
    assertThat(manyDigits.derive('b').match("12345", 0, 5)).isEqualTo(MISMATCH);
  }

  @Test
  public void escaped_derives_to_any_char_if_escape_char_matches() throws Exception {
    assertThat(Patterns.ESCAPED.derive('\\').match("a", 0, 1)).isEqualTo(1);
    assertThat(Patterns.ESCAPED.derive('a').match("a", 0, 1)).isEqualTo(MISMATCH);
  }

  @Test
  public void repeat_pattern_derives_to_number_of_expected_patterns_minus_1_prefixed_with_derived_element() throws Exception {
    assertThat(Patterns.repeat(3, string("ab")).derive('a').match("babab", 0, 5)).isEqualTo(5);
    assertThat(Patterns.repeat(3, string("ab")).derive('b').match("babab", 0, 5)).isEqualTo(MISMATCH);
    assertThat(Patterns.repeat(0, string("ab")).derive('a').match("", 0, 0)).isEqualTo(MISMATCH);
  }

  @Test
  public void repeat_char_predicate_derives_to_number_of_expected_chars_minus_1_if_char_matches_predicate() throws Exception {
    assertThat(Patterns.repeat(6, CharPredicates.IS_HEX_DIGIT).derive('a').match("123DF", 0, 5)).isEqualTo(5);
    assertThat(Patterns.repeat(6, CharPredicates.IS_HEX_DIGIT).derive('k').match("123DF", 0, 5)).isEqualTo(MISMATCH);
    assertThat(Patterns.repeat(0, CharPredicates.IS_HEX_DIGIT).derive('a').match("", 0, 5)).isEqualTo(MISMATCH);
  }

  @Test
  public void many_with_bound_derives_to_number_of_expected_patterns_minus_1_prefixed_with_derived_element() throws Exception {
    assertThat(Patterns.many(3, string("ab")).derive('a').match("bababab", 0, 7)).isEqualTo(7);
    assertThat(Patterns.many(0, string("ab")).derive('a').match("bababab", 0, 7)).isEqualTo(7);
    assertThat(Patterns.many(2, string("ab")).derive('b').match("bababab", 0, 7)).isEqualTo(MISMATCH);
  }

  @Test
  public void many_char_predicates_with_bound_derives_to_number_of_expected_chars_minus_1_if_char_matches_predicate() throws Exception {
    assertThat(Patterns.many(2, CharPredicates.IS_HEX_DIGIT).derive('a').match("123DF", 0, 5)).isEqualTo(5);
    assertThat(Patterns.many(0, CharPredicates.IS_HEX_DIGIT).derive('a').match("123DF", 0, 5)).isEqualTo(5);
    assertThat(Patterns.many(2, CharPredicates.IS_HEX_DIGIT).derive('k').match("123DF", 0, 5)).isEqualTo(MISMATCH);
    assertThat(Patterns.repeat(0, CharPredicates.IS_HEX_DIGIT).derive('a').match("", 0, 5)).isEqualTo(MISMATCH);
  }

  @Test
  public void some_derives_to_number_of_expected_patterns_minus_1_prefixed_with_derived_element() throws Exception {
    assertThat(Patterns.some(3, string("ab")).derive('a').match("babc", 0, 4)).isEqualTo(3);
    assertThat(Patterns.some(2, string("ab")).derive('a').derive('b').match("abc", 0, 3)).isEqualTo(2);
    assertThat(Patterns.some(2, string("ab")).derive('a').derive('b').match("ababc", 0, 3)).isEqualTo(2);
  }

  @Test
  public void some_derives_to_empty_string_given_empty_derivation() throws Exception {
    assertThat(Patterns.some(3, string("ab")).derive('b').match("babc", 0, 4)).isEqualTo(0);
  }

  @Test
  public void not_derives_to_not_of_derivative() throws Exception {
    assertThat(Patterns.not(string("abc")).derive('b').match("aa", 0, 2)).isEqualTo(0);
    assertThat(Patterns.not(string("abc")).derive('a').match("bc",0,2)).isEqualTo(MISMATCH);
  }

  @Test
  public void peek_derives_to_peek_of_derivative() throws Exception {
    assertThat(Patterns.peek(string("abc")).derive('b').match("aa", 0, 2)).isEqualTo(MISMATCH);
    assertThat(Patterns.peek(string("abc")).derive('a').match("bc",0,2)).isEqualTo(0);
  }

}

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

import org.codehaus.jparsec.misc.Mapper;
import org.junit.Test;

import java.util.List;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;


public class LocatableParserTest {

  @Test
  public void canProduceAnASTWithSourceForEachNode() throws Exception {
    Parser<?> lparen = Scanners.isChar('(');
    Parser<?> rparen = Scanners.isChar(')');

    Parser<AnnotatedPair<String, String>> pairOfNumbers = Mapper.curry(AnnotatedPair.class).sequence(Scanners.INTEGER, //
        Mapper._(Scanners.isChar(',')), //
        Scanners.INTEGER).between(lparen, rparen).locate().cast();

    Parser<List<AnnotatedPair<String, String>>> listOfPairsOfIntegers = pairOfNumbers.sepBy(Scanners.WHITESPACES);

    List<AnnotatedPair<String, String>> result = listOfPairsOfIntegers.parse("(5,2)  (45,12)");

    assertThat(result.size(), is(2));
    assertThat(result.get(0).endIndex, is(5));
    assertThat(result.get(0).source, is("(5,2)"));
    assertThat(result.get(1).beginIndex, is(7));
  }

  public static class AnnotatedPair<A, B> implements Locatable {

    private final A a;
    private final B b;
    private String source;
    private int beginIndex;
    private int endIndex;

    public AnnotatedPair(A a, B b) {
      this.a = a;
      this.b = b;
    }

    @Override
    public void setSource(String source) {
      this.source = source;
    }

    @Override
    public void setLocation(int beginIndex, int endIndex) {
      this.beginIndex = beginIndex;
      this.endIndex = endIndex;
    }
  }
}

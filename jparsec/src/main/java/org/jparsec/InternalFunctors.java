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

import java.util.function.BiFunction;

import org.jparsec.functors.Map3;
import org.jparsec.functors.Map4;
import org.jparsec.functors.Map5;

/**
 * Functors used only internally by this package. 
 * 
 * @author Ben Yu
 */
final class InternalFunctors {
  
  static TokenMap<Token> tokenWithSameValue(final Object value) {
    return new TokenMap<Token>() {
      @Override public Token map(Token token) {
        return (value == token.value()) ? token : null;
      }
      @Override public String toString() {
        return String.valueOf(value);
      }
    };
  }
  
  @SuppressWarnings("rawtypes")
  private static final BiFunction FIRST_OF_TWO = new BiFunction() {
    @Override public Object apply(Object first, Object b) {
      return first;
    }
    @Override public String toString() {
      return "followedBy";
    }
  };
  
  @SuppressWarnings("rawtypes")
  private static final BiFunction LAST_OF_TWO = new BiFunction() {
    @Override public Object apply(Object a, Object last) {
      return last;
    }
    @Override public String toString() {
      return "sequence";
    }
  };
  
  @SuppressWarnings("rawtypes")
  private static final Map3 LAST_OF_THREE = new Map3() {
    @Override public Object map(Object a, Object b, Object last) {
      return last;
    }
    @Override public String toString() {
      return "sequence";
    }
  };
  
  @SuppressWarnings("rawtypes")
  private static final Map4 LAST_OF_FOUR = new Map4() {
    @Override public Object map(Object a, Object b, Object c, Object last) {
      return last;
    }
    @Override public String toString() {
      return "sequence";
    }
  };
  
  @SuppressWarnings("rawtypes")
  private static final Map5 LAST_OF_FIVE = new Map5() {
    @Override public Object map(Object a, Object b, Object c, Object d, Object last) {
      return last;
    }
    @Override public String toString() {
      return "sequence";
    }
  };
  
  @SuppressWarnings("unchecked")
  static <T, B> BiFunction<T, B, T> firstOfTwo() {
    return FIRST_OF_TWO;
  }
  
  @SuppressWarnings("unchecked")
  static<A, T> BiFunction<A, T, T> lastOfTwo() {
    return LAST_OF_TWO;
  }
  
  @SuppressWarnings("unchecked")
  static<A, B, T> Map3<A, B, T, T> lastOfThree() {
    return LAST_OF_THREE;
  }
  
  @SuppressWarnings("unchecked")
  static<A, B, C, T> Map4<A, B, C, T, T> lastOfFour() {
    return LAST_OF_FOUR;
  }
  
  @SuppressWarnings("unchecked")
  static<A, B, C, D, T> Map5<A, B, C, D, T, T> lastOfFive() {
    return LAST_OF_FIVE;
  }
}

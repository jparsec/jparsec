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
package org.jparsec.functors;

import java.util.Locale;
import java.util.function.Function;
import java.util.function.UnaryOperator;

/**
 * Provides common implementations of {@link Map} interface and the variants.
 * 
 * @author Ben Yu
 */
public final class Maps {
  
  /**
   * The {@link Map} that maps a {@link String} to {@link Integer} by calling
   * {@link Integer#valueOf(String)}.
   *
   * @deprecated Use {@code Integer::valueOf} directly.
   */
  @Deprecated
  public static final Function<String, Integer> TO_INTEGER = Integer::valueOf;

  /** The {@link UnaryOperator} that maps a {@link String} to lower case using {@link Locale#US}. */
  public static UnaryOperator<String> TO_LOWER_CASE = toLowerCase(Locale.US);

  /** Returns a {@link UnaryOperator} that maps a {@link String} to lower case using {@code locale}. */
  public static UnaryOperator<String> toLowerCase(final Locale locale) {
    return  new UnaryOperator<String>() {
      @Override public String apply(String s) {
        return s.toLowerCase(locale);
      }
      @Override public String toString() {
        return "toLowerCase";
      }
    };
  }

  /** The {@link UnaryOperator} that maps a {@link String} to upper case using {@link Locale#US}. */
  public static UnaryOperator<String> TO_UPPER_CASE = toUpperCase(Locale.US);

  /** Returns a {@link UnaryOperator} that maps a {@link String} to upper case using {@code locale}. */
  public static UnaryOperator<String> toUpperCase(Locale locale) {
    return  new UnaryOperator<String>() {
      @Override public String apply(String s) {
        return s.toUpperCase(locale);
      }
      @Override public String toString() {
        return "toUpperCase";
      }
    };
  }

  /**
   * @deprecated Use {@code String::valueOf} directly.
   */
  @Deprecated
  public static <T> Map<T, String> mapToString() {
    return String::valueOf;
  }
  
  /**
   * Returns a {@link Map} that maps the string representation of an enum
   * to the corresponding enum value by calling {@link Enum#valueOf(Class, String)}.
   */
  public static <E extends Enum<E>> Function<String, E> toEnum(Class<E> enumType) {
    return new Function<String, E>() {
      @Override public E apply(String name) {
        return Enum.valueOf(enumType, name);
      }
      @Override public String toString() {
        return "-> " + enumType.getName();
      }
    };
  }
  
  /**
   * Returns an identity map that maps parameter to itself.
   *
   * @deprecated Use {@link Function#identity} instead.
   */
  @Deprecated
  public static <T> UnaryOperator<T> identity() {
    return v -> v;
  }
  
  /**
   * Returns a {@link Map} that always maps any object to {@code v}.
   *
   * @deprecated Use {@code from -> to} directly.
   */
  @Deprecated
  public static <F, T> Function <F, T> constant(T v) {
    return from -> v;
  }
  
  /**
   * Adapts a {@link java.util.Map} to {@link Map}.
   *
   * @deprecated Use {@code Map::get} instead.
   */
  @Deprecated
  public static <K, V> Function<K, V> map(java.util.Map<K, V> m) {
    return m::get;
  }
  
  /** A {@link Map2} object that maps 2 values into a {@link Pair} object. */
  @SuppressWarnings("unchecked")
  public static <A, B> Map2<A, B, Pair<A, B>> toPair() {
    return Pair::new;
  }
  
  /** A {@link Map3} object that maps 3 values to a {@link Tuple3} object. */
  @Deprecated
  @SuppressWarnings("unchecked")
  public static <A, B, C> Map3<A, B, C, Tuple3<A, B, C>> toTuple3() {
    return Tuple3::new;
  }
  
  /** A {@link Map4} object that maps 4 values to a {@link Tuple4} object. */
  @Deprecated
  @SuppressWarnings("unchecked")
  public static <A, B, C, D> Map4<A, B, C, D, Tuple4<A, B, C, D>> toTuple4() {
    return Tuple4::new;
  }
  
  /** A {@link Map5} object that maps 5 values to a {@link Tuple5} object. */
  @SuppressWarnings("unchecked")
  @Deprecated
  public static <A, B, C, D, E> Map5<A, B, C, D, E, Tuple5<A, B, C, D, E>> toTuple5() {
    return Tuple5::new;
  }
  
  private Maps() {}
}

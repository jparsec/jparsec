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
package org.codehaus.jparsec.functors;

import java.util.Locale;

/**
 * Provides common implementations of {@link Map} interface and the variants.
 * 
 * @author Ben Yu
 */
public final class Maps {
  
  /**
   * The {@link Map} that maps a {@link String} to {@link Integer} by calling
   * {@link Integer#valueOf(String)}.
   */
  public static final Map<String, Integer> TO_INTEGER = new Map<String, Integer>() {
    public Integer map(String v) {
      return Integer.valueOf(v);
    }
    @Override public String toString() {
      return "integer";
    }
  };

  /** The {@link Unary} that maps a {@link String} to lower case using {@link Locale#US}. */
  public static Unary<String> TO_LOWER_CASE = toLowerCase(Locale.US);

  /** Returns a {@link Unary} that maps a {@link String} to lower case using {@code locale}. */
  public static Unary<String> toLowerCase(final Locale locale) {
    return  new Unary<String>() {
      public String map(String s) {
        return s.toLowerCase(locale);
      }
      @Override public String toString() {
        return "toLowerCase";
      }
    };
  }

  /** The {@link Unary} that maps a {@link String} to upper case using {@link Locale#US}. */
  public static Unary<String> TO_UPPER_CASE = toUpperCase(Locale.US);

  /** Returns a {@link Unary} that maps a {@link String} to upper case using {@code locale}. */
  public static Unary<String> toUpperCase(final Locale locale) {
    return  new Unary<String>() {
      public String map(String s) {
        return s.toUpperCase(locale);
      }
      @Override public String toString() {
        return "toUpperCase";
      }
    };
  }
  
  /**
   * A {@link Map} instance that maps its parameter to a {@link String} by calling
   * {@link Object#toString()} against it.
   */
  @SuppressWarnings("unchecked")
  public static <T> Map<T, String> mapToString() {
    return TO_STRING;
  }
  
  /**
   * Returns a {@link Map} that maps the string representation of an enum
   * to the corresponding enum value by calling {@link Enum#valueOf(Class, String)}.
   */
  public static <E extends Enum<E>> Map<String, E> toEnum(final Class<E> enumType) {
    return new Map<String, E>() {
      public E map(String name) {
        return Enum.valueOf(enumType, name);
      }
      @Override public String toString() {
        return "-> " + enumType.getName();
      }
    };
  }
  
  /** Returns an identity map that maps parameter to itself. */
  @SuppressWarnings("unchecked")
  public static <T> Unary<T> identity() {
    return (Unary<T>) ID;
  }
  
  /** Returns a {@link Map} that always maps any object to {@code v}. */
  public static <F, T> Map <F, T> constant(final T v) {
    return new Map <F, T>() {
      public T map(F from) { return v; }
      @Override public String toString() {
        return String.valueOf(v);
      }
    };
  }
  
  /** Adapts a {@link java.util.Map} to {@link Map}. */
  public static <K, V> Map<K, V> map(final java.util.Map<K, V> m) {
    return new Map<K, V>() {
      public V map(K k) {
        return m.get(k);
      }
      @Override public String toString() {
        return m.toString();
      }
    };
  }
  
  @SuppressWarnings("unchecked")
  private static final Map2 ID2 = new Map2() {
    public Pair map(Object a, Object b) {
      return Tuples.pair(a, b);
    }
    @Override public String toString() {
      return "pair";
    }
  };
  
  @SuppressWarnings("unchecked")
  private static final Map3 ID3 = new Map3() {
    public Tuple3 map(Object a, Object b, Object c) {
      return Tuples.tuple(a, b, c);
    }
    @Override public String toString() {
      return "tuple";
    }
  };
  
  @SuppressWarnings("unchecked")
  private static final Map4 ID4 = new Map4() {
    public Tuple4 map(Object a, Object b, Object c, Object d) {
      return Tuples.tuple(a, b, c, d);
    }
    @Override public String toString() {
      return "tuple";
    }
  };
      
  @SuppressWarnings("unchecked")
  private static final Map5 ID5 = new Map5() {
    public Tuple5 map(Object a, Object b, Object c, Object d, Object e) {
      return Tuples.tuple(a, b, c, d, e);
    }
    @Override public String toString() {
      return "tuple";
    }
  };
  
  /** A {@link Map2} object that maps 2 values into a {@link Pair} object. */
  @SuppressWarnings("unchecked")
  public static <A, B> Map2<A, B, Pair<A, B>> toPair() {
    return ID2;
  }
  
  /** A {@link Map3} object that maps 3 values to a {@link Tuple3} object. */
  @SuppressWarnings("unchecked")
  public static <A, B, C> Map3<A, B, C, Tuple3<A, B, C>> toTuple3() {
    return ID3;
  }
  
  /** A {@link Map4} object that maps 4 values to a {@link Tuple4} object. */
  @SuppressWarnings("unchecked")
  public static <A, B, C, D> Map4<A, B, C, D, Tuple4<A, B, C, D>> toTuple4() {
    return ID4;
  }
  
  /** A {@link Map5} object that maps 5 values to a {@link Tuple5} object. */
  @SuppressWarnings("unchecked")
  public static <A, B, C, D, E> Map5<A, B, C, D, E, Tuple5<A, B, C, D, E>> toTuple5() {
    return ID5;
  }
  
  private static final Unary<Object> ID = new Unary<Object>() {
    public Object map(Object v) {
      return v;
    }
    @Override public String toString() {return "identity";}
  };
  
  @SuppressWarnings("unchecked")
  private static final Map TO_STRING = new Map<Object, String>() {
    public String map(Object obj) {
      return String.valueOf(obj);
    }
    @Override public String toString() {
      return "toString";
    }
  };
  
  private Maps() {}
}

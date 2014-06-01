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

import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.TreeSet;

import org.codehaus.jparsec.annotations.Private;
import org.codehaus.jparsec.functors.Map;
import org.codehaus.jparsec.functors.Maps;

/**
 * Helper class for creating lexers and parsers for keywords.
 * 
 * @author Ben Yu
 */
final class Keywords {
  
  private interface StringCase {
    Comparator<String> comparator();
    String toKey(String k);
    <T> org.codehaus.jparsec.functors.Map<String, T> toMap(java.util.Map<String, T> m);
  }
  
  private static final StringCase CASE_SENSITIVE = new CaseSensitive();
  private static final StringCase CASE_INSENSITIVE = new CaseInsensitive();
  
  private static class CaseSensitive implements StringCase {
    private static Comparator<String> COMPARATOR = new Comparator<String>() {
      public int compare(String a, String b) {
        if (a == b) return 0;
        else if (a == null) return -1;
        else if (b == null) return 1;
        else return a.compareTo(b);
      }
    };
    public Comparator<String> comparator() {
      return COMPARATOR;
    }
    public String toKey(String k) {
      return k;
    }
    public <T> org.codehaus.jparsec.functors.Map<String, T> toMap(java.util.Map<String, T> m) {
      return Maps.map(m);
    }
  }
  
  private static class CaseInsensitive implements StringCase {
    private static Comparator<String> COMPARATOR = new Comparator<String>() {
      public int compare(String a, String b) {
        if (a == b) return 0;
        else if (a == null) return -1;
        else if (b == null) return 1;
        else return a.compareToIgnoreCase(b);
      }
    };
    public Comparator<String> comparator() {
      return COMPARATOR;
    }
    public String toKey(String k) {
      return k.toLowerCase();
    }
    public <T> org.codehaus.jparsec.functors.Map<String, T> toMap(
        final java.util.Map<String, T> m) {
      return new org.codehaus.jparsec.functors.Map<String,T>() {
        public T map(String key) {
          return m.get(key.toLowerCase());
        }
      };
    }
  }
  
  private static StringCase getStringCase(boolean caseSensitive) {
    return caseSensitive ? CASE_SENSITIVE : CASE_INSENSITIVE;
  }
  
  @Private static String[] unique(Comparator<String> c, String... names) {
    TreeSet<String> set = new TreeSet<String>(c);
    set.addAll(Arrays.asList(names));
    return set.toArray(new String[set.size()]);
  }
  
  @SuppressWarnings("unchecked")
  static <T> Lexicon lexicon(
      Parser<String> wordScanner, String[] keywordNames,
      boolean caseSensitive, final Map<String, ?> defaultMap) {
    StringCase scase = getStringCase(caseSensitive);
    HashMap<String, Object> map = new HashMap<String, Object>();
    for (String n : unique(scase.comparator(), keywordNames)) {
      Object value = Tokens.reserved(n);
      map.put(scase.toKey(n), value);
    }
    final Map<String, Object> fmap = scase.toMap(map);
    Map<String, Object> tokenizerMap = new Map<String, Object>() {
      public Object map(String text) {
        Object val = fmap.map(text);
        if (val != null) return val;
        else return defaultMap.map(text);
      }
    };
    return new Lexicon(fmap, wordScanner.map(tokenizerMap));    
  }
}

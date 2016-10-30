package org.codehaus.jparsec;

import java.util.Comparator;
import java.util.Locale;
import java.util.function.Function;

enum StringCase implements Comparator<String> {
  CASE_SENSITIVE {
    @Override public int compare(String a, String b) {
      return a.compareTo(b);
    }
    @Override String toKey(String k) {
      return k;
    }
  },
  CASE_INSENSITIVE {
    @Override public int compare(String a, String b) {
      return a.compareToIgnoreCase(b);
    }
    @Override public String toKey(String k) {
      return k.toLowerCase(Locale.ENGLISH);
    }
  }
  ;

  abstract String toKey(String k);

  final <T> Function<String, T> byKey(Function<String, T> function) {
    return k -> function.apply(toKey(k));
  }
}
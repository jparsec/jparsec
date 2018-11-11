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

import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.TreeSet;
import java.util.function.Function;

import org.jparsec.internal.annotations.Private;

/**
 * Helper class for creating lexers and parsers for keywords.
 * 
 * @author Ben Yu
 */
final class Keywords {
  
  @Private static String[] unique(Comparator<String> c, String... names) {
    TreeSet<String> set = new TreeSet<String>(c);
    set.addAll(Arrays.asList(names));
    return set.toArray(new String[set.size()]);
  }

  static Lexicon lexicon(
      Parser<String> wordScanner, Collection<String> keywordNames,
      StringCase stringCase, final Function<String, ?> defaultMap) {
    HashMap<String, Object> map = new HashMap<String, Object>();
    for (String n : unique(stringCase, keywordNames.toArray(new String[keywordNames.size()]))) {
      Object value = Tokens.reserved(n);
      map.put(stringCase.toKey(n), value);
    }
    Function<String, Object> keywordMap = stringCase.byKey(map::get);
    return new Lexicon(keywordMap, wordScanner.map(Lexicon.fallback(keywordMap, defaultMap)));    
  }
}

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
package org.jparsec.examples.common;

import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Base class that implements {@link Object#equals(Object)}, {@link Object#hashCode()} and
 * {@code Object#toString()} on final fields.
 * 
 * @author Ben Yu
 */
public abstract class ValueObject {
  
  private volatile List<Object> fieldValues = null;
  
  private List<Object> valueList() {
    if (fieldValues == null) {
      fieldValues = Collections.unmodifiableList(toValueList(this, getValueFields(getClass())));
    }
    return fieldValues;
  }
  
  @Override public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null) {
      return false;
    }
    if (getClass() != obj.getClass()) {
      return false;
    }
    return valueList().equals(((ValueObject) obj).valueList());
  }

  @Override public int hashCode() {
    return valueList().hashCode();
  }

  @Override public String toString() {
    StringBuilder buf = new StringBuilder(getClass().getSimpleName());
    buf.append(" {");
    Field[] fields = getValueFields(getClass());
    int i = 0;
    for (Object value : valueList()) {
      if (i > 0) {
        buf.append(", ");
      }
      buf.append(fields[i++].getName()).append('=').append(value);
    }
    buf.append('}');
    return buf.toString();
  }



  private static final Comparator<Field> NAME_ORDER = new Comparator<Field>() {
    @Override public int compare(Field field1, Field field2) {
      return field1.getName().compareTo(field2.getName());
    }
  };
  
  private static final ConcurrentMap<Class<?>, Field[]> valueFieldMap =
      new ConcurrentHashMap<Class<?>, Field[]>();
      
  private static Field[] getValueFields(Class<?> type) {
    Field[] fields = valueFieldMap.get(type);
    if (fields == null) {
      fields = introspectValueFields(type);
      valueFieldMap.put(type, fields);
    }
    return fields;
  }
  
  private static List<Object> toValueList(Object obj, Field[] fields) {
    List<Object> list = new ArrayList<Object>();
    for (Field field : fields) {
      try {
        list.add(field.get(obj));
      } catch (IllegalAccessException e) {
        throw new RuntimeException(e);
      }
    }
    return list;
  }
  
  private static Field[] introspectValueFields(Class<?> type) {
    if (type == ValueObject.class) {
      return NO_FIELD;
    }
    List<Field> fieldList = new ArrayList<Field>();
    fieldList.addAll(Arrays.asList(introspectValueFields(type.getSuperclass())));
    List<Field> myFields = tail(fieldList);
    for (Field field : type.getDeclaredFields()) {
      if (!Modifier.isStatic(field.getModifiers()) && Modifier.isFinal(field.getModifiers())) {
        myFields.add(field);
      }
    }
    Collections.sort(myFields, NAME_ORDER);
    Field[] fields = fieldList.toArray(new Field[fieldList.size()]);
    AccessibleObject.setAccessible(fields, true);
    return fields;
  }

  private static <T> List<T> tail(List<T> list) {
    int size = list.size();
    return list.subList(size, size);
  }
  
  private static final Field[] NO_FIELD = new Field[0];
}

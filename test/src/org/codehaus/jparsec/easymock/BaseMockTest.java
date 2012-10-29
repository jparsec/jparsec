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
package org.codehaus.jparsec.easymock;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.easymock.EasyMock;
import org.easymock.IMocksControl;

import junit.framework.TestCase;

/**
 * Provides convenient API for using EasyMock.
 * 
 * @author Ben Yu
 */
public abstract class BaseMockTest extends TestCase {

  /** Annotates a field as being mocked. */
  @Retention(RetentionPolicy.RUNTIME)
  @Target(ElementType.FIELD)
  protected @interface Mock {}
  
  private static final Map<Class<?>, List<Field>> mockFields =
      new ConcurrentHashMap<Class<?>, List<Field>>();
  
  private static List<Field> getMockFields(Class<?> type) {
    List<Field> fields = mockFields.get(type);
    if (fields != null) {
      return fields;
    }
    if (type == TestCase.class || type == BaseMockTest.class || type == Object.class) {
      return Arrays.asList();
    }
    fields = new ArrayList<Field>();
    for (Field field : type.getDeclaredFields()) {
      if (field.isAnnotationPresent(Mock.class)) {
        fields.add(field);
      }
    }
    AccessibleObject.setAccessible(fields.toArray(new Field[fields.size()]), true);
    Class<?> superclass = type.getSuperclass();
    if (superclass != null) {
      fields.addAll(getMockFields(superclass));
    }
    fields = Collections.unmodifiableList(fields);
    mockFields.put(type, fields);
    return fields;
  }
  
  private IMocksControl control;
  private boolean replayed;
  private boolean verified;
  
  @Override public void runBare() throws Throwable {
    replayed = false;
    verified = false;
    control = EasyMock.createControl();
    for (Field field : getMockFields(getClass())) {
      field.set(this, mock(field.getType()));
    }
    super.runBare();
  }
  
  @Override protected void runTest() throws Throwable {
    super.runTest();
    if (replayed && !verified) {
      verify();
    }
  }

  /** Returns a mock of {@code type}. */
  protected final <T> T mock(Class<T> type) {
    return control.createMock(type);
  }

  protected final void replay() {
    control.replay();
    replayed = true;
  }
  
  protected final void verify() {
    verified = true;
    control.verify();
  }
}

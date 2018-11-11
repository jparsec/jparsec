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

import org.jparsec.util.ObjectTester;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * Unit test for {@link ValueObject}.
 * 
 * @author Ben Yu
 */
public class ValueObjectTest {
  
  private static final class BadObject {
    
    @Override public boolean equals(Object obj) {
      throw new UnsupportedOperationException();
    }

    @Override public int hashCode() {
      throw new UnsupportedOperationException();
    }

    @Override public String toString() {
      throw new UnsupportedOperationException();
    }
  }
  
  static class Animal extends ValueObject {
    
    static final Object INVALID_OBJECT = new BadObject();
    
    final String sex;
    final int age;
    Object badObject = new BadObject();
    
    Animal(String sex, int age) {
      this.sex = sex;
      this.age = age;
    }
  }
  
  static class Mammal extends Animal {
    Mammal(String sex, int age) {
      super(sex, age);
    }
  }
  
  static class Dog extends Mammal {
    final String name;

    public Dog(String sex, int age, final String name) {
      super(sex, age);
      this.name = name;
    }
  }
  
  static class Person extends ValueObject {
    final String name;

    Person(String name) {
      this.name = name;
    }
  }

  @Test
  public void testEquals() {
    Animal animal = new Animal("male", 1);
    ObjectTester.assertEqual(animal, animal, new Animal("male", 1));
    ObjectTester.assertNotEqual(animal,
        null, new Animal("male", 2), new Animal("female", 1), new Mammal("male", 1), "whatever");
    ObjectTester.assertEqual(new Mammal("female", 2), new Mammal("female", 2));
    ObjectTester.assertEqual(new Dog("male", 1, "tom"), new Dog("male", 1, "tom"));
    ObjectTester.assertNotEqual(new Dog("male", 1, "tom"), null, new Mammal("male", 1), 1);
  }

  @Test
  public void testToString() {
    assertEquals("Dog {age=1, sex=male, name=tom}", new Dog("male", 1, "tom").toString());
    assertEquals("Person {name=ben}", new Person("ben").toString());
  }
}

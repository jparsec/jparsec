package org.codehaus.jparsec.examples.common;

import junit.framework.TestCase;

import org.codehaus.jparsec.examples.common.ValueObject;
import org.codehaus.jparsec.util.ObjectTester;

/**
 * Unit test for {@link ValueObject}.
 * 
 * @author Ben Yu
 */
public class ValueObjectTest extends TestCase {
  
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
  
  public void testEquals() {
    Animal animal = new Animal("male", 1);
    ObjectTester.assertEqual(animal, animal, new Animal("male", 1));
    ObjectTester.assertNotEqual(animal,
        null, new Animal("male", 2), new Animal("female", 1), new Mammal("male", 1), "whatever");
    ObjectTester.assertEqual(new Mammal("female", 2), new Mammal("female", 2));
    ObjectTester.assertEqual(new Dog("male", 1, "tom"), new Dog("male", 1, "tom"));
    ObjectTester.assertNotEqual(new Dog("male", 1, "tom"), null, new Mammal("male", 1), 1);
  }
  
  public void testToString() {
    assertEquals("Dog {age=1, sex=male, name=tom}", new Dog("male", 1, "tom").toString());
    assertEquals("Person {name=ben}", new Person("ben").toString());
  }
}

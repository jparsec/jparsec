package org.codehaus.jparsec.misc;

import net.sf.cglib.reflect.FastConstructor;
import net.sf.cglib.reflect.FastMethod;

/**
 * Implementations of {@Link Invokable}.
 * 
 * @author Ben Yu
 */
final class Invokables {
  
  static Invokable constructor(final FastConstructor constructor) {
    final Class<?> ownerType = constructor.getDeclaringClass();
    final Class<?>[] parameterTypes = constructor.getParameterTypes();
    return new ValueInvokable(ownerType) {
      public Object invoke(Object[] args) throws Throwable {
        return constructor.newInstance(args);
      }
      public Class<?>[] parameterTypes() {
        return parameterTypes;
      }
      public Class<?> returnType() {
        return ownerType;
      }
    };
  }
  
  static Invokable method(final Object self, final FastMethod method) {
    final Class<?> returnType = method.getReturnType();
    final Class<?>[] parameterTypes = method.getParameterTypes();
    return new ValueInvokable(self) {
      public Object invoke(Object[] args) throws Throwable {
        return method.invoke(self, args);
      }
      public Class<?>[] parameterTypes() {
        return parameterTypes;
      }
      public Class<?> returnType() {
        return returnType;
      }
    };
  }
  
  private static abstract class ValueInvokable implements Invokable {
    private final Object value;

    ValueInvokable(Object value) {
      this.value = value;
    }

    @Override public int hashCode() {
      return value.hashCode();
    }

    @Override public boolean equals(Object obj) {
      if (obj instanceof ValueInvokable) {
        return value.equals(((ValueInvokable) obj).value);
      }
      return false;
    }
    
    @Override public String toString() {
      return value.toString();
    }
  }
}

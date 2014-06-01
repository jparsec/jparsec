package org.codehaus.jparsec.misc;

/**
 * Either a constructor or a method.
 * 
 * @author Ben Yu
 */
interface Invokable {
  Class<?> returnType();
  Class<?>[] parameterTypes();
  Object invoke(Object[] args) throws Throwable ;
}

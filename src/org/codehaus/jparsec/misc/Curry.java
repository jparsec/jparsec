package org.codehaus.jparsec.misc;

import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.List;

import net.sf.cglib.reflect.FastClass;

import org.codehaus.jparsec.Parser;
import org.codehaus.jparsec.annotations.Private;
import org.codehaus.jparsec.functors.Binary;
import org.codehaus.jparsec.functors.Unary;
import org.codehaus.jparsec.util.Checks;

/**
 * Curries the only public constructor defined in the {@code T} class and invokes it with
 * parameters returned by the sequentially executed {@link Parser} objects.
 * For example, to parse an expression with binary operator and create an instance of
 * the following object model:
 * <pre>
 * class BinaryExpression implements Expression {
 *   public BinaryExpression(Expression left, Operator op, Expression right) {...}
 *   ...
 * }
 * </pre>
 * The parser that parses this expression with binary operator can be written as:
 * <pre>
 * Parser&lt;Expression> binary(Parser&lt;Expression> expr, Parser&lt;Operator> op) {
 *   return Curry.&lt;Expression>of(BinaryExpression.class).sequence(expr, op, expr);
 * }
 * </pre>
 * Which is equivalent to the more verbose but reflection-free version:
 * <pre>
 * Parser&lt;Expression> binary(Parser&lt;Expression> expr, Parser&lt;Operator> op) {
 *   return Parsers.sequence(expr, op, expr,
 *       new Map3&lt;Expression, Operator, Expression, Expression>() {
 *         public Expression map(Expression left, Operator op, Expression right) {
 *           return new BinaryExpression(left, op, right);
 *         }
 *       });
 * }
 * </pre>
 * 
 * <p> Alternatively, instead of sequencing the operands and operators directly,
 * a {@link Unary} or {@link Binary} instance can be returned to cooperate with
 * {@link org.codehaus.jparsec.OperatorTable}, {@link Parser#prefix(Parser)},
 *  {@link Parser#postfix(Parser)}, {@link Parser#infixl(Parser)},
 * {@link Parser#infixn(Parser)} or {@link Parser#infixr(Parser)}.
 * 
 * <p> NOTE: cglib is required on the classpath.
 * 
 * @author Ben Yu
 */
final class Curry<T> extends Mapper<T> {
  private final Object[] curryArgs;
  private final int[] curryIndexes;
  
  private Curry(
      Object source, Invokable invokable, Object[] curryArgs, int[] curryIndexes) {
    super(source, invokable);
    this.curryArgs = curryArgs;
    this.curryIndexes = curryIndexes;
  }
  
  /**
   * Creates a {@link Curry} object that curries the only public constructor of {@code clazz}
   * with {@code curryArgs} by matching parameter types.
   */
  public static <T> Curry<T> of(Class<? extends T> clazz, Object... curryArgs) {
    Checks.checkArgument(!Modifier.isAbstract(clazz.getModifiers()),
        "Cannot curry abstract class: %s", clazz.getName());
    Constructor<?>[] constructors = clazz.getConstructors();
    Checks.checkArgument(constructors.length == 1,
        "Expecting 1 public constructor in %s, %s encountered.",
        clazz.getName(), constructors.length);
    Checks.checkArgument(!constructors[0].isVarArgs(),
        "Cannot curry for constructor with varargs: %s", constructors[0]);
    Constructor<?> constructor = constructors[0];
    Class<?>[] parameterTypes = constructor.getParameterTypes();
    int[] curryIndexes = new int[curryArgs.length];
    int curry = 0;
    for (Object curryArg : curryArgs) {
      int curryIndex = findCurryIndex(constructor, parameterTypes, curry, curryArg);
      checkDup(curryIndexes, curry, curryIndex, curryArg, constructor);
      curryIndexes[curry++] = curryIndex;
    }
    return new Curry<T>(
        clazz.getName(),
        Invokables.constructor(FastClass.create(clazz).getConstructor(constructor)),
        curryArgs, curryIndexes);
  }

  
  @Override void checkFutureParameters(Class<?> targetType, int providedParameters) {
    int totalProvidedParameters = providedParameters + curryArgs.length;
    int totalExpectedParameters = invokable.parameterTypes().length;
    checkFutureParameters(totalExpectedParameters, targetType, totalProvidedParameters);
  }
  
  /**
   * Two {@link Curry} objects are equal only if they curry the same class and have equal
   * curry arguments.
   */
  @Override public int hashCode() {
    return valueList().hashCode();
  }
  
  /**
   * Two {@link Curry} objects are equal only if they curry the same class and have equal
   * curry arguments.
   */
  @Override public boolean equals(Object obj) {
    if (obj instanceof Curry) {
      return valueList().equals(((Curry) obj).valueList());
    }
    return false;
  }
  
  private List<?> valueList() {
    return Arrays.asList(invokable, Arrays.asList(curryArgs));
  }
  
  private static void checkDup(
      int[] curryIndexes, int curry, int curryIndex, Object curryArg, Constructor<?> constructor) {
    for (int i = 0; i < curry; i++) {
      if (curryIndexes[i] == curryIndex) {
        throw new IllegalArgumentException(
            "More than one curry arguments match the "
            + constructor.getParameterTypes()[curryIndex].getName()
            + " parameter of " + constructor);
      }
    }
  }

  private static int findCurryIndex(
      Constructor<?> constructor, Class<?>[] parameterTypes, int index, Object object) {
    for (int i = 0; i < parameterTypes.length; i++) {
      if (Reflection.isInstance(parameterTypes[i], object)) return i;
    }
    throw new IllegalArgumentException(
        "Curry parameter " + index + " is " + Reflection.getClassName(object)
        + ", which isn't compatible to any parameter of " + constructor);
  }
  
  @Override Object invoke(Object[] args) throws Throwable {
    if (args.length != expectedParams()) {
      throw new IllegalArgumentException(
          expectedParams() + " parameters expected, " + args.length + " provided: "
          + invokable);
    }
    Class<?>[] parameterTypes = invokable.parameterTypes();
    Object[] actualArgs = new Object[parameterTypes.length];
    for (int i = 0, argIndex = 0; i < actualArgs.length; i++) {
      int curryIndex = find(curryIndexes, i);
      if (curryIndex >= 0) {
        actualArgs[i] = curryArgs[curryIndex];
        continue;
      }
      Class<?> parameterType = parameterTypes[i];
      Object arg = args[argIndex];
      checkArgumentType(i, parameterType, arg);
      actualArgs[i] = arg;
      argIndex++;
    }
    return invokable.invoke(actualArgs);
  }
  
  @Private static int find(int[] array, int value) {
    for (int i = 0 ; i < array.length; i++) {
      if (array[i] == value) {
        return i;
      }
    }
    return -1;
  }
  
  @Override int expectedParams() {
    return super.expectedParams() - curryArgs.length;
  }
}

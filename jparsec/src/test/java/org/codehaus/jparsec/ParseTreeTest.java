package org.codehaus.jparsec;

import java.util.Arrays;

import org.junit.Assert;
import org.junit.Test;

public class ParseTreeTest {

  @Test
  public void noChildren() {
    Assert.assertEquals("foo: 123", tree("foo", 123).toString());
  }

  @Test
  public void oneChild() {
    Assert.assertEquals("foo: {\nbar: x\n}", tree("foo", 123, tree("bar", "x")).toString());
  }

  @Test
  public void oneChildren() {
    Assert.assertEquals("foo: {\nbar: x,\nbaz: y\n}",
        tree("foo", 123, tree("bar", "x"), tree("baz", "y")).toString());
  }

  private static ParseTree tree(String name, Object value, ParseTree... children) {
    return new ParseTree(name, 0, 1000, value, Arrays.asList(children));
  }
}

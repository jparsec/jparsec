package org.codehaus.jparsec;

import java.util.Collections;
import java.util.List;

import org.codehaus.jparsec.internal.util.Strings;

/** Represents the parse tree of an unsuccessful parse attempt. */
public final class ParseTree {
  private final String name;
  private final int beginIndex;
  private final int endIndex;
  private final Object value;
  private final List<ParseTree> children;

  ParseTree(
      String name,
      int beginIndex,
      int endIndex,
      Object value,
      List<ParseTree> children) {
    this.name = name;
    this.beginIndex = beginIndex;
    this.endIndex = endIndex;
    this.value = value;
    this.children = Collections.unmodifiableList(children);
  }

  public String getName() {
    return name;
  }

  public int getBeginIndex() {
    return beginIndex;
  }

  public int getEndIndex() {
    return endIndex;
  }

  public Object getValue() {
    return value;
  }

  public List<ParseTree> getChildren() {
    return children;
  }

  @Override public String toString() {
    StringBuilder builder = new StringBuilder(name).append(": ");
    if (children.isEmpty()){
      builder.append(value);
    } else {
      builder.append("{\n");
      Strings.join(builder, ",\n", children);
      builder.append("\n}");
    }
    return builder.toString();
  }
}

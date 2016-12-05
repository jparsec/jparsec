// Copyright 2008 Google Inc. All rights reserved.

package org.jparsec.internal.util;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

/**
 * Unit test for {@link Checks}.
 * 
 * @author Ben Yu
 */
public class ChecksTest {

  @Rule
  public ExpectedException exception = ExpectedException.none();

  @Test
  public void checkArgument_noThrowIfConditionIsTrue() {
    Checks.checkArgument(true, "whatever");
    Checks.checkArgument(true, "whatever", 1, 2);
    Checks.checkArgument(true, "bad format %s and %s", 1);
  }

  @Test
  public void checkArgument_throwsIfConditionIsFalse() {
    exception.expect(IllegalArgumentException.class);
    exception.expectMessage("one = 1");
    Checks.checkArgument(false, "one = %s", 1);
  }

  @Test
  public void checkState_noThrowIfConditionIsTrue() {
    Checks.checkState(true, "whatever");
    Checks.checkState(true, "whatever", 1, 2);
    Checks.checkState(true, "bad format %s and %s", 1);
  }

  @Test
  public void checkState_throwsIfConditionIsFalse() {
    exception.expect(IllegalStateException.class);
    exception.expectMessage("one = 1");
    Checks.checkState(false, "one = %s", 1);
  }

  @Test
  public void checkNotNullState_noThrowIfObjectIsntNull() {
    Checks.checkNotNullState("1", "whatever");
    Checks.checkNotNullState("1", "whatever", 1, 2);
    Checks.checkNotNullState("1", "bad format %s and %s", 1);
  }

  @Test
  public void checkNotNullState_throwsIfObjectIsNull() {
    exception.expect(IllegalStateException.class);
    exception.expectMessage("object = null");
    Checks.checkNotNullState(null, "object = %s", "null");
  }

}

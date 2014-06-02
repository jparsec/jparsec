// Copyright 2008 Google Inc. All rights reserved.

package org.codehaus.jparsec.util;

import junit.framework.TestCase;

/**
 * Unit test for {@link Checks}.
 * 
 * @author Ben Yu
 */
public class ChecksTest extends TestCase {
  
  public void testCheckArgument_noThrowIfConditionIsTrue() {
    Checks.checkArgument(true, "whatever");
    Checks.checkArgument(true, "whatever", 1, 2);
    Checks.checkArgument(true, "bad format %s and %s", 1);
  }
  
  public void testCheckArgument_throwsIfConditionIsFalse() {
    try {
      Checks.checkArgument(false, "one = %s", 1);
      fail();
    } catch (IllegalArgumentException e) {
      assertEquals("one = 1", e.getMessage());
    }
  }
  
  public void testCheckState_noThrowIfConditionIsTrue() {
    Checks.checkState(true, "whatever");
    Checks.checkState(true, "whatever", 1, 2);
    Checks.checkState(true, "bad format %s and %s", 1);
  }
  
  public void testCheckState_throwsIfConditionIsFalse() {
    try {
      Checks.checkState(false, "one = %s", 1);
      fail();
    } catch (IllegalStateException e) {
      assertEquals("one = 1", e.getMessage());
    }
  }

  public void testCheckNotNullState_noThrowIfObjectIsntNull() {
    Checks.checkNotNullState("1", "whatever");
    Checks.checkNotNullState("1", "whatever", 1, 2);
    Checks.checkNotNullState("1", "bad format %s and %s", 1);
  }

  public void testCheckNotNullState_throwsIfObjectIsNull() {
    try {
      Checks.checkNotNullState(null, "object = %s", "null");
      fail();
    } catch (IllegalStateException e) {
      assertEquals("object = null", e.getMessage());
    }
  }
}

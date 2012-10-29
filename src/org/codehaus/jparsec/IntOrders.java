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
package org.codehaus.jparsec;

/**
 * Common {@link IntOrder} implementations.
 * 
 * @author Ben Yu
 */
final class IntOrders {
  
  /**
   * An {@link IntOrder} instance that determines if the first integer is less than the second one.
   * 
   * <p> {@code LT.compare(1, 2) == true}.
   */
  static final IntOrder LT = new IntOrder() {
    public boolean compare(int a, int b) { return a < b; }
    @Override public String toString() {
      return "shortest";
    }
  };
  
  /**
   * An {@link IntOrder} instance that determines if the first integer is smaller than the second
   * one.
   * 
   * <p> {@code GT.compare(2, 1) == true}.
   */
  static final IntOrder GT = new IntOrder() {
    public boolean compare(int a, int b) {return a > b;}
    @Override public String toString() {
      return "longest";
    }
  };
}

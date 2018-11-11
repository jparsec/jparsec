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
package org.jparsec.examples.java.ast.expression;

/**
 * Distinguishes between "L", "F", "D" and int.
 * 
 * @author Ben Yu
 */
public enum NumberType {
  INT(""), LONG("L"), FLOAT("F"), DOUBLE("D");
  
  private final String name;
  
  private NumberType(String name) {
    this.name = name;
  }
  
  @Override public String toString() {
    return name;
  }
}

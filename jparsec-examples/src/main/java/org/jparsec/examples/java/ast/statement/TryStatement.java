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

package org.jparsec.examples.java.ast.statement;

import java.util.List;

import org.jparsec.examples.common.Strings;
import org.jparsec.examples.common.ValueObject;

/**
 * Represents the "try-catch-finally" statement.
 * 
 * @author Ben Yu
 */
public final class TryStatement extends ValueObject implements Statement {
  
  public static final class CatchBlock extends ValueObject {
    public final ParameterDef parameter;
    public final BlockStatement body;
    
    public CatchBlock(ParameterDef parameter, BlockStatement body) {
      this.parameter = parameter;
      this.body = body;
    }
    
    @Override public String toString() {
      return "catch (" + parameter + ") " + body; 
    }
  }
  
  public final BlockStatement tryBlock;
  public final List<CatchBlock> catchBlocks;
  public final BlockStatement finallyBlock;
  
  public TryStatement(
      BlockStatement tryBlock, List<CatchBlock> catchBlocks, BlockStatement finallyBlock) {
    this.tryBlock = tryBlock;
    this.catchBlocks = catchBlocks;
    this.finallyBlock = finallyBlock;
  }
  
  @Override public String toString() {
    return "try " + tryBlock + Strings.prependEach(" ", catchBlocks) + 
      (finallyBlock == null ? "" : " finally " + finallyBlock);
  }
}

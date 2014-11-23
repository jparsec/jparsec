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
 * A parser that first consumes the start and end, and then
 * tries to consume the middle. Useful when the end terminator
 * may be present in the body (e.g. {@code (bodyWithParens ()())())}
 * @author michael
 */
final class ReluctantBetweenParser<T> extends Parser<T> {
	
	private final Parser<?> start;
	private final Parser<T> between;
	private final Parser<?> end;

	public ReluctantBetweenParser(Parser<?> start, Parser<T> between, Parser<?> end) {
		this.start = start;
		this.between = between;
		this.end = end;
	}
	
	@Override
	boolean apply(ParseContext ctxt) {
		boolean r1 = start.run(ctxt);
		if (!r1) return false;
		int betweenAt = ctxt.getAt();
		
		// try to match the end of the sequence beginning from the very end giving a chance to empty parser to be matched
		// (see https://github.com/abailly/jparsec/issues/25)
		ctxt.setAt(ctxt.source.length());
		boolean r2 = end.run(ctxt);
		int endAt = ctxt.getAt();
		while ( !r2 && ctxt.getAt() >=betweenAt ) {
      ctxt.setAt(ctxt.getAt() - 1);
      endAt = ctxt.getAt();
			r2 = end.run(ctxt);
		}
		if (!r2) return false;
		ParseContext betweenCtxt = new ScannerState(ctxt.module, ctxt.source, betweenAt, endAt, ctxt.locator, ctxt.getResult());
		boolean rb = between.run(betweenCtxt);
		
		if ( ! rb ) return false;
		
		ctxt.setResult(between.getReturn(betweenCtxt));
		return true;
	}
	
}

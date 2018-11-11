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
package org.jparsec;

/**
 * A parser that first consumes the start and end, and then
 * tries to consume the middle. Useful when the end terminator
 * may be present in the body (e.g. {@code (bodyWithParens ()())())}
 * @author michael
 */
@Deprecated
final class ReluctantBetweenParser<T> extends Parser<T> {
	
	private final Parser<?> start;
	private final Parser<T> between;
	private final Parser<?> end;

 ReluctantBetweenParser(Parser<?> start, Parser<T> between, Parser<?> end) {
		this.start = start;
		this.between = between;
		this.end = end;
	}
	
	@Override
	boolean apply(ParseContext ctxt) {
		if (!start.apply(ctxt)) return false;
		int betweenAt = ctxt.at;
		
		// try to match the end of the sequence beginning from the very end giving a chance to empty parser to be matched
		// (see https://github.com/abailly/jparsec/issues/25)
		ctxt.at = ctxt.source.length();
		boolean r2 = end.apply(ctxt);
		int endAt = ctxt.at;
		while ( !r2 && ctxt.at >=betweenAt ) {
      ctxt.at--;
      endAt = ctxt.at;
			r2 = end.apply(ctxt);
		}
		if (!r2) return false;
		ParseContext betweenCtxt = new ScannerState(ctxt.module, ctxt.source, betweenAt, endAt, ctxt.locator, ctxt.result );
		boolean rb = between.apply(betweenCtxt);
		
		if ( ! rb ) return false;
		
		ctxt.result = between.getReturn(betweenCtxt);
		return true;
	}
	
}

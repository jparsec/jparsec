package org.codehaus.jparsec.examples.statement;

import static org.junit.Assert.assertEquals;

import org.codehaus.jparsec.Parser;
import org.codehaus.jparsec.Parsers;
import org.codehaus.jparsec.Scanners;
import org.codehaus.jparsec.Terminals;
import org.codehaus.jparsec.examples.statement.ast.DoubleExpression;
import org.codehaus.jparsec.examples.statement.ast.SingleExpression;
import org.junit.Test;

public class FirstTest {

	//low-level grammar
	private static final Terminals OPERATORS =
			Terminals.operators("=", "readonly", "var");

	//for integers and identifiers we need a PARSER and a TOKENIZER for each
	//here are the parsers
	public static Parser<String> identSyntacticParser = Terminals.Identifier.PARSER;
	public static Parser<String> integerSyntacticParser = Terminals.IntegerLiteral.PARSER;
	
	//and here are the tokenizers, combined into our single tokenizer
	static final Parser<?> TOKENIZER = Parsers.or(
					OPERATORS.tokenizer(),
					Terminals.IntegerLiteral.TOKENIZER, 
					Terminals.Identifier.TOKENIZER, 
					Terminals.Identifier.TOKENIZER);

	static final Parser<Void> IGNORED = Parsers.or(
			Scanners.JAVA_LINE_COMMENT,
			Scanners.JAVA_BLOCK_COMMENT,
			Scanners.WHITESPACES).skipMany();	
	
	private static Parser<String> readonly() {
		return OPERATORS.token("readonly").retn("readonly");
	}
	private static Parser<String> var() {
		return OPERATORS.token("var").retn("var");
	}
	private static Parser<String> eq() {
		return OPERATORS.token("=").retn("=");
	}

	//high-level grammar
	
	//parse single value into a SingleExpression
	private static Parser<SingleExpression> singleExpression01() {
		return Parsers.or(readonly())
				.map(new org.codehaus.jparsec.functors.Map<String, SingleExpression>() {
					@Override
					public SingleExpression map(String arg0) {
						return new SingleExpression(arg0);
					}
				});
	}
	
	//parse two tokens but only take the output of last one in sequence()
	private static Parser<SingleExpression> singleExpression02() {
		return Parsers.sequence(readonly(), var()).map(SingleExpression::new);
	}
	
	//parse two tokens and use the output of all tokens in sequence()
	private static Parser<DoubleExpression> doubleExpression01() {
		return Parsers.sequence(readonly(), var(), (String s, String s2) -> new DoubleExpression(s,s2));
	}
	//same thing but use Java 8 functional interface
	private static Parser<DoubleExpression> doubleExpression02() {
		return Parsers.sequence(readonly(), var(), DoubleExpression::new);
	}
	
	@Test
	public void testSingle1() {
		SingleExpression exp = singleExpression01().from(TOKENIZER, IGNORED).parse("readonly");
		assertEquals("readonly", exp.s);
	}
	@Test
	public void testSingle2() {
		SingleExpression exp = singleExpression02().from(TOKENIZER, IGNORED).parse("readonly var");
		assertEquals("var", exp.s);
	}
	
	@Test
	public void testDouble1() {
		DoubleExpression exp = doubleExpression01().from(TOKENIZER, IGNORED).parse("readonly var");
		assertEquals("readonly", exp.s);
		assertEquals("var", exp.s2);
	}
	@Test
	public void testDouble2() {
		DoubleExpression exp = doubleExpression02().from(TOKENIZER, IGNORED).parse("readonly var");
		assertEquals("readonly", exp.s);
		assertEquals("var", exp.s2);
	}

}

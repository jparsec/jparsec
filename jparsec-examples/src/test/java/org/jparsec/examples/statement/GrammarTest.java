package org.jparsec.examples.statement;

import static org.junit.Assert.assertEquals;

import org.jparsec.Parser;
import org.jparsec.Parsers;
import org.jparsec.Scanners;
import org.jparsec.Terminals;
import org.jparsec.examples.statement.ast.FullExpression;
import org.jparsec.examples.statement.ast.IdentExpression;
import org.jparsec.examples.statement.ast.ReadonlyExpression;
import org.jparsec.examples.statement.ast.ValueExpression;
import org.jparsec.examples.statement.ast.VarExpression;
import org.junit.Test;

public class GrammarTest {

    //low-level grammar
    private static final Terminals OPERATORS =
            Terminals.operators("=", "readonly", "var");

    //for integers and identifiers we need a PARSER and a TOKENIZER for each
    //here are the parsers
    private static Parser<String> IDENTIFIER_PARSER = Terminals.Identifier.PARSER;
    private static Parser<String> INTEGER_PARSER = Terminals.IntegerLiteral.PARSER;

    //and here are the tokenizers, combined into our single tokenizer
    private static final Parser<?> TOKENIZER = Parsers.or(
            OPERATORS.tokenizer(),
            Terminals.IntegerLiteral.TOKENIZER, 
            Terminals.Identifier.TOKENIZER, 
            Terminals.Identifier.TOKENIZER);

    private static final Parser<Void> IGNORED = Parsers.or(
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
    private static Parser<ReadonlyExpression> readonlyExpression() {
        return readonly().map(ReadonlyExpression::new);
    }
    private static Parser<VarExpression> varExpression() {
        return var().map(VarExpression::new);
    }
    private static Parser<IdentExpression> ident() {
        return IDENTIFIER_PARSER.map(IdentExpression::new);
    }

    //'=' followed by an integer value
    private static Parser<ValueExpression> assignment() {
        return Parsers.sequence(eq(), INTEGER_PARSER).map(ValueExpression::new);
    }


    //var x = 5
    private static Parser<FullExpression> full() {
        return Parsers.sequence(readonly().asOptional(), var(), Parsers.sequence(ident(), assignment(), FullExpression::new));
    }

    private static final FullExpression parse(String input) {
        Parser<FullExpression> grammar = full();
        FullExpression exp = grammar.from(TOKENIZER, IGNORED).parse(input);
        return exp;
    }


    @Test
    public void test1() {
        FullExpression exp = parse("var x = 50");
        assertEquals("x", exp.identExpr.s);
        assertEquals(50, exp.valueExpr.nVal.intValue());
    }

    @Test
    public void test2() {
        FullExpression exp = parse("readonly var x = 50");
        assertEquals("x", exp.identExpr.s);
        assertEquals(50, exp.valueExpr.nVal.intValue());
    }

    //you can test individual pieces of the grammar
    @Test
    public void test3() {
        ValueExpression exp = assignment().from(TOKENIZER, IGNORED).parse("= 5");
        assertEquals(5, exp.nVal.intValue());
    }

}

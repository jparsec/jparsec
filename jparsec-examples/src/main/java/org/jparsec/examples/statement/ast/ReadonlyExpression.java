package org.jparsec.examples.statement.ast;

public class ReadonlyExpression implements Expression {
	public String s;
	public ReadonlyExpression(String s) {
		this.s = s;
	}
}

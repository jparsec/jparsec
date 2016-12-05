package org.jparsec.examples.statement.ast;

public class ValueExpression implements Expression {
	public Integer nVal;
	
	public ValueExpression(String s) {
		this.nVal = Integer.parseInt(s);
	}
}

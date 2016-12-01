package org.codehaus.jparsec.examples.statement.ast;

public class FullExpression implements Expression {
	public IdentExpression identExpr;
	public ValueExpression valueExpr;
	
	public FullExpression(IdentExpression identExpr, ValueExpression valueExpr) {
		this.identExpr = identExpr;
		this.valueExpr = valueExpr;
	}
}
package org.codehaus.jparsec.parameters;

public class Declaration extends Node {
	private Identifier id;

	public Declaration(Identifier id) {
		super();
		this.id = id;
	}

	public Identifier getId() {
		return id;
	}

	@Override
	public String toString() {
		return "Declaration [id=" + id + "]";
	}
}

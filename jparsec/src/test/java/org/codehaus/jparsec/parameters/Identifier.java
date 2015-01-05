package org.codehaus.jparsec.parameters;

public class Identifier extends Node {
	private String name;

	public Identifier(String name) {
		super();
		this.name = name;
	}

	public String getName() {
		return name;
	}

	@Override
	public String toString() {
		return "Identifier [name=" + name + "]";
	}
}

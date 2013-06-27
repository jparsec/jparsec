package org.codehaus.jparsec;

import static org.junit.Assert.*;

import org.codehaus.jparsec.functors.Map;
import org.junit.Test;

public class LocatableParserTest2 {

	public static class SourceInfo {
		private String module;
		private int start;
		private int end;
		
		public SourceInfo(String module, int start, int end) {
			super();
			this.module = module;
			this.start = start;
			this.end = end;
		}

		public int getStart() {
			return start;
		}

		public int getEnd() {
			return end;
		}

		public String getModule() {
			return module;
		}
	}
	
	public static class Node {
		private SourceInfo info;

		public SourceInfo getInfo() {
			return info;
		}

		public void setInfo(SourceInfo info) {
			this.info = info;
		}
	}

	public static class Identifier extends Node {
		private String name;

		public Identifier(String name) {
			super();
			this.name = name;
		}

		public String getName() {
			return name;
		}
	}

	public static class Declaration extends Node {
		private Identifier id;

		public Declaration(Identifier id) {
			super();
			this.id = id;
		}

		public Identifier getId() {
			return id;
		}
	}

	public static Terminals KEYWORDS = Terminals.caseInsensitive(new String[] {";"}, new String[] {"def"});
	
	public static Parser<Object> TOKENIZER = KEYWORDS.tokenizer().cast().or(Scanners.IDENTIFIER);

	public static LocatableHandler<Node> handler = new LocatableHandler<Node>() {
		@Override
		public void handle(Node obj, String source, String module, int beginIndex, int endIndex) {
			obj.setInfo(new SourceInfo(module, beginIndex, endIndex));
		}
	};
	
	public static Parser<Identifier> IDENTIFIER = Terminals.Identifier.PARSER.map(new Map<String, Identifier>() {
		@Override
		public Identifier map(String from) {
			return new Identifier(from);
		}
	}).locate(handler);
	
	public static Parser<Declaration> DECL = KEYWORDS.token("def").next(IDENTIFIER).followedBy(KEYWORDS.token(";")).map(new Map<Identifier, Declaration>() {
		@Override
		public Declaration map(Identifier id) {
			return new Declaration(id);
		}}).locate(handler);
	
	@Test
	public void test() {
		Parser<Declaration> parser = DECL.from(TOKENIZER, Scanners.WHITESPACES.optional());
		Declaration decl = parser.parse("def abcd;", "module");
		assertEquals(0, decl.getInfo().getStart());
		assertEquals(9, decl.getInfo().getEnd());
		assertEquals("module", decl.getInfo().getModule());
		assertEquals(4, decl.getId().getInfo().getStart());
		assertEquals(8, decl.getId().getInfo().getEnd());
		assertEquals("module", decl.getId().getInfo().getModule());
	}

}

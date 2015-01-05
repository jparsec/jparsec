package org.codehaus.jparsec.parameters;

import static org.junit.Assert.*;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.codehaus.jparsec.Parser;
import org.codehaus.jparsec.Scanners;
import org.codehaus.jparsec.Terminals;
import org.codehaus.jparsec.Parser.Mode;
import org.codehaus.jparsec.functors.Map;
import org.codehaus.jparsec.misc.Mapper;
import org.codehaus.jparsec.parameters.MapListener;
import org.codehaus.jparsec.parameters.Parameters;
import org.codehaus.jparsec.parameters.ParseLevelState;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;


@RunWith(Parameterized.class)
public class RuntimeParamsTest {
  
  public static Terminals KEYWORDS = Terminals.operators(";").words(Scanners.IDENTIFIER).keywords(Arrays.asList(new String[]{"def"})).build();
  
  public static Parser<Object> TOKENIZER = KEYWORDS.tokenizer().cast().or(Scanners.IDENTIFIER);

  public static Parser<Identifier> IDENTIFIER = Terminals.Identifier.PARSER.map(new Map<String, Identifier>() {
    @Override
    public Identifier map(String from) {
      return new Identifier(from);
    }
  });
  
  public static Parser<Declaration> DECL = KEYWORDS.token("def").next(IDENTIFIER).followedBy(KEYWORDS.token(";")).map(new Map<Identifier, Declaration>() {
    @Override
    public Declaration map(Identifier id) {
      return new Declaration(id);
    }});
  
  public static Parser<Declaration> DECL2 = new Mapper<Declaration>() {
    @SuppressWarnings("unused")
    public Declaration map(Identifier id) {
      return new Declaration(id);
    }
  }.sequence(KEYWORDS.token("def").next(IDENTIFIER).followedBy(KEYWORDS.token(";")));

  @org.junit.runners.Parameterized.Parameters
  public static Collection<Object[]> data() {
    return Arrays.asList(new Object[] {DECL}, new Object[] {DECL2});
  }
  
  private Parser<Declaration> parser;
  
  public RuntimeParamsTest(Parser<Declaration> parser) {
    this.parser = parser;
  }
  
  @Test
  public void testSourceInfo() {
    
    class TestParameters extends Parameters {
      private String filename;

      public String getFilename() {
        return filename;
      }

      public void setFilename(String filename) {
        this.filename = filename;
      }
      
    }
    
    TestParameters params = new TestParameters();
    params.setFilename("filename");
    params.setMapListener(new MapListener() {
      @Override
      public void onMap(Object object, ParseLevelState state) {
        if (object instanceof Node) {
          TestParameters params = (TestParameters) state.getParams();
          
          ((Node) object).setInfo(new SourceInfo(params.getFilename(), state
              .getFirstToken().index(), state.getLastToken().index()
              + state.getLastToken().length()));
        }
      }
    });
    
    Parser<List<Declaration>> parser = this.parser.many().from(TOKENIZER, Scanners.WHITESPACES.optional());
    List<Declaration> decls = parser.parse("def abcd; def efgh;", params);
    Declaration decl = decls.get(0);
    assertEquals("filename", decl.getInfo().getFilename());
    assertEquals(0, decl.getInfo().getStart());
    assertEquals(9, decl.getInfo().getEnd());
    assertEquals(4, decl.getId().getInfo().getStart());
    assertEquals(8, decl.getId().getInfo().getEnd());

    decl = decls.get(1);
    assertEquals("filename", decl.getInfo().getFilename());
    assertEquals(10, decl.getInfo().getStart());
    assertEquals(19, decl.getInfo().getEnd());
    assertEquals(14, decl.getId().getInfo().getStart());
    assertEquals(18, decl.getId().getInfo().getEnd());
  }
}

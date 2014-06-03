package org.codehaus.jparsec.examples.sql.parser;

import static org.codehaus.jparsec.examples.sql.parser.ExpressionParser.NUMBER;
import static org.codehaus.jparsec.examples.sql.parser.ExpressionParserTest.number;
import static org.codehaus.jparsec.examples.sql.parser.RelationParser.TABLE;
import static org.codehaus.jparsec.examples.sql.parser.TerminalParserTest.assertParser;

import java.util.Arrays;
import java.util.List;

import org.codehaus.jparsec.Parser;
import org.codehaus.jparsec.examples.sql.ast.AliasedRelation;
import org.codehaus.jparsec.examples.sql.ast.BinaryExpression;
import org.codehaus.jparsec.examples.sql.ast.CrossJoinRelation;
import org.codehaus.jparsec.examples.sql.ast.GroupBy;
import org.codehaus.jparsec.examples.sql.ast.JoinRelation;
import org.codehaus.jparsec.examples.sql.ast.JoinType;
import org.codehaus.jparsec.examples.sql.ast.Op;
import org.codehaus.jparsec.examples.sql.ast.OrderBy;
import org.codehaus.jparsec.examples.sql.ast.Projection;
import org.codehaus.jparsec.examples.sql.ast.QualifiedName;
import org.codehaus.jparsec.examples.sql.ast.Relation;
import org.codehaus.jparsec.examples.sql.ast.Select;
import org.codehaus.jparsec.examples.sql.ast.TableRelation;
import org.codehaus.jparsec.examples.sql.ast.UnaryRelationalExpression;
import org.codehaus.jparsec.examples.sql.ast.UnionRelation;
import org.junit.Test;

/**
 * Unit test for {@link RelationParser}.
 * 
 * @author Ben Yu
 */
public class RelationParserTest {

  @Test
  public void testTable() {
    assertParser(TABLE, "a.b", table("a", "b"));
  }

  @Test
  public void testAliasable() {
    Parser<Relation> parser = RelationParser.aliasable(TABLE);
    assertParser(parser, "table t", new AliasedRelation(table("table"), "t"));
    assertParser(parser, "table as t", new AliasedRelation(table("table"), "t"));
    assertParser(parser, "table", table("table"));
  }

  @Test
  public void testOrderByItem() {
    Parser<OrderBy.Item> parser = RelationParser.orderByItem(NUMBER);
    assertParser(parser, "1", new OrderBy.Item(number(1), true));
    assertParser(parser, "1 asc", new OrderBy.Item(number(1), true));
    assertParser(parser, "1 desc", new OrderBy.Item(number(1), false));
  }

  @Test
  public void testOrderByClause() {
    Parser<OrderBy> parser = RelationParser.orderByClause(NUMBER);
    assertParser(parser, "order by 1, 2 desc, 3 asc", new OrderBy(Arrays.asList(
        new OrderBy.Item(number(1), true), new OrderBy.Item(number(2), false),
        new OrderBy.Item(number(3), true))));
  }

  @Test
  public void testInnerJoin() {
    Parser<JoinType> parser = RelationParser.INNER_JOIN;
    assertParser(parser, "join", JoinType.INNER);
    assertParser(parser, "inner join", JoinType.INNER);
  }

  @Test
  public void testLeftJoin() {
    Parser<JoinType> parser = RelationParser.LEFT_JOIN;
    assertParser(parser, "left join", JoinType.LEFT);
    assertParser(parser, "left outer join", JoinType.LEFT);
  }

  @Test
  public void testRightJoin() {
    Parser<JoinType> parser = RelationParser.RIGHT_JOIN;
    assertParser(parser, "right join", JoinType.RIGHT);
    assertParser(parser, "right outer join", JoinType.RIGHT);
  }

  @Test
  public void testFullJoin() {
    Parser<JoinType> parser = RelationParser.FULL_JOIN;
    assertParser(parser, "full join", JoinType.FULL);
    assertParser(parser, "full outer join", JoinType.FULL);
  }

  @Test
  public void testJoin() {
    Parser<Relation> parser = RelationParser.join(TABLE, NUMBER);
    assertParser(parser, "a", table("a"));
    assertParser(parser, "a cross join table2 as b",
        new CrossJoinRelation(table("a"), new AliasedRelation(table("table2"), "b")));
    assertParser(parser, "a inner join b on 1",
        new JoinRelation(table("a"), JoinType.INNER, table("b"), number(1)));
    assertParser(parser, "a inner join b on 1 left join c on 2 cross join d",
        new CrossJoinRelation(
            new JoinRelation(
                new JoinRelation(table("a"), JoinType.INNER, table("b"), number(1))
                , JoinType.LEFT, table("c"), number(2)),
            table("d")));
    assertParser(parser, "a cross join b inner join c right join d on 1 on 2",
        new JoinRelation(new CrossJoinRelation(table("a"), table("b")),
            JoinType.INNER,
            new JoinRelation(table("c"), JoinType.RIGHT, table("d"), number(1)),
            number(2)));
    assertParser(parser, "a cross join (b FULL join c on 1)",
        new CrossJoinRelation(table("a"),
            new JoinRelation(table("b"), JoinType.FULL, table("c"), number(1))));
  }

  @Test
  public void testUnion() {
    Parser<Relation> parser = RelationParser.union(TABLE);
    assertParser(parser, "a", table("a"));
    assertParser(parser, "a union b", new UnionRelation(table("a"), false, table("b")));
    assertParser(parser, "a union all b union (c)",
        new UnionRelation(
            new UnionRelation(table("a"), true, table("b")),
            false, table("c")
        )
    );
    assertParser(parser, "a union all (b union (c))",
        new UnionRelation(
            table("a"),
            true,
            new UnionRelation(table("b"), false, table("c"))
        )
    );
  }

  @Test
  public void testProjection() {
    Parser<Projection> parser = RelationParser.projection(NUMBER);
    assertParser(parser, "1", new Projection(number(1), null));
    assertParser(parser, "1 id", new Projection(number(1), "id"));
    assertParser(parser, "1 as id", new Projection(number(1), "id"));
  }

  @Test
  public void testSelectClause() {
    Parser<Boolean> parser = RelationParser.selectClause();
    assertParser(parser, "select", false);
    assertParser(parser, "select distinct", true);
  }

  @Test
  public void testFromClause() {
    Parser<List<Relation>> parser = RelationParser.fromClause(TABLE);
    assertListParser(parser, "from a", table("a"));
    assertListParser(parser, "from a x", new AliasedRelation(table("a"), "x"));
    assertListParser(parser, "from table1 t1, t2",
        new AliasedRelation(table("table1"), "t1"), table("t2"));
  }

  @Test
  public void testGroupByClause() {
    Parser<GroupBy> parser = RelationParser.groupByClause(NUMBER, NUMBER);
    assertParser(parser, "group by 1, 2", new GroupBy(Arrays.asList(number(1), number(2)), null));
    assertParser(parser, "group by 1, 2 having 3",
        new GroupBy(Arrays.asList(number(1), number(2)), number(3)));
  }

  @Test
  public void testSelect() {
    Parser<Relation> parser = RelationParser.select(NUMBER, NUMBER, TABLE);
    assertParser(parser, "select distinct 1, 2 as id from t1, t2",
        new Select(true, 
            Arrays.asList(new Projection(number(1), null), new Projection(number(2), "id")),
            Arrays.asList(table("t1"), table("t2")),
            null, null, null));
    assertParser(parser, "select 1 as id from t where 1",
        new Select(false, 
            Arrays.asList(new Projection(number(1), "id")),
            Arrays.asList(table("t")),
            number(1), null, null));
    assertParser(parser, "select 1 as id from t group by 2, 3",
        new Select(false, 
            Arrays.asList(new Projection(number(1), "id")),
            Arrays.asList(table("t")),
            null, new GroupBy(Arrays.asList(number(2), number(3)), null), null));
    assertParser(parser, "select 1 as id from t group by 2, 3 having 4",
        new Select(false, 
            Arrays.asList(new Projection(number(1), "id")),
            Arrays.asList(table("t")),
            null, new GroupBy(Arrays.asList(number(2), number(3)), number(4)), null));
    assertParser(parser, "select 1 as id from t order by 2 asc, 3 desc",
        new Select(false, 
            Arrays.asList(new Projection(number(1), "id")),
            Arrays.asList(table("t")),
            null, null, new OrderBy(Arrays.asList(
                new OrderBy.Item(number(2), true), new OrderBy.Item(number(3), false)))));
  }

  @Test
  public void testQuery() {
    Parser<Relation> parser = RelationParser.query(NUMBER, NUMBER, TABLE);
    assertParser(parser, "select 1 from t",
        new Select(false, 
            Arrays.asList(new Projection(number(1), null)),
            Arrays.asList(table("t")),
            null, null, null));
    assertParser(parser, "select 1 from a union select distinct 2 from b",
        new UnionRelation(
            new Select(false, 
                Arrays.asList(new Projection(number(1), null)),
                Arrays.asList(table("a")),
                null, null, null),
            false,
             new Select(true, 
                Arrays.asList(new Projection(number(2), null)),
                Arrays.asList(table("b")),
                null, null, null)));
  }

  @Test
  public void testCompleteQuery() {
    Parser<Relation> parser = RelationParser.query();
    assertParser(parser, "select 1 from t",
        new Select(false, 
            Arrays.asList(new Projection(number(1), null)),
            Arrays.asList(table("t")),
            null, null, null));
    assertParser(parser, "select 1 from (select * from table) t",
        new Select(false, 
            Arrays.asList(new Projection(number(1), null)),
            Arrays.<Relation>asList(new AliasedRelation(
                new Select(false, 
                    Arrays.asList(new Projection(ExpressionParserTest.wildcard(), null)),
                    Arrays.asList(table("table")),
                    null, null, null),
                "t")),
            null, null, null));
    assertParser(parser, "select 1 from t where x > 1",
        new Select(false, 
            Arrays.asList(new Projection(number(1), null)),
            Arrays.asList(table("t")),
            new BinaryExpression(ExpressionParserTest.name("x"), Op.GT, number(1)), null, null));
    assertParser(parser, "select 1 from t where exists (select * from t2)",
        new Select(false, 
            Arrays.asList(new Projection(number(1), null)),
            Arrays.asList(table("t")),
            new UnaryRelationalExpression(
                new Select(false, 
                    Arrays.asList(new Projection(ExpressionParserTest.wildcard(), null)),
                    Arrays.asList(table("t2")),
                    null, null, null),
                Op.EXISTS), null, null));
    assertParser(parser, "select case when exists (select * from t1) then 1 end from t",
        new Select(false, 
            Arrays.asList(new Projection(
                ExpressionParserTest.fullCase(new UnaryRelationalExpression(
                    new Select(false, 
                        Arrays.asList(new Projection(ExpressionParserTest.wildcard(), null)),
                        Arrays.asList(table("t1")),
                        null, null, null), Op.EXISTS), number(1), null),
                    null)),
            Arrays.asList(table("t")),
            null, null, null));
  }
  
  static Relation table(String... names) {
    return new TableRelation(QualifiedName.of(names));
  }
  
  static void assertListParser(Parser<?> parser, String source, Object... expected) {
    assertParser(parser, source, Arrays.asList(expected));
  }
}

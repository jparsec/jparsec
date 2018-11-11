/*****************************************************************************
 * Copyright (C) jparsec.org                                                *
 * ------------------------------------------------------------------------- *
 * Licensed under the Apache License, Version 2.0 (the "License");           *
 * you may not use this file except in compliance with the License.          *
 * You may obtain a copy of the License at                                   *
 *                                                                           *
 * http://www.apache.org/licenses/LICENSE-2.0                                *
 *                                                                           *
 * Unless required by applicable law or agreed to in writing, software       *
 * distributed under the License is distributed on an "AS IS" BASIS,         *
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  *
 * See the License for the specific language governing permissions and       *
 * limitations under the License.                                            *
 *****************************************************************************/
package org.jparsec.examples.sql.parser;

import org.jparsec.Parser;
import org.jparsec.examples.sql.ast.*;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;

import static org.jparsec.examples.sql.parser.RelationParser.TABLE;

/**
 * Unit test for {@link RelationParser}.
 * 
 * @author Ben Yu
 */
public class RelationParserTest {

  @Test
  public void testTable() {
    TerminalParserTest.assertParser(TABLE, "a.b", table("a", "b"));
  }

  @Test
  public void testAliasable() {
    Parser<Relation> parser = RelationParser.aliasable(TABLE);
    TerminalParserTest.assertParser(parser, "table t", new AliasedRelation(table("table"), "t"));
    TerminalParserTest.assertParser(parser, "table as t", new AliasedRelation(table("table"), "t"));
    TerminalParserTest.assertParser(parser, "table", table("table"));
  }

  @Test
  public void testOrderByItem() {
    Parser<OrderBy.Item> parser = RelationParser.orderByItem(ExpressionParser.NUMBER);
    TerminalParserTest.assertParser(parser, "1", new OrderBy.Item(ExpressionParserTest.number(1), true));
    TerminalParserTest.assertParser(parser, "1 asc", new OrderBy.Item(ExpressionParserTest.number(1), true));
    TerminalParserTest.assertParser(parser, "1 desc", new OrderBy.Item(ExpressionParserTest.number(1), false));
  }

  @Test
  public void testOrderByClause() {
    Parser<OrderBy> parser = RelationParser.orderByClause(ExpressionParser.NUMBER);
    TerminalParserTest.assertParser(parser, "order by 1, 2 desc, 3 asc", new OrderBy(Arrays.asList(
        new OrderBy.Item(ExpressionParserTest.number(1), true), new OrderBy.Item(ExpressionParserTest.number(2), false),
        new OrderBy.Item(ExpressionParserTest.number(3), true))));
  }

  @Test
  public void testInnerJoin() {
    Parser<JoinType> parser = RelationParser.INNER_JOIN;
    TerminalParserTest.assertParser(parser, "join", JoinType.INNER);
    TerminalParserTest.assertParser(parser, "inner join", JoinType.INNER);
  }

  @Test
  public void testLeftJoin() {
    Parser<JoinType> parser = RelationParser.LEFT_JOIN;
    TerminalParserTest.assertParser(parser, "left join", JoinType.LEFT);
    TerminalParserTest.assertParser(parser, "left outer join", JoinType.LEFT);
  }

  @Test
  public void testRightJoin() {
    Parser<JoinType> parser = RelationParser.RIGHT_JOIN;
    TerminalParserTest.assertParser(parser, "right join", JoinType.RIGHT);
    TerminalParserTest.assertParser(parser, "right outer join", JoinType.RIGHT);
  }

  @Test
  public void testFullJoin() {
    Parser<JoinType> parser = RelationParser.FULL_JOIN;
    TerminalParserTest.assertParser(parser, "full join", JoinType.FULL);
    TerminalParserTest.assertParser(parser, "full outer join", JoinType.FULL);
  }

  @Test
  public void testJoin() {
    Parser<Relation> parser = RelationParser.join(TABLE, ExpressionParser.NUMBER);
    TerminalParserTest.assertParser(parser, "a", table("a"));
    TerminalParserTest.assertParser(parser, "a cross join table2 as b",
        new CrossJoinRelation(table("a"), new AliasedRelation(table("table2"), "b")));
    TerminalParserTest.assertParser(parser, "a inner join b on 1",
        new JoinRelation(table("a"), JoinType.INNER, table("b"), ExpressionParserTest.number(1)));
    TerminalParserTest.assertParser(parser, "a inner join b on 1 left join c on 2 cross join d",
        new CrossJoinRelation(
            new JoinRelation(
                new JoinRelation(table("a"), JoinType.INNER, table("b"), ExpressionParserTest.number(1))
                , JoinType.LEFT, table("c"), ExpressionParserTest.number(2)),
            table("d")));
    TerminalParserTest.assertParser(parser, "a cross join b inner join c right join d on 1 on 2",
        new JoinRelation(new CrossJoinRelation(table("a"), table("b")),
            JoinType.INNER,
            new JoinRelation(table("c"), JoinType.RIGHT, table("d"), ExpressionParserTest.number(1)),
            ExpressionParserTest.number(2)));
    TerminalParserTest.assertParser(parser, "a cross join (b FULL join c on 1)",
        new CrossJoinRelation(table("a"),
            new JoinRelation(table("b"), JoinType.FULL, table("c"), ExpressionParserTest.number(1))));
  }

  @Test
  public void testUnion() {
    Parser<Relation> parser = RelationParser.union(TABLE);
    TerminalParserTest.assertParser(parser, "a", table("a"));
    TerminalParserTest.assertParser(parser, "a union b", new UnionRelation(table("a"), false, table("b")));
    TerminalParserTest.assertParser(parser, "a union all b union (c)",
        new UnionRelation(
            new UnionRelation(table("a"), true, table("b")),
            false, table("c")
        )
    );
    TerminalParserTest.assertParser(parser, "a union all (b union (c))",
        new UnionRelation(
            table("a"),
            true,
            new UnionRelation(table("b"), false, table("c"))
        )
    );
  }

  @Test
  public void testProjection() {
    Parser<Projection> parser = RelationParser.projection(ExpressionParser.NUMBER);
    TerminalParserTest.assertParser(parser, "1", new Projection(ExpressionParserTest.number(1), null));
    TerminalParserTest.assertParser(parser, "1 id", new Projection(ExpressionParserTest.number(1), "id"));
    TerminalParserTest.assertParser(parser, "1 as id", new Projection(ExpressionParserTest.number(1), "id"));
  }

  @Test
  public void testSelectClause() {
    Parser<Boolean> parser = RelationParser.selectClause();
    TerminalParserTest.assertParser(parser, "select", false);
    TerminalParserTest.assertParser(parser, "select distinct", true);
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
    Parser<GroupBy> parser = RelationParser.groupByClause(ExpressionParser.NUMBER, ExpressionParser.NUMBER);
    TerminalParserTest.assertParser(parser, "group by 1, 2", new GroupBy(Arrays.asList(ExpressionParserTest.number(1), ExpressionParserTest.number(2)), null));
    TerminalParserTest.assertParser(parser, "group by 1, 2 having 3",
        new GroupBy(Arrays.asList(ExpressionParserTest.number(1), ExpressionParserTest.number(2)), ExpressionParserTest.number(3)));
  }

  @Test
  public void testSelect() {
    Parser<Relation> parser = RelationParser.select(ExpressionParser.NUMBER, ExpressionParser.NUMBER, TABLE);
    TerminalParserTest.assertParser(parser, "select distinct 1, 2 as id from t1, t2",
        new Select(true, 
            Arrays.asList(new Projection(ExpressionParserTest.number(1), null), new Projection(ExpressionParserTest.number(2), "id")),
            Arrays.asList(table("t1"), table("t2")),
            null, null, null));
    TerminalParserTest.assertParser(parser, "select 1 as id from t where 1",
        new Select(false, 
            Arrays.asList(new Projection(ExpressionParserTest.number(1), "id")),
            Arrays.asList(table("t")),
            ExpressionParserTest.number(1), null, null));
    TerminalParserTest.assertParser(parser, "select 1 as id from t group by 2, 3",
        new Select(false, 
            Arrays.asList(new Projection(ExpressionParserTest.number(1), "id")),
            Arrays.asList(table("t")),
            null, new GroupBy(Arrays.asList(ExpressionParserTest.number(2), ExpressionParserTest.number(3)), null), null));
    TerminalParserTest.assertParser(parser, "select 1 as id from t group by 2, 3 having 4",
        new Select(false, 
            Arrays.asList(new Projection(ExpressionParserTest.number(1), "id")),
            Arrays.asList(table("t")),
            null, new GroupBy(Arrays.asList(ExpressionParserTest.number(2), ExpressionParserTest.number(3)), ExpressionParserTest.number(4)), null));
    TerminalParserTest.assertParser(parser, "select 1 as id from t order by 2 asc, 3 desc",
        new Select(false, 
            Arrays.asList(new Projection(ExpressionParserTest.number(1), "id")),
            Arrays.asList(table("t")),
            null, null, new OrderBy(Arrays.asList(
                new OrderBy.Item(ExpressionParserTest.number(2), true), new OrderBy.Item(ExpressionParserTest.number(3), false)))));
  }

  @Test
  public void testQuery() {
    Parser<Relation> parser = RelationParser.query(ExpressionParser.NUMBER, ExpressionParser.NUMBER, TABLE);
    TerminalParserTest.assertParser(parser, "select 1 from t",
        new Select(false, 
            Arrays.asList(new Projection(ExpressionParserTest.number(1), null)),
            Arrays.asList(table("t")),
            null, null, null));
    TerminalParserTest.assertParser(parser, "select 1 from a union select distinct 2 from b",
        new UnionRelation(
            new Select(false, 
                Arrays.asList(new Projection(ExpressionParserTest.number(1), null)),
                Arrays.asList(table("a")),
                null, null, null),
            false,
             new Select(true, 
                Arrays.asList(new Projection(ExpressionParserTest.number(2), null)),
                Arrays.asList(table("b")),
                null, null, null)));
  }

  @Test
  public void testCompleteQuery() {
    Parser<Relation> parser = RelationParser.query();
    TerminalParserTest.assertParser(parser, "select 1 from t",
        new Select(false, 
            Arrays.asList(new Projection(ExpressionParserTest.number(1), null)),
            Arrays.asList(table("t")),
            null, null, null));
    TerminalParserTest.assertParser(parser, "select 1 from (select * from table) t",
        new Select(false, 
            Arrays.asList(new Projection(ExpressionParserTest.number(1), null)),
            Arrays.<Relation>asList(new AliasedRelation(
                new Select(false, 
                    Arrays.asList(new Projection(ExpressionParserTest.wildcard(), null)),
                    Arrays.asList(table("table")),
                    null, null, null),
                "t")),
            null, null, null));
    TerminalParserTest.assertParser(parser, "select 1 from t where x > 1",
        new Select(false, 
            Arrays.asList(new Projection(ExpressionParserTest.number(1), null)),
            Arrays.asList(table("t")),
            new BinaryExpression(ExpressionParserTest.name("x"), Op.GT, ExpressionParserTest.number(1)), null, null));
    TerminalParserTest.assertParser(parser, "select 1 from t where exists (select * from t2)",
        new Select(false, 
            Arrays.asList(new Projection(ExpressionParserTest.number(1), null)),
            Arrays.asList(table("t")),
            new UnaryRelationalExpression(
                new Select(false, 
                    Arrays.asList(new Projection(ExpressionParserTest.wildcard(), null)),
                    Arrays.asList(table("t2")),
                    null, null, null),
                Op.EXISTS), null, null));
    TerminalParserTest.assertParser(parser, "select case when exists (select * from t1) then 1 end from t",
        new Select(false, 
            Arrays.asList(new Projection(
                ExpressionParserTest.fullCase(new UnaryRelationalExpression(
                    new Select(false, 
                        Arrays.asList(new Projection(ExpressionParserTest.wildcard(), null)),
                        Arrays.asList(table("t1")),
                        null, null, null), Op.EXISTS), ExpressionParserTest.number(1), null),
                    null)),
            Arrays.asList(table("t")),
            null, null, null));
  }
  
  static Relation table(String... names) {
    return new TableRelation(QualifiedName.of(names));
  }
  
  static void assertListParser(Parser<?> parser, String source, Object... expected) {
    TerminalParserTest.assertParser(parser, source, Arrays.asList(expected));
  }
}

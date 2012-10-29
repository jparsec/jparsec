/*****************************************************************************
 * Copyright (C) Codehaus.org                                                *
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
package org.codehaus.jparsec.examples.sql.parser;

import static org.codehaus.jparsec.examples.sql.parser.ExpressionParser.paren;
import static org.codehaus.jparsec.examples.sql.parser.TerminalParser.phrase;
import static org.codehaus.jparsec.examples.sql.parser.TerminalParser.term;

import java.util.List;

import org.codehaus.jparsec.Parser;
import org.codehaus.jparsec.Parsers;
import org.codehaus.jparsec.Parser.Reference;
import org.codehaus.jparsec.examples.sql.ast.AliasedRelation;
import org.codehaus.jparsec.examples.sql.ast.CrossJoinRelation;
import org.codehaus.jparsec.examples.sql.ast.Expression;
import org.codehaus.jparsec.examples.sql.ast.GroupBy;
import org.codehaus.jparsec.examples.sql.ast.JoinRelation;
import org.codehaus.jparsec.examples.sql.ast.JoinType;
import org.codehaus.jparsec.examples.sql.ast.OrderBy;
import org.codehaus.jparsec.examples.sql.ast.Projection;
import org.codehaus.jparsec.examples.sql.ast.Relation;
import org.codehaus.jparsec.examples.sql.ast.Select;
import org.codehaus.jparsec.examples.sql.ast.TableRelation;
import org.codehaus.jparsec.examples.sql.ast.UnionRelation;
import org.codehaus.jparsec.functors.Unary;
import org.codehaus.jparsec.misc.Mapper;

/**
 * Parser for relation.
 * 
 * @author Ben Yu
 */
public final class RelationParser {

  static final Parser<String> ALIAS = term("as").optional().next(TerminalParser.NAME);
  
  static final Parser<JoinType> FULL_JOIN = joinType(JoinType.FULL, "full join", "full outer join");
  static final Parser<JoinType> RIGHT_JOIN =
      joinType(JoinType.RIGHT, "right join", "right outer join");
  static final Parser<JoinType> LEFT_JOIN = joinType(JoinType.LEFT, "left join", "left outer join");
  static final Parser<JoinType> INNER_JOIN = joinType(JoinType.INNER, "join", "inner join");
  
  static final Parser<Relation> TABLE =
      curry(TableRelation.class).sequence(TerminalParser.QUALIFIED_NAME);
  static final Parser<Boolean> SELECT_CLAUSE = term("select").next(term("distinct").succeeds());
  
  static final Parser<Projection> projection(Parser<Expression> expr) {
    return Mapper.curry(Projection.class).sequence(expr, ALIAS.optional());
  }
  
  static final Parser<Relation> alias(Parser<Relation> rel) {
    return curry(AliasedRelation.class).sequence(rel, ALIAS);
  }
  
  static final Parser<Relation> aliasable(Parser<Relation> rel) {
    return alias(rel).or(rel);
  }
  
  static final Parser<Boolean> selectClause() {
    return term("select").next(term("distinct").succeeds());
  }
  
  static Parser<List<Relation>> fromClause(Parser<Relation> rel) {
    return term("from").next(aliasable(rel).sepBy1(term(",")));
  }
  
  static Parser<Expression> whereClause(Parser<Expression> cond) {
    return term("where").next(cond);
  }
  
  static Parser<GroupBy> groupByClause(Parser<Expression> expr, Parser<Expression> cond) {
    return Mapper.curry(GroupBy.class).sequence(
        phrase("group by").next(list(expr)), phrase("having").next(cond).optional());
  }
  
  static Parser<Expression> havingClause(Parser<Expression> cond) {
    return term("having").next(cond);
  }
  
  static Parser<OrderBy.Item> orderByItem(Parser<Expression> expr) {
    return Mapper.curry(OrderBy.Item.class).sequence(expr, Parsers.or(
        term("asc").retn(true),
        term("desc").retn(false)
    ).optional(true));
  }
  
  static Parser<OrderBy> orderByClause(Parser<Expression> expr) {
    return Mapper.curry(OrderBy.class).sequence(
        phrase("order by").next(list(orderByItem(expr))));
  }
  
  static Parser<Relation> join(Parser<Relation> rel, Parser<Expression> cond) {
    Reference<Relation> ref = Parser.newReference();
    Parser<Relation> lazy = ref.lazy();
    Parser<Relation> atom = aliasable(paren(lazy).or(rel));
    
    // Cannot use regular infix operator because of the "join ... on ..." syntax.
    Parser<Relation> parser = atom.postfix(Parsers.or(
        joinOn(INNER_JOIN, lazy, cond), 
        joinOn(LEFT_JOIN, lazy, cond),
        joinOn(RIGHT_JOIN, lazy, cond),
        joinOn(FULL_JOIN, lazy, cond),
        curry(CrossJoinRelation.class).postfix(phrase("cross join"), atom)
    ));
    ref.set(parser);
    return parser;
  }
  
  static Parser<Relation> select(
      Parser<Expression> expr, Parser<Expression> cond, Parser<Relation> rel) {
    return curry(Select.class).sequence(
        SELECT_CLAUSE, list(projection(expr)),
        fromClause(join(rel, cond)),
        whereClause(cond).optional(),
        groupByClause(expr, cond).optional(),
        orderByClause(expr).optional());
  }
  
  static Parser<Relation> union(Parser<Relation> rel) {
    Reference<Relation> ref = Parser.newReference();
    Parser<Relation> parser = paren(ref.lazy()).or(rel).infixl(
        curry(UnionRelation.class).infix(term("union"), term("all").succeeds())).label("relation");
    ref.set(parser);
    return parser;
  }
  
  static Parser<Relation> query(
      Parser<Expression> expr, Parser<Expression> cond, Parser<Relation> rel) {
    return union(select(expr, cond, rel));
  }
  
  /** The {@link Parser} for a full fledged SQL query. */
  public static Parser<Relation> query() {
    Reference<Relation> relationRef = Parser.newReference();
    Parser<Relation> subQuery = paren(relationRef.lazy());
    Reference<Expression> conditionRef = Parser.newReference();
    Parser<Expression> expr = ExpressionParser.expression(conditionRef.lazy());
    Parser<Expression> cond = ExpressionParser.condition(expr, subQuery);
    Parser<Relation> relation = query(expr, cond, subQuery.or(TABLE));
    conditionRef.set(cond);
    relationRef.set(relation);
    return relation;
  }
  
  private static Parser<JoinType> joinType(JoinType joinType, String phrase1, String phrase2) {
    return Parsers.or(phrase(phrase1), phrase(phrase2)).retn(joinType);
  }
  
  private static Parser<Unary<Relation>> joinOn(
      Parser<JoinType> joinType, Parser<Relation> right, Parser<Expression> cond) {
    return curry(JoinRelation.class).postfix(joinType, right, term("on"), cond);
  }
  
  private static <T> Parser<List<T>> list(Parser<T> p) {
    return p.sepBy1(term(","));
  }
  
  private static Mapper<Relation> curry(Class<? extends Relation> clazz, Object... args) {
    return Mapper.curry(clazz, args);
  }
}

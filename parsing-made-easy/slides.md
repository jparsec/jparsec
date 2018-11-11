<!-- -*- coding: utf-8-unix; -*- -->

% jparsec: Parsing Made Easy
% Arnaud Bailly
% 2013-09


# Traditional Parsing

* The Dragon Book !
* Lexer: Transforms stream of characters into stream of tokens
    * lex, jlex...
* Parser: Transform stream of tokens into syntax trees
    * yacc, bison, javacc, antlr...
    
# What's Wrong?

* Separate compilation process → More complex builds
* Grammar is expressed in a different language (EBNF), usually mixed with *semantic rules* in target language → Not type-safe, no IDE support...
* Testing and debugging is hard and painful ("shift-reduce conflict" anyone?)

# What is a Parser Actually? 

Any parsing rule is a *function* that takes as input a stream of characters and returns some  $T$ 
and *remaining* input:

$$
  \pi : \Sigma^* \rightarrow (T, \Sigma^*)
$$

A *full parser* is a parser that produces $(T,\epsilon)$ from its input, eg. it consumes all its input and returns some produced
value. 

# Parser Combinators

* Parser combinators extend this idea to allow combining different parsers in a single parser
* Leverage standard function composition: Parser combinators have no side-effect
* Provide expressivity of EBNF in an *embedded DSL* by composing parser fragments

# Advantages

* Grammar is expressed in the same language than the *semantic rules* → Benefits from full IDE/compiler support
* Rules are objects/functions that can be freely composed → Reuse, compose, factorize grammar units
* Each rules can be unit tested → Safer, faster, use the TDD you love
* Use the full power of the host language to express exactly the grammar you need!

# Implementations

* Haskell: Parsec, attoparsec
* Scala: ??
* Clojure: ??
* Python: ??
* Java: jparsec

# Java Parser Combinators

* Developed by Ben Yu
* Current version is 2.1
* Maintained on github: http://github.com/abailly/jparsec

# Main features

* Provides complete combinators set for any grammar, including indentation rules (Haskell, Python), possibly ambiguous constructs (Java >>>)
* Helpers for common stuff: Identifiers, decimal/integral/scientific numbers, keywords, operators precedence...
* Works only on character streams (eg. String, Readable, Reader)


# Roadmap

* Incremental parsing: Feed input when available without blocking, return partial results
* Generalized streams: Input any kind of stream, not only characters' streams
* Error recovery: Provide *recovery points* to parse more input
* Error reporting & debugging: Better error reporting, allow easy debugging of rules execution and tree building

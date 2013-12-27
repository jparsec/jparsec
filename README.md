jparsec
=======

> This is code repository for jParsec 3.0 development branch. For current version see [Codehaus](http://jparsec.codehaus.org/) site

[![Build Status](https://travis-ci.org/abailly/jparsec.png)](https://travis-ci.org/abailly/jparsec)

## What is jparsec?

Jparsec is a recursive-descent parser combinator framework written for Java. It constructs parsers in native Java language only.

## Why yet another parser framework?

Jparsec stands out for its combinator nature. It is no parser generator like [Yacc](http://dinosaur.compilertools.net/) or [Antlr](http://www.antlr.org/). No extra grammar file is required. Grammar is written in native Java language, which also means you can utilize all the utilities in the Java community to get your parser fancy.

## What does "jparsec" stand for?

Jparsec is an implementation of [Haskell Parsec](http://www.haskell.org/haskellwiki/Parsec) on the Java platform.

## Feature highlights.

* Operator precedence grammar,
* Accurate error location and customizable error message,
* Rich set of pre-defined reusable combinator functions,
* Declarative API that resembles BNF.

## Documentation

Look at the [wiki](https://github.com/abailly/jparsec/wiki) for documentation on implementing parsers with jparsec.

# Talking about jparsec

* [Nantes JUG - 2014-01-16](http://nantesjug.org/#/events/2014_01_20): Quickie on
  for local JUG
* [JUGSummerCamp 2013](http://www.jugsummercamp.com/edition/4): Directory `parsing-made-easy` contains material for the talk (slides + sample code)

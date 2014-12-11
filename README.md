jparsec
=======

> This is code repository for jParsec 3.0 development branch. It used to be maintained on  [Codehaus](http://jparsec.codehaus.org/) site. I took over its maintenance on github.

[![Build Status](https://travis-ci.org/abailly/jparsec.png)](https://travis-ci.org/abailly/jparsec)

# How to Use?

jparsec is available in maven-central. [Snapshot Javadoc](http://jparsec.github.io/jparsec/apidocs/)

## Maven

Add the following fragment to your `<dependencies>` section:

      <dependency>
        <groupId>org.jparsec</groupId>
        <artifactId>jparsec</artifactId>
        <version>2.2</version>
      </dependency>

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

* 2014-01-16 - [Nantes JUG](http://nantesjug.org/#/events/2014_01_20):
  Quickie on jparsec for local JUG
* 2013-09-23 - [JUGSummerCamp 2013](http://www.jugsummercamp.com/edition/4): Directory `parsing-made-easy` contains material for the talk (slides + sample code)

jparsec
=======

Builds mini parsers in pure Java.

![](https://travis-ci.org/jparsec/jparsec.svg?branch=master)

# News

## 2016-10-29

* Master branch is current development branch for version 3.0 that will target Java 8 ##
* Support for OSGi is merged in master, thanks to [Alex Michael Berry](https://github.com/almibe) and [this PR](https://github.com/jparsec/jparsec/pull/47)

## How to Use?

jparsec is available in maven-central. [Snapshot Javadoc](http://jparsec.github.io/jparsec/apidocs/)

## Maven

Add the following fragment to your `<dependencies>` section:

      <dependency>
        <groupId>org.jparsec</groupId>
        <artifactId>jparsec</artifactId>
        <version>2.3</version>
      </dependency>

## Tell me more

Jparsec is a recursive-descent parser combinator framework written for Java.
It's an implementation of [Haskell Parsec](http://www.haskell.org/haskellwiki/Parsec) on the Java platform.

## Feature highlights

* Operator precedence grammar,
* Accurate error location and customizable error message,
* Rich set of pre-defined reusable combinator functions,
* Declarative API that resembles BNF.

## Documentation

Look at the [wiki](https://github.com/jparsec/jparsec/wiki) for documentation on implementing parsers with jparsec.

# Talking about jparsec

* 2014-01-16 - [Nantes JUG](http://nantesjug.org/#/events/2014_01_20):
  Quickie on jparsec for local JUG
* 2013-09-23 - [JUGSummerCamp 2013](http://www.jugsummercamp.com/edition/4): Directory `parsing-made-easy` contains material for the talk (slides + sample code)


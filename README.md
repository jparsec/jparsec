jparsec
=======

Builds mini parsers in pure Java.

![](https://travis-ci.org/jparsec/jparsec.svg?branch=master)

**Latest version**: 3.0 (requires Java 8+)

# News

## 2025-10-23
* If you are on Java 21+, prefer using Google Mug [Dot Parse](https://github.com/google/mug/blob/master/dot-parse/README.md) — a re-imagined parser combinator library:
  * The API uses static type to completely eliminate infinite loops or accidental left recursions caused by zero-consumption parser — traditionally painful to debug in current jparsec or similar combinator or PEG libraries.
  * It supports lazy, streaming parsing through the [`parseToStream()`](https://google.github.io/mug/apidocs/com/google/common/labs/parse/Parser.html#parseToStream(java.io.Reader)) and [`probe()`](https://google.github.io/mug/apidocs/com/google/common/labs/parse/Parser.html#probe(java.io.Reader)) methods.
  * Significantly lower ceremony. For example to parse by tokens, just use `.parseSkipping(Character::isWhitespace, input)`.
  * Embraces modern Java (`Stream`, pattern matching).
  * Offers a regex and a csv parser out-of-the-box. You can also check out the [code](https://github.com/google/mug/blob/master/dot-parse/src/main/java/com/google/common/labs/csv/Csv.java) as examples for how easy it is to build moderately complex parsers in few lines of code.

## 2016-12-05

* Removed references to Codehaus in copyright **and** package layout
* Support for Java 8 now complete
* Support for OSGi is merged in master, thanks to [Alex Michael Berry](https://github.com/almibe) and [this PR](https://github.com/jparsec/jparsec/pull/47)

## How to Use?

jparsec is available in maven-central. [Snapshot Javadoc](http://jparsec.github.io/jparsec/apidocs/)

## Maven

Add the following fragment to your `<dependencies>` section:

If using Java 7-:

      <dependency>
        <groupId>org.jparsec</groupId>
        <artifactId>jparsec</artifactId>
        <version>2.3</version>
      </dependency>

If using Java 8+:

      <dependency>
        <groupId>org.jparsec</groupId>
        <artifactId>jparsec</artifactId>
        <version>3.1</version>
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


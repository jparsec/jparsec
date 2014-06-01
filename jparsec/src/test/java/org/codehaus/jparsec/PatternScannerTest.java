package org.codehaus.jparsec;

import org.junit.Test;

import static org.fest.assertions.Assertions.assertThat;

public class PatternScannerTest {

  @Test
  public void returns_incremental_when_parsing_incrementally_given_incomplete_input() throws Exception {
    assertThat(Scanners.lineComment("#").incrementally()
        .parse("# a comment").parse(" more input").isDone()).isFalse();
  }

  @Test
  public void returns_done_when_parsing_incrementally_given_complete_input() throws Exception {
    Parser.Incremental<Void> incremental = Scanners.lineComment("#").incrementally()
        .parse("# a comment");
    assertThat(incremental.parse(" more input\n").isDone()).isTrue();
  }

  @Test
  public void returns_failed_when_parsing_incrementally_given_incorrect_input() throws Exception {
    assertThat(Scanners.lineComment("#").incrementally().parse(" a comment\n").isFailed()).isTrue();
  }

}

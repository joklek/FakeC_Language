package com.jole;

import org.hamcrest.CoreMatchers;
import org.junit.jupiter.api.Test;

import java.util.List;

import static com.jole.TokenType.INTEGER;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

public class ScannerErrorTests {
    @Test
    void shouldReturnErrorOnUnterminatedString() {
        String code = "\"";

        Scanner testScan = new Scanner(code);
        List<LexerError> errors = testScan.scanTokens().getErrors();
        assertThat(errors.size(), is(1));
    }

    @Test
    void shouldReturnErrorOnUnterminatedChar() {
        String code = "'";

        Scanner testScan = new Scanner(code);
        List<LexerError> errors = testScan.scanTokens().getErrors();
        assertThat(errors.size(), is(1));
    }

    @Test
    void shouldReturnErrorOnBadEscapeChar() {
        String code = "'\\.'";

        Scanner testScan = new Scanner(code);
        List<LexerError> errors = testScan.scanTokens().getErrors();
        assertThat(errors.size(), is(1));
    }

    @Test
    void shouldReturnErrorOnBadEscapeString() {
        String code = "\"\\.\"";

        Scanner testScan = new Scanner(code);
        List<LexerError> errors = testScan.scanTokens().getErrors();
        assertThat(errors.size(), is(1));
    }

    @Test
    void shouldNotParseIllegalInt() {
        String code = "123aa";

        Scanner testScan = new Scanner(code);
        ScannerResults scannerResults = testScan.scanTokens();
        List<Token> tokens = scannerResults.getTokens();
        List<LexerError> errors = scannerResults.getErrors();
        assertThat(tokens.get(0).getType(), not(CoreMatchers.is(INTEGER)));
        assertThat(errors.size(), is(1));
    }

}

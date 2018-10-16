package com.jole;

import org.junit.jupiter.api.Test;

import java.util.List;

import static com.jole.TokenType.EOF;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

public class ScannerErrorTests {
    // STRING
    @Test
    void shouldReturnErrorOnUnterminatedString() {
        String code = "\"";

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

    // CHAR
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
        assertThat(errors.size(), is(2));  // one error for bad escape symbol, another for incorrect size
    }


    @Test
    void shouldReturnErrorOnEmptyChar() {
        String code = "''";

        Scanner testScan = new Scanner(code);
        List<LexerError> errors = testScan.scanTokens().getErrors();
        assertThat(errors.size(), is(1));
    }

    // EXPONENT
    @Test
    void shouldReturnErrorOnEmptyExponent() {
        String[] codes = {"12e", "12.0e", "12.e", ".2e"};

        for(String code: codes) {
            Scanner testScan = new Scanner(code);
            List<LexerError> errors = testScan.scanTokens().getErrors();
            assertThat(errors.size(), is(1));
        }
    }

    @Test
    void shouldReturnErrorOnZeroExponent() {
        String code = ".e12";

        Scanner testScan = new Scanner(code);
        List<LexerError> errors = testScan.scanTokens().getErrors();
        assertThat(errors.size(), is(1));
    }

    // GENERAL
    @Test
    void shouldNotParseIllegalInt() {
        String code = "123aa";

        Scanner testScan = new Scanner(code);
        ScannerResults scannerResults = testScan.scanTokens();
        List<Token> tokens = scannerResults.getTokens();
        List<LexerError> errors = scannerResults.getErrors();
        assertThat(tokens.get(0).getType(), is(EOF));
        assertThat(errors.size(), is(1));
    }

    @Test
    void shouldNotParseIllegalExponent() {
        String code = "123ea";

        Scanner testScan = new Scanner(code);
        ScannerResults scannerResults = testScan.scanTokens();
        List<Token> tokens = scannerResults.getTokens();
        List<LexerError> errors = scannerResults.getErrors();
        assertThat(tokens.get(0).getType(), is(EOF));
        assertThat(errors.size(), is(1));
    }

}

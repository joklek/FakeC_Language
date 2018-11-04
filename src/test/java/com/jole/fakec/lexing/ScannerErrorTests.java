package com.jole.fakec.lexing;

import com.jole.fakec.lexing.error.LexerError;
import com.jole.fakec.tokens.Token;
import com.jole.fakec.tokens.TokenType;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

public class ScannerErrorTests {
    // STRING
    @Test
    void shouldReturnErrorOnUnterminatedString() {
        String code = "\"";

        assertScanGetsErrors(code, 1);
    }

    @Test
    void shouldReturnErrorOnBadEscapeString() {
        String code = "\"\\.\"";

        assertScanGetsErrors(code, 1);
    }

    // CHAR
    @Test
    void shouldReturnErrorOnUnterminatedChar() {
        String code = "'";

        assertScanGetsErrors(code, 1);
    }

    @Test
    void shouldReturnErrorOnBadEscapeChar() {
        String code = "'\\.'";

        assertScanGetsErrors(code, 2);
    }


    @Test
    void shouldReturnErrorOnEmptyChar() {
        String code = "''";

        assertScanGetsErrors(code, 1);
    }

    // EXPONENT
    @Test
    void shouldReturnErrorOnEmptyExponent() {
        String[] codes = {"12e", "12.0e", "12.e", ".2e"};

        for(String code: codes) {
            assertScanGetsErrors(code, 1);
        }
    }

    @Test
    void shouldReturnErrorOnZeroExponent() {
        String code = ".e12";

        assertScanGetsErrors(code, 1);
    }

    // GENERAL
    @Test
    void shouldNotParseIllegalInts() {
        String[] codes = {"123aa", "123aa12"};

        assertCodeShouldNotProduceCodeAndOneError(codes);
    }

    @Test
    void shouldNotParseIllegalExponent() {
        String[] codes = {"123ea", "123ea12"};

        assertCodeShouldNotProduceCodeAndOneError(codes);
    }

    private void assertCodeShouldNotProduceCodeAndOneError(String[] codes) {
        for(String code: codes) {
            Scanner testScan = new Scanner(code);
            ScannerResults scannerResults = testScan.scanTokens();
            List<Token> tokens = scannerResults.getTokens();
            List<LexerError> errors = scannerResults.getErrors();
            MatcherAssert.assertThat(tokens.get(0).getType(), Matchers.is(TokenType.EOF));
            assertThat(errors.size(), is(1));
        }
    }

    private void assertScanGetsErrors(String code, int amountOfErrors) {
        Scanner testScan = new Scanner(code);
        List<LexerError> errors = testScan.scanTokens().getErrors();
        assertThat(errors.size(), is(amountOfErrors));
    }
}

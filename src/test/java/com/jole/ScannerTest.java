package com.jole;

import org.junit.jupiter.api.Test;

import java.util.List;

import static com.jole.TokenType.*;
import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.assertThat;

public class ScannerTest {

    @Test
    public void test() {
        Scanner testScan = new Scanner("12.12");
        testScan.scanTokens();
    }


    // FLOAT
    @Test
    void shouldLexSimpleFloat() {
        String code = "12.12";
        Double value = Double.parseDouble(code);

        Scanner testScan = new Scanner(code);
        List<Token> tokens = testScan.scanTokens();
        assertThat(tokens.get(0).getType(), is(FLOAT));
        assertThat(tokens.get(0).getLiteral(), is(value));
        assertThat(tokens.get(0).getLexeme(), is(code));
    }

    @Test
    void shouldLexSimpleExponent() {
        String code = "12e1";
        Double value = Double.parseDouble(code);

        Scanner testScan = new Scanner(code);
        List<Token> tokens = testScan.scanTokens();
        assertThat(tokens.get(0).getType(), is(FLOAT));
        assertThat(tokens.get(0).getLiteral(), is(value));
        assertThat(tokens.get(0).getLexeme(), is(code));
    }

    @Test
    void shouldLexFloatWithDotStart() {
        String code = ".12";
        Double value = Double.parseDouble(code);

        Scanner testScan = new Scanner(code);
        List<Token> tokens = testScan.scanTokens();
        assertThat(tokens.get(0).getType(), is(FLOAT));
        assertThat(tokens.get(0).getLiteral(), is(value));
        assertThat(tokens.get(0).getLexeme(), is(code));
    }

    @Test
    void shouldLexExponentWithDotStart() {
        String code = ".12e10";
        Double value = Double.parseDouble(code);

        Scanner testScan = new Scanner(code);
        List<Token> tokens = testScan.scanTokens();
        assertThat(tokens.get(0).getType(), is(FLOAT));
        assertThat(tokens.get(0).getLiteral(), is(value));
        assertThat(tokens.get(0).getLexeme(), is(code));
    }

    @Test
    void shouldLexFloatWithDotEnd() {
        String code = "12.";
        Double value = Double.parseDouble(code);

        Scanner testScan = new Scanner(code);
        List<Token> tokens = testScan.scanTokens();
        assertThat(tokens.get(0).getType(), is(FLOAT));
        assertThat(tokens.get(0).getLiteral(), is(value));
        assertThat(tokens.get(0).getLexeme(), is(code));
    }

    @Test
    void shouldLexExponentWithDotEnd() {
        String code = "12.e12";
        Double value = Double.parseDouble(code);

        Scanner testScan = new Scanner(code);
        List<Token> tokens = testScan.scanTokens();
        assertThat(tokens.get(0).getType(), is(FLOAT));
        assertThat(tokens.get(0).getLiteral(), is(value));
        assertThat(tokens.get(0).getLexeme(), is(code));
    }

    // INTEGER
    @Test
    void shouldLexInteger() {
        String code = "12";
        Integer value = Integer.parseInt(code);

        Scanner testScan = new Scanner(code);
        List<Token> tokens = testScan.scanTokens();
        assertThat(tokens.get(0).getType(), is(INTEGER));
        assertThat(tokens.get(0).getLiteral(), is(value));
        assertThat(tokens.get(0).getLexeme(), is(code));
    }

    @Test
    void shouldLexNegativeInteger() {
        String code = "-12";
        Integer value = Integer.parseInt(code);

        Scanner testScan = new Scanner(code);
        List<Token> tokens = testScan.scanTokens();
        assertThat(tokens.get(0).getType(), is(INTEGER));
        assertThat(tokens.get(0).getLiteral(), is(value));
        assertThat(tokens.get(0).getLexeme(), is(code));
    }
}

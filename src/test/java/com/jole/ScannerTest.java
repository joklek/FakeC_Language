package com.jole;

import org.junit.jupiter.api.Test;

import java.util.List;

import static com.jole.TokenType.*;
import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.assertThat;

public class ScannerTest {
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
    void shouldLexNegativeExponent() {
        String code = "12e-1";
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

    // Identifiers
    @Test
    void shouldLexIdentifier() {
        String code = "text_12";

        Scanner testScan = new Scanner(code);
        List<Token> tokens = testScan.scanTokens();
        assertThat(tokens.get(0).getType(), is(IDENTIFIER));
        assertThat(tokens.get(0).getLexeme(), is(code));
    }

    @Test
    void shouldLexReservedKeyword() {
        String code = "return";

        Scanner testScan = new Scanner(code);
        List<Token> tokens = testScan.scanTokens();
        assertThat(tokens.get(0).getType(), is(RETURN));
        assertThat(tokens.get(0).getLexeme(), is(code));
    }

    // Strings
    @Test
    void shouldLexSimpleString() {
        String stringContent = "text";
        String code = "\"" + stringContent +"\"";

        Scanner testScan = new Scanner(code);
        List<Token> tokens = testScan.scanTokens();
        assertThat(tokens.get(0).getType(), is(STRING));
        assertThat(tokens.get(0).getLiteral(), is(stringContent));
        assertThat(tokens.get(0).getLexeme(), is(code));
    }

    @Test
    void shouldLexEscapedSymbols() {
        String stringContent = "\n \r \t \\ \" abc";
        String code = "\"" + "\\n \\r \\t \\\\ \\\" abc" + "\""; // \n \r \t \\ " abc

        Scanner testScan = new Scanner(code);
        List<Token> tokens = testScan.scanTokens();
        assertThat(tokens.get(0).getType(), is(STRING));
        assertThat(tokens.get(0).getLiteral(), is(stringContent));
        assertThat(tokens.get(0).getLexeme(), is(code));
    }

    @Test
    void shouldLexEscapedSymbols2() {
        String stringContent = "\\";        //  \
        String code = "\"" + "\\\\" + "\""; // "\\"

        Scanner testScan = new Scanner(code);
        List<Token> tokens = testScan.scanTokens();
        assertThat(tokens.get(0).getType(), is(STRING));
        assertThat(tokens.get(0).getLiteral(), is(stringContent));
        assertThat(tokens.get(0).getLexeme(), is(code));
    }

    @Test
    void shouldLexEmptyString() {
        String stringContent = "";
        String code = "\"" + "\"";

        Scanner testScan = new Scanner(code);
        List<Token> tokens = testScan.scanTokens();
        assertThat(tokens.get(0).getType(), is(STRING));
        assertThat(tokens.get(0).getLiteral(), is(stringContent));
        assertThat(tokens.get(0).getLexeme(), is(code));
    }

    // Char
    @Test
    void shouldLexNormalChars() {
        String wantedChar = "a";
        String code = "'a'";

        Scanner testScan = new Scanner(code);
        List<Token> tokens = testScan.scanTokens();
        assertThat(tokens.get(0).getType(), is(CHAR));
        assertThat(tokens.get(0).getLiteral(), is(wantedChar));
        assertThat(tokens.get(0).getLexeme(), is(code));
    }

    @Test
    void shouldLexEscapedChars() {
        String wantedChar = "'";
        String code = "'\\''";

        Scanner testScan = new Scanner(code);
        List<Token> tokens = testScan.scanTokens();
        assertThat(tokens.get(0).getType(), is(CHAR));
        assertThat(tokens.get(0).getLiteral(), is(wantedChar));
        assertThat(tokens.get(0).getLexeme(), is(code));
    }

    @Test
    void shouldLexEscapedSlash() {
        String wantedChar = "\\";
        String code = "'\\\\'";

        Scanner testScan = new Scanner(code);
        List<Token> tokens = testScan.scanTokens();
        assertThat(tokens.get(0).getType(), is(CHAR));
        assertThat(tokens.get(0).getLiteral(), is(wantedChar));
        assertThat(tokens.get(0).getLexeme(), is(code));
    }

    // IO
    @Test
    void shouldParseInput() {
        String code = "input    >> test";

        Scanner testScan = new Scanner(code);
        List<Token> tokens = testScan.scanTokens();
        assertThat(tokens.get(0).getType(), is(INPUT));
        assertThat(tokens.get(1).getType(), is(INPUT_SIGN));
        assertThat(tokens.get(2).getType(), is(IDENTIFIER));
    }

    @Test
    void shouldParseOutput() {
        String code = "output << test";

        Scanner testScan = new Scanner(code);
        List<Token> tokens = testScan.scanTokens();
        assertThat(tokens.get(0).getType(), is(OUTPUT));
        assertThat(tokens.get(1).getType(), is(OUTPUT_SIGN));
        assertThat(tokens.get(2).getType(), is(IDENTIFIER));
    }
}

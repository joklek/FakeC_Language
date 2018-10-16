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
        ScannerResults scannerResults = testScan.scanTokens();
        List<Token> tokens = scannerResults.getTokens();
        assertThat(tokens.get(0).getType(), is(FLOAT));
        assertThat(tokens.get(0).getLiteral(), is(value));
        assertThat(tokens.get(0).getLexeme(), is(code));
        assertThat(scannerResults.hasErrors(), is(false));
    }

    @Test
    void shouldLexSimpleExponent() {
        String code = "12e1";
        Double value = Double.parseDouble(code);

        Scanner testScan = new Scanner(code);
        ScannerResults scannerResults = testScan.scanTokens();
        List<Token> tokens = scannerResults.getTokens();
        assertThat(tokens.get(0).getType(), is(FLOAT));
        assertThat(tokens.get(0).getLiteral(), is(value));
        assertThat(tokens.get(0).getLexeme(), is(code));
        assertThat(scannerResults.hasErrors(), is(false));
    }

    @Test
    void shouldLexNegativeExponent() {
        String code = "12e-1";
        Double value = Double.parseDouble(code);

        Scanner testScan = new Scanner(code);
        ScannerResults scannerResults = testScan.scanTokens();
        List<Token> tokens = scannerResults.getTokens();
        assertThat(tokens.get(0).getType(), is(FLOAT));
        assertThat(tokens.get(0).getLiteral(), is(value));
        assertThat(tokens.get(0).getLexeme(), is(code));
        assertThat(scannerResults.hasErrors(), is(false));
    }

    @Test
    void shouldLexFloatWithDotStart() {
        String code = ".12";
        Double value = Double.parseDouble(code);

        Scanner testScan = new Scanner(code);
        ScannerResults scannerResults = testScan.scanTokens();
        List<Token> tokens = scannerResults.getTokens();
        assertThat(tokens.get(0).getType(), is(FLOAT));
        assertThat(tokens.get(0).getLiteral(), is(value));
        assertThat(tokens.get(0).getLexeme(), is(code));
        assertThat(scannerResults.hasErrors(), is(false));
    }

    @Test
    void shouldLexExponentWithDotStart() {
        String code = ".12e10";
        Double value = Double.parseDouble(code);

        Scanner testScan = new Scanner(code);
        ScannerResults scannerResults = testScan.scanTokens();
        List<Token> tokens = scannerResults.getTokens();
        assertThat(tokens.get(0).getType(), is(FLOAT));
        assertThat(tokens.get(0).getLiteral(), is(value));
        assertThat(tokens.get(0).getLexeme(), is(code));
        assertThat(scannerResults.hasErrors(), is(false));
    }

    @Test
    void shouldLexFloatWithDotEnd() {
        String code = "12.";
        Double value = Double.parseDouble(code);

        Scanner testScan = new Scanner(code);
        ScannerResults scannerResults = testScan.scanTokens();
        List<Token> tokens = scannerResults.getTokens();
        assertThat(tokens.get(0).getType(), is(FLOAT));
        assertThat(tokens.get(0).getLiteral(), is(value));
        assertThat(tokens.get(0).getLexeme(), is(code));
        assertThat(scannerResults.hasErrors(), is(false));
    }

    @Test
    void shouldLexExponentWithDotEnd() {
        String code = "12.e12";
        Double value = Double.parseDouble(code);

        Scanner testScan = new Scanner(code);
        ScannerResults scannerResults = testScan.scanTokens();
        List<Token> tokens = scannerResults.getTokens();
        assertThat(tokens.get(0).getType(), is(FLOAT));
        assertThat(tokens.get(0).getLiteral(), is(value));
        assertThat(tokens.get(0).getLexeme(), is(code));
        assertThat(scannerResults.hasErrors(), is(false));
    }

    // INTEGER
    @Test
    void shouldLexInteger() {
        String code = "12";
        Integer value = Integer.parseInt(code);

        Scanner testScan = new Scanner(code);
        ScannerResults scannerResults = testScan.scanTokens();
        List<Token> tokens = scannerResults.getTokens();
        assertThat(tokens.get(0).getType(), is(INTEGER));
        assertThat(tokens.get(0).getLiteral(), is(value));
        assertThat(tokens.get(0).getLexeme(), is(code));
        assertThat(scannerResults.hasErrors(), is(false));
    }

    // Identifiers
    @Test
    void shouldLexIdentifier() {
        String code = "text_12";

        Scanner testScan = new Scanner(code);
        ScannerResults scannerResults = testScan.scanTokens();
        List<Token> tokens = scannerResults.getTokens();
        assertThat(tokens.get(0).getType(), is(IDENTIFIER));
        assertThat(tokens.get(0).getLexeme(), is(code));
        assertThat(scannerResults.hasErrors(), is(false));
    }

    @Test
    void shouldLexReservedKeyword() {
        String code = "return";

        Scanner testScan = new Scanner(code);
        ScannerResults scannerResults = testScan.scanTokens();
        List<Token> tokens = scannerResults.getTokens();
        assertThat(tokens.get(0).getType(), is(RETURN));
        assertThat(tokens.get(0).getLexeme(), is(code));
        assertThat(scannerResults.hasErrors(), is(false));
    }

    // Strings
    @Test
    void shouldLexSimpleString() {
        String stringContent = "text";
        String code = "\"" + stringContent +"\"";

        Scanner testScan = new Scanner(code);
        ScannerResults scannerResults = testScan.scanTokens();
        List<Token> tokens = scannerResults.getTokens();
        assertThat(tokens.get(0).getType(), is(STRING));
        assertThat(tokens.get(0).getLiteral(), is(stringContent));
        assertThat(tokens.get(0).getLexeme(), is(code));
        assertThat(scannerResults.hasErrors(), is(false));
    }

    @Test
    void shouldLexEscapedSymbols() {
        String stringContent = "\n \r \t \\ \" abc";
        String code = "\"" + "\\n \\r \\t \\\\ \\\" abc" + "\""; // \n \r \t \\ " abc

        Scanner testScan = new Scanner(code);
        ScannerResults scannerResults = testScan.scanTokens();
        List<Token> tokens = scannerResults.getTokens();
        assertThat(tokens.get(0).getType(), is(STRING));
        assertThat(tokens.get(0).getLiteral(), is(stringContent));
        assertThat(tokens.get(0).getLexeme(), is(code));
        assertThat(scannerResults.hasErrors(), is(false));
    }

    @Test
    void shouldLexEscapedSymbols2() {
        String stringContent = "\\";        //  \
        String code = "\"" + "\\\\" + "\""; // "\\"

        Scanner testScan = new Scanner(code);
        ScannerResults scannerResults = testScan.scanTokens();
        List<Token> tokens = scannerResults.getTokens();
        assertThat(tokens.get(0).getType(), is(STRING));
        assertThat(tokens.get(0).getLiteral(), is(stringContent));
        assertThat(tokens.get(0).getLexeme(), is(code));
        assertThat(scannerResults.hasErrors(), is(false));
    }

    @Test
    void shouldLexEmptyString() {
        String stringContent = "";
        String code = "\"" + "\"";

        Scanner testScan = new Scanner(code);
        ScannerResults scannerResults = testScan.scanTokens();
        List<Token> tokens = scannerResults.getTokens();
        assertThat(tokens.get(0).getType(), is(STRING));
        assertThat(tokens.get(0).getLiteral(), is(stringContent));
        assertThat(tokens.get(0).getLexeme(), is(code));
        assertThat(scannerResults.hasErrors(), is(false));
    }

    // Char
    @Test
    void shouldLexNormalChars() {
        String wantedChar = "a";
        String code = "'a'";

        Scanner testScan = new Scanner(code);
        ScannerResults scannerResults = testScan.scanTokens();
        List<Token> tokens = scannerResults.getTokens();
        assertThat(tokens.get(0).getType(), is(CHAR));
        assertThat(tokens.get(0).getLiteral(), is(wantedChar));
        assertThat(tokens.get(0).getLexeme(), is(code));
        assertThat(scannerResults.hasErrors(), is(false));
    }

    @Test
    void shouldLexEscapedChars() {
        String wantedChar = "'";
        String code = "'\\''";

        Scanner testScan = new Scanner(code);
        ScannerResults scannerResults = testScan.scanTokens();
        List<Token> tokens = scannerResults.getTokens();
        assertThat(tokens.get(0).getType(), is(CHAR));
        assertThat(tokens.get(0).getLiteral(), is(wantedChar));
        assertThat(tokens.get(0).getLexeme(), is(code));
        assertThat(scannerResults.hasErrors(), is(false));
    }

    @Test
    void shouldLexEscapedSlash() {
        String wantedChar = "\\";
        String code = "'\\\\'";

        Scanner testScan = new Scanner(code);
        ScannerResults scannerResults = testScan.scanTokens();
        List<Token> tokens = scannerResults.getTokens();
        assertThat(tokens.get(0).getType(), is(CHAR));
        assertThat(tokens.get(0).getLiteral(), is(wantedChar));
        assertThat(tokens.get(0).getLexeme(), is(code));
        assertThat(scannerResults.hasErrors(), is(false));
    }

    // IO
    @Test
    void shouldParseInput() {
        String code = "input    >> test";

        Scanner testScan = new Scanner(code);
        ScannerResults scannerResults = testScan.scanTokens();
        List<Token> tokens = scannerResults.getTokens();
        assertThat(tokens.get(0).getType(), is(INPUT));
        assertThat(tokens.get(1).getType(), is(INPUT_SIGN));
        assertThat(tokens.get(2).getType(), is(IDENTIFIER));
        assertThat(scannerResults.hasErrors(), is(false));
    }

    @Test
    void shouldParseOutput() {
        String code = "output << test";

        Scanner testScan = new Scanner(code);
        ScannerResults scannerResults = testScan.scanTokens();
        List<Token> tokens = scannerResults.getTokens();
        assertThat(tokens.get(0).getType(), is(OUTPUT));
        assertThat(tokens.get(1).getType(), is(OUTPUT_SIGN));
        assertThat(tokens.get(2).getType(), is(IDENTIFIER));
        assertThat(scannerResults.hasErrors(), is(false));
    }


    // GENERAL
    @Test
    void shouldIgnoreSingleLineComments() {
        String code = "//This is a comment. A number is after new line and it should be parsed\n12";

        Scanner testScan = new Scanner(code);
        ScannerResults scannerResults = testScan.scanTokens();
        List<Token> tokens = scannerResults.getTokens();
        assertThat(tokens.get(0).getType(), is(INTEGER));
        assertThat(scannerResults.hasErrors(), is(false));
    }

    @Test
    void shouldIgnoreMultiLineComments() {
        String code = "/* This is a  multiline comment. \n A number is after the comment and it should be parsed*/ 12";

        Scanner testScan = new Scanner(code);
        ScannerResults scannerResults = testScan.scanTokens();
        List<Token> tokens = scannerResults.getTokens();
        assertThat(tokens.get(0).getType(), is(INTEGER));
        assertThat(scannerResults.hasErrors(), is(false));
    }

    @Test
    void shouldIgnoreUnclosedMultiLineComments() {
        String code = "/* This is an unclosed multiline comment";

        Scanner testScan = new Scanner(code);
        ScannerResults scannerResults = testScan.scanTokens();
        List<Token> tokens = scannerResults.getTokens();
        assertThat(tokens.get(0).getType(), is(EOF));
        assertThat(scannerResults.hasErrors(), is(false));
    }

    @Test
    void shouldIgnoreNestedComments() {
        String code = "/*  /*  /* /**/ // text  */  */  */";

        Scanner testScan = new Scanner(code);
        ScannerResults scannerResults = testScan.scanTokens();
        List<Token> tokens = scannerResults.getTokens();
        assertThat(tokens.get(0).getType(), is(EOF));
        assertThat(scannerResults.hasErrors(), is(false));
    }
}

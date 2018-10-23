package com.jole;

import com.jole.tokens.Token;
import com.jole.tokens.TokenType;
import org.junit.jupiter.api.Test;

import java.util.List;

import static com.jole.tokens.TokenType.*;
import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.assertThat;

public class ScannerTest {

    private void scanAndAssertValueWithType(String code, Object value, TokenType expectedType) {
        Scanner testScan = new Scanner(code);
        ScannerResults scannerResults = testScan.scanTokens();
        List<Token> tokens = scannerResults.getTokens();
        assertThat(tokens.get(0).getType(), is(expectedType));
        assertThat(tokens.get(0).getLiteral(), is(value));
        assertThat(tokens.get(0).getLexeme(), is(code));
        assertThat(scannerResults.hasErrors(), is(false));
    }

    private void scanAndAssertLexemeWithTypeNoValue(String code, TokenType expectedType) {
        scanAndAssertValueWithType(code, null, expectedType);
    }

    private void scanAndAssertResolvedType(String code, TokenType expectedType) {
        Scanner testScan = new Scanner(code);
        ScannerResults scannerResults = testScan.scanTokens();
        List<Token> tokens = scannerResults.getTokens();
        assertThat(tokens.get(0).getType(), is(expectedType));
        assertThat(scannerResults.hasErrors(), is(false));
    }

    private void scanAndAssertResolvedValueAndType(String code, Integer value, TokenType expectedType) {
        Scanner testScan = new Scanner(code);
        ScannerResults scannerResults = testScan.scanTokens();
        List<Token> tokens = scannerResults.getTokens();
        assertThat(tokens.get(0).getType(), is(expectedType));
        assertThat(tokens.get(0).getLiteral(), is(value));
        assertThat(scannerResults.hasErrors(), is(false));
    }

    // FLOAT
    @Test
    void shouldLexSimpleFloat() {
        String code = "12.12";
        Double value = Double.parseDouble(code);

        scanAndAssertValueWithType(code, value, FLOAT);
    }

    @Test
    void shouldLexSimpleExponent() {
        String code = "12e1";
        Double value = Double.parseDouble(code);

        scanAndAssertValueWithType(code, value, FLOAT);
    }

    @Test
    void shouldLexNegativeExponent() {
        String code = "12e-1";
        Double value = Double.parseDouble(code);

        scanAndAssertValueWithType(code, value, FLOAT);
    }

    @Test
    void shouldLexFloatWithDotStart() {
        String code = ".12";
        Double value = Double.parseDouble(code);

        scanAndAssertValueWithType(code, value, FLOAT);
    }

    @Test
    void shouldLexExponentWithDotStart() {
        String code = ".12e10";
        Double value = Double.parseDouble(code);

        scanAndAssertValueWithType(code, value, FLOAT);
    }

    @Test
    void shouldLexFloatWithDotEnd() {
        String code = "12.";
        Double value = Double.parseDouble(code);

        scanAndAssertValueWithType(code, value, FLOAT);
    }

    @Test
    void shouldLexExponentWithDotEnd() {
        String code = "12.e12";
        Double value = Double.parseDouble(code);

        scanAndAssertValueWithType(code, value, FLOAT);
    }

    // INTEGER
    @Test
    void shouldLexInteger() {
        String code = "12";
        Integer value = Integer.parseInt(code);

        scanAndAssertValueWithType(code, value, INTEGER);
    }

    // Identifiers
    @Test
    void shouldLexIdentifier() {
        String code = "text_12";
        scanAndAssertValueWithType(code, code, IDENTIFIER);
    }

    @Test
    void shouldLexReservedKeyword() {
        String code = "return";
        scanAndAssertLexemeWithTypeNoValue(code, RETURN);
    }

    // Strings
    @Test
    void shouldLexSimpleString() {
        String stringContent = "text";
        String code = "\"" + stringContent +"\"";

        scanAndAssertValueWithType(code, stringContent, STRING);
    }

    @Test
    void shouldLexEscapedSymbols() {
        String stringContent = "\n \r \t \\ \" abc";
        String code = "\"" + "\\n \\r \\t \\\\ \\\" abc" + "\""; // \n \r \t \\ " abc

        scanAndAssertValueWithType(code, stringContent, STRING);
    }

    @Test
    void shouldLexEscapedSymbols2() {
        String stringContent = "\\";        //  \
        String code = "\"" + "\\\\" + "\""; // "\\"

        scanAndAssertValueWithType(code, stringContent, STRING);
    }

    @Test
    void shouldLexEmptyString() {
        String stringContent = "";
        String code = "\"" + "\"";

        scanAndAssertValueWithType(code, stringContent, STRING);
    }

    // Char
    @Test
    void shouldLexNormalChars() {
        String wantedChar = "a";
        String code = "'a'";

        scanAndAssertValueWithType(code, wantedChar, CHAR);
    }

    @Test
    void shouldLexEscapedChars() {
        String wantedChar = "'";
        String code = "'\\''";

        scanAndAssertValueWithType(code, wantedChar, CHAR);
    }

    @Test
    void shouldLexEscapedSlash() {
        String wantedChar = "\\";
        String code = "'\\\\'";

        scanAndAssertValueWithType(code, wantedChar, CHAR);
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
        Integer value = 12;
        String code = "//This is a comment. A number is after new line and it should be parsed\n" + value;

        scanAndAssertResolvedValueAndType(code, value, INTEGER);
    }

    @Test
    void shouldIgnoreMultiLineComments() {
        Integer value = 12;
        String code = "/* This is a  multiline comment. \n A number is after the comment and it should be parsed*/" + value;

        scanAndAssertResolvedValueAndType(code, value, INTEGER);
    }

    @Test
    void shouldIgnoreUnclosedMultiLineComments() {
        String code = "/* This is an unclosed multiline comment";
        TokenType expectedType = EOF;

        scanAndAssertResolvedType(code, expectedType);
    }

    @Test
    void shouldIgnoreNestedComments() {
        String code = "/*  /*  /* /**/ // text  */  */  */";
        scanAndAssertResolvedType(code, EOF);
    }
}

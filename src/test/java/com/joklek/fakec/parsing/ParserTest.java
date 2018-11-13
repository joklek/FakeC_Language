package com.joklek.fakec.parsing;

import com.joklek.fakec.tokens.Token;
import com.joklek.fakec.tokens.TokenType;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

import static com.joklek.fakec.tokens.TokenType.*;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.params.provider.Arguments.arguments;


// TODO: These test sucks because I'm using the printer class. Should probably create a dedicated visitor
public class ParserTest {
    private AstPrinter printer = new AstPrinter();

    private static Stream<Arguments> tokenAndExpectedResult() {
        return Stream.of(
                arguments(IDENTIFIER, "name", "name", "name"),
                arguments(FALSE, null, false, "false"),
                arguments(TRUE, null, true, "true"),
                arguments(NULL, null, null, "null"),
                arguments(INTEGER, null, 12, "12"),
                arguments(FLOAT, null, 12.12, "12.12"),
                arguments(STRING, null, "test", "test")
        );
    }
    @ParameterizedTest
    @MethodSource("tokenAndExpectedResult")
    void shouldFormVariable(TokenType type, String lexeme, Object literal, String expectedValue) {
        List<Token> tokens = Arrays.asList(new Token(type, lexeme, literal, 0), new Token(TokenType.EOF, 0));
        Parser parser = new Parser(tokens);
        assertThat(printer.print(parser.parseElement()), is(expectedValue));
    }

    @Test
    void shouldParseUnarySimple() {
        Token integer = new Token(INTEGER, null, 12, 0);
        List<Token> tokens = Arrays.asList(integer, new Token(STAR, 0), integer, new Token(TokenType.EOF, 0));
        Parser parser = new Parser(tokens);
        assertThat(printer.print(parser.parseMultiplication()), is("BinaryExpr(STAR):\r\n" +
                                                              "  Left: 12\r\n" +
                                                              "  Right: 12"));
    }

   /* @Test
    void shouldParseMultiplication() {
        List<Token> tokens = Arrays.asList(new Token(type, lexeme, literal, 0), new Token(TokenType.EOF, 0));
        Parser parser = new Parser(tokens);
        assertThat(printer.print(parser.multiplication()), is(expectedValue));
    }*/

    private List<Token> formTokens(TokenType... args) {
        List<Token> tokens = new ArrayList<>();
        for(TokenType type : args) {
            tokens.add(new Token(type, 0));
        }
        tokens.add(new Token(TokenType.EOF, 0));
        return tokens;
    }
}

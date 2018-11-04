package com.jole.fakec;

import com.jole.fakec.tokens.TokenType;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.params.provider.Arguments.arguments;

public class SimpleTokenTest {

    private static Stream<Arguments> tokensAndExpectedTypes() {
        return Stream.of(
                arguments("(", TokenType.LEFT_PAREN),
                arguments(")", TokenType.RIGHT_PAREN),
                arguments("{", TokenType.CURLY_LEFT),
                arguments("}", TokenType.CURLY_RIGHT),
                arguments("[", TokenType.LEFT_BRACE),
                arguments("]", TokenType.RIGHT_BRACE),
                arguments(",", TokenType.COMMA),
                arguments(";", TokenType.SEMICOLON),

                arguments("+", TokenType.PLUS),
                arguments("+=", TokenType.PLUS_EQUAL),
                arguments("-", TokenType.MINUS),
                arguments("-=", TokenType.MINUS_EQUAL),
                arguments("*", TokenType.STAR),
                arguments("*=", TokenType.MUL_EQUAL),
                arguments("%", TokenType.MOD),
                arguments("%=", TokenType.MOD_EQUAL),
                arguments("=", TokenType.EQUAL),
                arguments("==", TokenType.EQUAL_EQUAL),
                arguments("!", TokenType.NOT),
                arguments("!=", TokenType.NOT_EQUAL),

                arguments(">", TokenType.GREATER),
                arguments(">=", TokenType.GREATER_EQUAL),
                arguments(">>", TokenType.INPUT_SIGN),

                arguments("<", TokenType.LESS),
                arguments("<=", TokenType.LESS_EQUAL),
                arguments("<<", TokenType.OUTPUT_SIGN),

                arguments("/=", TokenType.DIV_EQUAL),
                arguments("/", TokenType.SLASH)
        );
    }

    @ParameterizedTest
    @MethodSource("tokensAndExpectedTypes")
    void shouldGetCorrectTokenType(String token, TokenType expectedType) {
        Scanner scanner = new Scanner(token);
        ScannerResults scannerResults = scanner.scanTokens();
        assertThat(scannerResults.getTokens().get(0).getType(), is(expectedType));
    }
}

package com.jole;

import com.jole.tokens.TokenType;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static com.jole.tokens.TokenType.*;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.params.provider.Arguments.arguments;

public class SimpleTokenTest {

    private static Stream<Arguments> tokensAndExpectedTypes() {
        return Stream.of(
                arguments("(", LEFT_PAREN),
                arguments(")", RIGHT_PAREN),
                arguments("{", CURLY_LEFT),
                arguments("}", CURLY_RIGHT),
                arguments("[", LEFT_BRACE),
                arguments("]", RIGHT_BRACE),
                arguments(",", COMMA),
                arguments(";", SEMICOLON),

                arguments("+", PLUS),
                arguments("+=", PLUS_EQUAL),
                arguments("-", MINUS),
                arguments("-=", MINUS_EQUAL),
                arguments("*", STAR),
                arguments("*=", MUL_EQUAL),
                arguments("%", MOD),
                arguments("%=", MOD_EQUAL),
                arguments("=", EQUAL),
                arguments("==", EQUAL_EQUAL),
                arguments("!", NOT),
                arguments("!=", NOT_EQUAL),

                arguments(">", GREATER),
                arguments(">=", GREATER_EQUAL),
                arguments(">>", INPUT_SIGN),

                arguments("<", LESS),
                arguments("<=", LESS_EQUAL),
                arguments("<<", OUTPUT_SIGN),

                arguments("/=", DIV_EQUAL),
                arguments("/", SLASH)
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

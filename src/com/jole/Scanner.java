package com.jole;

import java.util.ArrayList;
import java.util.List;

import static com.jole.TokenType.*;

public class Scanner {
    private final String source;
    private final List<Token> tokens;
    private int start = 0;
    private int current = 0;
    private int line = 1;

    public Scanner(String source) {
        this.source = source;
        this.tokens = new ArrayList<Token>();
    }

    public List<Token> scanTokens() {
        while (isAtEnd()) {
            start = current;
            scanToken();
        }
        tokens.add(new Token(EOF, line));
        return tokens;
    }

    private void scanToken() {
        char c = advance();
        switch (c) {
            case '"': lexString(); break;

            case  '(': addToken(LEFT_PAREN); break;
            case  ')': addToken(RIGHT_PAREN); break;
            case  '{': addToken(CURLY_LEFT); break;
            case  '}': addToken(CURLY_RIGHT); break;
            case  '[': addToken(LEFT_BRACE); break;
            case  ']': addToken(RIGHT_BRACE); break;
            case  ',': addToken(COMMA); break;
            case  '.': addToken(DOT); break;
            case  ';': addToken(SEMICOLON); break;

            case  '+': addToken(match('=') ? PLUS_EQUAL : PLUS); break;
            case  '-': addToken(match('=') ? MINUS_EQUAL : MINUS); break;
            case  '!': addToken(match('=') ? NOT_EQUAL : NOT); break;
            case  '*': addToken(match('=') ? MUL_EQUAL : STAR); break;
            case  '%': addToken(match('=') ? MOD_EQUAL : PERCENT); break;
            case  '=': addToken(match('=') ? EQUAL_EQUAL : EQUAL); break;
            case  '>': addToken(match('=') ? GREATER_EQUAL : GREATER); break;
            case  '<': addToken(match('=') ? LESS_EQUAL : LESS); break;

            case  '/':
                if(match('/')) {
                    while (peek() != '\n' && !isAtEnd()) {
                        advance();
                    }
                }
                if(match('=')) {
                    addToken(DIV_EQUAL);
                }
                else {
                    addToken(SLASH);
                }
                break;
            case ' ':
            case '\t':
            case '\r':
                // do nothing
                break;
            case '\n':
                line++;
                break;
            default:
                // TODO ERROR
                break;
        }
    }

    private void lexString() {
        while(peek() != '"' && !isAtEnd()) {
            if(peek() == '\n') {
                line++;
            }
            advance();
        }
        if(isAtEnd()) {
            // TODO: error unterminated string
            return;
        }

        // for closing "
        advance();

        String value = source.substring(start + 1, current - 1);
        addToken(STRING, value);
    }

    private char peek() {
        if (isAtEnd()) {
            return '\n';
        }
        return source.charAt(current);
    }

    private boolean match(char charToMatch) {
        if(isAtEnd()) {
            return false;
        }
        if(source.charAt(current) != charToMatch) {
            return false;
        }
        current++;
        return true;
    }

    private char advance() {
        current++;
        return source.charAt(current - 1);
    }

    private void addToken(TokenType tokenType) {
        addToken(tokenType, null);
    }

    private void addToken(TokenType tokenType, Object literal) {
        String text = source.substring(start, current);
        tokens.add(new Token(tokenType, text, literal, line));
    }

    private boolean isAtEnd() {
        return current >= source.length();
    }
}

package com.jole;

import com.jole.utils.ReservedKeywordUtils;
import com.jole.utils.StringParsingUtils;

import java.util.ArrayList;
import java.util.List;

import static com.jole.TokenType.*;

public class Scanner {

    private final String source;
    private final List<Token> tokens;
    private final StringParsingUtils stringParsingUtils;
    private final ReservedKeywordUtils reservedKeywordUtils;
    private int start = 0;
    private int current = 0;
    private int currentLine = 1;
    private int startLine;

    public Scanner(String source, StringParsingUtils stringParsingUtils, ReservedKeywordUtils reservedKeywordUtils) {
        this.source = source;
        this.tokens = new ArrayList<>();
        this.stringParsingUtils = stringParsingUtils;
        this.reservedKeywordUtils = reservedKeywordUtils;
    }

    public Scanner(String source) {
        this.source = source;
        this.tokens = new ArrayList<>();
        this.stringParsingUtils = new StringParsingUtils();
        this.reservedKeywordUtils = new ReservedKeywordUtils();
    }

    public List<Token> scanTokens() {
        while (!isAtEnd()) {
            start = current;
            scanToken();
        }
        tokens.add(new Token(EOF, currentLine));
        return tokens;
    }

    private void scanToken() {
        char c = advance();
        startLine = currentLine;
        switch (c) {
            case  '"': lexString(); break;
            case '\'': lexChar(); break;

            case  '(': addToken(LEFT_PAREN); break;
            case  ')': addToken(RIGHT_PAREN); break;
            case  '{': addToken(CURLY_LEFT); break;
            case  '}': addToken(CURLY_RIGHT); break;
            case  '[': addToken(LEFT_BRACE); break;
            case  ']': addToken(RIGHT_BRACE); break;
            case  ',': addToken(COMMA); break;
            case  ';': addToken(SEMICOLON); break;

            case  '+': addToken(match('=') ? PLUS_EQUAL : PLUS); break;
            case  '-': addToken(match('=') ? MINUS_EQUAL : MINUS); break;
            case  '*': addToken(match('=') ? MUL_EQUAL : STAR); break;
            case  '%': addToken(match('=') ? MOD_EQUAL : MOD); break;
            case  '=': addToken(match('=') ? EQUAL_EQUAL : EQUAL); break;
            case  '>':
                if (match('=')) {
                    addToken(GREATER_EQUAL);
                } else {
                    addToken(match('>') ? INPUT_SIGN : GREATER);
                }
                break;
            case  '<':
                if (match('=')) {
                    addToken(LESS_EQUAL);
                } else {
                    addToken(match('<') ? OUTPUT_SIGN : LESS);
                }
                break;
            case  '/':
                if(match('/')) {
                    while (peek() != '\n' && !isAtEnd()) {
                        advance();
                    }
                }
                else if(match('*')) {
                    parseMultilineComment();
                }
                else if(match('=')) {
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
                currentLine++;
                break;
            default:
                if(isDigit(c) || (c == '.' && isDigit(peek())) ) {
                    lexNumber();
                }
                else if (isAlpha(c)) {
                    lexIdentifier();
                }
                else {
                    error("Unidentified lexema \"" + source.substring(start, current) + "\" at line", currentLine);
                }
                break;
        }
    }

    private void parseMultilineComment() {
        while ((peek() != '*' || peekNext() != '/') && !isAtEnd()) {
            if (peek() == '\n') {
                currentLine++;
            }
            if(match('/') && match('*')) {
                parseMultilineComment();
            }
            else if (!isAtEnd()){
                advance();
            }
        }
        match('*');
        match('/');
    }

    private void error(String errorMessage, int line) {
        System.out.printf("ERROR at line %s with message: %s%n", line, errorMessage);
    }

    private void lexChar() {
        while(peek() != '\'' && !isAtEnd()) {
            if(peek() == '\n') {
                currentLine++;
            }
            if(peek() == '\\') {
                char nextChar = peekNext();
                if (nextChar == '\'' || stringParsingUtils.escapedChar(nextChar)) {
                    advance();
                }
                else {
                    error("Illegal escaped character \" \\" + nextChar + "\"", currentLine);
                    return;
                }
            }
            advance();
        }
        if(isAtEnd()) {
            error("Unterminated char", startLine);
            return;
        }

        // for closing '
        advance();

        String value = source.substring(start + 1, current - 1);
        String unescapedValue = stringParsingUtils.unescapeCharSymbols(value);
        if (unescapedValue.length() == 1) {
            addToken(CHAR, unescapedValue);
        }
        else {
            error("Char should be of one character length and is of: " + unescapedValue.length(), currentLine);
        }
    }

    private void lexIdentifier() {
        while (isAlphaNumeric(peek())) {
            advance();
        }

        String text = source.substring(start, current);
        TokenType type = reservedKeywordUtils.getTokenType(text);
        // check if text is reserved
        if (type == null) {
            type = IDENTIFIER;
            addToken(type, text);
        }
        else {
            addToken(type);
        }
    }

    private void lexNumber() {
        boolean isFloat = false;

        if(source.charAt(current - 1) != '.') {
            collectNumbers();
        }
        else {
            isFloat = true;  // this flag is optionally set here for clarity
            current--; // if dot is consumed, unconsume for simplified lexing
        }

        if (peek() == '.') {
            isFloat = true;
            advance();
            collectNumbers();
        }
        if (peek() == 'e') {
            isFloat = true;
            advance();
            if (peek() == '-') {
                advance();
            }
            int amount = collectNumbers();
            if(amount == 0) {
                error("Exponential should have numbers after e", currentLine);
                return;
            }
        }

        if(isAlphaNumeric(peek())) {
            error("Numbers should not have trailing letters", currentLine);
            return;
        }

        String value = source.substring(start, current);
        if(isFloat) {
            Double unescapedValue = Double.parseDouble(value);
            addToken(FLOAT, unescapedValue);
        }
        else {

            Integer unescapedValue = Integer.parseInt(value);
            addToken(INTEGER, unescapedValue);
        }
    }

    /**
     *
     * @return amount of collected numbers
     */
    private int collectNumbers() {
        int amount = 0;
        while (isDigit(peek())) {
            advance();
            amount++;
        }
        return amount;
    }

    private void lexString() {
        while(peek() != '"' && !isAtEnd()) {
            if(peek() == '\n') {
                currentLine++;
            }
            if(peek() == '\\') {
                char nextChar = peekNext();
                if (nextChar == '"' || stringParsingUtils.escapedChar(nextChar)) {
                    advance();
                }
                else {
                    error("Illegal escaped character \" \\" + nextChar + "\"", currentLine);
                    return;
                }
            }
            advance();
        }
        if(isAtEnd()) {
            error("Unterminated string", startLine);
            return;
        }

        // for closing "
        advance();

        String value = source.substring(start + 1, current - 1);
        String unescapedValue = stringParsingUtils.unescapeSymbols(value);
        addToken(STRING, unescapedValue);
    }

    /**
     * peeks a character
     * @return character in front
     */
    private char peek() {
        if (isAtEnd()) {
            return '\n';
        }
        return source.charAt(current);
    }

    /**
     * peeks following character
     * @return character in front
     */
    private char peekNext() {
        if (current + 1 >= source.length()) {
            return '\0';
        }
        return source.charAt(current + 1);
    }

    /**
     * @param charToMatch character to match
     * @return true if current char is charToMatch
     */
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

    /**
     * steps one character
     * @return previous character
     */
    private char advance() {
        current++;
        return source.charAt(current - 1);
    }

    private void addToken(TokenType tokenType) {
        addToken(tokenType, null);
    }

    private void addToken(TokenType tokenType, Object literal) {
        String text = source.substring(start, current);
        tokens.add(new Token(tokenType, text, literal, startLine));
    }

    private boolean isAtEnd() {
        return current >= source.length();
    }

    private boolean isDigit(char c) {
        return c >= '0' && c <= '9';
    }

    private boolean isAlpha(char c) {
        return (c >= 'a' && c <= 'z') ||
                (c >= 'A' && c <= 'Z') ||
                c == '_';
    }

    private boolean isAlphaNumeric(char c) {
        return isAlpha(c) || isDigit(c);
    }
}

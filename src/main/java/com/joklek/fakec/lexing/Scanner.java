package com.joklek.fakec.lexing;

import com.joklek.fakec.lexing.utils.StringParsingUtils;
import com.joklek.fakec.tokens.Token;
import com.joklek.fakec.tokens.TokenType;
import com.joklek.fakec.lexing.error.LexerError;
import com.joklek.fakec.lexing.utils.ReservedKeywordUtils;

import java.util.ArrayList;
import java.util.List;

public class Scanner {

    private final String source;
    private final List<Token> tokens;
    private final List<LexerError> errors;
    private final StringParsingUtils stringParsingUtils;
    private final ReservedKeywordUtils reservedKeywordUtils;
    private int start = 0;
    private int current = 0;
    private int currentLine = 1;
    private int startLine;

    public Scanner(String source) {
        this.source = source;
        this.tokens = new ArrayList<>();
        this.errors = new ArrayList<>();
        this.stringParsingUtils = new StringParsingUtils();
        this.reservedKeywordUtils = new ReservedKeywordUtils();
    }

    public ScannerResults scanTokens() {
        while (!isAtEnd()) {
            start = current;
            scanToken();
        }
        tokens.add(new Token(TokenType.EOF, currentLine));
        return new ScannerResults(tokens, errors);
    }

    private void scanToken() {
        char c = advance();
        startLine = currentLine;
        switch (c) {
            case  '"': lexString(); break;
            case '\'': lexChar(); break;

            case  '(': addToken(TokenType.LEFT_PAREN); break;
            case  ')': addToken(TokenType.RIGHT_PAREN); break;
            case  '{': addToken(TokenType.CURLY_LEFT); break;
            case  '}': addToken(TokenType.CURLY_RIGHT); break;
            case  '[': addToken(TokenType.LEFT_BRACE); break;
            case  ']': addToken(TokenType.RIGHT_BRACE); break;
            case  ',': addToken(TokenType.COMMA); break;
            case  ';': addToken(TokenType.SEMICOLON); break;

            case  '+':
                TokenType plusType;
                if (match('=')) {
                    plusType = TokenType.PLUS_EQUAL;
                }
                else if(match('+')) {
                    plusType = TokenType.INC;
                }
                else {
                    plusType = TokenType.PLUS;
                }
                addToken(plusType);
                break;
            case  '-':
                TokenType minusType;
                if (match('=')) {
                    minusType = TokenType.MINUS_EQUAL;
                }
                else if(match('-')) {
                    minusType = TokenType.DEC;
                }
                else {
                    minusType = TokenType.MINUS;
                }
                addToken(minusType);
                break;
            case  '*': addToken(match('=') ? TokenType.MUL_EQUAL : TokenType.STAR); break;
            case  '%': addToken(match('=') ? TokenType.MOD_EQUAL : TokenType.MOD); break;
            case  '=': addToken(match('=') ? TokenType.EQUAL_EQUAL : TokenType.EQUAL); break;
            case  '!': addToken(match('=') ? TokenType.NOT_EQUAL : TokenType.NOT); break;
            case  '>':
                if (match('=')) {
                    addToken(TokenType.GREATER_EQUAL);
                } else {
                    addToken(match('>') ? TokenType.INPUT_SIGN : TokenType.GREATER);
                }
                break;
            case  '<':
                if (match('=')) {
                    addToken(TokenType.LESS_EQUAL);
                } else {
                    addToken(match('<') ? TokenType.OUTPUT_SIGN : TokenType.LESS);
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
                    addToken(TokenType.DIV_EQUAL);
                }
                else {
                    addToken(TokenType.SLASH);
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
                    error("Unidentified lexema \"" + source.substring(start, current) + "\"", currentLine);
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
        errors.add(new LexerError(errorMessage, line));
    }

    private void lexChar() {
        while(peek() != '\'' && !isAtEnd()) {
            if(peek() == '\n') {
                currentLine++;
            }
            if(match('\\')) {
                char nextChar = peek();
                if (nextChar != '\'' && !stringParsingUtils.escapedChar(nextChar)) {
                    advance();
                    error("Illegal escaped character \" \\" + nextChar + "\"", currentLine);
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
            addToken(TokenType.CHAR, unescapedValue);
        }
        else {
            error("Char should be of one character length and is of: " + unescapedValue.length(), currentLine);
        }
    }

    private void lexIdentifier() {
        collectAlphanumeric();

        String text = source.substring(start, current);
        TokenType type = reservedKeywordUtils.getTokenType(text);
        // check if text is reserved
        if (type == null) {
            type = TokenType.IDENTIFIER;
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
                collectAlphanumeric();
                error("Exponential should have numbers after e: \"" + source.substring(start, current) + "\"", currentLine);  // TODO export substring logic to method
                return;
            }
        }

        if(isAlpha(peek())) {
            collectAlphanumeric();
            error("Numbers should not have trailing letters \"" + source.substring(start, current) + "\"", currentLine);
            return;
        }

        String value = source.substring(start, current);
        if(isFloat) {
            Double unescapedValue = Double.parseDouble(value);
            addToken(TokenType.FLOAT, unescapedValue);
        }
        else {

            Integer unescapedValue = Integer.parseInt(value);
            addToken(TokenType.INTEGER, unescapedValue);
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

    private int collectAlphanumeric() {
        int amount = 0;
        while (isAlphaNumeric(peek())) {
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
            if(match('\\')) {
                char nextChar = peek();
                if (nextChar != '"' && !stringParsingUtils.escapedChar(nextChar)) {
                    error("Illegal escaped character \" \\" + nextChar + "\"", currentLine);
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
        addToken(TokenType.STRING, unescapedValue);
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

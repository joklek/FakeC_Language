package com.joklek.fakec.parsing.types;

import com.joklek.fakec.parsing.error.ParserError;
import com.joklek.fakec.tokens.Token;
import com.joklek.fakec.tokens.TokenType;

import java.util.EnumMap;

import static com.joklek.fakec.tokens.TokenType.*;

public class OperationConverter implements TokenConverter<OperationType> {

    private final EnumMap<TokenType, OperationType> operationTokenMap;

    public OperationConverter() {
        this.operationTokenMap = new EnumMap<>(TokenType.class);
        this.operationTokenMap.put(STAR, OperationType.MULT);
        this.operationTokenMap.put(SLASH, OperationType.DIV);
        this.operationTokenMap.put(PLUS, OperationType.ADD);
        this.operationTokenMap.put(MINUS, OperationType.SUB);
        this.operationTokenMap.put(MOD, OperationType.MOD);
        this.operationTokenMap.put(NOT, OperationType.NOT);

        this.operationTokenMap.put(LESS, OperationType.LESS);
        this.operationTokenMap.put(LESS_EQUAL, OperationType.LESS_EQUAL);
        this.operationTokenMap.put(GREATER, OperationType.GREATER);
        this.operationTokenMap.put(GREATER_EQUAL, OperationType.GREATER_EQUAL);
        this.operationTokenMap.put(EQUAL, OperationType.EQUAL);
        this.operationTokenMap.put(EQUAL_EQUAL, OperationType.EQUAL_EQUAL);
        this.operationTokenMap.put(NOT_EQUAL, OperationType.NOT_EQUAL);

        this.operationTokenMap.put(PLUS_EQUAL, OperationType.PLUS_EQUAL);
        this.operationTokenMap.put(MINUS_EQUAL, OperationType.MINUS_EQUAL);
        this.operationTokenMap.put(MUL_EQUAL, OperationType.MUL_EQUAL);
        this.operationTokenMap.put(DIV_EQUAL, OperationType.DIV_EQUAL);
        this.operationTokenMap.put(MOD_EQUAL, OperationType.MOD_EQUAL);

        this.operationTokenMap.put(AND, OperationType.AND);
        this.operationTokenMap.put(OR, OperationType.AND);
    }

    @Override
    public OperationType convertToken(Token token) {
        OperationType operationType = operationTokenMap.get(token.getType());
        if(operationType == null) {
            throw new ParserError(String.format("Operation token %s is not supported", token.getType()), token);
        }
        return operationType;
    }
}

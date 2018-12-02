package com.joklek.fakec.parsing.types;

import com.joklek.fakec.parsing.error.ParserError;
import com.joklek.fakec.tokens.Token;
import com.joklek.fakec.tokens.TokenType;

import java.util.EnumMap;

import static com.joklek.fakec.tokens.TokenType.*;
import static com.joklek.fakec.tokens.TokenType.BOOL_TYPE;
import static com.joklek.fakec.tokens.TokenType.VOID_TYPE;

public class TypeConverter implements TokenConverter<DataType> {

    private final EnumMap<TokenType, DataType> typeTokenMap;

    public TypeConverter() {
        this.typeTokenMap = new EnumMap<>(TokenType.class);
        this.typeTokenMap.put(INT_TYPE, DataType.INT);
        this.typeTokenMap.put(INTEGER, DataType.INT);
        this.typeTokenMap.put(FLOAT_TYPE, DataType.FLOAT);
        this.typeTokenMap.put(FLOAT, DataType.FLOAT);
        this.typeTokenMap.put(CHAR_TYPE, DataType.CHAR);
        this.typeTokenMap.put(CHAR, DataType.CHAR);
        this.typeTokenMap.put(STRING_TYPE, DataType.STRING);
        this.typeTokenMap.put(STRING, DataType.STRING);
        this.typeTokenMap.put(BOOL_TYPE, DataType.BOOL);
        this.typeTokenMap.put(VOID_TYPE, DataType.VOID);
    }

    @Override
    public DataType convertToken(Token token) {
        DataType dataType = typeTokenMap.get(token.getType());
        if(dataType == null) {
            throw new ParserError(String.format("Data type %s is not supported", token.getType()), token);
        }
        return dataType;
    }
}

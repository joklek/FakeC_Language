package com.joklek.fakec.parsing.types;

import com.joklek.fakec.tokens.Token;

public interface TokenConverter<E> {
    E convertToken(Token token);
}

package com.joklek.fakec.parsing;

import com.joklek.fakec.parsing.nodes.DefFunc;
import com.joklek.fakec.parsing.nodes.Node;
import com.joklek.fakec.parsing.nodes.Program;
import com.joklek.fakec.parsing.nodes.Type;
import com.joklek.fakec.tokens.Token;
import com.joklek.fakec.tokens.TokenType;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static com.joklek.fakec.tokens.TokenType.*;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

public class ParserTest {
    @Test
    void shouldFormEmptyFunction() {
        // int ident (){}
        List<Token> tokens = formTokens(INT_TYPE, IDENTIFIER, LEFT_PAREN, RIGHT_PAREN, CURLY_LEFT, CURLY_RIGHT);
        /*Parser parser = new Parser(tokens);
        ParserResults parserResults = parser.parse());
        Program rootNode = (Program) parserResults.getRootNode();
        List<Node> nodes = rootNode.getNodes();

        assertThat(parserResults.getErrors().isEmpty(), is(true));
        assertThat(nodes.size(), is(1));
        DefFunc node = (DefFunc) nodes.get(0);
        assertThat(node.getReturnType(), is(instanceOf(Type.TypeInt.class)));
        assertThat(node.getParameters().isEmpty(), is(true));
        assertThat(node.getBody().getStatements().isEmpty(), is(true));*/
    }

    private List<Token> formTokens(TokenType... args) {
        List<Token> tokens = new ArrayList<>();
        for(TokenType type : args) {
            tokens.add(new Token(type, 0));
        }
        tokens.add(new Token(TokenType.EOF, 0));
        return tokens;
    }
}

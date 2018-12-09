package com.joklek.fakec;

import com.joklek.fakec.codegen.CodeGenerator;
import com.joklek.fakec.codegen.InstructionResolver;
import com.joklek.fakec.codegen.InstructionType;
import com.joklek.fakec.codegen.IntermediateRepresentation;
import com.joklek.fakec.lexing.Lexer;
import com.joklek.fakec.parsing.*;
import com.joklek.fakec.parsing.ast.Stmt;
import com.joklek.fakec.parsing.error.ParserError;
import com.joklek.fakec.scope.error.ScopeError;
import com.joklek.fakec.scope.error.TypeError;
import com.joklek.fakec.parsing.types.operation.OperationConverter;
import com.joklek.fakec.parsing.types.data.TypeConverter;
import com.joklek.fakec.scope.Scope;
import com.joklek.fakec.scope.ScopeChecker;
import com.joklek.fakec.scope.ScopeResolver;
import com.joklek.fakec.scope.TypeChecker;
import com.joklek.fakec.tokens.Token;
import com.joklek.fakec.tokens.TokenType;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class Compiler {

    public static void main(String[] args) {
        String filename = args[0];
        Lexer lexer = new Lexer();
        Map<String, List<Token>> tokensForFile = lexer.lexFile(filename);

        OperationConverter operationConverter = new OperationConverter();
        TypeConverter typeConverter = new TypeConverter();
        // TODO work with more files than one
        Parser parser = new Parser(tokensForFile.get(filename), operationConverter, typeConverter);
        ParserResults results = parser.parseProgram();
        List<ParserError> errors = results.getErrors();
        Stmt.Program program = results.getRootNode();
        System.out.println(new AstPrinter().print(program));

        for(ParserError error: errors) {
            error(error, filename);
        }

        // 4.1
        ScopeResolver scopeResolver = new ScopeResolver();
        List<ScopeError> scopeErrors = scopeResolver.resolveNames(program, new Scope());

        for(ScopeError error: scopeErrors) {
            error(error, filename);
        }

        // 4.2
        TypeChecker typeChecker = new TypeChecker();
        List<TypeError> typeErrors = typeChecker.checkForTypeErrors(program);

        for (TypeError typeError : typeErrors) {
            error(typeError, filename);
        }

        // 4.3
        ScopeChecker scopeChecker = new ScopeChecker();
        List<TypeError> scopeCheckerErrors = scopeChecker.checkScope(program);
        for (TypeError typeError : scopeCheckerErrors) {
            error(typeError, filename);
        }

        // 4.4
        InstructionResolver resolver = new InstructionResolver();
        CodeGenerator generator = new CodeGenerator(resolver);
        IntermediateRepresentation intermediateRepresentation = generator.generate(program);
        List<Integer> bytes = intermediateRepresentation.getInstructionBytes();
        String bytesInString = bytes.stream()
                .map(Object::toString)
                .collect(Collectors.joining(", "));

        System.out.print("[");
        System.out.print(bytesInString);
        System.out.printf("]%n");

        for(int offset = 0; offset < bytes.size(); offset++) {
            int opcode = bytes.get(offset);
            InstructionType instruction = resolver.resolveInstruction(opcode);

            StringBuilder ops = new StringBuilder();
            for(int j = 1; j <= instruction.getOps(); j++) {
                ops.append(bytes.get(offset+j));
            }
            System.out.printf("%04d: %-10s %s%n", offset, instruction, ops.toString());
            offset += instruction.getOps();
        }

        // 5
        // RUN THIS
    }

    private static void error(ScopeError error, String filename) {
        Token token = error.getErroneousName();
        String message = error.getErrorMessage();
        report(token.getLine(), filename,"Error at '" + token.getLexeme() + "': " + message);
    }

    private static void error(TypeError error, String filename) {
        String message = error.getErrorMessage();
        report(error.getLine(), filename,  message);
    }

    private static void error(ParserError error, String filename) {
        Token token = error.getToken();
        String message = error.getErrorMessage();
        if (token.getType() == TokenType.EOF) {
            report(token.getLine(), filename,"Error at end: " + message);
        } else {
            report(token.getLine(), filename,"Error at '" + token.getLexeme() + "': " + message);
        }
    }

    private static void report(int line, String filename, String message) {
        System.err.printf("%s:%d: error: %s%n", filename, line, message);
    }
}

package com.joklek.fakec;

import com.joklek.fakec.codegen.*;
import com.joklek.fakec.error.Error;
import com.joklek.fakec.lexing.Scanner;
import com.joklek.fakec.lexing.ScannerResults;
import com.joklek.fakec.lexing.error.LexerError;
import com.joklek.fakec.lexing.sourceproviders.CodeCollector;
import com.joklek.fakec.lexing.sourceproviders.SourceFromFile;
import com.joklek.fakec.parsing.AstPrinter;
import com.joklek.fakec.parsing.Parser;
import com.joklek.fakec.parsing.ParserResults;
import com.joklek.fakec.parsing.ast.Stmt;
import com.joklek.fakec.parsing.error.ParserError;
import com.joklek.fakec.parsing.types.data.TypeConverter;
import com.joklek.fakec.parsing.types.operation.OperationConverter;
import com.joklek.fakec.scope.Scope;
import com.joklek.fakec.scope.ScopeChecker;
import com.joklek.fakec.scope.ScopeResolver;
import com.joklek.fakec.scope.TypeChecker;
import com.joklek.fakec.scope.error.ScopeError;
import com.joklek.fakec.scope.error.TypeError;
import com.joklek.fakec.tokens.Token;
import com.joklek.fakec.tokens.TokenType;

import java.util.*;
import java.util.stream.Collectors;

import static com.joklek.fakec.codegen.InstructionType.PUSHF;

@SuppressWarnings("squid:S106")
public class Compiler {

    public static void main(String[] args) {
        List<Error> compilerErrors = new ArrayList<>();

        String filename = args[0];
        CodeCollector codeCollector = new CodeCollector(new SourceFromFile());

        Map<String, List<LexerError>> errorsForFiles = new HashMap<>();
        Map<String, List<Token>> tokensForFiles = new HashMap<>();

        codeCollector.getAllRelatedCode(filename).forEach((fileName, source) -> {
            Scanner scanner = new Scanner(source);
            ScannerResults scannerResults = scanner.scanTokens();
            List<Token> tokens = scannerResults.getTokens();
            List<LexerError> errors = scannerResults.getErrors();
            errorsForFiles.put(fileName, errors);
            tokensForFiles.put(fileName, tokens);
            //printLexemas(tokens, fileName);
        });
        errorsForFiles.forEach((fileName, errors) ->
                compilerErrors.addAll(errors));

        OperationConverter operationConverter = new OperationConverter();
        TypeConverter typeConverter = new TypeConverter();
        // TODO work with more files than one
        Parser parser = new Parser(tokensForFiles.get(filename), operationConverter, typeConverter);
        ParserResults results = parser.parseProgram();
        List<ParserError> errors = results.getErrors();
        Stmt.Program program = results.getRootNode();
        System.out.println(new AstPrinter().print(program));

        compilerErrors.addAll(errors);

        // 4.1
        ScopeResolver scopeResolver = new ScopeResolver();
        List<ScopeError> scopeErrors = scopeResolver.resolveNames(program, new Scope());
        compilerErrors.addAll(scopeErrors);

        // 4.2
        TypeChecker typeChecker = new TypeChecker();
        List<TypeError> typeErrors = typeChecker.checkForTypeErrors(program);
        compilerErrors.addAll(typeErrors);

        // 4.3
        ScopeChecker scopeChecker = new ScopeChecker();
        List<TypeError> scopeCheckerErrors = scopeChecker.checkScope(program);
        compilerErrors.addAll(scopeCheckerErrors);

        if(!compilerErrors.isEmpty()) {
            System.err.println("Compiler stopped before code generation. Errors occurred");
            compilerErrors.sort((lhs, rhs) -> Integer.compare(rhs.getLine(), lhs.getLine()));
            Collections.reverse(compilerErrors);

            for (Error error : compilerErrors) {
                if(error instanceof LexerError) {
                    error((LexerError) error, filename);
                }
                else if(error instanceof ParserError) {
                    error((ParserError) error, filename);
                }
                else if(error instanceof ScopeError) {
                    error((ScopeError) error, filename);
                }
                else if(error instanceof TypeError) {
                    error((TypeError) error, filename);
                }
            }
            return;
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

            if(instruction == null) {
                System.err.printf("Instruction with code %d is not found%n", opcode);
                continue;
            }

            StringBuilder ops = new StringBuilder();
            for(int j = 1; j <= instruction.getOps(); j++) {
                Integer codeFromIR = bytes.get(offset + j);
                if(instruction == InstructionType.POPF || instruction == InstructionType.ADDF || instruction == PUSHF) {
                    ops.append(Float.intBitsToFloat(codeFromIR));
                }
                else {
                    ops.append(codeFromIR);
                }
            }

            System.out.printf("%04d: %-10s %s%n", offset, instruction, ops.toString());
            offset += instruction.getOps();
        }

        // 5
        // RUN THIS
        Interpreter vm = new Interpreter(bytes, intermediateRepresentation.getStringTable());
        vm.execute();
    }

    private static void printLexemas(List<Token> tokens, String fileName) {
        String tableFormat = "%4s|%4s|%-15s|%-50s%n";
        System.out.println("Lexemas for file: " + fileName);
        System.out.printf(tableFormat, "ID", "LN", "TYPE", "VALUE");
        System.out.printf(tableFormat, "----", "----", "---------------", "--------------------------------------------------");
        for (Token token : tokens) {
            Object value = token.getLiteral() == null ? "" : token.getLiteral();
            System.out.printf(tableFormat, tokens.indexOf(token), token.getLine(), token.getType(), value);
        }
        System.out.println();
    }

    private static void error(LexerError error, String fileName) {
        report(error.getLine(), fileName, error.getMessage());
    }

    private static void error(ScopeError error, String filename) {
        Token token = error.getErroneousName();
        String message = error.getMessage();
        report(token.getLine(), filename,"Error at '" + token.getLexeme() + "': " + message);
    }

    private static void error(TypeError error, String filename) {
        String message = error.getMessage();
        report(error.getLine(), filename,  message);
    }

    private static void error(ParserError error, String filename) {
        Token token = error.getToken();
        String message = error.getMessage();
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

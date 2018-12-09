package com.joklek.fakec.codegen;

import java.util.HashMap;
import java.util.Map;

public class InstructionResolver {
    private final Map<Integer, InstructionType> instructionMap;

    public InstructionResolver() {
        instructionMap = new HashMap<>();
        for (InstructionType instructionType : InstructionType.values()) {
            int value = instructionType.getValue();
            if(!instructionMap.containsKey(value)) {
                instructionMap.put(value, instructionType);
            }
            else {
                throw new UnsupportedOperationException(String.format("Two instruction types with the same value are declared %s and %s", instructionMap.get(value), instructionType));
            }
        }
    }

    public InstructionType resolveInstruction(int value) {
        return instructionMap.get(value);
    }
}

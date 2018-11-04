package com.jole.fakec.utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class StringParsingUtils {

    private Map<String, String> escapedSymbols = new HashMap<>();
    private List<Character> escapedChars = new ArrayList<>();

    public StringParsingUtils() {
        escapedChars.add('n');
        escapedChars.add('r');
        escapedChars.add('t');
        escapedChars.add('\\');

        escapedSymbols.put("\\n", "\n");
        escapedSymbols.put("\\r", "\r");
        escapedSymbols.put("\\t", "\t");
        escapedSymbols.put("\\\\", "\\");
    }

    public boolean escapedChar(char nextChar) {
        return escapedChars.contains(nextChar);
    }

    public String unescapeSymbols(String escapedString) {
        String unescaped = escapedString;
        for(Map.Entry<String, String> entry : escapedSymbols.entrySet()) {
            unescaped = unescaped.replace(entry.getKey(), entry.getValue());
        }
        unescaped = unescaped.replace("\\\"", "\"");
        return unescaped;
    }

    public String unescapeCharSymbols(String escapedString) {
        String unescaped = escapedString;
        for(Map.Entry<String, String> entry : escapedSymbols.entrySet()) {
            unescaped = unescaped.replace(entry.getKey(), entry.getValue());
        }
        unescaped = unescaped.replace("\\'", "'");
        return unescaped;
    }
}

package com.jole.utils;

import java.util.HashMap;
import java.util.Map;

public class StringParsingUtils {

    Map<String, String> escapedChars = new HashMap<>();

    public StringParsingUtils() {
        escapedChars.put("\\n", "\n");
        escapedChars.put("\\r", "\r");
        escapedChars.put("\\t", "\t");
    }

    public String unescapeSymbols(String escapedString) {
        String unescaped = escapedString;
        for(Map.Entry<String, String> entry : escapedChars.entrySet()) {
            unescaped = unescaped.replace(entry.getKey(), entry.getValue());
        }
        unescaped = unescaped.replace("\\\"", "\"");
        return unescaped;
    }

    public String unescapeCharSymbols(String escapedString) {
        String unescaped = escapedString;
        for(Map.Entry<String, String> entry : escapedChars.entrySet()) {
            unescaped = unescaped.replace(entry.getKey(), entry.getValue());
        }
        unescaped = unescaped.replace("\\'", "'");
        return unescaped;
    }
}

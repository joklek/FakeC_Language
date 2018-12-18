package com.joklek.fakec.codegen;

import java.util.HashMap;
import java.util.Map;

public class StringTable {

    private final Map<Integer, String> strings;
    private int current;

    public StringTable(){
        strings = new HashMap<>();
        current = 0;
    }

    public int add(String string){
        strings.put(current,string);
        return current++;
    }

    public String get(int key){
        return strings.get(key);
    }
}

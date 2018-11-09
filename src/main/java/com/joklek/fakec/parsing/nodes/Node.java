package com.joklek.fakec.parsing.nodes;

import com.joklek.fakec.parsing.Printer;

public interface Node {
    default void print(Printer printer) {
        printer.println(String.format("Print is not implemented for current class '%s'", this.getClass().getCanonicalName()));
    }
}

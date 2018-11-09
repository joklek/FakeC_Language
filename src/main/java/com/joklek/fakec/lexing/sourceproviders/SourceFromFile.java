package com.joklek.fakec.lexing.sourceproviders;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;

public class SourceFromFile implements SourceProvider {

    @Override
    public String getSource(String path) {
        byte[] bytes;
        try {
            bytes = Files.readAllBytes(Paths.get(path));
        } catch (IOException e) {
            throw new IllegalArgumentException(String.format("File with name \"%s\" does not exist", path), e);
        }
        return new String(bytes, Charset.defaultCharset());
    }
}

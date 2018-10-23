package com.jole.sourceproviders;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class CodeCollector {

    private final SourceProvider provider;
    private final Pattern pattern = Pattern.compile("#include\\s*<\\s*[a-zA-Z0-9]([a-zA-Z0-9 ._-]*[a-zA-Z0-9])?(\\.[a-zA-Z0-9_-]+)?\\s*>");

    public CodeCollector(SourceProvider provider) {
        this.provider = provider;
    }

    /**
     * This method gets source from given file, and gathers sources from all related files
     * @param filename for the base file
     * @return map with key as filename and value as the source code from the file
     */
    public Map<String, String> getAllRelatedCode(String filename) {
        Map<String, String> mapOfCode = new HashMap<>();
        return getAllRelatedCodeRecursively(filename, mapOfCode);
    }

    private Map<String, String> getAllRelatedCodeRecursively(String filename, Map<String, String> mapOfCode) {
        if( !mapOfCode.containsKey(filename) ) {
            String source = provider.getSource(filename);
            mapOfCode.put(filename, source.replaceAll(pattern.toString(), ""));
            getIncludedFileNames(source)
                    .forEach(includedFile -> mapOfCode
                            .putAll(getAllRelatedCodeRecursively(includedFile, mapOfCode)));
        }
        return mapOfCode;
    }

    protected List<String> getIncludedFileNames(String source) {
        Matcher matcher = pattern.matcher(source);

        List<String> list = new ArrayList<>();

        while (matcher.find()) {
            list.add(matcher.group());
        }

        return list.stream()
                .map(raw -> raw.split("<")[1].split(">")[0].trim())
                .collect(Collectors.toList());
    }
}

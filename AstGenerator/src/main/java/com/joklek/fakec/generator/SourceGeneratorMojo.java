package com.joklek.fakec.generator;

import org.apache.commons.lang3.StringUtils;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Generates AST nodes
 */
@Mojo(name = "generateAST", defaultPhase = LifecyclePhase.GENERATE_SOURCES)
public class SourceGeneratorMojo extends AbstractMojo {

    @Parameter(required = true, defaultValue = "${project.basedir}/src/main/resources/ASTResources")
    private String sourceDirectory;

    @Parameter(required = true, defaultValue = "${project.build.directory}/generated-sources/java")
    private String targetDirectory;

    @Parameter(required = true, defaultValue = "com.joklek.fakec.parsing.ast")
    private String currentPackage;

    @Parameter
    private List<String> resourceFiles;

    @Parameter
    private List<String> imports;

    @Parameter(defaultValue = "${project}")
    private MavenProject project;

    public void execute()
    {
        for(String resource: resourceFiles) {
            getLog().info( sourceDirectory + "/" + resource + ".txt");
            if (resourceFiles.isEmpty()) {
                System.err.println("No resource files given");
                System.exit(1);
            }
            String outputDir = targetDirectory + "/" + currentPackage.replace('.', '/');
            List<String> readFile = null;

            String fileName = resource + ".txt";
            try {
                readFile = Arrays.asList(readFile(new File(sourceDirectory, fileName)).split("\n"));
            } catch (IOException e) {
                System.err.println(String.format("File %s, could not be found", fileName));
                e.printStackTrace();
                System.exit(1);
            }
            try {
                defineAst(outputDir, resource, readFile);
            } catch (IOException e) {
                System.err.println(String.format("File %s.java, could not be created", fileName));
                e.printStackTrace();
                System.exit(1);
            }
        }
        project.addCompileSourceRoot(targetDirectory);
    }

    private String readFile(File file) throws IOException {
        try(BufferedReader br = new BufferedReader(new FileReader(file))) {
            StringBuilder sb = new StringBuilder();
            String line = br.readLine();

            while (line != null) {
                sb.append(line);
                sb.append(System.lineSeparator());
                line = br.readLine();
            }
            return sb.toString();
        }
    }

    private void defineAst(String outputDir, String baseName, List<String> typesOld) throws IOException {
        File file = new File(outputDir, baseName + ".java");
        if(!file.exists()) {
            file.getParentFile().mkdirs();
            file.createNewFile();
        }

        PrintWriter writer = new PrintWriter(file, "UTF-8");

        writer.println("/*This code is autogenerated for FakeC language*/");
        writer.println(String.format("package %s;", currentPackage));
        writer.println();
        writer.println("import java.util.List;");
        writer.println("import java.util.Map;");
        writer.println("import org.apache.commons.lang3.tuple.Pair;");
        writer.println("import com.joklek.fakec.parsing.types.Node;");

        for (String anImport : imports) {
            writer.println(String.format("import %s;", anImport));
        }
        writer.println();
        writer.println(String.format("public abstract class %s implements Node {", baseName));

        List<String> withSetters = new ArrayList<>();
        for (String line : typesOld) {
            if (line.charAt(0) == '+') {
                withSetters.add(line);
            }
        }

        List<String> types = new ArrayList<>();
        for (String line : typesOld) {
            if (!withSetters.contains(line)) {
                types.add(line);
            }
        }

        for(int i = 0; i < withSetters.size(); i++) {
            withSetters.set(i, withSetters.get(i).substring(1));
        }

        defineVisitor(writer, baseName, types);
        defineVisitorWithError(writer, baseName, types);

        List<Field> fieldsWithSetters = new ArrayList<>();

        for (String withSetter : withSetters) {
            fieldsWithSetters.addAll(extractFields(withSetter));
        }

        for (Field fieldWithSetter : fieldsWithSetters) {
            defineScope(fieldWithSetter, writer);
        }


        for (String type : types) {
            String[] splitType = type.split(":");
            String className = splitType[0].trim();
            String fieldsUnconverted = splitType[1];

            List<Field> fields = extractFields(fieldsUnconverted);

            writer.println();
            defineType(writer, baseName, className, fields);
        }

        // The base accept() method.
        writer.println();
        writer.println("  public abstract <R> R accept(Visitor<R> visitor);");
        // accept() with error propagation
        writer.println();
        writer.println("  public abstract <R, E extends Error> R accept(VisitorWithErrors<R, E> visitor, List<E> errors);");

        writer.println("}");
        writer.close();
    }

    private List<Field> extractFields(String fieldsUnconverted) {
        List<Field> fields = new ArrayList<>();
        String[] fieldsArr = fieldsUnconverted.split("; ");
        for(String field: fieldsArr) {
            fields.add(new Field(field.trim().split(" ")));
        }
        return fields;
    }

    private void defineVisitor(PrintWriter writer, String baseName, List<String> types) {
        writer.println("  public interface Visitor<R> {");
        for (String type : types) {
            String typeName = type.split(":")[0].trim();
            writer.println(String.format("    R visit%s%s (%s %s);", typeName, baseName, typeName, typeName.substring(0, 1).toLowerCase() + typeName.substring(1) + baseName));
        }
        writer.println("  }");
    }

    private void defineVisitorWithError(PrintWriter writer, String baseName, List<String> types) {
        writer.println("  public interface VisitorWithErrors<R, E extends Error> {");
        for (String type : types) {
            String typeName = type.split(":")[0].trim();
            writer.println(String.format("    R visit%s%s (%s %s, List<E> errors);", typeName, baseName, typeName, typeName.substring(0, 1).toLowerCase() + typeName.substring(1) + baseName));
        }
        writer.println("  }");
    }

    private void defineType(PrintWriter writer, String baseName, String className, List<Field> fieldList) {
        writer.println(String.format("  public static class %s extends %s {", className, baseName));
        writer.println();

        defineFields(writer, fieldList);
        defineConstructor(writer, className, fieldList);
        defineGetters(writer, fieldList);

        defineAccept(writer, baseName, className);
        defineAcceptWithError(writer, baseName, className);

        writer.println("  }");
    }

    private void defineScope(Field field, PrintWriter writer) {
        writer.println(String.format("  private %s = null;", field.toString()));
        defineGetter(writer, field);
        defineSetter(writer, field);
    }

    private void defineFields(PrintWriter writer, List<Field> fields) {
        for (Field field : fields) {
            writer.println(String.format("    private final %s;", field.toString()));
        }
        writer.println();
    }

    private void defineConstructor(PrintWriter writer, String className, List<Field> fields) {
        String fieldList = StringUtils.join(fields,  ", ");
        writer.println(String.format("    public %s(%s) {", className, fieldList));

        // Store parameters in fields.
        for (Field field : fields) {
            writer.println(String.format("      this.%s = %s;", field.getName(), field.getName()));
        }
        writer.println("    }");
        writer.println();
    }

    private void defineGetters(PrintWriter writer, List<Field> fields) {
        for (Field field : fields) {
            defineGetter(writer, field);
        }
    }

    private void defineGetter(PrintWriter writer, Field field) {
        writer.println(String.format("    public %s get%s() {", field.getType(), StringUtils.capitalize(field.getName())));
        writer.println(String.format("      return %s;", field.getName()));
        writer.println("    }");
        writer.println();
    }

    private void defineSetter(PrintWriter writer, Field field) {
        writer.println(String.format("    public void set%s(%s) {", StringUtils.capitalize(field.getName()), field.toString()));
        writer.println(String.format("      this.%s = %s;", field.getName(), field.getName()));
        writer.println("    }");
        writer.println();
    }

    private void defineAccept(PrintWriter writer, String baseName, String className) {
        writer.println("    public <R> R accept(Visitor<R> visitor) {");
        writer.println(String.format("      return visitor.visit%s%s(this);", className, baseName));
        writer.println("    }");
    }

    private void defineAcceptWithError(PrintWriter writer, String baseName, String className) {
        writer.println("    public <R, E extends Error> R accept(VisitorWithErrors<R, E> visitor, List<E> errors) {");
        writer.println(String.format("      return visitor.visit%s%s(this, errors);", className, baseName));
        writer.println("    }");
    }

    private static class Field {
        private final String type;
        private final String name;

        private Field(String type, String name) {
            this.type = type;
            this.name = name;
        }

        private Field(String[] typeAndName) {
            if(typeAndName.length != 2) {
                throw new IllegalArgumentException("Type and name should contain two elements: type and name");
            }
            this.type = typeAndName[0].trim();
            this.name = typeAndName[1].trim();
        }

        public String getType() {
            return type;
        }

        public String getName() {
            return name;
        }

        @Override
        public String toString() {
            return type + " " + name;
        }
    }
}

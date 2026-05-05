package org.example;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.StringJoiner;

import static org.junit.jupiter.api.Assertions.*;

class FixedLengthParserTest {

    private static String parserCode;
    private static List<SchemaField> fields;

    @BeforeAll
    static void setup() {
        fields = List.of(
                new SchemaField("name", 1, 20),
                new SchemaField("gender", 21, 22),
                new SchemaField("age", 23, 25)
        );
        parserCode = ParserGenerator.generate(fields);
    }

    @Test
    void parserHasRequiredImports() {
        assertTrue(parserCode.contains("import java.io.BufferedReader;"));
        assertTrue(parserCode.contains("import java.io.FileReader;"));
        assertTrue(parserCode.contains("import java.io.IOException;"));
        assertTrue(parserCode.contains("import java.util.ArrayList;"));
        assertTrue(parserCode.contains("import java.util.List;"));
    }

    @Test
    void parserHasCorrectConstants() {
        long constantCount = parserCode.lines()
                .filter(line -> line.contains("private static final int"))
                .count();
        assertEquals(fields.size() * 2, constantCount);

        for (SchemaField f : fields) {
            String upper = f.getSchemaVariable().toUpperCase();
            assertTrue(parserCode.contains(upper + "_START = " + f.getStart()),
                    "Missing constant: " + upper + "_START");
            assertTrue(parserCode.contains(upper + "_END ="),
                    "Missing constant: " + upper + "_END");
        }
    }

    @Test
    void parserHasCorrectStructure() {
        assertTrue(parserCode.contains("public List<Record> parseFile(String filePath)"),
                "Missing parseFile method");
        assertTrue(parserCode.contains("private String extractField(String line, int start, int end)"),
                "Missing extractField method");
        assertTrue(parserCode.contains("public static void main(String[] args)"),
                "Missing main method");
        assertTrue(parserCode.contains("return line.substring(start - 1, end)"),
                "extractField should use substring(start - 1, end)");
    }

    @Test
    void parserCreatesRecordWithAllFields() {
        StringJoiner params = new StringJoiner(", ");
        fields.forEach(f -> params.add(f.getSchemaVariable()));
        assertTrue(parserCode.contains("new Record(" + params + ")"));
    }

    @Test
    void parserSkipsShortLines() {
        SchemaField lastField = fields.get(fields.size() - 1);
        String lastEndConstant = lastField.getSchemaVariable().toUpperCase() + "_END";
        assertTrue(parserCode.contains("line.length() < " + lastEndConstant),
                "Parser should check line length against last field end constant");
    }

    @Test
    void parserExtractsAllFieldsWithTrim() {
        for (SchemaField f : fields) {
            String upper = f.getSchemaVariable().toUpperCase();
            assertTrue(parserCode.contains("extractField(line, " + upper + "_START, " + upper + "_END).trim()"),
                    f.getSchemaVariable() + " should use extractField with trim");
        }
    }

}

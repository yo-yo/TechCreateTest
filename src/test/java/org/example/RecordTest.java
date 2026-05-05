package org.example;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.StringJoiner;

import static org.junit.jupiter.api.Assertions.*;

class RecordTest {

    private static String recordCode;
    private static List<SchemaField> fields;

    @BeforeAll
    static void generateFiles() throws IOException {
        Main.main(new String[]{});
        recordCode = Files.readString(Path.of("src/main/java/org/example/Record.java"));
        fields = Files.readAllLines(Path.of("src/main/java/org/example/schema.txt")).stream()
                .map(line -> line.split("\\s+"))
                .map(parts -> new SchemaField(parts[0], Integer.parseInt(parts[1]), Integer.parseInt(parts[2])))
                .toList();
    }

    @Test
    void recordHasCorrectFields() {
        long fieldCount = recordCode.lines()
                .filter(line -> line.trim().startsWith("private String "))
                .count();
        assertEquals(fields.size(), fieldCount);

        for (SchemaField f : fields) {
            assertTrue(recordCode.contains("private String " + f.schemaVariable),
                    "Missing field: " + f.schemaVariable);
        }
    }

    @Test
    void recordHasCorrectConstructor() {
        StringJoiner params = new StringJoiner(", ");
        fields.forEach(f -> params.add("String " + f.schemaVariable));
        assertTrue(recordCode.contains("public Record(" + params + ")"));

        for (SchemaField f : fields) {
            assertTrue(recordCode.contains("this." + f.schemaVariable + "=" + f.schemaVariable),
                    "Missing assignment for: " + f.schemaVariable);
        }
    }

    @Test
    void recordToStringContainsAllFields() {
        for (SchemaField f : fields) {
            assertTrue(recordCode.contains(f.schemaVariable + "='\""),
                    "toString missing field: " + f.schemaVariable);
        }
    }
}

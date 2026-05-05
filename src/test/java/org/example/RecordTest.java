package org.example;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.StringJoiner;

import static org.junit.jupiter.api.Assertions.*;

class RecordTest {

    private static String recordCode;
    private static List<SchemaField> fields;

    @BeforeAll
    static void setup() {
        fields = List.of(
                new SchemaField("name", 1, 20),
                new SchemaField("gender", 21, 22),
                new SchemaField("age", 23, 25)
        );
        recordCode = RecordGenerator.generate(fields);
    }

    @Test
    void recordHasCorrectFields() {
        long fieldCount = recordCode.lines()
                .filter(line -> line.trim().startsWith("private String "))
                .count();
        assertEquals(fields.size(), fieldCount);

        for (SchemaField f : fields) {
            assertTrue(recordCode.contains("private String " + f.getSchemaVariable()),
                    "Missing field: " + f.getSchemaVariable());
        }
    }

    @Test
    void recordHasCorrectConstructor() {
        StringJoiner params = new StringJoiner(", ");
        fields.forEach(f -> params.add("String " + f.getSchemaVariable()));
        assertTrue(recordCode.contains("public Record(" + params + ")"));

        for (SchemaField f : fields) {
            assertTrue(recordCode.contains("this." + f.getSchemaVariable() + "=" + f.getSchemaVariable()),
                    "Missing assignment for: " + f.getSchemaVariable());
        }
    }

    @Test
    void recordToStringContainsAllFields() {
        for (SchemaField f : fields) {
            assertTrue(recordCode.contains(f.getSchemaVariable() + "='\""),
                    "toString missing field: " + f.getSchemaVariable());
        }
    }
}

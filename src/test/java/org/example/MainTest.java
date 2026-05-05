package org.example;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

class MainTest {

    @TempDir
    Path tempDir;

    private File writeSchemaFile(String content) throws IOException {
        File file = tempDir.resolve("testSchema.txt").toFile();
        try (FileWriter writer = new FileWriter(file)) {
            writer.write(content);
        }
        return file;
    }

    @Test
    void parseSchema_validSchema_returnsFields() throws IOException, SchemaParseException {
        File schema = new File("src/main/java/org/example/schema.txt").getAbsoluteFile();
        long expectedCount = Files.readAllLines(schema.toPath()).stream()
                .filter(line -> !line.trim().isEmpty())
                .count();
        List<SchemaField> fields = Main.parseSchema(schema);
        assertEquals(expectedCount, fields.size());
        for (SchemaField field : fields) {
            assertNotNull(field.getSchemaVariable());
            assertTrue(field.getStart() >= 1);
            assertTrue(field.getEnd() >= field.getStart());
        }
    }

    static Stream<Arguments> validSchemas() {
        return Stream.of(
                Arguments.of("flag 1 1\n", 1, "single char field"),
                Arguments.of("name 1 500\ndata 501 1000\n", 2, "large positions"),
                Arguments.of("a 1 5\nb 6 10\nc 11 15\nd 16 20\ne 21 25\n", 5, "many fields"),
                Arguments.of("name 1 20\ngender 20 21\nage 22 25\n", 3, "single position overlap")
        );
    }

    @ParameterizedTest(name = "accepts {2}")
    @MethodSource("validSchemas")
    void parseSchema_validSchema_parsesCorrectly(String content, int expectedCount, String reason) throws IOException, SchemaParseException {
        File schema = writeSchemaFile(content);
        List<SchemaField> fields = Main.parseSchema(schema);
        assertEquals(expectedCount, fields.size());
    }

    static Stream<Arguments> invalidSchemas() {
        return Stream.of(
                Arguments.of("", "empty file"),
                Arguments.of("name 1\n", "missing columns"),
                Arguments.of("name abc def\n", "non-integer positions"),
                Arguments.of("name 2 20\n", "first field not starting at 1"),
                Arguments.of("name 1 10\nname 11 20\n", "duplicate field names"),
                Arguments.of("fieldA 1 10\nfieldB 1 5\n", "fields not in order"),
                Arguments.of("name 1 20\ngender 15 21\n", "overlapping fields"),
                Arguments.of("name 1 18\ngender 20 21\n", "gap between fields"),
                Arguments.of("123invalid 1 20\n", "invalid field name"),
                Arguments.of("name 1 0\n", "end less than start"),
                Arguments.of("name 0 10\n", "zero start position"),
                Arguments.of("_FIRST_NAME 1 20\n", "leading underscore in field name"),
                Arguments.of("-name 1 20\n", "leading hyphen in field name")
        );
    }

    @ParameterizedTest(name = "rejects {1}")
    @MethodSource("invalidSchemas")
    void parseSchema_invalidSchema_throws(String content, String reason) throws IOException {
        File invalidSchema = writeSchemaFile(content);
        assertThrows(SchemaParseException.class, () -> Main.parseSchema(invalidSchema));
    }

    @Test
    void parserGenerator_overlapAdjustsConstant() {
        List<SchemaField> fields = List.of(
                new SchemaField("name", 1, 20),
                new SchemaField("gender", 20, 21)
        );
        String code = ParserGenerator.generate(fields);
        assertTrue(code.contains("NAME_END = 19"), "Overlap should adjust NAME_END to 19");
        assertTrue(code.contains("GENDER_START = 20"));
    }

}

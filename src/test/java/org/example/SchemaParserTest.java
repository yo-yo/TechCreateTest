package org.example;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

class SchemaParserTest {

    private static List<String> toLines(String content) {
        return Arrays.asList(content.split("\n"));
    }

    static Stream<Arguments> validSchemas() {
        return Stream.of(
                Arguments.of("flag|1|1", 1, "single char field"),
                Arguments.of("name|1|500\ndata|501|1000", 2, "large positions"),
                Arguments.of("a|1|5\nb|6|10\nc|11|15\nd|16|20\ne|21|25", 5, "many fields"),
                Arguments.of("name|1|20\ngender|20|21\nage|22|25", 3, "single position overlap"),
                Arguments.of("123invalid|1|20", 1, "leading digit sanitized"),
                Arguments.of("_FIRST_NAME|1|20", 1, "leading underscore"),
                Arguments.of("-name|1|20", 1, "leading hyphen sanitized"),
                Arguments.of("remaining balance|1|20", 1, "name with spaces sanitized"),
                Arguments.of("class|1|20", 1, "reserved keyword sanitized")
        );
    }

    @ParameterizedTest(name = "accepts {2}")
    @MethodSource("validSchemas")
    void parseSchema_validSchema_parsesCorrectly(String content, int expectedCount, String reason) throws SchemaParseException {
        List<SchemaField> fields = SchemaParser.parse(toLines(content));
        assertEquals(expectedCount, fields.size());
    }

    static Stream<Arguments> invalidSchemas() {
        return Stream.of(
                Arguments.of("", "empty file"),
                Arguments.of("name|1", "missing columns"),
                Arguments.of("name|abc|def", "non-integer positions"),
                Arguments.of("name|2|20", "first field not starting at 1"),
                Arguments.of("name|1|10\nname|11|20", "duplicate field names"),
                Arguments.of("fieldA|1|10\nfieldB|1|5", "fields not in order"),
                Arguments.of("name|1|20\ngender|15|21", "overlapping fields"),
                Arguments.of("name|1|18\ngender|20|21", "gap between fields"),
                Arguments.of("name|1|0", "end less than start"),
                Arguments.of("name|0|10", "zero start position"),
                Arguments.of("@#$%|1|20", "no valid chars after sanitize")
        );
    }

    @ParameterizedTest(name = "rejects {1}")
    @MethodSource("invalidSchemas")
    void parseSchema_invalidSchema_throws(String content, String reason) {
        assertThrows(SchemaParseException.class, () -> SchemaParser.parse(toLines(content)));
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

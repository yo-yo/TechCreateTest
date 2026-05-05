package org.example;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

class SchemaFieldTest {

    static Stream<Arguments> validInputs() {
        return Stream.of(
                Arguments.of("name", 1, 20, "simple name"),
                Arguments.of("flag", 5, 5, "single position"),
                Arguments.of("first_name", 1, 20, "underscore name"),
                Arguments.of("_name", 1, 20, "leading underscore"),
                Arguments.of("field1", 1, 10, "alphanumeric name")
        );
    }

    @ParameterizedTest(name = "accepts {3}")
    @MethodSource("validInputs")
    void validInputs_setsAllFieldsCorrectly(String name, int start, int end, String reason) {
        SchemaField field = new SchemaField(name, start, end);
        assertEquals(name, field.getSchemaVariable());
        assertEquals(start, field.getStart());
        assertEquals(end, field.getEnd());
    }

    static Stream<Arguments> invalidInputs() {
        return Stream.of(
                Arguments.of(null, 1, 20, "null name"),
                Arguments.of("", 1, 20, "empty name"),
                Arguments.of("   ", 1, 20, "blank name"),
                Arguments.of("123abc", 1, 20, "invalid Java identifier"),
                Arguments.of("name", -1, 20, "negative start"),
                Arguments.of("name", 0, 20, "zero start"),
                Arguments.of("name", 25, 20, "start greater than end"),
                Arguments.of("null", 1, 20, "string null as name"),
                Arguments.of("first name", 1, 20, "name with spaces"),
                Arguments.of("first-name", 1, 20, "name with hyphens"),
                Arguments.of("name", -1, -5, "both start and end negative")
        );
    }

    @ParameterizedTest(name = "rejects {3}")
    @MethodSource("invalidInputs")
    void invalidInputs_expect_errorThrown(String name, int start, int end, String reason) {
        assertThrows(IllegalArgumentException.class, () -> new SchemaField(name, start, end));
    }

}

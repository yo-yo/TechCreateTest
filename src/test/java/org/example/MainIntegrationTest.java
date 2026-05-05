package org.example;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class MainIntegrationTest {

    @TempDir
    Path tempDir;

    @Test
    void main_validSchema_generatesBothFiles() throws IOException {
        Path schema = tempDir.resolve("schema.txt");
        Files.writeString(schema, "firstName 1 20\nlastName 21 40\nage 41 43");

        Main.main(new String[]{schema.toString()});

        File record = tempDir.resolve("Record.java").toFile();
        File parser = tempDir.resolve("FixedLengthParser.java").toFile();

        assertTrue(record.exists(), "Record.java should be generated");
        assertTrue(parser.exists(), "FixedLengthParser.java should be generated");

        String recordContent = Files.readString(record.toPath());
        assertTrue(recordContent.contains("firstName"));
        assertTrue(recordContent.contains("lastName"));
        assertTrue(recordContent.contains("age"));

        String parserContent = Files.readString(parser.toPath());
        assertTrue(parserContent.contains("FIRSTNAME_START"));
        assertTrue(parserContent.contains("LASTNAME_START"));
        assertTrue(parserContent.contains("AGE_START"));
    }

    @Test
    void main_invalidSchema_doesNotGenerateFiles() throws IOException {
        Path schema = tempDir.resolve("schema.txt");
        Files.writeString(schema, "name 5 20");

        Main.main(new String[]{schema.toString()});

        assertFalse(tempDir.resolve("Record.java").toFile().exists());
        assertFalse(tempDir.resolve("FixedLengthParser.java").toFile().exists());
    }

    @Test
    void main_missingFile_doesNotThrow() {
        assertDoesNotThrow(() -> Main.main(new String[]{"nonexistent.txt"}));
    }
}

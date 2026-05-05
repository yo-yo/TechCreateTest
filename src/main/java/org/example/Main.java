package org.example;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Main {
    private static final Logger logger = LoggerFactory.getLogger(Main.class);

    public static void main(String[] args) {
        String schemaPath = args.length > 0 ? args[0] : "src/main/java/org/example/schema.txt";
        File schemaFile = new File(schemaPath).getAbsoluteFile();
        File outputDir = schemaFile.getParentFile();

        if (!schemaFile.exists()) {
            logger.error("Schema file not found: {}", schemaFile.getPath());
            return;
        }

        if (!outputDir.canWrite()) {
            logger.error("Output directory not writable: {}", outputDir.getPath());
            return;
        }

        try {
            List<SchemaField> schemaFieldsList = SchemaParser.parse(schemaFile);

            String recordContent = RecordGenerator.generate(schemaFieldsList);
            String parserContent = ParserGenerator.generate(schemaFieldsList);

            writeFile(new File(outputDir, "Record.java"), recordContent);
            writeFile(new File(outputDir, "FixedLengthParser.java"), parserContent);
        } catch (SchemaParseException e) {
            logger.error("Schema validation failed: {}", e.getMessage());
        } catch (IOException e) {
            logger.error("I/O error: {}", e.getMessage());
        }
    }

    private static void writeFile(File file, String content) throws IOException {
        try (OutputStreamWriter writer = new OutputStreamWriter(
                new FileOutputStream(file), StandardCharsets.UTF_8)) {
            writer.write(content);
            logger.info("{} generated.", file.getName());
        }
    }
}

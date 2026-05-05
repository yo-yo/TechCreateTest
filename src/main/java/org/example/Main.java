package org.example;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Main {
    private static final Logger logger = LoggerFactory.getLogger(Main.class);

    public static void main(String[] args) {
        String schemaPath = args.length > 0 ? args[0] : "src/main/java/org/example/schema.txt";
        File schemaFile = new File(schemaPath).getAbsoluteFile();
        String outputDir = schemaFile.getParent() + File.separator;

        if (!schemaFile.exists()) {
            logger.error("Schema file not found: {}", schemaFile.getPath());
            return;
        }

        try {
            List<SchemaField> schemaFieldsList = SchemaParser.parse(schemaFile);
            writeFile(outputDir + "Record.java", RecordGenerator.generate(schemaFieldsList), "Record.java");
            writeFile(outputDir + "FixedLengthParser.java", ParserGenerator.generate(schemaFieldsList), "FixedLengthParser.java");
        } catch (SchemaParseException e) {
            logger.error("Schema validation failed: {}", e.getMessage());
        } catch (IOException e) {
            logger.error("I/O error: {}", e.getMessage());
        }
    }

    private static void writeFile(String path, String content, String fileName) throws IOException {
        try (FileWriter writer = new FileWriter(path)) {
            writer.write(content);
            logger.info("{} generated.", fileName);
        }
    }
}

package org.example;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
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
            logger.error("Schema file not found: " + schemaFile.getPath());
            return;
        }

        try {
            List<SchemaField> schemaFieldsList = parseSchema(schemaFile);
            writeFile(outputDir + "Record.java", RecordGenerator.generate(schemaFieldsList), "Record.java");
            writeFile(outputDir + "FixedLengthParser.java", ParserGenerator.generate(schemaFieldsList), "FixedLengthParser.java");
        } catch (SchemaParseException e) {
            logger.error("Schema validation failed: " + e.getMessage());
        } catch (IOException e) {
            logger.error("I/O error: {}", e.getMessage());
        }
    }

    static List<SchemaField> parseSchema(File schemaFile) throws IOException, SchemaParseException {
        List<SchemaField> schemaFieldsList = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(schemaFile))) {
            String line;
            int lineNumber = 0;
            while ((line = reader.readLine()) != null) {
                lineNumber++;
                line = line.trim();
                if (line.isEmpty())
                    continue;

                String[] lineContents = line.split("\\s+");
                if (lineContents.length < 3) {
                    throw new SchemaParseException("Line " + lineNumber + ": expected 3 columns (name start end), got " + lineContents.length);
                }

                try {
                    String fieldName = toCamelCase(lineContents[0]);
                    int start = Integer.parseInt(lineContents[1]);
                    int end = Integer.parseInt(lineContents[2]);
                    schemaFieldsList.add(new SchemaField(fieldName, start, end));
                } catch (NumberFormatException e) {
                    throw new SchemaParseException("Line " + lineNumber + ": start/end must be integers");
                } catch (IllegalArgumentException e) {
                    throw new SchemaParseException("Line " + lineNumber + ": " + e.getMessage());
                }
            }
        }

        if (schemaFieldsList.isEmpty()) {
            throw new SchemaParseException("Schema file is empty, no fields to generate");
        }

        if (schemaFieldsList.get(0).getStart() != 1) {
            throw new SchemaParseException("First field must start at position 1, got " + schemaFieldsList.get(0).getStart());
        }

        for (int i = 0; i < schemaFieldsList.size(); i++) {
            SchemaField current = schemaFieldsList.get(i);

            for (int j = i + 1; j < schemaFieldsList.size(); j++) {
                if (current.getSchemaVariable().equals(schemaFieldsList.get(j).getSchemaVariable())) {
                    throw new SchemaParseException("Duplicate field name: " + current.getSchemaVariable());
                }
            }

            if (i + 1 < schemaFieldsList.size()) {
                SchemaField next = schemaFieldsList.get(i + 1);

                if (next.getStart() <= current.getStart()) {
                    throw new SchemaParseException("Fields not in sequential order: '" + current.getSchemaVariable() + "' (start=" + current.getStart() + ") and '" + next.getSchemaVariable() + "' (start=" + next.getStart() + ")");
                }

                if (next.getStart() < current.getEnd()) {
                    throw new SchemaParseException("Overlapping fields: '" + current.getSchemaVariable() + "' (end=" + current.getEnd() + ") and '" + next.getSchemaVariable() + "' (start=" + next.getStart() + ")");
                }

                if (next.getStart() > current.getEnd() + 1) {
                    throw new SchemaParseException("Gap between fields: '" + current.getSchemaVariable() + "' (end=" + current.getEnd() + ") and '" + next.getSchemaVariable() + "' (start=" + next.getStart() + "), positions " + (current.getEnd() + 1) + "-" + (next.getStart() - 1) + " unused");
                }
            }
        }

        return schemaFieldsList;
    }

    private static void writeFile(String path, String content, String fileName) throws IOException {
        try (FileWriter writer = new FileWriter(path)) {
            writer.write(content);
            logger.info("{} generated.", fileName);
        }
    }

    static String toCamelCase(String input) {
        String[] parts = input.split("[_\\-\\s]+");
        StringBuilder result = new StringBuilder(parts[0].toLowerCase());
        for (int i = 1; i < parts.length; i++) {
            result.append(parts[i].substring(0, 1).toUpperCase());
            result.append(parts[i].substring(1).toLowerCase());
        }
        return result.toString();
    }
}

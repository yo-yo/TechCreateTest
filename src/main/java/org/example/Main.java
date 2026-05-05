package org.example;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

public class Main {
    private static final Logger logger = Logger.getLogger(Main.class.getName());

    public static void main(String[] args) {
        String schemaPath = "src/main/java/org/example/schema.txt";
        File schemaFile = new File(schemaPath).getAbsoluteFile();
        String outputDir = schemaFile.getParent() + File.separator;

        if (!schemaFile.exists()) {
            logger.severe("Schema file not found: " + schemaFile.getPath());
            return;
        }

        try {
            List<SchemaField> schemaFieldsList = parseSchema(schemaFile);
            writeFile(outputDir + "Record.java", RecordGenerator.generate(schemaFieldsList), "Record.java");
            writeFile(outputDir + "FixedLengthParser.java", ParserGenerator.generate(schemaFieldsList), "FixedLengthParser.java");
        } catch (SchemaParseException e) {
            logger.severe("Schema validation failed: " + e.getMessage());
        } catch (IOException e) {
            logger.severe("Error reading schema file: " + e.getMessage());
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

        if (schemaFieldsList.get(0).start != 1) {
            throw new SchemaParseException("First field must start at position 1, got " + schemaFieldsList.get(0).start);
        }

        for (int i = 0; i < schemaFieldsList.size(); i++) {
            SchemaField current = schemaFieldsList.get(i);

            for (int j = i + 1; j < schemaFieldsList.size(); j++) {
                if (current.schemaVariable.equals(schemaFieldsList.get(j).schemaVariable)) {
                    throw new SchemaParseException("Duplicate field name: " + current.schemaVariable);
                }
            }

            if (i + 1 < schemaFieldsList.size()) {
                SchemaField next = schemaFieldsList.get(i + 1);

                if (next.start <= current.start) {
                    throw new SchemaParseException("Fields not in sequential order: '" + current.schemaVariable + "' (start=" + current.start + ") and '" + next.schemaVariable + "' (start=" + next.start + ")");
                }

                if (next.start < current.end) {
                    throw new SchemaParseException("Overlapping fields: '" + current.schemaVariable + "' (end=" + current.end + ") and '" + next.schemaVariable + "' (start=" + next.start + ")");
                }

                if (next.start > current.end + 1) {
                    throw new SchemaParseException("Gap between fields: '" + current.schemaVariable + "' (end=" + current.end + ") and '" + next.schemaVariable + "' (start=" + next.start + "), positions " + (current.end + 1) + "-" + (next.start - 1) + " unused");
                }
            }
        }

        return schemaFieldsList;
    }

    private static void writeFile(String path, String content, String fileName) {
        try (FileWriter writer = new FileWriter(path)) {
            writer.write(content);
            logger.info(fileName + " generated.");
        } catch (IOException e) {
            logger.severe("Error writing " + fileName + ": " + e.getMessage());
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

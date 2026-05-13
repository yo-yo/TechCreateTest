package org.example;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

public class SchemaParser {

    public static List<SchemaField> parse(File schemaFile) throws IOException, SchemaParseException {
        List<String> lines = Files.readAllLines(schemaFile.toPath());
        return parse(lines);
    }

    public static List<SchemaField> parse(List<String> lines) throws SchemaParseException {
        List<SchemaField> schemaFieldsList = new ArrayList<>();
        int lineNumber = 0;
        for (String rawLine : lines) {
            lineNumber++;
            String line = rawLine.trim();
            if (line.isEmpty())
                continue;

            String[] lineContents = line.split("\\s+");
            if (lineContents.length < 3) {
                throw new SchemaParseException("Line " + lineNumber + ": expected 3 columns (name start end), got " + lineContents.length);
            }

            try {
                String fieldName = lineContents[0];
                int start = Integer.parseInt(lineContents[1]);
                int end = Integer.parseInt(lineContents[2]);
                schemaFieldsList.add(new SchemaField(fieldName, start, end));
            } catch (NumberFormatException e) {
                throw new SchemaParseException("Line " + lineNumber + ": start/end must be integers");
            } catch (IllegalArgumentException e) {
                throw new SchemaParseException("Line " + lineNumber + ": " + e.getMessage());
            }
        }

        if (schemaFieldsList.isEmpty()) {
            throw new SchemaParseException("Schema file is empty, no fields to generate");
        }

        if (schemaFieldsList.get(0).getStart() != 1) {
            throw new SchemaParseException("First field must start at position 1, got " + schemaFieldsList.get(0).getStart());
        }

        // Validate field ordering: no duplicates, sequential positions, no overlaps or gaps
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
}

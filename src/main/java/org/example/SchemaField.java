package org.example;

public class SchemaField {

    private String schemaVariable;

    private int start;

    private int end;

    public SchemaField(String schemaVariable, int start, int end) {
        validateName(schemaVariable);
        validatePositions(start, end);

        this.schemaVariable = sanitize(schemaVariable.trim());
        this.start = start;
        this.end = end;
    }

    public String getSchemaVariable() {
        return schemaVariable;
    }

    public int getStart() {
        return start;
    }

    public int getEnd() {
        return end;
    }

    private static String sanitize(String name) {
        String[] parts = name.split("[\\s\\-]+");
        StringBuilder sb = new StringBuilder();
        for (String part : parts) {
            part = part.replaceAll("[^a-zA-Z0-9_]", "");
            if (part.isEmpty()) continue;
            if (sb.length() == 0) {
                sb.append(part);
            } else {
                sb.append(Character.toUpperCase(part.charAt(0)));
                if (part.length() > 1) sb.append(part.substring(1));
            }
        }
        String result = sb.toString();
        if (!result.isEmpty() && Character.isDigit(result.charAt(0))) {
            result = "_" + result;
        }
        return result;
    }

    private static void validateName(String name) {
        if (name == null || name.trim().isEmpty() || name.equals("null")) {
            throw new IllegalArgumentException("schemaVariable cannot be null or empty");
        }
        if (sanitize(name.trim()).isEmpty()) {
            throw new IllegalArgumentException("schemaVariable produces no valid Java identifier: " + name);
        }
    }

    private static void validatePositions(int start, int end) {
        if (start < 1 || end < 1) {
            throw new IllegalArgumentException("start and end must be positive");
        }
        if (start > end) {
            throw new IllegalArgumentException("start (" + start + ") cannot be greater than end (" + end + ")");
        }
    }

    @Override
    public String toString() {
        return "SchemaField{" +
                "schemaVariable='" + schemaVariable + '\'' +
                ", start=" + start +
                ", end=" + end +
                '}';
    }
}

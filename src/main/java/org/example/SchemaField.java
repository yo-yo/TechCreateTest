package org.example;

public class SchemaField {

    private String schemaVariable;

    private int start;

    private int end;

    public SchemaField(String schemaVariable, int start, int end) {
        if (schemaVariable == null || schemaVariable.trim().isEmpty() || schemaVariable.equals("null")) {
            throw new IllegalArgumentException("schemaVariable cannot be null or empty");
        }
        if (!schemaVariable.matches("[a-zA-Z_][a-zA-Z0-9_]*")) {
            throw new IllegalArgumentException("schemaVariable must be a valid Java identifier: " + schemaVariable);
        }
        if (start < 1 || end < 1) {
            throw new IllegalArgumentException("start and end must be positive");
        }
        if (start > end) {
            throw new IllegalArgumentException("start (" + start + ") cannot be greater than end (" + end + ")");
        }
        this.schemaVariable = schemaVariable;
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

    @Override
    public String toString() {
        return "SchemaField{" +
                "schemaVariable='" + schemaVariable + '\'' +
                ", start=" + start +
                ", end=" + end +
                '}';
    }
}

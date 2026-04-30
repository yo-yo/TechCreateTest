package org.example;

public class SchemaField {

    public String schemaVariable;

    public int start;

    public int end;

    public SchemaField(String schemaVariable, int start, int end) {
        this.schemaVariable = schemaVariable;
        this.start = start;
        this.end = end;
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

package org.example;

import java.util.List;
import java.util.StringJoiner;

public class RecordGenerator {

    public static String generate(List<SchemaField> schemaFieldsList) {
        StringBuilder sb = new StringBuilder();
        sb.append("public class Record {\n");
        for (SchemaField x : schemaFieldsList) {
            sb.append("     private String " + x.schemaVariable + ";\n");
        }
        sb.append("\n");
        sb.append("     public Record(");

        StringJoiner params = new StringJoiner(", ");
        schemaFieldsList.forEach(x -> params.add("String " + x.schemaVariable));
        sb.append(params + "){\n");

        for (SchemaField x : schemaFieldsList) {
            sb.append("         this." + x.schemaVariable + "=" + x.schemaVariable + ";\n");
        }

        sb.append("     }\n\n");
        sb.append("     @Override\n");
        sb.append("     public String toString() {\n");
        sb.append("         return \"Record{");

        StringJoiner toStringFields = new StringJoiner(" + \"', ");
        schemaFieldsList.forEach(x -> toStringFields.add(x.schemaVariable + "='\" + " + x.schemaVariable));
        sb.append(toStringFields + " + \"'}\";\n");

        sb.append("     }\n");
        sb.append("     // Getters and Setters (if needed)\n\n}");

        return sb.toString();
    }
}

package org.example;

import java.util.List;
import java.util.StringJoiner;

public class ParserGenerator {

    public static String generate(List<SchemaField> schemaFieldsList) {
        StringBuilder sb = new StringBuilder();
        sb.append("import java.io.BufferedReader;\n");
        sb.append("import java.io.FileReader;\n");
        sb.append("import java.io.IOException;\n");
        sb.append("import java.util.ArrayList;\n");
        sb.append("import java.util.List;\n\n");
        sb.append("public class FixedLengthParser {\n\n");
        sb.append("    // Schema configuration\n");

        for (int i = 0; i < schemaFieldsList.size(); i++) {
            SchemaField field = schemaFieldsList.get(i);
            String upper = field.getSchemaVariable().toUpperCase();
            int end = field.getEnd();
            if (i + 1 < schemaFieldsList.size() && field.getEnd() >= schemaFieldsList.get(i + 1).getStart()) {
                end = schemaFieldsList.get(i + 1).getStart() - 1;
            }
            sb.append("    private static final int " + upper + "_START = " + field.getStart() + ";\n");
            sb.append("    private static final int " + upper + "_END = " + end + ";\n");
        }
        sb.append("\n");

        SchemaField lastField = schemaFieldsList.get(schemaFieldsList.size() - 1);
        String lastEndConstant = lastField.getSchemaVariable().toUpperCase() + "_END";

        sb.append("    public List<Record> parseFile(String filePath) throws IOException {\n");
        sb.append("        List<Record> records = new ArrayList<>();\n");
        sb.append("        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {\n");
        sb.append("            String line;\n");
        sb.append("            while ((line = reader.readLine()) != null) {\n");
        sb.append("                if (line.length() < " + lastEndConstant + ") {\n");
        sb.append("                    // Handle lines that are shorter than expected\n");
        sb.append("                    continue;\n");
        sb.append("                }\n");
        sb.append("                \n");
        sb.append("                // Extract fields based on fixed positions\n");

        for (SchemaField field : schemaFieldsList) {
            String upper = field.getSchemaVariable().toUpperCase();
            sb.append("                String " + field.getSchemaVariable() + " = extractField(line, " + upper + "_START, " + upper + "_END).trim();\n");
        }
        sb.append("                \n");
        sb.append("                // Create a new Record object\n");

        StringJoiner recordParams = new StringJoiner(", ");
        schemaFieldsList.forEach(x -> recordParams.add(x.getSchemaVariable()));
        sb.append("                Record record = new Record(" + recordParams + ");\n");
        sb.append("                records.add(record);\n");
        sb.append("            }\n");
        sb.append("        }\n");
        sb.append("        return records;\n");
        sb.append("    }\n\n");
        sb.append("    private String extractField(String line, int start, int end) {\n");
        sb.append("        return line.substring(start - 1, end);\n");
        sb.append("    }\n\n");
        sb.append("    public static void main(String[] args) {\n");
        sb.append("        FixedLengthParser parser = new FixedLengthParser();\n");
        sb.append("        try {\n");
        sb.append("            List<Record> records = parser.parseFile(\"path/to/your/file.txt\");\n");
        sb.append("            for (Record record : records) {\n");
        sb.append("                System.out.println(record);\n");
        sb.append("            }\n");
        sb.append("        } catch (IOException e) {\n");
        sb.append("            e.printStackTrace();\n");
        sb.append("        }\n");
        sb.append("    }\n");
        sb.append("}\n");

        return sb.toString();
    }
}

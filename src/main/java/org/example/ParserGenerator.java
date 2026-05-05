package org.example;

import java.util.List;
import java.util.StringJoiner;

public class ParserGenerator {

    public static String generate(List<SchemaField> schemaFieldsList) {
        StringBuilder fp = new StringBuilder();
        fp.append("import java.io.BufferedReader;\n");
        fp.append("import java.io.FileReader;\n");
        fp.append("import java.io.IOException;\n");
        fp.append("import java.util.ArrayList;\n");
        fp.append("import java.util.List;\n\n");
        fp.append("public class FixedLengthParser {\n\n");
        fp.append("    // Schema configuration\n");

        for (int i = 0; i < schemaFieldsList.size(); i++) {
            SchemaField field = schemaFieldsList.get(i);
            String upper = field.getSchemaVariable().toUpperCase();
            int end = field.getEnd();
            if (i + 1 < schemaFieldsList.size() && field.getEnd() >= schemaFieldsList.get(i + 1).getStart()) {
                end = schemaFieldsList.get(i + 1).getStart() - 1;
            }
            fp.append("    private static final int " + upper + "_START = " + field.getStart() + ";\n");
            fp.append("    private static final int " + upper + "_END = " + end + ";\n");
        }
        fp.append("\n");

        SchemaField lastField = schemaFieldsList.get(schemaFieldsList.size() - 1);
        String lastEndConstant = lastField.getSchemaVariable().toUpperCase() + "_END";

        fp.append("    public List<Record> parseFile(String filePath) throws IOException {\n");
        fp.append("        List<Record> records = new ArrayList<>();\n");
        fp.append("        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {\n");
        fp.append("            String line;\n");
        fp.append("            while ((line = reader.readLine()) != null) {\n");
        fp.append("                if (line.length() < " + lastEndConstant + ") {\n");
        fp.append("                    // Handle lines that are shorter than expected\n");
        fp.append("                    continue;\n");
        fp.append("                }\n");
        fp.append("                \n");
        fp.append("                // Extract fields based on fixed positions\n");

        for (SchemaField field : schemaFieldsList) {
            String upper = field.getSchemaVariable().toUpperCase();
            fp.append("                String " + field.getSchemaVariable() + " = extractField(line, " + upper + "_START, " + upper + "_END).trim();\n");
        }
        fp.append("                \n");
        fp.append("                // Create a new Record object\n");

        StringJoiner recordParams = new StringJoiner(", ");
        schemaFieldsList.forEach(x -> recordParams.add(x.getSchemaVariable()));
        fp.append("                Record record = new Record(" + recordParams + ");\n");
        fp.append("                records.add(record);\n");
        fp.append("            }\n");
        fp.append("        }\n");
        fp.append("        return records;\n");
        fp.append("    }\n\n");
        fp.append("    private String extractField(String line, int start, int end) {\n");
        fp.append("        return line.substring(start - 1, end);\n");
        fp.append("    }\n\n");
        fp.append("    public static void main(String[] args) {\n");
        fp.append("        FixedLengthParser parser = new FixedLengthParser();\n");
        fp.append("        try {\n");
        fp.append("            List<Record> records = parser.parseFile(\"path/to/your/file.txt\");\n");
        fp.append("            for (Record record : records) {\n");
        fp.append("                System.out.println(record);\n");
        fp.append("            }\n");
        fp.append("        } catch (IOException e) {\n");
        fp.append("            e.printStackTrace();\n");
        fp.append("        }\n");
        fp.append("    }\n");
        fp.append("}\n");

        return fp.toString();
    }
}

package org.example;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.StringJoiner;
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

        List<SchemaField> schemaFieldsList = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(schemaFile))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] lineContents = line.split("\\s+");

                String fieldName = lineContents[0];
                int start = Integer.parseInt(lineContents[1]);
                int end = Integer.parseInt(lineContents[2]);
                schemaFieldsList.add(new SchemaField(fieldName, start, end));
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        StringBuilder sb = new StringBuilder();
        sb.append("public class Record {\n");
        for(SchemaField x: schemaFieldsList){
            sb.append("     private String " + x.schemaVariable + ";\n");
        }
        sb.append("\n");
        sb.append("     public Record(");

        StringJoiner params = new StringJoiner(", ");
        schemaFieldsList.forEach(x -> params.add("String " + x.schemaVariable));
        sb.append(params + "){\n");

        for(SchemaField x: schemaFieldsList){
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

        try (FileWriter writer = new FileWriter(outputDir + "Record.java")) {
            writer.write(sb.toString());
            logger.info("Record.java generated.");
        } catch (IOException e) {
            logger.severe("Error writing file: " + e.getMessage());
        }

        StringBuilder fp = new StringBuilder();
        fp.append("import java.io.BufferedReader;\n");
        fp.append("import java.io.FileReader;\n");
        fp.append("import java.io.IOException;\n");
        fp.append("import java.util.ArrayList;\n");
        fp.append("import java.util.List;\n\n");
        fp.append("public class FixedLengthParser {\n\n");
        fp.append("     // Schema configuration\n");

        for (SchemaField x : schemaFieldsList) {
            fp.append("     private static final int " + x.schemaVariable.toUpperCase() + "_START = " + x.start + ";\n");
            fp.append("     private static final int " + x.schemaVariable.toUpperCase() + "_END = " + x.end + ";\n");
        }
        fp.append("\n");

        SchemaField lastField = schemaFieldsList.get(schemaFieldsList.size() - 1);
        String lastEndConstant = lastField.schemaVariable.toUpperCase() + "_END";

        fp.append("     public List<Record> parseFile(String filePath) throws IOException {\n");
        fp.append("         List<Record> records = new ArrayList<>();\n");
        fp.append("         try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {\n");
        fp.append("             String line;\n");
        fp.append("             while ((line = reader.readLine()) != null) {\n");
        fp.append("                 if (line.length() < " + lastEndConstant + ") {\n");
        fp.append("                     // Handle lines that are shorter than expected\n");
        fp.append("                     continue;\n");
        fp.append("                 }\n\n");
        fp.append("                 // Extract fields based on fixed positions\n");

        for (SchemaField x : schemaFieldsList) {
            String upper = x.schemaVariable.toUpperCase();
            fp.append("                 String " + x.schemaVariable + " = extractField(line, " + upper + "_START, " + upper + "_END).trim();\n");
        }
        fp.append("\n");
        fp.append("                 // Create a new Record object\n");

        StringJoiner recordParams = new StringJoiner(", ");
        schemaFieldsList.forEach(x -> recordParams.add(x.schemaVariable));

        fp.append("                 Record record = new Record(" + recordParams + ");\n");
        fp.append("                 records.add(record);\n");
        fp.append("             }\n");
        fp.append("         }\n");
        fp.append("         return records;\n");
        fp.append("     }\n\n");
        fp.append("     private String extractField(String line, int start, int end) {\n");
        fp.append("         return line.substring(start - 1, end);\n");
        fp.append("     }\n\n");
        fp.append("     public static void main(String[] args) {\n");
        fp.append("         FixedLengthParser parser = new FixedLengthParser();\n");
        fp.append("         try {\n");
        fp.append("             List<Record> records = parser.parseFile(\"path/to/your/file.txt\");\n");
        fp.append("             for (Record record : records) {\n");
        fp.append("                 System.out.println(record);\n");
        fp.append("             }\n");
        fp.append("         } catch (IOException e) {\n");
        fp.append("             e.printStackTrace();\n");
        fp.append("         }\n");
        fp.append("     }\n");
        fp.append("}\n");

        try (FileWriter writer = new FileWriter(outputDir + "FixedLengthParser.java")) {
            writer.write(fp.toString());
            logger.info("FixedLengthParser.java generated.");
        } catch (IOException e) {
            logger.severe("Error writing file: " + e.getMessage());
        }
    }
}
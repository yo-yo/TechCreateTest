package org.example;

import org.apache.velocity.VelocityContext;

import java.util.List;

public class ParserGenerator {

    public static String generate(List<SchemaField> schemaFieldsList) {
        for (int i = 0; i < schemaFieldsList.size(); i++) {
            SchemaField field = schemaFieldsList.get(i);
            if (i + 1 < schemaFieldsList.size() && field.getEnd() >= schemaFieldsList.get(i + 1).getStart()) {
                field.setEnd(schemaFieldsList.get(i + 1).getStart() - 1);
            }
        }

        SchemaField lastField = schemaFieldsList.get(schemaFieldsList.size() - 1);

        VelocityContext ctx = new VelocityContext();
        ctx.put("fields", schemaFieldsList);
        ctx.put("lastField", lastField);
        return TemplateRenderer.render("templates/FixedLengthParser.vm", ctx);
    }
}

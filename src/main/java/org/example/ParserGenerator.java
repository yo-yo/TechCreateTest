package org.example;

import org.apache.velocity.VelocityContext;

import java.util.List;

public class ParserGenerator {

    public static String generate(List<SchemaField> schemaFieldsList) {
        // Adjust field boundaries when end overlaps with next field's start
        // ex. name(1-20), gender(20-21) → name(1-19), gender(20-21)
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

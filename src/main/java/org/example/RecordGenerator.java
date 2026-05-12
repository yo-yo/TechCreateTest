package org.example;

import org.apache.velocity.VelocityContext;

import java.util.List;

public class RecordGenerator {

    public static String generate(List<SchemaField> schemaFieldsList) {
        VelocityContext ctx = new VelocityContext();
        ctx.put("fields", schemaFieldsList);
        return TemplateRenderer.render("templates/Record.vm", ctx);
    }
}

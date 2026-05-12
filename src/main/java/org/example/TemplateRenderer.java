package org.example;

import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;

import java.io.StringWriter;

public class TemplateRenderer {

    private static final VelocityEngine engine;

    static {
        engine = new VelocityEngine();
        engine.setProperty("resource.loaders", "class");
        engine.setProperty("resource.loader.class.class",
                "org.apache.velocity.runtime.resource.loader.ClasspathResourceLoader");
        engine.init();
    }

    public static String render(String templatePath, VelocityContext ctx) {
        Template t = engine.getTemplate(templatePath);
        StringWriter sw = new StringWriter();
        t.merge(ctx, sw);
        return sw.toString();
    }
}

package com.example.config;

import com.google.common.io.CharStreams;
import org.springframework.core.io.ResourceLoader;
import org.springframework.ui.freemarker.SpringTemplateLoader;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;

/**
 * Freemarker's template loader with automatic html escape.
 */
public class AutoEscapeTemplateLoader extends SpringTemplateLoader {
    public AutoEscapeTemplateLoader(ResourceLoader resourceLoader,
                                    String templateLoaderPath) {
        super(resourceLoader, templateLoaderPath);
    }

    @Override
    public Reader getReader(Object templateSource, String encoding) throws IOException {
        String s = CharStreams.toString(super.getReader(templateSource, encoding));
        return new StringReader("<#escape x as x?html>" + s + "</#escape>");
    }
}

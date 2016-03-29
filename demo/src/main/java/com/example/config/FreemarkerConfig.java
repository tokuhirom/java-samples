package com.example.config;

import freemarker.cache.TemplateLoader;
import freemarker.template.TemplateModelException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.freemarker.FreeMarkerProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.ui.freemarker.SpringTemplateLoader;
import org.springframework.web.context.ServletContextAware;
import org.springframework.web.context.support.ServletContextResourceLoader;
import org.springframework.web.servlet.view.freemarker.FreeMarkerConfigurer;

import javax.servlet.ServletContext;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

@Configuration
public class FreemarkerConfig implements ServletContextAware {
    private ServletContext servletContext;
    @Autowired
    public FreeMarkerProperties properties;

    @Bean
    @ConditionalOnMissingBean(org.springframework.web.servlet.view.freemarker.FreeMarkerConfig.class)
    public FreeMarkerConfigurer freeMarkerConfigurer() throws TemplateModelException {
        FreeMarkerConfigurer configurer = new FreeMarkerConfigurer();
        configurer.setPreferFileSystemAccess(this.properties.isPreferFileSystemAccess());
        configurer.setDefaultEncoding(this.properties.getCharsetName());
        configurer.setFreemarkerSettings(buildSettings());
        configurer.setFreemarkerVariables(buildVariables());
        configurer.setPreTemplateLoaders(buildPreTemplateLoaders());
        return configurer;
    }

    // enable AutoEscapeTemplateLoader.
    private TemplateLoader[] buildPreTemplateLoaders() {
        ServletContextResourceLoader servletContextResourceLoader =
                new ServletContextResourceLoader(servletContext);
        return Arrays.stream(this.properties.getTemplateLoaderPath())
                .map(
                        templateLoaderPath -> new AutoEscapeTemplateLoader(
                                servletContextResourceLoader,
                                templateLoaderPath)
                ).toArray(TemplateLoader[]::new);
    }

    private Properties buildSettings() {
        Properties settings = new Properties();
        settings.putAll(this.properties.getSettings());
        // default charset for string?url()
        settings.put("url_escaping_charset", "UTF-8");
        // no auto commify
        settings.put("number_format", "0.#######");
        // import spring.ftl
        settings.put("auto_import", "/spring.ftl as spring");
        return settings;
    }

    // Provide global variables.
    private Map<String, Object> buildVariables() {
        Map<String, Object> variables = new HashMap<>();
        variables.put("Hello", "world");
        return variables;
    }

    @Override
    public void setServletContext(ServletContext servletContext) {
        this.servletContext = servletContext;
    }
}

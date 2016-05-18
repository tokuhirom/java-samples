package io.prometheus.client.spring_boot;

class SpringBootExporterUtils {
    // Correct label name is /[a-zA-Z_][a-zA-Z0-9_]*/
    static String makeName(String label) {
        return label.replaceFirst("^[^a-zA-Z_]", "_")
                .replaceAll("[^a-zA-Z0-9_]", "_");
    }
}

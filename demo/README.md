# spring-boot + spring mvc demo application

This application includes some tweaks for writing better spring boot apps.

## Freemarker

### layout

You can use freemarker's macro for template inheritance.
See `src/main/resources/templates/__wrapper.ftl`.

### Template reloading without restart web app.

Pass `spring.freemarker.templateLoaderPath` for template reloading without restarting web app.
 
     bootRun {
         // Load templates from local file system on local development.
         systemProperty "spring.freemarker.templateLoaderPath", [
                 "file:${project.projectDir}/src/main/resources/templates/",
                 "classpath:/templates/"
         ].join(",")
     }

See `build.gradle`.

### Automatic HTML escape.

com.example.config.AutoEscapeTemplateLoader provides automatic HTML escape.
I recommend to use this.

It will load by FreemarkerConfig.

## TODO

 * customize whitelabel
 * multipart/form-data sample
 
## DONE

 * get
 * layout.ftl

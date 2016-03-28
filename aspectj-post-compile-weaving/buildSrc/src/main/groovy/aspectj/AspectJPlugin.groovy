package aspectj

import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.file.FileCollection
import org.gradle.api.plugins.JavaPlugin
import org.gradle.api.tasks.SourceSet
import org.gradle.api.tasks.TaskAction

// taken from spring security.
// https://github.com/spring-projects/spring-security/blob/master/buildSrc/src/main/groovy/aspectj/AspectJPlugin.groovy
class AspectJPlugin implements Plugin<Project> {

    void apply(Project project) {
        project.plugins.apply(JavaPlugin)

        if (!project.hasProperty('aspectjVersion')) {
            throw new GradleException("You must set the property 'aspectjVersion' before applying the aspectj plugin")
        }

        if (project.configurations.findByName('ajtools') == null) {
            project.configurations.create('ajtools')
            project.dependencies {
                ajtools "org.aspectj:aspectjtools:${project.aspectjVersion}"
                compile "org.aspectj:aspectjrt:${project.aspectjVersion}"
            }
        }

        project.afterEvaluate {

            project.tasks.create(name: 'compileAspect', overwrite: true, description: 'Compiles AspectJ Source', type: Ajc) {
                dependsOn project.processResources, project.compileJava

                sourceSet = project.sourceSets.main

                tmpDir = "${project.buildDir}/aspect/"
                aspectpath = project.files(project.sourceSets.main.output.classesDir)
            }
            project.tasks.classes.dependsOn project.tasks.compileAspect

            project.tasks.create(name: 'compileTestAspect', overwrite: true, description: 'Compiles AspectJ Test Source', type: Ajc) {
                dependsOn project.processTestResources, project.compileTestJava

                sourceSet = project.sourceSets.test

                tmpDir = "${project.buildDir}/test-aspect/"
                aspectpath = project.files(project.sourceSets.main.output.classesDir,
                        project.sourceSets.test.output.classesDir)
            }
            project.tasks.testClasses.dependsOn project.tasks.compileTestAspect
        }
    }
}

class Ajc extends DefaultTask {
    String tmpDir
    FileCollection aspectpath
    SourceSet sourceSet

    Ajc() {
        logging.captureStandardOutput(LogLevel.INFO)
    }

    //http://www.eclipse.org/aspectj/doc/released/devguide/ajc-ref.html

    // http://stackoverflow.com/questions/3660547/apt-and-aop-in-the-same-project-using-maven
    // https://github.com/uPhyca/gradle-android-aspectj-plugin/blob/8d580d8117932a23209421193da77f175d19d416/plugin/src/main/groovy/com/uphyca/gradle/android/AspectjCompile.groovy
    @TaskAction
    def compile() {
        logger.info("=" * 30)
        logger.info("=" * 30)
        logger.info("Running ajc ...")

        ant.taskdef(resource: "org/aspectj/tools/ant/taskdefs/aspectjTaskdefs.properties", classpath: project.configurations.ajtools.asPath)
        ant.iajc(
                classpath: sourceSet.compileClasspath.asPath,
                fork: 'true',
                inpath: sourceSet.output.classesDir.toPath(),
                destDir: tmpDir,
                source: project.convention.plugins.java.sourceCompatibility,
                target: project.convention.plugins.java.targetCompatibility,
                // aspectPath: aspectPath.asPath,
                aspectPath: aspectpath.asPath,
                showWeaveInfo: 'true') {
        }

        ant.move(file: tmpDir, tofile: sourceSet.output.classesDir)
    }
}
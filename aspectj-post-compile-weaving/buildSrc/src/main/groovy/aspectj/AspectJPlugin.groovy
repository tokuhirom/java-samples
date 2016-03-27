package aspectj

import org.aspectj.bridge.IMessage
import org.aspectj.bridge.MessageHandler
import org.aspectj.tools.ajc.Main
import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.plugins.JavaPlugin
import org.gradle.api.tasks.TaskAction

import java.nio.file.Path

// taken from spring security.
// https://github.com/spring-projects/spring-security/blob/master/buildSrc/src/main/groovy/aspectj/AspectJPlugin.groovy
/**
 *
 * @author Luke Taylor
 */
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

                tmpDir = "${project.buildDir}/aspect/"
                args = [
                        "-inpath", project.sourceSets.main.output.classesDir.toPath(),
                        "-showWeaveInfo",
                        "-1.8",
                        "-d", tmpDir,
                        "-classpath", project.sourceSets.main.compileClasspath.asPath,
                ];
                dstDir = project.sourceSets.main.output.classesDir.toPath()
            }
            project.tasks.classes.dependsOn project.tasks.compileAspect

            project.tasks.create(name: 'compileTestAspect', overwrite: true, description: 'Compiles AspectJ Test Source', type: Ajc) {
                dependsOn project.processTestResources, project.compileTestJava

                tmpDir = "${project.buildDir}/test-aspect/"
                def classpath = project.sourceSets.test.compileClasspath.files.grep({ it.exists() }).join(":")
                args = [
                        "-inpath", project.sourceSets.test.output.classesDir.toPath(),
                        "-showWeaveInfo",
                        "-1.8",
                        "-d", tmpDir,
                        "-classpath", classpath
                ];
                dstDir = project.sourceSets.test.output.classesDir.toPath()
            }
            project.tasks.testClasses.dependsOn project.tasks.compileTestAspect
        }
    }
}

class Ajc extends DefaultTask {
    String[] args
    String tmpDir
    Path dstDir

    Ajc() {
        // logging.captureStandardOutput(LogLevel.INFO)
    }

    //http://www.eclipse.org/aspectj/doc/released/devguide/ajc-ref.html

    // http://stackoverflow.com/questions/3660547/apt-and-aop-in-the-same-project-using-maven
    // https://github.com/uPhyca/gradle-android-aspectj-plugin/blob/8d580d8117932a23209421193da77f175d19d416/plugin/src/main/groovy/com/uphyca/gradle/android/AspectjCompile.groovy
    @TaskAction
    def compile() {
        logger.info("=" * 30)
        logger.info("=" * 30)
        logger.info("Running ajc ...")

        MessageHandler handler = new MessageHandler(false);
        println("args: $args")
        new Main().run(args as String[], handler);
        for (IMessage message : handler.getMessages(null, true)) {
            switch (message.getKind()) {
                case IMessage.ABORT:
                case IMessage.ERROR:
                case IMessage.FAIL:
                    logger.error message.message, message.thrown
                    break;
                case IMessage.WARNING:
                    logger.warn message.message, message.thrown
                    break;
                case IMessage.INFO:
                    logger.info message.message, message.thrown
                    break;
                case IMessage.DEBUG:
                    logger.debug message.message, message.thrown
                    break;
            }
        }

        ant.move(file: tmpDir, tofile: dstDir)
    }
}
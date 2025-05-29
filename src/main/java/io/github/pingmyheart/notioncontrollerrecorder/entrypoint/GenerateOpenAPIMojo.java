package io.github.pingmyheart.notioncontrollerrecorder.entrypoint;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileVisitOption;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.regex.Pattern;
import java.util.stream.Stream;

@Mojo(name = "generate-openapi",
        defaultPhase = LifecyclePhase.PACKAGE,
        aggregator = true)
public class GenerateOpenAPIMojo extends AbstractMojo {
    @Parameter(defaultValue = "${project.basedir}/src/main/java", required = true)
    private File sourceDirectory;

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        getLog().info("Scanning source files in: " + sourceDirectory);

        try (Stream<Path> stream = Files.walk(sourceDirectory.toPath(), FileVisitOption.FOLLOW_LINKS)) {
            stream.filter(Files::isRegularFile)
                    .filter(file -> Pattern.compile(".*\\.java$").matcher(file.toFile().getName()).matches())
                    .forEach(javaClass -> {
                        try {
                            new JavaParser().parse(javaClass)
                                    .getResult()
                                    .ifPresent(result -> {
                                        result.findAll(ClassOrInterfaceDeclaration.class)
                                                .forEach(clazz -> {
                                                    if (clazz.isAnnotationPresent("RestController") ||
                                                            clazz.isAnnotationPresent("Controller")) {
                                                        getLog().info("Found controller: " + clazz.getNameAsString());
                                                    }
                                                });
                                    });
                        } catch (IOException e) {
                            getLog().error(e.getMessage(), e);
                        }
                    });
        } catch (IOException e) {
            getLog().error(e.getMessage(), e);
        }
    }
}

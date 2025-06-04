package io.github.pingmyheart.notioncontrollerrecorder.entrypoint;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.util.DefaultPrettyPrinter;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.javaparser.JavaParser;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.expr.AnnotationExpr;
import io.github.pingmyheart.notioncontrollerrecorder.dto.internal.ControllerDTO;
import io.github.pingmyheart.notioncontrollerrecorder.dto.internal.EndpointDTO;
import io.github.pingmyheart.notioncontrollerrecorder.dto.internal.ReportDTO;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.FileVisitOption;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.stream.Stream;

/**
 * Generates OpenAPI documentation for controllers annotated with Spring's @RestController or @Controller annotations.
 * This Mojo scans the source directory for Java files,
 * parses them to find controller classes,
 * and extracts endpoint information
 */
@Mojo(name = "generate",
        defaultPhase = LifecyclePhase.PACKAGE,
        aggregator = true)
public class GenerateDocumentationMojo extends AbstractMojo {
    @Parameter(defaultValue = "${project.basedir}/src/main/java",
            required = true)
    private File sourceDirectory;
    @Parameter(defaultValue = "${project.build.directory}/generated-sources/ncrmp/notion-report.json",
            required = true)
    private File outputFile;

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        if (!outputFile.getParentFile().exists()) {
            if (outputFile.getParentFile().mkdirs()) {
                getLog().info("Created directories for output file: " + outputFile.getParentFile().getAbsolutePath());
            } else {
                getLog().warn("Failed to create directories for output file: " + outputFile.getParentFile().getAbsolutePath());
                throw new MojoExecutionException("Failed to create directories for output file: " + outputFile.getParentFile().getAbsolutePath());
            }
        }
        getLog().info("Scanning source files in: " + sourceDirectory);

        ReportDTO reportDTO = ReportDTO.builder()
                .build();

        try (Stream<Path> stream = Files.walk(sourceDirectory.toPath(), FileVisitOption.FOLLOW_LINKS)) {
            stream.filter(Files::isRegularFile)
                    .filter(file -> Pattern.compile(".*\\.java$").matcher(file.toFile().getName()).matches())
                    .forEach(javaClass -> {
                        try {
                            new JavaParser().parse(javaClass)
                                    .getResult()
                                    .ifPresent(result ->
                                            result.findAll(ClassOrInterfaceDeclaration.class)
                                                    .forEach(clazz -> {
                                                        if (isController(clazz)) {
                                                            getLog().info("Found controller: " + clazz.getNameAsString());
                                                            getLog().info("Generating custom OpenAPI documentation for: " + clazz.getNameAsString());
                                                            ControllerDTO controllerDTO = ControllerDTO.builder()
                                                                    .name(clazz.getNameAsString())
                                                                    .basePath(extractControllerPath(clazz))
                                                                    .build();
                                                            clazz.getMethods()
                                                                    .forEach(methodDeclaration -> {
                                                                        if (isRestConcreteAnnotation(methodDeclaration)) {
                                                                            getLog().debug("Found endpoint: " + methodDeclaration.getNameAsString());
                                                                            controllerDTO.getEndpoints()
                                                                                    .add(extractEndpointInfo(methodDeclaration));
                                                                        }
                                                                    });
                                                            reportDTO.getControllers().add(controllerDTO);
                                                        }
                                                    })
                                    );
                        } catch (IOException e) {
                            getLog().error(e.getMessage(), e);
                        }
                    });
        } catch (IOException e) {
            getLog().error(e.getMessage(), e);
        }

        getLog().info("OpenAPI documentation generation completed.");
        try {
            String content = new ObjectMapper().setDefaultPrettyPrinter(new DefaultPrettyPrinter())
                    .writerWithDefaultPrettyPrinter()
                    .writeValueAsString(reportDTO);
            try (BufferedWriter writer = new BufferedWriter(new FileWriter(outputFile))) {
                writer.write(content);
            }
        } catch (JsonProcessingException e) {
            getLog().error("Error serializing report to JSON: " + e.getMessage(), e);
        } catch (IOException e) {
            getLog().error("Error writing report to file: " + e.getMessage(), e);
        }
    }

    private boolean isRestConcreteAnnotation(MethodDeclaration methodDeclaration) {
        return methodDeclaration.isAnnotationPresent("GetMapping") ||
                methodDeclaration.isAnnotationPresent("PostMapping") ||
                methodDeclaration.isAnnotationPresent("PutMapping") ||
                methodDeclaration.isAnnotationPresent("PatchMapping") ||
                methodDeclaration.isAnnotationPresent("DeleteMapping");
    }

    private boolean isController(ClassOrInterfaceDeclaration clazz) {
        return clazz.isAnnotationPresent("RestController") ||
                clazz.isAnnotationPresent("Controller");
    }

    private EndpointDTO extractEndpointInfo(MethodDeclaration methodDeclaration) {
        EndpointDTO endpoint = EndpointDTO.builder()
                .methodName(methodDeclaration.getNameAsString())
                .methodSignature((methodDeclaration.getDeclarationAsString(true, true, true)))
                .build();
        List.of("GetMapping",
                "PostMapping",
                "PutMapping",
                "PatchMapping",
                "DeleteMapping").forEach(mapping -> {
            getLog().debug("Processing mapping: " + mapping + " for method: " + methodDeclaration.getNameAsString());
            methodDeclaration.getAnnotationByName(mapping)
                    .ifPresent(annotation -> {
                        endpoint.setPath(extractAnnotationFields(annotation).getOrDefault("path", ""));
                        endpoint.setProduces(extractAnnotationFields(annotation).getOrDefault("produces", "application/json").replace("\"", ""));
                        endpoint.setConsumes(extractAnnotationFields(annotation).getOrDefault("consumes", "application/json").replace("\"", ""));
                        endpoint.setHttpMethod(mapping.replace("Mapping", "").toUpperCase());
                    });
        });
        return endpoint;
    }

    private String extractControllerPath(ClassOrInterfaceDeclaration clazz) {
        List<String> paths = new ArrayList<>();
        clazz.getAnnotationByName("RequestMapping")
                .ifPresent(annotation -> paths.add(extractAnnotationFields(annotation, "path").get("path")));
        return String.join(", ", paths);
    }

    private Map<String, String> extractAnnotationFields(AnnotationExpr annotation, String defaultField) {
        if (annotation.isNormalAnnotationExpr()) {
            return annotation.asNormalAnnotationExpr()
                    .getPairs()
                    .stream()
                    .collect(HashMap::new,
                            (m, pair) -> m.put(pair.getNameAsString(), pair.getValue().toString()),
                            HashMap::putAll);
        } else if (annotation.isSingleMemberAnnotationExpr()) {
            return Collections.singletonMap(defaultField, annotation.asSingleMemberAnnotationExpr().getMemberValue().toString());
        }
        return Collections.emptyMap();
    }


    private Map<String, String> extractAnnotationFields(AnnotationExpr annotation) {
        return extractAnnotationFields(annotation, "value");
    }
}

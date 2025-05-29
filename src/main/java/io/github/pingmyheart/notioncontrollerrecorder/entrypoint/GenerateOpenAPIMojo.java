package io.github.pingmyheart.notioncontrollerrecorder.entrypoint;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.javaparser.JavaParser;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.expr.AnnotationExpr;
import io.github.pingmyheart.notioncontrollerrecorder.dto.ControllerDTO;
import io.github.pingmyheart.notioncontrollerrecorder.dto.EndpointDTO;
import io.github.pingmyheart.notioncontrollerrecorder.dto.ReportDTO;
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
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.stream.Stream;

@Mojo(name = "generate-json",
        defaultPhase = LifecyclePhase.PACKAGE,
        aggregator = true)
public class GenerateOpenAPIMojo extends AbstractMojo {
    @Parameter(defaultValue = "${project.basedir}/src/main/java", required = true)
    private File sourceDirectory;

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
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
                                                        if (clazz.isAnnotationPresent("RestController") ||
                                                                clazz.isAnnotationPresent("Controller")) {
                                                            getLog().info("Found controller: " + clazz.getNameAsString());
                                                            getLog().info("Generating custom OpenAPI documentation for: " + clazz.getNameAsString());
                                                            ControllerDTO controllerDTO = ControllerDTO.builder()
                                                                    .name(clazz.getNameAsString())
                                                                    .basePath(extractControllerPath(clazz))
                                                                    .build();
                                                            clazz.getMethods()
                                                                    .forEach(methodDeclaration -> {
                                                                        if (methodDeclaration.isAnnotationPresent("GetMapping") ||
                                                                                methodDeclaration.isAnnotationPresent("PostMapping") ||
                                                                                methodDeclaration.isAnnotationPresent("PutMapping") ||
                                                                                methodDeclaration.isAnnotationPresent("PatchMapping") ||
                                                                                methodDeclaration.isAnnotationPresent("DeleteMapping")) {
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
            getLog().info(new ObjectMapper().writeValueAsString(reportDTO));
        } catch (JsonProcessingException e) {
            getLog().error("Error serializing report to JSON: " + e.getMessage(), e);
        }
    }

    private EndpointDTO extractEndpointInfo(MethodDeclaration methodDeclaration) {
        EndpointDTO endpoint = EndpointDTO.builder()
                .methodName(methodDeclaration.getNameAsString())
                .build();
        methodDeclaration.getAnnotationByName("GetMapping")
                .ifPresent(annotation -> {
                    endpoint.setPath(extractAnnotationFields(annotation).getOrDefault("path", ""));
                    endpoint.setProduces(extractAnnotationFields(annotation).getOrDefault("produces", "MediaType.APPLICATION_JSON_VALUE"));
                    endpoint.setConsumes(extractAnnotationFields(annotation).getOrDefault("consumes", "MediaType.APPLICATION_JSON_VALUE"));
                    endpoint.setHttpMethod("GET");
                });
        methodDeclaration.getAnnotationByName("PostMapping")
                .ifPresent(annotation -> {
                    endpoint.setPath(extractAnnotationFields(annotation).getOrDefault("path", ""));
                    endpoint.setProduces(extractAnnotationFields(annotation).getOrDefault("produces", "MediaType.APPLICATION_JSON_VALUE"));
                    endpoint.setConsumes(extractAnnotationFields(annotation).getOrDefault("consumes", "MediaType.APPLICATION_JSON_VALUE"));
                    endpoint.setHttpMethod("POST");
                });
        methodDeclaration.getAnnotationByName("PutMapping")
                .ifPresent(annotation -> {
                    endpoint.setPath(extractAnnotationFields(annotation).getOrDefault("path", ""));
                    endpoint.setProduces(extractAnnotationFields(annotation).getOrDefault("produces", "MediaType.APPLICATION_JSON_VALUE"));
                    endpoint.setConsumes(extractAnnotationFields(annotation).getOrDefault("consumes", "MediaType.APPLICATION_JSON_VALUE"));
                    endpoint.setHttpMethod("PUT");
                });
        methodDeclaration.getAnnotationByName("PatchMapping")
                .ifPresent(annotation -> {
                    endpoint.setPath(extractAnnotationFields(annotation).getOrDefault("path", ""));
                    endpoint.setProduces(extractAnnotationFields(annotation).getOrDefault("produces", "MediaType.APPLICATION_JSON_VALUE"));
                    endpoint.setConsumes(extractAnnotationFields(annotation).getOrDefault("consumes", "MediaType.APPLICATION_JSON_VALUE"));
                    endpoint.setHttpMethod("PATCH");
                });
        methodDeclaration.getAnnotationByName("DeleteMapping")
                .ifPresent(annotation -> {
                    endpoint.setPath(extractAnnotationFields(annotation).getOrDefault("path", ""));
                    endpoint.setProduces(extractAnnotationFields(annotation).getOrDefault("produces", "MediaType.APPLICATION_JSON_VALUE"));
                    endpoint.setConsumes(extractAnnotationFields(annotation).getOrDefault("consumes", "MediaType.APPLICATION_JSON_VALUE"));
                    endpoint.setHttpMethod("DELETE");
                });

        return endpoint;
    }

    private String extractControllerPath(ClassOrInterfaceDeclaration clazz) {
        List<String> paths = new ArrayList<>();
        clazz.getAnnotationByName("RequestMapping")
                .ifPresent(annotation -> paths.add(extractAnnotationFields(annotation).get("path")));
        return String.join(", ", paths);
    }

    private Map<String, String> extractAnnotationFields(AnnotationExpr annotation) {
        if (annotation.isNormalAnnotationExpr()) {
            return annotation.asNormalAnnotationExpr()
                    .getPairs()
                    .stream()
                    .collect(HashMap::new,
                            (m, pair) -> m.put(pair.getNameAsString(), pair.getValue().toString()),
                            HashMap::putAll);
        } else if (annotation.isSingleMemberAnnotationExpr()) {
            return Collections.singletonMap("value", annotation.asSingleMemberAnnotationExpr().getMemberValue().toString());
        }
        return Collections.emptyMap();
    }
}

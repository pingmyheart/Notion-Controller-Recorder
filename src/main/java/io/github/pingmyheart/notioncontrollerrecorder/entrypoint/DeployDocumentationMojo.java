package io.github.pingmyheart.notioncontrollerrecorder.entrypoint;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.pingmyheart.notioncontrollerrecorder.configuration.Context;
import io.github.pingmyheart.notioncontrollerrecorder.dto.internal.ReportDTO;
import io.github.pingmyheart.notioncontrollerrecorder.service.NotionServiceImpl;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;

import java.io.File;
import java.io.IOException;

import static java.util.Objects.nonNull;

@Mojo(name = "deploy",
        defaultPhase = LifecyclePhase.PACKAGE,
        aggregator = true)
public class DeployDocumentationMojo extends AbstractMojo {
    @Parameter(defaultValue = "${project.build.directory}/generated-sources/notion-report.json", required = true)
    private File outputFile;
    @Parameter(defaultValue = "${project}",
            readonly = true,
            required = true)
    private MavenProject project;
    @Parameter(required = true)
    private String notionToken;
    @Parameter(required = true)
    private String notionPageId;

    private final NotionServiceImpl notionServiceImpl = Context.getNotionService();

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        if (!outputFile.exists()) {
            throw new MojoExecutionException("Output file does not exist: " + outputFile.getAbsolutePath());
        }

        try {
            ReportDTO reportDTO = new ObjectMapper().readValue(outputFile, ReportDTO.class);
            String projectPageId = notionServiceImpl.getOrCreateProjectId(notionToken, notionPageId, project.getArtifactId());
            if (nonNull(projectPageId)) {
                String projectVersionPageId = notionServiceImpl.getOrCreateProjectId(notionToken, projectPageId, project.getVersion());
                if (nonNull(projectVersionPageId)) {
                    notionServiceImpl.createDocumentation(notionToken, projectVersionPageId, reportDTO);
                }
            }
        } catch (IOException e) {
            throw new MojoExecutionException("Failed to read output file: " + outputFile.getAbsolutePath(), e);
        }
    }
}

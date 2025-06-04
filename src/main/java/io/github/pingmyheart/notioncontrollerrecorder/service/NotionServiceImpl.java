package io.github.pingmyheart.notioncontrollerrecorder.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.pingmyheart.notioncontrollerrecorder.dto.external.request.CreatePageContentNotionRequest;
import io.github.pingmyheart.notioncontrollerrecorder.dto.external.request.CreatePageNotionRequest;
import io.github.pingmyheart.notioncontrollerrecorder.dto.external.response.CreatePageNotionResponse;
import io.github.pingmyheart.notioncontrollerrecorder.dto.external.response.NotionBaseResponse;
import io.github.pingmyheart.notioncontrollerrecorder.dto.external.response.RetrieveBlocksNotionResponse;
import io.github.pingmyheart.notioncontrollerrecorder.dto.internal.ReportDTO;
import io.github.pingmyheart.notioncontrollerrecorder.dto.notion.PageDTO;
import lombok.Builder;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.apache.maven.plugin.MojoExecutionException;
import org.springframework.http.HttpMethod;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;

@RequiredArgsConstructor
@Builder
public class NotionServiceImpl implements NotionService {
    private final WebClientImpl notionWebClient;
    private final ObjectMapper objectMapper;

    /**
     * Retrieve page id from project name and if not found, create a new project.
     *
     * @param projectName the name of the project
     * @return the project id
     */
    @Override
    public String getOrCreateProjectId(String notionPageId,
                                       String projectName) {
        String projectId = getProjectId(notionPageId, projectName);
        if (isNull(projectId)) {
            projectId = createProject(notionPageId, projectName);
        }
        return projectId;
    }

    @SneakyThrows
    private List<Object> getPageBlocks(String notionPageId) {
        String nextCursor = null;
        List<Object> blocks = new ArrayList<>();
        do {
            String uri = isNull(nextCursor) ?
                    MessageFormat.format("/blocks/{0}/children", notionPageId) :
                    MessageFormat.format("/blocks/{0}/children?start_cursor={1}", notionPageId, nextCursor);
            RetrieveBlocksNotionResponse rsp = notionWebClient.exchange(uri,
                    HttpMethod.GET,
                    RetrieveBlocksNotionResponse.class,
                    null);
            if ("error".equals(rsp.getObject())) {
                throw new MojoExecutionException("Failed to retrieve project page in Notion: " + rsp.getMessage());
            }
            nextCursor = rsp.getNextCursor();
            blocks.addAll(rsp.getResults());
        } while (nonNull(nextCursor));
        return blocks;
    }

    @SneakyThrows
    private List<PageDTO> retrieveProjectPages(String notionPageId) {
        return getPageBlocks(notionPageId)
                .stream()
                .map(json -> {
                    Map<String, Object> block = objectMapper.convertValue(json, new TypeReference<>() {
                    });
                    if ("child_page".equals(block.get("type"))) {
                        return objectMapper.convertValue(block, PageDTO.class);
                    }
                    return null;
                })
                .filter(Objects::nonNull)
                .toList();
    }

    @SneakyThrows
    private String getProjectId(String notionPageId,
                                String projectName) {
        return retrieveProjectPages(notionPageId).stream()
                .filter(o -> o.getChildPage().getTitle().equals(projectName))
                .findFirst()
                .map(PageDTO::getId)
                .orElse(null);
    }

    @SneakyThrows
    private String createProject(String notionPageId,
                                 String projectName) {
        CreatePageNotionRequest request = CreatePageNotionRequest.builder()
                .parent(CreatePageNotionRequest.Parent.builder()
                        .pageId(notionPageId)
                        .build())
                .icon(CreatePageNotionRequest.Icon.builder()
                        .emoji("📁")
                        .build())
                .properties(CreatePageNotionRequest.Properties.builder()
                        .title(List.of(CreatePageNotionRequest.Title.builder()
                                .type("text")
                                .text(CreatePageNotionRequest.Text.builder()
                                        .content(projectName)
                                        .build())
                                .build()))
                        .build())
                .build();
        CreatePageNotionResponse response = notionWebClient.exchange("/pages",
                HttpMethod.POST,
                CreatePageNotionResponse.class,
                request);
        if ("error".equals(response.getObject())) {
            throw new MojoExecutionException("Failed to create project page in Notion: " + response.getMessage());
        }
        return response.getId();
    }

    /**
     * Create documentation in Notion.
     *
     * @param projectVersionPageId the page id of the project version
     * @param reportDTO            the report data transfer object containing the documentation
     */
    @Override
    public void createDocumentation(String projectVersionPageId,
                                    ReportDTO reportDTO) {
        deletePageContent(projectVersionPageId);
        List<CreatePageContentNotionRequest.GenericObject> genericObjects = new ArrayList<>();
        genericObjects.add(CreatePageContentNotionRequest.GenericObject.builder()
                .type("table_of_contents")
                .toc(CreatePageContentNotionRequest.GenericObject.TOC.builder()
                        .build())
                .build());
        reportDTO.getControllers()
                .forEach(controller -> {
                    genericObjects.add(CreatePageContentNotionRequest.GenericObject.builder()
                            .type("heading_1")
                            .heading1(CreatePageContentNotionRequest.GenericObject.ContentHolder.builder()
                                    .richText(List.of(CreatePageContentNotionRequest.GenericObject.RichText.builder()
                                            .type("text")
                                            .text(CreatePageContentNotionRequest.GenericObject.Text.builder()
                                                    .content(controller.getName())
                                                    .build())
                                            .build()))
                                    .build())
                            .build());
                    genericObjects.add(CreatePageContentNotionRequest.GenericObject.builder()
                            .type("paragraph")
                            .paragraph(CreatePageContentNotionRequest.GenericObject.ContentHolder.builder()
                                    .richText(List.of(CreatePageContentNotionRequest.GenericObject.RichText.builder()
                                                    .type("text")
                                                    .text(CreatePageContentNotionRequest.GenericObject.Text.builder()
                                                            .content("BasePath")
                                                            .build())
                                                    .annotations(CreatePageContentNotionRequest.GenericObject.Annotations.builder()
                                                            .bold(Boolean.TRUE)
                                                            .build())
                                                    .build(),
                                            CreatePageContentNotionRequest.GenericObject.RichText.builder()
                                                    .type("text")
                                                    .text(CreatePageContentNotionRequest.GenericObject.Text.builder()
                                                            .content(": ")
                                                            .build())
                                                    .build(),
                                            CreatePageContentNotionRequest.GenericObject.RichText.builder()
                                                    .type("text")
                                                    .text(CreatePageContentNotionRequest.GenericObject.Text.builder()
                                                            .content(controller.getBasePath())
                                                            .build())
                                                    .annotations(CreatePageContentNotionRequest.GenericObject.Annotations.builder()
                                                            .code(Boolean.TRUE)
                                                            .build())
                                                    .build()))
                                    .build())
                            .build());
                    controller.getEndpoints()
                            .forEach(endpoint -> {
                                genericObjects.add(CreatePageContentNotionRequest.GenericObject.builder()
                                        .type("heading_2")
                                        .heading2(CreatePageContentNotionRequest.GenericObject.ContentHolder.builder()
                                                .richText(List.of(CreatePageContentNotionRequest.GenericObject.RichText.builder()
                                                        .type("text")
                                                        .text(CreatePageContentNotionRequest.GenericObject.Text.builder()
                                                                .content(endpoint.getMethodName())
                                                                .build())
                                                        .build()))
                                                .build())
                                        .build());
                                genericObjects.add(createEndpointPart("Signature", endpoint.getMethodSignature()));
                                genericObjects.add(createEndpointPart("Endpoint", MessageFormat.format("{0} @ {1}",
                                        endpoint.getHttpMethod(),
                                        endpoint.getPath())));
                                genericObjects.add(createEndpointPart("Consumes", endpoint.getConsumes()));
                                genericObjects.add(createEndpointPart("Produces", endpoint.getProduces()));
                            });
                });

        // Create the page content request

        genericObjects.forEach(genericObject -> {
            CreatePageContentNotionRequest request = CreatePageContentNotionRequest.builder()
                    .children(List.of(genericObject))
                    .build();
            notionWebClient.exchange(MessageFormat.format("/blocks/{0}/children", projectVersionPageId),
                    HttpMethod.PATCH,
                    NotionBaseResponse.class,
                    request);
        });
        // Send the request to create the page content

    }

    private CreatePageContentNotionRequest.GenericObject createEndpointPart(String type,
                                                                            String content) {
        return CreatePageContentNotionRequest.GenericObject.builder()
                .paragraph(CreatePageContentNotionRequest.GenericObject.ContentHolder.builder()
                        .richText(List.of(CreatePageContentNotionRequest.GenericObject.RichText.builder()
                                        .type("text")
                                        .text(CreatePageContentNotionRequest.GenericObject.Text.builder()
                                                .content(type)
                                                .build())
                                        .annotations(CreatePageContentNotionRequest.GenericObject.Annotations.builder()
                                                .bold(Boolean.TRUE)
                                                .build())
                                        .build(),
                                CreatePageContentNotionRequest.GenericObject.RichText.builder()
                                        .type("text")
                                        .text(CreatePageContentNotionRequest.GenericObject.Text.builder()
                                                .content(": ")
                                                .build())
                                        .build(),
                                CreatePageContentNotionRequest.GenericObject.RichText.builder()
                                        .type("text")
                                        .text(CreatePageContentNotionRequest.GenericObject.Text.builder()
                                                .content(content)
                                                .build())
                                        .annotations(CreatePageContentNotionRequest.GenericObject.Annotations.builder()
                                                .code(Boolean.TRUE)
                                                .build())
                                        .build()))
                        .build())
                .build();
    }

    private void deletePageContent(String pageId) {
        getPageBlocks(pageId).forEach(pageBlock -> {
            Map<String, Object> block = objectMapper.convertValue(pageBlock, new TypeReference<>() {
            });
            notionWebClient.exchange(MessageFormat.format("/blocks/{0}", block.get("id")),
                    HttpMethod.DELETE,
                    NotionBaseResponse.class,
                    null);
        });
    }
}

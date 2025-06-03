package io.github.pingmyheart.notioncontrollerrecorder.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
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
import org.apache.maven.plugin.logging.SystemStreamLog;
import org.springframework.http.HttpMethod;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

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
    }

    private void deletePageContent(String pageId) {
        ExecutorService executor = Executors.newFixedThreadPool(3);
        //retrieve all blocks in the page
        List<Object> blocks = getPageBlocks(pageId);
        while (!blocks.isEmpty()) {
            blocks.forEach(pageBlock -> {
                Map<String, Object> block = objectMapper.convertValue(pageBlock, new TypeReference<>() {
                });
                executor.submit(() -> {
                    var response = notionWebClient.exchange(MessageFormat.format("/blocks/{0}", block.get("id")),
                            HttpMethod.DELETE,
                            NotionBaseResponse.class,
                            null);
                    new SystemStreamLog().info(response.toString());
                });
            });
            blocks = getPageBlocks(pageId);
        }
    }
}

package io.github.pingmyheart.notioncontrollerrecorder.service;

import io.github.pingmyheart.notioncontrollerrecorder.dto.external.request.CreatePageNotionRequest;
import io.github.pingmyheart.notioncontrollerrecorder.dto.internal.ReportDTO;
import lombok.Builder;
import lombok.RequiredArgsConstructor;
import org.json.JSONObject;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;

import static java.util.Objects.isNull;

@RequiredArgsConstructor
@Builder
public class NotionServiceImpl implements NotionService {
    private static final String EMPTY_STRING = "";
    private final WebClient notionWebClient;

    /**
     * Retrieve page id from project name and if not found, create a new project.
     *
     * @param projectName
     * @return
     */
    @Override//TODO handle next cursor for pagination tio retrieve all pages
    public String getOrCreateProjectId(String notionToken,
                                       String notionPageId,
                                       String projectName) {
        String projectId = getProjectId(notionToken, notionPageId, projectName);
        if (projectId.equals(EMPTY_STRING)) {
            projectId = createProject(notionToken, notionPageId, projectName);
        }
        return projectId;
    }

    private String getProjectId(String notionToken,
                                String notionPageId,
                                String projectName) {
        String rsp = notionWebClient.get()
                .uri("/blocks/{notionPageId}/children", notionPageId)
                .header("Authorization", "Bearer " + notionToken)
                .exchangeToMono(clientResponse -> clientResponse.statusCode().is2xxSuccessful() ?
                        clientResponse.bodyToMono(String.class) :
                        Mono.just(EMPTY_STRING))
                .block();
        if (EMPTY_STRING.equals(rsp)) {
            return EMPTY_STRING;
        }
        AtomicReference<String> result = new AtomicReference<>(EMPTY_STRING);
        JSONObject jsonResponse = new JSONObject(rsp);
        if (jsonResponse.has("results")) {
            jsonResponse.getJSONArray("results")
                    .forEach(json -> {
                        JSONObject block = (JSONObject) json;
                        if (block.get("type").equals("child_page")) {
                            String pageTitle = block.getJSONObject("child_page").getString("title");
                            if (pageTitle.equals(projectName)) {
                                result.set(((JSONObject) json).getString("id"));
                            }
                        }
                    });
        }
        return result.get();
    }

    private String createProject(String notionToken,
                                 String notionPageId,
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
        String response = notionWebClient.post()
                .uri("/pages")
                .header("Authorization", "Bearer " + notionToken)
                .bodyValue(request)
                .exchangeToMono(clientResponse -> clientResponse.statusCode().is2xxSuccessful() ?
                        clientResponse.bodyToMono(String.class) :
                        Mono.just(EMPTY_STRING))
                .block();
        if (EMPTY_STRING.equals(response)) {
            return EMPTY_STRING;
        }
        JSONObject jsonResponse = new JSONObject(Objects.requireNonNull(response));
        return jsonResponse.getString("id");
    }

    /**
     * Create documentation in Notion.
     *
     * @param notionToken
     * @param projectVersionPageId
     * @param reportDTO
     */
    @Override
    public void createDocumentation(String notionToken,
                                    String projectVersionPageId,
                                    ReportDTO reportDTO) {

    }

    private void deletePageContent(String notionToken,
                                   String pageId) {
        String nextCursor = null;
        String uri = isNull(nextCursor) ?
                "/blocks/{pageId}/children" :
                "/blocks/{pageId}/children?start_cursor={nextCursor}";
        String response = notionWebClient.get()
                .uri(uri, pageId)
                .header("Authorization", "Bearer " + notionToken)
                .exchangeToMono(clientResponse -> clientResponse.statusCode().is2xxSuccessful() ?
                        clientResponse.bodyToMono(String.class) :
                        Mono.just(EMPTY_STRING))
                .block();
        if (!EMPTY_STRING.equals(response)) {
            JSONObject jsonResponse = new JSONObject(Objects.requireNonNull(response));
            jsonResponse.getJSONArray("results")
                    .forEach(json -> {
                        JSONObject block = (JSONObject) json;
                    });
        }
    }
}

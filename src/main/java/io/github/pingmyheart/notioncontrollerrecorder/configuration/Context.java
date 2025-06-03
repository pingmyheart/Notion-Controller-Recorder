package io.github.pingmyheart.notioncontrollerrecorder.configuration;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import io.github.pingmyheart.notioncontrollerrecorder.service.NotionServiceImpl;
import io.github.pingmyheart.notioncontrollerrecorder.service.WebClientImpl;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.concurrent.atomic.AtomicReference;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class Context {
    private static final AtomicReference<WebClient> notionWebClient = new AtomicReference<>();
    private static final AtomicReference<NotionServiceImpl> notionService = new AtomicReference<>();
    private static final AtomicReference<WebClientImpl> webClientImpl = new AtomicReference<>();
    private static final AtomicReference<ObjectMapper> objectMapper = new AtomicReference<>();
    private static final Object mutex = new Object();

    public static WebClient getNotionWebClient() {
        WebClient webClient = notionWebClient.get();
        if (webClient == null) {
            synchronized (mutex) {
                webClient = notionWebClient.get();
                if (webClient == null) {
                    webClient = WebClient.builder()
                            .baseUrl("https://api.notion.com/v1")
                            .defaultHeader("Notion-Version", "2022-06-28")
                            .defaultHeader("Content-Type", "application/json")
                            .defaultHeader("Authorization", "Bearer " + Environment.get("notionToken"))
                            .build();
                    notionWebClient.set(webClient);
                }
            }
        }
        return webClient;
    }

    public static NotionServiceImpl getNotionService() {
        NotionServiceImpl result = notionService.get();
        if (result == null) {
            synchronized (mutex) {
                result = notionService.get();
                if (result == null) {
                    result = NotionServiceImpl.builder()
                            .notionWebClient(getWebClientImpl())
                            .objectMapper(getObjectMapper())
                            .build();
                    notionService.set(result);
                }
            }
        }
        return result;
    }

    public static WebClientImpl getWebClientImpl() {
        WebClientImpl result = webClientImpl.get();
        if (result == null) {
            synchronized (mutex) {
                result = webClientImpl.get();
                if (result == null) {
                    result = WebClientImpl.builder()
                            .notionWebClient(getNotionWebClient())
                            .objectMapper(getObjectMapper())
                            .notionToken(Environment.get("notionToken"))
                            .build();
                }
            }
        }
        return result;
    }

    public static ObjectMapper getObjectMapper() {
        ObjectMapper result = objectMapper.get();
        if (result == null) {
            synchronized (mutex) {
                result = objectMapper.get();
                if (result == null) {
                    result = new ObjectMapper();
                    result.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
                    result.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
                    result.setSerializationInclusion(JsonInclude.Include.NON_NULL);
                    result.findAndRegisterModules();
                    objectMapper.set(result);
                }
            }
        }
        return result;
    }
}

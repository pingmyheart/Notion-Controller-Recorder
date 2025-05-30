package io.github.pingmyheart.notioncontrollerrecorder.configuration;

import io.github.pingmyheart.notioncontrollerrecorder.service.NotionServiceImpl;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.concurrent.atomic.AtomicReference;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class Context {
    private static final AtomicReference<WebClient> notionWebClient = new AtomicReference<>();
    private static final AtomicReference<NotionServiceImpl> notionService = new AtomicReference<>();
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
                            .notionWebClient(getNotionWebClient())
                            .build();
                    notionService.set(result);
                }
            }
        }
        return result;
    }
}

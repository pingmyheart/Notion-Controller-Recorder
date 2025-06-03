package io.github.pingmyheart.notioncontrollerrecorder.dto.external.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreatePageNotionRequest {
    private Parent parent;
    private Icon icon;
    private Properties properties;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Parent {
        @JsonProperty("page_id")
        private String pageId;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Icon {
        private String emoji;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Properties {
        private List<Title> title;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Title {
        private String type;
        private Text text;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Text {
        private String content;
    }
}

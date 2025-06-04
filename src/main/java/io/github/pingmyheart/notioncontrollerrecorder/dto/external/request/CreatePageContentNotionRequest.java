package io.github.pingmyheart.notioncontrollerrecorder.dto.external.request;

import com.fasterxml.jackson.annotation.JsonInclude;
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
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CreatePageContentNotionRequest {
    private List<GenericObject> children;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class GenericObject {
        private String object = "block";
        private String type;
        @JsonProperty("heading_1")
        private ContentHolder heading1;
        @JsonProperty("heading_2")
        private ContentHolder heading2;
        @JsonProperty("heading_3")
        private ContentHolder heading3;
        private ContentHolder paragraph;
        @JsonProperty("table_of_contents")
        private TOC toc;

        @Data
        @NoArgsConstructor
        @AllArgsConstructor
        @Builder
        public static class ContentHolder {
            @JsonProperty("rich_text")
            private List<RichText> richText;
        }

        @Data
        @NoArgsConstructor
        @AllArgsConstructor
        @Builder
        public static class RichText {
            private String type;
            private Text text;
            private Annotations annotations;
        }

        @Data
        @NoArgsConstructor
        @AllArgsConstructor
        @Builder
        public static class Text {
            private String content;
        }

        @Data
        @NoArgsConstructor
        @AllArgsConstructor
        @Builder
        public static class Annotations {
            @Builder.Default
            private Boolean bold = Boolean.FALSE;
            @Builder.Default
            private Boolean italic = Boolean.FALSE;
            @Builder.Default
            private Boolean strikethrough = Boolean.FALSE;
            @Builder.Default
            private Boolean underline = Boolean.FALSE;
            @Builder.Default
            private Boolean code = Boolean.FALSE;
            @Builder.Default
            private String color = "default";
        }

        @Data
        @AllArgsConstructor
        @Builder
        public static class TOC {
        }
    }
}

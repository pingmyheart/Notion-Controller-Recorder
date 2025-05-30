package io.github.pingmyheart.notioncontrollerrecorder.dto.notion;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PageDTO {
    private String object;
    private String id;
    private Parent parent;
    @JsonProperty("created_time")
    private Date createdTime;
    @JsonProperty("last_edited_time")
    private Date lastEditedTime;
    @JsonProperty("created_by")
    private CreatedBy createdBy;
    @JsonProperty("last_edited_by")
    private LastEditedBy lastEditedBy;
    @JsonProperty("has_children")
    private boolean hasChildren;
    private boolean archived;
    @JsonProperty("in_trash")
    private boolean inTrash;
    private String type;
    @JsonProperty("child_page")
    private ChildPage childPage;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ChildPage {
        private String title;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class CreatedBy {
        private String object;
        private String id;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class LastEditedBy {
        private String object;
        private String id;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Parent {
        private String type;
        @JsonProperty("page_id")
        private String pageIf;
    }
}



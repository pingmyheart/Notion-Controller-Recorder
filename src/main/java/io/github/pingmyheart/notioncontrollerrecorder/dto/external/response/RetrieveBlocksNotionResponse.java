package io.github.pingmyheart.notioncontrollerrecorder.dto.external.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@ToString(callSuper = true)
@Data
@EqualsAndHashCode(callSuper = true)
public class RetrieveBlocksNotionResponse extends NotionBaseResponse {
    @JsonProperty("next_cursor")
    private String nextCursor;
    @JsonProperty("has_more")
    private Boolean hasMore;
    @JsonProperty("type")
    private String type;
    private List<Object> results;
}

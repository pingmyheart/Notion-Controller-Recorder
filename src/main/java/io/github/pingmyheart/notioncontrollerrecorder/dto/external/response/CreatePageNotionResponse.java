package io.github.pingmyheart.notioncontrollerrecorder.dto.external.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@ToString(callSuper = true)
@Data
@EqualsAndHashCode(callSuper = true)
public class CreatePageNotionResponse extends NotionBaseResponse {
    private String id;
}

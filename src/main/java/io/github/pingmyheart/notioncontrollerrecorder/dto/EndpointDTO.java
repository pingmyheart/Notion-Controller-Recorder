package io.github.pingmyheart.notioncontrollerrecorder.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class EndpointDTO {
    private String methodName;
    private String path;
    private String produces;
    private String consumes;
    private String httpMethod;
}

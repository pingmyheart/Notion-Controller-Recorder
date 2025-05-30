package io.github.pingmyheart.notioncontrollerrecorder.dto.internal;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EndpointDTO {
    private String methodName;
    private String methodSignature;
    private String path;
    private String produces;
    private String consumes;
    private String httpMethod;
}

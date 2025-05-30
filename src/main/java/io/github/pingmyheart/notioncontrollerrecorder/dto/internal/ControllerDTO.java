package io.github.pingmyheart.notioncontrollerrecorder.dto.internal;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ControllerDTO {
    private String name;
    private String basePath;
    @Builder.Default
    private List<EndpointDTO> endpoints = new ArrayList<>();
}

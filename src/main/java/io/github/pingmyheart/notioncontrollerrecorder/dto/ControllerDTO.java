package io.github.pingmyheart.notioncontrollerrecorder.dto;

import lombok.Builder;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
@Builder
public class ControllerDTO {
    private String name;
    private String basePath;
    @Builder.Default
    private List<EndpointDTO> endpoints = new ArrayList<>();
}

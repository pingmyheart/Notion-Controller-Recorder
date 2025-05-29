package io.github.pingmyheart.notioncontrollerrecorder.dto;

import lombok.Builder;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
@Builder
public class ReportDTO {
    @Builder.Default
    private List<ControllerDTO> controllers = new ArrayList<>();
}

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
public class ReportDTO {
    @Builder.Default
    private List<ControllerDTO> controllers = new ArrayList<>();
}

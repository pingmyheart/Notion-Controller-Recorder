package io.github.pingmyheart.notioncontrollerrecorder.service;

import io.github.pingmyheart.notioncontrollerrecorder.dto.internal.ReportDTO;

public interface NotionService {
    String getOrCreateProjectId(String notionToken,
                                String notionPageId,
                                String projectName);

    void createDocumentation(String notionToken,
                             String projectVersionPageId,
                             ReportDTO reportDTO);
}

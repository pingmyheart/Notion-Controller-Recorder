package io.github.pingmyheart.notioncontrollerrecorder.service;

import io.github.pingmyheart.notioncontrollerrecorder.dto.internal.ReportDTO;

public interface NotionService {
    //TODO handle next cursor for pagination tio retrieve all pages
    String getOrCreateProjectId(String notionPageId,
                                String projectName);

    void createDocumentation(String projectVersionPageId,
                             ReportDTO reportDTO);
}

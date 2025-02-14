package com.project.foradhd.domain.board.business.service;

import com.project.foradhd.domain.board.persistence.enums.Report;

public interface PostReportService {
    void postReport(Long postId, Report reportType);
}

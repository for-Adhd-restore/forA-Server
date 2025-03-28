package com.project.foradhd.domain.board.business.service;

import com.project.foradhd.domain.board.persistence.entity.Post;
import com.project.foradhd.domain.board.persistence.enums.HandleReport;
import com.project.foradhd.domain.board.persistence.enums.Report;
import java.util.HashMap;
import java.util.List;

public interface PostReportService {
    void postReport(Long postId, Report reportType);

    void handleReport(Long postId, HandleReport handleReportType);

    List<Post> findReportedPostList();

    HashMap<Report, Integer> getReportTypeCounts(Post post);
}

package com.project.foradhd.domain.board.persistence.repository;

import com.project.foradhd.domain.board.persistence.entity.Post;
import com.project.foradhd.domain.board.persistence.entity.ReportPost;
import com.project.foradhd.domain.board.persistence.enums.Report;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface ReportPostRepository extends JpaRepository<ReportPost, Long> {

    Optional<ReportPost> findByPostAndReportType(Post post, Report reportType);
}

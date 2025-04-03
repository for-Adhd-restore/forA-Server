package com.project.foradhd.domain.board.persistence.repository;

import com.project.foradhd.domain.board.persistence.entity.Post;
import com.project.foradhd.domain.board.persistence.entity.ReportPost;
import com.project.foradhd.domain.board.persistence.enums.Report;
import io.lettuce.core.dynamic.annotation.Param;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

public interface ReportPostRepository extends JpaRepository<ReportPost, Long> {

    Optional<ReportPost> findByPostAndReportType(Post post, Report reportType);

//       최적화 했을 때... 이전에는 쿼리 9개 최적화 하면 8개..
    @Query("""
    select distinct p
    from Post p
    join fetch p.user
    where p.id in (
        select rp.post.id from ReportPost rp
    )
""")
    List<Post> findAllDistinctReportedPost();

    List<ReportPost> findByPost(Post post);

    @Modifying
    @Query("""
        delete from ReportPost r where r.post = :post
""")
    void deleteByPost(@Param("post")Post post);
}

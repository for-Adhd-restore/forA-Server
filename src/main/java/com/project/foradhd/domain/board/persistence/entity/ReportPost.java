package com.project.foradhd.domain.board.persistence.entity;
import com.project.foradhd.domain.board.persistence.enums.Report;
import com.project.foradhd.global.audit.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.*;

import com.project.foradhd.domain.user.persistence.entity.User;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "report_post")
public class ReportPost extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "report_post_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", referencedColumnName = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id", referencedColumnName = "post_id", nullable = false)
    private Post post;

    @Column(name = "report_type")
    @Enumerated(EnumType.STRING)
    private Report reportType;

}

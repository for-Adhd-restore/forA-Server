package com.project.foradhd.domain.board.persistence.entity;
import com.project.foradhd.domain.board.persistence.enums.Report;
import com.project.foradhd.global.audit.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.*;

import com.project.foradhd.domain.user.persistence.entity.User;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
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
    @JoinColumn(name = "post_id")
    private Post post;

    @Column(name = "report_type", nullable = false)
    @Enumerated(EnumType.STRING)
    private Report reportType;

    @Builder.Default
    @Column(name = "report_count", nullable = false)
    private int reportCount = 1;

    public void increaseCount(){
        this.reportCount += 1;
    }
}

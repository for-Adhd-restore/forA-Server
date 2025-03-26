package com.project.foradhd.domain.board.persistence.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.project.foradhd.domain.user.persistence.entity.User;
import com.project.foradhd.global.audit.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.ColumnDefault;

import java.util.ArrayList;
import java.util.List;

@Getter
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "comment")
public class Comment extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "comment_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id", referencedColumnName = "post_id")
    @JsonIgnore
    private Post post; // 이 댓글이 속한 게시글 id

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", referencedColumnName = "user_id")
    @JsonIgnore
    private User user; // 댓글 작성자 id

    @Column(nullable = false, columnDefinition = "longtext")
    private String content;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_comment_id", referencedColumnName = "comment_id")
    @JsonIgnore
    private Comment parentComment;

    @OneToMany(mappedBy = "parentComment", fetch = FetchType.LAZY, cascade = CascadeType.REMOVE, orphanRemoval = true)
    private List<Comment> childComments = new ArrayList<>();

    @Builder.Default
    @ColumnDefault("0")
    @Column(nullable = false)
    private Boolean anonymous = Boolean.FALSE;

    @Builder.Default
    @ColumnDefault("0")
    @Column(nullable = false)
    private Integer likeCount = 0;

    private String nickname;

    private String profileImage;

    @Column(nullable = false)
    private boolean deleted = false;

    @Column
    private String deletedMessage;
}

package com.project.foradhd.domain.board.persistence.entity;

import com.project.foradhd.domain.board.persistence.enums.Category;
import com.project.foradhd.domain.hospital.persistence.converter.StringListConverter;
import com.project.foradhd.domain.user.persistence.entity.User;
import com.project.foradhd.global.audit.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.*;
import java.util.List;

import org.hibernate.annotations.ColumnDefault;

@Getter
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "post")
public class Post extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "post_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", referencedColumnName = "user_id")
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(name = "category")
    private Category category;

    @OneToMany(mappedBy = "post", fetch = FetchType.LAZY, cascade = CascadeType.REMOVE)
    private List<Comment> comments;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false, columnDefinition = "longtext")
    private String content;

    private String nickname;

    private String profileImage;

    @Builder.Default
    @ColumnDefault("0")
    @Column(nullable = false)
    private Boolean anonymous = Boolean.FALSE;

    @Builder.Default
    @Convert(converter = StringListConverter.class)
    @Column(columnDefinition = "varchar(1000)")
    private List<String> images = List.of();

    @Builder.Default
    @ColumnDefault("0")
    @Column(nullable = false)
    private Integer likeCount = 0;

    @Builder.Default
    @ColumnDefault("0")
    @Column(nullable = false)
    private Integer commentCount = 0;

    @Builder.Default
    @ColumnDefault("0")
    @Column(nullable = false)
    private Integer scrapCount = 0;

    @Builder.Default
    @ColumnDefault("0")
    @Column(nullable = false)
    private Integer viewCount = 0;

    public void incrementLikeCount() {
        this.likeCount++;
    }

    public void decrementLikeCount() {
        if (this.likeCount > 0) this.likeCount--;
    }

    public void incrementScrapCount() {
        this.scrapCount++;
    }

    public void decrementScrapCount() {
        if (this.scrapCount > 0) this.scrapCount--;
    }

    public void incrementViewCount() {
        this.viewCount++;
    }

    public void setUser(User user) {
        this.user = user;
    }
}

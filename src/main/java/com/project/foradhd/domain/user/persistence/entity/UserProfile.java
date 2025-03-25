package com.project.foradhd.domain.user.persistence.entity;

import com.project.foradhd.domain.user.persistence.enums.ForAdhdType;
import com.project.foradhd.global.audit.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.GenericGenerator;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
public class UserProfile extends BaseTimeEntity {

    @Id
    @GenericGenerator(name = "uuid-generator", type = com.project.foradhd.global.util.UUIDGenerator.class)
    @GeneratedValue(generator = "uuid-generator")
    @Column(name = "user_profile_id", columnDefinition = "varchar(32)")
    private String id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @Column(nullable = false, unique = true, length = 50)
    private String nickname;

    private String profileImage;

    @Enumerated(value = EnumType.STRING)
    @Column(nullable = false)
    private ForAdhdType forAdhdType;

    public void updateProfile(UserProfile newUserProfile) {
        this.nickname = newUserProfile.getNickname();
        this.profileImage = newUserProfile.getProfileImage();
        this.forAdhdType = newUserProfile.getForAdhdType();
    }

    public void withdraw() {
        this.nickname = "withdrawn_" + this.id;
        this.profileImage = "";
    }
}

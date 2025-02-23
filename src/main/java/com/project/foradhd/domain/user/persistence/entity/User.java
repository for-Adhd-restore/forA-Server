package com.project.foradhd.domain.user.persistence.entity;

import com.project.foradhd.domain.board.persistence.enums.HandleReport;
import com.project.foradhd.domain.user.persistence.enums.Role;
import com.project.foradhd.global.audit.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.GenericGenerator;

import java.time.LocalDateTime;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
public class User extends BaseTimeEntity {

    @Id
    @GenericGenerator(name = "uuid-generator", type = com.project.foradhd.global.util.UUIDGenerator.class)
    @GeneratedValue(generator = "uuid-generator")
    @Column(name = "user_id", columnDefinition = "varchar(32)")
    private String id;

    @Column(nullable = false, length = 100)
    private String email;

    @Builder.Default
    @Enumerated(value = EnumType.STRING)
    @Column(nullable = false)
    private Role role = Role.ANONYMOUS;

    @Enumerated(value = EnumType.STRING)
    private HandleReport status;

    @Builder.Default
    @ColumnDefault(value = "0")
    @Column(nullable = false)
    private Boolean isVerifiedEmail = Boolean.FALSE;

    @Builder.Default
    @ColumnDefault("0")
    @Column(nullable = false)
    private Boolean deleted = Boolean.FALSE;

    private LocalDateTime deletedAt;

    public String getAuthority() {
        return this.role.getName();
    }

    public void updateEmail(String email) {
        this.email = email;
    }

    public void updateAsUserRole(boolean hasProfile) {
        if (hasProfile && isVerifiedEmail) {
            this.role = Role.USER;
        }
    }

    public void updateAsUserRole(boolean isVerifiedEmail, boolean hasProfile) {
        this.isVerifiedEmail = isVerifiedEmail;
        if (hasProfile && isVerifiedEmail) {
            this.role = Role.USER;
        }
    }

    public void withdraw() {
        this.email = "";
        this.role = Role.ANONYMOUS;
        this.isVerifiedEmail = false;
        this.deleted = true;
        this.deletedAt = LocalDateTime.now();
    }

    public void updateUserStatus(HandleReport handleReportType){
        this.status = handleReportType;
    }
}

package com.project.foradhd.domain.medicine.persistence.entity;

import com.project.foradhd.domain.hospital.persistence.converter.StringListConverter;
import com.project.foradhd.domain.user.persistence.entity.User;
import com.project.foradhd.domain.user.persistence.enums.Gender;
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
@Table(name = "medicine_review")
public class MedicineReview extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "medicine_review_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "medicine_id")
    private Medicine medicine;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @Column(nullable = false, length = 50)
    private String ageRange;

    @Builder.Default
    @Enumerated(value = EnumType.STRING)
    @Column(nullable = false)
    private Gender gender = Gender.UNKNOWN;

    @Column(nullable = false, length = 50)
    private String nickname;

    @Column(nullable = false)
    private String profileImage;

    @OneToMany(mappedBy = "review", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<MedicineCoMedication> coMedications = new ArrayList<>();

    @Column(nullable = false, columnDefinition = "longtext")
    private String content;

    @Builder.Default
    @Convert(converter = StringListConverter.class)
    @Column(length = 1000)
    private List<String> images = List.of();

    @Column(nullable = false)
    private Double grade;

    @Builder.Default
    @ColumnDefault("0")
    @Column(nullable = false)
    private Integer helpCount = 0;

    public void incrementHelpCount() {
        this.helpCount++;
    }

    public void decrementHelpCount() {
        if (this.helpCount > 0) this.helpCount--;
    }
}

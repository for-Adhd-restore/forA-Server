package com.project.foradhd.domain.medicine.web.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.project.foradhd.domain.user.persistence.enums.Gender;
import com.project.foradhd.global.paging.web.dto.response.PagingResponse;
import com.project.foradhd.global.serializer.LocalDateTimeToEpochSecondSerializer;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MedicineReviewResponse {

    private Long id;
    private String userId;
    private Long medicineId;
    private String content;
    private List<String> images;
    private float grade;
    private int helpCount;
    private List<CoMedicationResponse> coMedications;
    private String nickname;
    private String profileImage;
    private String ageRange;
    private Gender gender;
    private double averageGrade;
    private String medicineName;
    @JsonProperty("isAuthor")
    private boolean isAuthor;
    @JsonSerialize(using = LocalDateTimeToEpochSecondSerializer.class)
    private LocalDateTime createdAt;
    @JsonSerialize(using = LocalDateTimeToEpochSecondSerializer.class)
    private LocalDateTime lastModifiedAt;

    @Getter
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class CoMedicationResponse {
        private Long id;
        private String name;
    }

    @Getter
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class PagedMedicineReviewResponse {
        private List<MedicineReviewResponse> data;
        private PagingResponse paging;
    }
}

package com.project.foradhd.domain.medicine.web.mapper;

import com.project.foradhd.domain.medicine.persistence.entity.Medicine;
import com.project.foradhd.domain.medicine.persistence.entity.MedicineCoMedication;
import com.project.foradhd.domain.medicine.persistence.entity.MedicineReview;
import com.project.foradhd.domain.medicine.persistence.repository.MedicineRepository;
import com.project.foradhd.domain.medicine.web.dto.request.MedicineReviewRequest;
import com.project.foradhd.domain.medicine.web.dto.response.MedicineReviewResponse;
import com.project.foradhd.domain.user.business.service.UserService;
import com.project.foradhd.domain.user.persistence.entity.User;
import com.project.foradhd.domain.user.persistence.entity.UserPrivacy;
import com.project.foradhd.domain.user.persistence.entity.UserProfile;
import com.project.foradhd.domain.user.web.mapper.UserMapper;
import com.project.foradhd.global.exception.BusinessException;
import com.project.foradhd.global.exception.ErrorCode;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import org.mapstruct.*;

import java.util.List;

@Mapper(componentModel = "spring", uses = {MedicineMapper.class, UserMapper.class})
public interface MedicineReviewMapper {

    @Mapping(target = "medicine", ignore = true)
    @Mapping(target = "user", ignore = true)
    @Mapping(target = "coMedications", ignore = true)
    @Mapping(target = "ageRange", ignore = true)
    @Mapping(target = "gender", ignore = true)
    MedicineReview toEntity(MedicineReviewRequest request);

    @Mapping(target = "medicineId", source = "medicine.id")
    @Mapping(target = "userId", source = "user.id")
    @Mapping(target = "coMedications", expression = "java(mapCoMedications(review.getCoMedications()))") // 리스트 매핑
    MedicineReviewResponse toResponseDto(MedicineReview review, @Context UserService userService, @Context String currentUserId);

    // ✅ coMedications 매핑 메서드 추가 (List<MedicineCoMedication> → List<Long>)
    default List<MedicineReviewResponse.CoMedicationResponse> mapCoMedications(List<MedicineCoMedication> coMedications) {
        if (coMedications == null) return List.of();
        return coMedications.stream()
                .map(coMedication -> MedicineReviewResponse.CoMedicationResponse.builder()
                        .id(coMedication.getMedicine().getId())
                        .name(coMedication.getMedicine().getItemName())
                        .build())
                .toList();
    }


    @AfterMapping
    default void setUserAndMedicine(@MappingTarget MedicineReview.MedicineReviewBuilder reviewBuilder,
                                    MedicineReviewRequest request,
                                    @Context User user,
                                    @Context Medicine medicine,
                                    @Context MedicineRepository medicineRepository) {
        // ✅ 빌더 패턴을 유지하면서 User, Medicine 설정
        reviewBuilder
                .user(user)
                .medicine(medicine);

        // ✅ coMedications 변환 (List<Long> → List<MedicineCoMedication>)
        List<MedicineCoMedication> coMedications = request.getCoMedications().stream()
                .map(medicineId -> {
                    Medicine coMedicine = medicineRepository.findById(medicineId)
                            .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND_MEDICINE));
                    return MedicineCoMedication.builder().medicine(coMedicine).build();
                })
                .toList();

        reviewBuilder.coMedications(coMedications);
    }

    @AfterMapping
    default void setUserProfileDetails(@MappingTarget MedicineReviewResponse.MedicineReviewResponseBuilder responseBuilder,
                                       MedicineReview review,
                                       @Context UserService userService,
                                       @Context String currentUserId) {
        // ✅ 유저 ID를 사용해 프로필 정보 가져오기
        UserProfile userProfile = userService.getUserProfile(review.getUser().getId());
        UserPrivacy userPrivacy = userService.getUserPrivacy(review.getUser().getId());

        if (userProfile != null) {
            responseBuilder.nickname(userProfile.getNickname());
            responseBuilder.profileImage(userProfile.getProfileImage());
        }
        if (userPrivacy != null) {
            responseBuilder.ageRange(userPrivacy.getAgeRange());
            responseBuilder.gender(userPrivacy.getGender());
        }
        responseBuilder.isAuthor(review.getUser().getId().equals(currentUserId));
    }

    @AfterMapping
    default void setMedicineAndCoMedications(@MappingTarget MedicineReview.MedicineReviewBuilder reviewBuilder,
                                             MedicineReviewRequest request,
                                             @Context MedicineRepository medicineRepository) {
        // ✅ coMedications 변환
        List<MedicineCoMedication> coMedications = request.getCoMedications().stream()
                .map(medicineId -> {
                    Medicine coMedicine = medicineRepository.findById(medicineId)
                            .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND_MEDICINE));
                    return MedicineCoMedication.builder()
                            .medicine(coMedicine)
                            .build();
                })
                .toList();
        reviewBuilder.coMedications(coMedications);
    }

    @AfterMapping
    default void setMedicineId(@MappingTarget MedicineReviewResponse.MedicineReviewResponseBuilder responseBuilder, MedicineReview review) {
        responseBuilder.medicineId(review.getMedicine().getId());
    }
}


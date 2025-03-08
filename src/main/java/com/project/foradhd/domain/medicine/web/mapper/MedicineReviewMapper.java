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

    @Mapping(target = "medicine", ignore = true) // medicine은 수동으로 설정
    @Mapping(target = "user", ignore = true) // user도 수동 설정
    @Mapping(target = "coMedications", ignore = true) // coMedications도 수동 설정
    @Mapping(target = "ageRange", ignore = true)
    @Mapping(target = "gender", ignore = true)
    MedicineReview toEntity(MedicineReviewRequest request);

    @Mapping(target = "medicineId", source = "medicine.id") // Medicine 객체의 ID 매핑
    @Mapping(target = "userId", source = "user.id") // User 객체의 ID 매핑
    @Mapping(target = "coMedications", expression = "java(mapCoMedications(review.getCoMedications()))") // 리스트 매핑
    MedicineReviewResponse toResponseDto(MedicineReview review, @Context UserService userService);

    // ✅ coMedications 매핑 메서드 추가 (List<MedicineCoMedication> → List<Long>)
    default List<Long> mapCoMedications(List<MedicineCoMedication> coMedications) {
        if (coMedications == null) return List.of();
        return coMedications.stream()
                .map(coMedication -> coMedication.getMedicine().getId())
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
                                       @Context UserService userService) {
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


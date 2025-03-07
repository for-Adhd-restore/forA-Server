package com.project.foradhd.domain.medicine.business.service.impl;

import com.project.foradhd.domain.board.persistence.enums.SortOption;
import com.project.foradhd.domain.medicine.business.service.MedicineReviewService;
import com.project.foradhd.domain.medicine.persistence.entity.Medicine;
import com.project.foradhd.domain.medicine.persistence.entity.MedicineCoMedication;
import com.project.foradhd.domain.medicine.persistence.entity.MedicineReview;
import com.project.foradhd.domain.medicine.persistence.entity.MedicineReviewLikeFilter;
import com.project.foradhd.domain.medicine.persistence.repository.MedicineRepository;
import com.project.foradhd.domain.medicine.persistence.repository.MedicineReviewLikeRepository;
import com.project.foradhd.domain.medicine.persistence.repository.MedicineReviewRepository;
import com.project.foradhd.domain.medicine.web.dto.request.MedicineReviewRequest;
import com.project.foradhd.domain.user.business.service.UserService;
import com.project.foradhd.domain.user.persistence.entity.User;
import com.project.foradhd.global.exception.BusinessException;
import com.project.foradhd.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@RequiredArgsConstructor
@Transactional(readOnly = true)
@Service
public class MedicineReviewServiceImpl implements MedicineReviewService {

    private final UserService userService;
    private final MedicineReviewRepository medicineReviewRepository;
    private final MedicineRepository medicineRepository;
    private final MedicineReviewLikeRepository medicineReviewLikeRepository;

    @Override
    @Transactional
    public MedicineReview createReview(MedicineReviewRequest request, String userId) {
        User user = userService.getUser(userId);
        Medicine medicine = medicineRepository.findById(request.getMedicineId())
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND_MEDICINE));

// ✅ 1. 리뷰 먼저 저장 (coMedications 없이)
        MedicineReview savedReview = medicineReviewRepository.save(
                MedicineReview.builder()
                        .medicine(medicine)
                        .user(user)
                        .content(request.getContent())
                        .grade(request.getGrade())
                        .images(request.getImages() != null ? request.getImages() : new ArrayList<>()) // null 방지
                        .coMedications(new ArrayList<>()) // 빈 리스트로 초기화
                        .build()
        );

// ✅ 2. coMedications 추가
        if (request.getCoMedications() != null && !request.getCoMedications().isEmpty()) {
            List<MedicineCoMedication> coMedicationEntities = new ArrayList<>();
            for (Long medicineId : request.getCoMedications()) {
                Medicine foundMedicine = medicineRepository.findById(medicineId)
                        .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND_MEDICINE));

                MedicineCoMedication coMedication = MedicineCoMedication.builder()
                        .review(savedReview) // ✅ review_id가 존재하는 상태
                        .medicine(foundMedicine)
                        .build();
                coMedicationEntities.add(coMedication);
            }

            // ✅ 3. savedReview에 coMedications 추가 후 다시 저장
            savedReview.getCoMedications().addAll(coMedicationEntities);
            medicineReviewRepository.save(savedReview); // ⭐️ 여기서 다시 저장
        }

        return savedReview;
    }

    @Override
    @Transactional
    public void toggleHelpCount(Long reviewId, String userId) {
        MedicineReview medicineReview = medicineReviewRepository.findById(reviewId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND_MEDICINE_REVIEW));
        User user = userService.getUser(userId);

        if (medicineReviewLikeRepository.existsByUserIdAndMedicineReviewId(userId, reviewId)) {
            medicineReviewLikeRepository.deleteByUserIdAndMedicineReviewId(userId, reviewId);
            medicineReview.decrementHelpCount();
        } else {
            MedicineReviewLikeFilter newLike = MedicineReviewLikeFilter.builder()
                    .user(user)
                    .medicineReview(medicineReview)
                    .build();
            medicineReviewLikeRepository.save(newLike);
            medicineReview.incrementHelpCount();
        }
    }

    @Override
    @Transactional
    public MedicineReview updateReview(Long reviewId, MedicineReviewRequest request, String userId) {
        MedicineReview existingReview = medicineReviewRepository.findById(reviewId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND_MEDICINE_REVIEW));

        // 리뷰 작성자와 요청한 유저가 같은지 확인
        if (!existingReview.getUser().getId().equals(userId)) {
            throw new BusinessException(ErrorCode.FORBIDDEN_MEDICINE_REVIEW);
        }

        Medicine medicine = medicineRepository.findById(request.getMedicineId())
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND_MEDICINE));

        // ✅ 기존 coMedications 삭제 (먼저 새로운 객체를 만든 후 추가해야 lambda 문제 없음)
        List<MedicineCoMedication> coMedications = request.getCoMedications().stream()
                .map(medicineId -> {
                    Medicine coMedicine = medicineRepository.findById(medicineId)
                            .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND_MEDICINE));
                    return MedicineCoMedication.builder()
                            .review(existingReview)  // 기존 객체 사용 가능
                            .medicine(coMedicine)
                            .build();
                })
                .toList();

        // ✅ 새 `updatedReview` 객체 생성 (기존 review 변경 X)
        MedicineReview updatedReview = existingReview.toBuilder()
                .medicine(medicine)
                .coMedications(coMedications)  // 새 coMedications 추가
                .content(request.getContent())
                .images(request.getImages())
                .grade(request.getGrade())
                .build();

        MedicineReview savedReview = medicineReviewRepository.save(updatedReview);

        // ✅ 약의 평균 별점 업데이트
        updateMedicineRating(medicine);

        return savedReview; // DTO 변환 없이 엔티티를 반환
    }



    @Override
    @Transactional
    public void deleteReview(Long reviewId, String userId) {
        // 리뷰가 존재하는지 확인
        MedicineReview medicineReview = medicineReviewRepository.findById(reviewId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND_MEDICINE_REVIEW));

        // 리뷰 작성자와 요청한 유저가 동일한지 확인
        if (!medicineReview.getUser().getId().equals(userId)) {
            throw new BusinessException(ErrorCode.FORBIDDEN_MEDICINE_REVIEW);
        }

        // 리뷰 삭제
        medicineReviewRepository.deleteById(reviewId);

        // 약물의 평균 별점을 업데이트
        Medicine medicine = medicineReview.getMedicine();
        updateMedicineRating(medicine);
    }

    @Override
    public Page<MedicineReview> findReviews(Pageable pageable) {
        return medicineReviewRepository.findAll(pageable);
    }


    @Override
    public Page<MedicineReview> findReviewsByUserId(String userId, Pageable pageable, SortOption sortOption) {
        Sort sort = getSortByOption(sortOption);
        Pageable sortedPageable = PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), sort);
        return medicineReviewRepository.findByUserIdWithDetails(userId, sortedPageable); // 엔티티를 반환
    }

    @Override
    public Page<MedicineReview> findReviewsByMedicineId(Long medicineId, Pageable pageable, SortOption sortOption) {
        Sort sort = getSortByOption(sortOption);
        Pageable sortedPageable = PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), sort);
        return medicineReviewRepository.findByMedicineIdWithDetails(medicineId, sortedPageable); // 엔티티를 반환
    }

    private void updateMedicineRating(Medicine medicine) {
        double averageGrade = medicine.calculateAverageGrade();
        Medicine updatedMedicine = medicine.toBuilder()
                .rating(averageGrade)
                .build();
        medicineRepository.save(updatedMedicine);
    }

    private Sort getSortByOption(SortOption sortOption) {
        switch (sortOption) {
            case NEWEST_FIRST:
                return Sort.by(Sort.Direction.DESC, "createdAt");
            case OLDEST_FIRST:
                return Sort.by(Sort.Direction.ASC, "createdAt");
            case HIGHEST_GRADE:
                return Sort.by(Sort.Direction.DESC, "grade");
            case LOWEST_GRADE:
                return Sort.by(Sort.Direction.ASC, "grade");
            default:
                return Sort.by(Sort.Direction.DESC, "createdAt");
        }
    }
}

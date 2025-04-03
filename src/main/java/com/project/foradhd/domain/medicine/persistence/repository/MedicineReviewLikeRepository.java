package com.project.foradhd.domain.medicine.persistence.repository;

import com.project.foradhd.domain.medicine.persistence.entity.MedicineReviewLikeFilter;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MedicineReviewLikeRepository extends JpaRepository<MedicineReviewLikeFilter, Long> {

    boolean existsByUserIdAndMedicineReviewId(String userId, Long reviewId);
    void deleteByUserIdAndMedicineReviewId(String userId, Long reviewId);
    void deleteByMedicineReviewId(Long reviewId);
}

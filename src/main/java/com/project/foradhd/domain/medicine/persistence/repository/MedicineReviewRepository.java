package com.project.foradhd.domain.medicine.persistence.repository;

import com.project.foradhd.domain.medicine.persistence.entity.MedicineReview;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface MedicineReviewRepository extends JpaRepository<MedicineReview, Long> {

    // 리뷰 ID로 유저 프로필 및 개인정보와 함께 조회
    @Query("""
        SELECT r FROM MedicineReview r
        JOIN FETCH r.user u
        JOIN UserProfile up ON up.user.id = u.id
        JOIN UserPrivacy uv ON uv.user.id = u.id
        WHERE r.id = :reviewId
        """)
    Optional<MedicineReview> findByIdWithUserDetails(@Param("reviewId") Long reviewId);

    // 특정 유저의 모든 리뷰를 유저 프로필 및 개인정보와 함께 조회
    @Query("""
        SELECT r FROM MedicineReview r
        JOIN FETCH r.user u
        JOIN UserProfile up ON up.user.id = u.id
        JOIN UserPrivacy uv ON uv.user.id = u.id
        WHERE u.id = :userId
        """)
    Page<MedicineReview> findByUserIdWithDetails(@Param("userId") String userId, Pageable pageable);

    // 특정 약물의 모든 리뷰를 유저 프로필 및 개인정보와 함께 조회
    @Query("SELECT r FROM MedicineReview r WHERE r.medicine.id = :medicineId")
    @EntityGraph(attributePaths = {
            "user", "medicine", "coMedications.medicine"
    })
    Page<MedicineReview> findByMedicineIdWithDetails(@Param("medicineId") Long medicineId, Pageable pageable);

    // 모든 리뷰를 유저 프로필 및 개인정보와 함께 조회
    @Query("""
        SELECT r FROM MedicineReview r
        JOIN FETCH r.user u
        JOIN UserProfile up ON up.user.id = u.id
        JOIN UserPrivacy uv ON uv.user.id = u.id
        """)
    Page<MedicineReview> findAllWithUserDetails(Pageable pageable);
}

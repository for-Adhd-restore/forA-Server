package com.project.foradhd.domain.medicine.web.controller;

import com.project.foradhd.domain.board.persistence.enums.SortOption;
import com.project.foradhd.domain.medicine.business.service.MedicineReviewService;
import com.project.foradhd.domain.medicine.persistence.entity.MedicineReview;
import com.project.foradhd.domain.medicine.web.dto.request.MedicineReviewRequest;
import com.project.foradhd.domain.medicine.web.dto.response.MedicineReviewResponse;
import com.project.foradhd.domain.medicine.web.mapper.MedicineReviewMapper;
import com.project.foradhd.domain.user.business.service.UserService;
import com.project.foradhd.global.AuthUserId;
import com.project.foradhd.global.paging.web.dto.response.PagingResponse;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@AllArgsConstructor
@RequestMapping("/api/v1/medicines/reviews")
public class MedicineReviewController {

    private final MedicineReviewService medicineReviewService;
    private final MedicineReviewMapper medicineReviewMapper;
    private final UserService userService;

    @PostMapping
    public ResponseEntity<MedicineReviewResponse> createReview(@RequestBody MedicineReviewRequest request, @AuthUserId String userId) {
        MedicineReview review = medicineReviewService.createReview(request, userId);
        // 리뷰를 조회할 때마다 실시간으로 유저 정보를 매핑
        MedicineReviewResponse response = medicineReviewMapper.toResponseDto(review, userService, userId);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}")
    public ResponseEntity<MedicineReviewResponse> updateReview(@PathVariable Long id, @RequestBody MedicineReviewRequest request, @AuthUserId String userId) {
        MedicineReview review = medicineReviewService.updateReview(id, request, userId);
        MedicineReviewResponse response = medicineReviewMapper.toResponseDto(review, userService, userId);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{reviewId}")
    public ResponseEntity<Void> deleteReview(
            @PathVariable Long reviewId,
            @AuthUserId String userId) {
        medicineReviewService.deleteReview(reviewId, userId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping
    public ResponseEntity<MedicineReviewResponse.PagedMedicineReviewResponse> getReviews(
            @PageableDefault(sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable,
            @AuthUserId String userId) {
        Page<MedicineReview> reviews = medicineReviewService.findReviews(pageable);
        List<MedicineReviewResponse> reviewDtoList = reviews.stream()
                .map(review -> medicineReviewMapper.toResponseDto(review, userService, userId))
                .toList();

        PagingResponse pagingResponse = PagingResponse.from(reviews);
        MedicineReviewResponse.PagedMedicineReviewResponse response = MedicineReviewResponse.PagedMedicineReviewResponse.builder()
                .data(reviewDtoList)
                .paging(pagingResponse)
                .build();

        return ResponseEntity.ok(response);
    }

    @GetMapping("/my-reviews")
    public ResponseEntity<MedicineReviewResponse.PagedMedicineReviewResponse> getUserReviews(
            @AuthUserId String userId,
            @RequestParam(defaultValue = "NEWEST_FIRST") SortOption sortOption,
            @PageableDefault(sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {

        // MedicineReview 엔티티를 반환하도록 서비스 메서드 수정
        Page<MedicineReview> reviews = medicineReviewService.findReviewsByUserId(userId, pageable, sortOption);

        // 엔티티를 DTO로 변환
        List<MedicineReviewResponse> reviewDtos = reviews.stream()
                .map(review -> medicineReviewMapper.toResponseDto(review, userService, userId))
                .collect(Collectors.toList());

        PagingResponse pagingResponse = PagingResponse.from(reviews);
        MedicineReviewResponse.PagedMedicineReviewResponse response = MedicineReviewResponse.PagedMedicineReviewResponse.builder()
                .data(reviewDtos)
                .paging(pagingResponse)
                .build();

        return ResponseEntity.ok(response);
    }


    @GetMapping("/medicine/{medicineId}")
    public ResponseEntity<MedicineReviewResponse.PagedMedicineReviewResponse> getReviewsByMedicineId(
            @PathVariable Long medicineId,
            @RequestParam(defaultValue = "NEWEST_FIRST") SortOption sortOption,
            @PageableDefault(sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable, @AuthUserId String userId) {
        Page<MedicineReview> reviews = medicineReviewService.findReviewsByMedicineId(medicineId, pageable, sortOption);
        List<MedicineReviewResponse> reviewDtos = reviews.stream()
                .map(review -> medicineReviewMapper.toResponseDto(review, userService, userId))
                .collect(Collectors.toList());

        PagingResponse pagingResponse = PagingResponse.from(reviews);
        MedicineReviewResponse.PagedMedicineReviewResponse response = MedicineReviewResponse.PagedMedicineReviewResponse.builder()
                .data(reviewDtos)
                .paging(pagingResponse)
                .build();

        return ResponseEntity.ok(response);
    }

    @PostMapping("/{id}/help")
    public ResponseEntity<Void> toggleHelpCount(@PathVariable Long id, @AuthUserId String userId) {
        medicineReviewService.toggleHelpCount(id, userId);
        return ResponseEntity.ok().build();
    }
}

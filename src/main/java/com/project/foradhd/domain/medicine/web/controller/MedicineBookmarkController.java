package com.project.foradhd.domain.medicine.web.controller;

import com.project.foradhd.domain.medicine.business.service.MedicineBookmarkService;
import com.project.foradhd.domain.medicine.persistence.entity.MedicineBookmark;
import com.project.foradhd.domain.medicine.web.dto.response.MedicineBookmarkResponse;
import com.project.foradhd.domain.medicine.web.mapper.MedicineMapper;
import com.project.foradhd.global.AuthUserId;
import com.project.foradhd.global.paging.web.dto.response.PagingResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/medicine/bookmarks")
public class MedicineBookmarkController {
    @Autowired
    private MedicineBookmarkService medicineBookmarkService;

    @Autowired
    private MedicineMapper medicineMapper;

    // 약품 북마크 토글 (추가/제거)
    @PostMapping("/toggle")
    public ResponseEntity<?> toggleBookmark(
            @AuthUserId String userId,
            @RequestParam("medicineId") Long medicineId) {
        try {
            medicineBookmarkService.toggleBookmark(userId, medicineId);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }

    // 내가 북마크한 약 목록 조회
    @GetMapping("/my")
    public ResponseEntity<MedicineBookmarkResponse.PagedMedicineBookmarkResponse> getMyBookmarks(
            @AuthUserId String userId,
            @RequestParam(defaultValue = "newest") String sort,
            @PageableDefault(size = 10) Pageable pageable
    ) {
        Sort.Direction direction = sort.equalsIgnoreCase("oldest") ? Sort.Direction.ASC : Sort.Direction.DESC;
        Pageable sortedPageable = PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), Sort.by(direction, "createdAt"));

        Page<MedicineBookmark> bookmarks = medicineBookmarkService.getBookmarksByUser(userId, sortedPageable);
        List<MedicineBookmarkResponse> bookmarkResponses = bookmarks.map(medicineMapper::toResponseDto).getContent();

        PagingResponse pagingResponse = PagingResponse.from(bookmarks);
        MedicineBookmarkResponse.PagedMedicineBookmarkResponse response = MedicineBookmarkResponse.PagedMedicineBookmarkResponse.builder()
                .data(bookmarkResponses)
                .paging(pagingResponse)
                .build();

        return ResponseEntity.ok(response);
    }

}
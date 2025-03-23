package com.project.foradhd.domain.medicine.web.controller;

import com.project.foradhd.domain.medicine.business.service.MedicineSearchHistoryService;
import com.project.foradhd.domain.medicine.business.service.MedicineService;
import com.project.foradhd.domain.medicine.persistence.entity.Medicine;
import com.project.foradhd.domain.medicine.persistence.enums.IngredientType;
import com.project.foradhd.domain.medicine.persistence.enums.TabletType;
import com.project.foradhd.domain.medicine.web.dto.MedicineDto;
import com.project.foradhd.domain.medicine.web.dto.response.MedicineSearchResponse;
import com.project.foradhd.domain.medicine.web.dto.response.MedicineSortedResponse;
import com.project.foradhd.domain.medicine.web.mapper.MedicineMapper;
import com.project.foradhd.global.AuthUserId;
import com.project.foradhd.global.paging.web.dto.response.PagingResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/medicines")
@RequiredArgsConstructor
public class MedicineController {

    private final MedicineService medicineService;
    private final MedicineMapper medicineMapper;
    private final MedicineSearchHistoryService medicineSearchHistoryService;


   // 의약품 데이터를 가져와 저장하는 API
    @GetMapping("/fetch-and-save")
    public ResponseEntity<Object> fetchAndSaveMedicine(@RequestParam String itemname) {
        try {
            medicineService.saveMedicine(itemname);
            return ResponseEntity.ok(Map.of("message", "Medicine data successfully fetched and saved."));
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", "Failed to fetch or save medicine data", "details", e.getMessage()));
        }
    }

    // 의약품 목록을 정렬 옵션에 따라 조회하는 API
    @GetMapping("/sorted")
    public ResponseEntity<MedicineSortedResponse> getSortedMedicines(
            @RequestParam(defaultValue = "nameAsc") String sortOption,
            @AuthUserId String userId) {
        try {
            List<MedicineDto> medicines = medicineService.getSortedMedicines(sortOption, userId);
            MedicineSortedResponse response = MedicineSortedResponse.builder()
                    .kind("종류")
                    .medicineList(medicines)
                    .build();
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    // 약 모양, 색상, 제형 검색 api
    @GetMapping("/search")
    public ResponseEntity<MedicineSearchResponse> searchMedicines(
            @RequestParam(required = false) String shape,
            @RequestParam(required = false) String color1,
            @RequestParam(required = false) String formCodeName,
            @RequestParam(required = false) String itemName,
            @RequestParam(required = false) TabletType tabletType,
            @AuthUserId String userId,
            Pageable pageable) {

        List<Medicine> medicines;

        if (itemName != null) {
            medicines = medicineService.searchByItemName(itemName, userId);
            medicineSearchHistoryService.saveSearchTerm(userId, itemName); // 검색어 저장
        } else if (shape != null || color1 != null || formCodeName != null || tabletType != null) {
            medicines = medicineService.searchByFormCodeNameShapeColorAndTabletType(formCodeName, shape, color1, tabletType);
        } else {
            return ResponseEntity.badRequest().build();
        }

        List<MedicineSearchResponse.MedicineSearchListResponse> responseDtoList = medicines.stream()
                .map(medicineMapper::toMedicineSearchListResponse)
                .collect(Collectors.toList());

        PagingResponse pagingResponse = PagingResponse.from(pageable.getPageNumber(), pageable.getPageSize(), responseDtoList.size(), medicines.size());

        MedicineSearchResponse response = MedicineSearchResponse.builder()
                .data(responseDtoList)
                .paging(pagingResponse)
                .build();

        return ResponseEntity.ok(response);
    }

    // 개별 약 조회 API
    @GetMapping("/{id}")
    public ResponseEntity<MedicineDto> getMedicineById(@PathVariable Long id, @AuthUserId String userId) {
        MedicineDto medicine = medicineService.getMedicineById(id, userId);
        return ResponseEntity.ok(medicine);
    }

    // 약 성분별 조회
    @GetMapping("/sorted-by-ingredient")
    public ResponseEntity<MedicineSortedResponse> getMedicinesByIngredientType(
            @RequestParam IngredientType ingredientType) {
        try {
            List<MedicineDto> medicines = medicineService.getMedicinesByIngredientType(ingredientType);
            MedicineSortedResponse response = MedicineSortedResponse.builder()
                    .kind("종류")
                    .medicineList(medicines)
                    .build();
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    // 최근 검색어 조회 API
    @GetMapping("/recent-searches")
    public ResponseEntity<List<String>> getRecentSearchTerms(@AuthUserId String userId) {
        List<String> recentSearchTerms = medicineSearchHistoryService.getRecentSearchTerms(userId);
        return ResponseEntity.ok(recentSearchTerms);
    }

    // 특정 검색어 삭제
    @DeleteMapping("/recent-searches/{id}")
    public ResponseEntity<Void> deleteSearchTermById(@PathVariable Long id) {
        medicineSearchHistoryService.deleteSearchTermById(id);
        return ResponseEntity.noContent().build();
    }
}
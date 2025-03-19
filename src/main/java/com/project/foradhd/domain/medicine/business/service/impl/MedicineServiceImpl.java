package com.project.foradhd.domain.medicine.business.service.impl;

import com.nimbusds.jose.shaded.gson.*;
import com.project.foradhd.domain.medicine.business.service.MedicineSearchHistoryService;
import com.project.foradhd.domain.medicine.business.service.MedicineService;
import com.project.foradhd.domain.medicine.persistence.entity.Medicine;
import com.project.foradhd.domain.medicine.persistence.enums.IngredientType;
import com.project.foradhd.domain.medicine.persistence.enums.TabletType;
import com.project.foradhd.domain.medicine.persistence.repository.MedicineRepository;
import com.project.foradhd.domain.medicine.web.dto.MedicineDto;
import com.project.foradhd.domain.medicine.web.mapper.MedicineMapper;
import com.project.foradhd.global.exception.BusinessException;
import com.project.foradhd.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.List;
import java.util.stream.Collectors;
import java.util.Collections;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class MedicineServiceImpl implements MedicineService {

    private final MedicineRepository medicineRepository;
    private final MedicineMapper medicineMapper;
    private final MedicineSearchHistoryService medicineSearchHistoryService;

    @Value("${service.medicine.url}")
    private String SERVICE_URL;

    @Value("${service.medicine.key}")
    private String SERVICE_KEY;

    @Override
    @Transactional
    public void saveMedicine(String itemname) throws IOException {
        String json = fetchMedicineInfo(itemname);
        MedicineDto dto = parseMedicine(json);
        if (dto == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_MEDICINE);
        }
        Medicine medicine = medicineMapper.toEntity(dto);
        medicineRepository.save(medicine);
    }

    public String fetchMedicineInfo(String itemname) throws IOException {
        StringBuilder urlBuilder = new StringBuilder(SERVICE_URL);
        urlBuilder.append("?" + URLEncoder.encode("serviceKey", "UTF-8") + "=" + SERVICE_KEY);
        urlBuilder.append("&" + URLEncoder.encode("item_name", "UTF-8") + "=" + URLEncoder.encode(itemname, "UTF-8"));
        urlBuilder.append("&" + URLEncoder.encode("pageNo", "UTF-8") + "=1");
        urlBuilder.append("&" + URLEncoder.encode("numOfRows", "UTF-8") + "=10");
        urlBuilder.append("&" + URLEncoder.encode("type", "UTF-8") + "=json");

        URL url = new URL(urlBuilder.toString());
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");
        connection.setRequestProperty("Content-type", "application/json");

        BufferedReader rd;
        if (connection.getResponseCode() >= 200 && connection.getResponseCode() <= 300) {
            rd = new BufferedReader(new InputStreamReader(connection.getInputStream()));
        } else {
            rd = new BufferedReader(new InputStreamReader(connection.getErrorStream()));
        }

        StringBuilder sb = new StringBuilder();
        String line;
        while ((line = rd.readLine()) != null) {
            sb.append(line);
        }
        rd.close();
        connection.disconnect();

        return sb.toString();
    }

    public MedicineDto parseMedicine(String json) {
        try {
            Gson gson = new GsonBuilder().setLenient().create();
            JsonElement jsonElement = gson.fromJson(json, JsonElement.class);

            if (jsonElement.isJsonObject()) {
                JsonObject jsonResponse = jsonElement.getAsJsonObject();
                JsonArray items = jsonResponse.getAsJsonObject("body").getAsJsonArray("items");

                if (items != null && items.size() > 0) {
                    JsonObject item = items.get(0).getAsJsonObject();
                    return gson.fromJson(item, MedicineDto.class);
                }
            }
            return null;
        } catch (JsonSyntaxException e) {
            throw new BusinessException(ErrorCode.JSON_PARSE_ERROR);
        }
    }

    // 약 정렬
    @Override
    public List<MedicineDto> getSortedMedicines(String sortOption, String userId) {
        List<Medicine> medicines;

        if (sortOption.equalsIgnoreCase("MY_FAVORITES")) {
            medicines = medicineRepository.findMedicinesByUserFavorites(userId);
        } else {
            switch (sortOption) {
                case "nameAsc":
                    medicines = medicineRepository.findAllByOrderByItemNameAsc();
                    break;
                case "ratingDesc":
                    medicines = medicineRepository.findAllByOrderByRatingDesc();
                    break;
                case "ratingAsc":
                    medicines = medicineRepository.findAllByOrderByRatingAsc();
                    break;
                case "ingredientAsc":
                    List<String> ingredientNames = List.of("메틸페니데이트", "아토목세틴", "클로니딘");
                    medicines = medicineRepository.findByIngredientNames(ingredientNames);
                    break;
                default:
                    medicines = medicineRepository.findAll();
            }
        }

        if (medicines.isEmpty()) {
            return Collections.emptyList();
        } else {
            return medicines.stream()
                    .map(medicineMapper::toDto)
                    .collect(Collectors.toList());
        }
    }

    // 약 모양 or 색상 or 제형으로 검색
    @Override
    public List<Medicine> searchByFormCodeNameShapeColorAndTabletType(String formCodeName, String shape, String color1, TabletType tabletType) {
        return medicineRepository.findByAllAttributes(formCodeName, shape, color1, tabletType);
    }

    // 약 이름으로 검색
    @Override
    @Transactional
    public List<Medicine> searchByItemName(String itemName, String userId) {
        // 검색어 저장 로직 추가
        medicineSearchHistoryService.saveSearchTerm(userId, itemName);
        return medicineRepository.findByItemNameContaining(itemName);
    }

    // 개별 약 조회
    @Override
    public MedicineDto getMedicineById(Long id) {
        Medicine medicine = medicineRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND_MEDICINE));
        return medicineMapper.toDto(medicine);
    }

    // 약 성분별 정렬
    @Override
    public List<MedicineDto> getMedicinesByIngredientType(IngredientType ingredientType) {
        List<Medicine> medicines = medicineRepository.findAllByIngredientTypeOrderByItemNameAsc(ingredientType);
        return medicines.stream()
                .map(medicineMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<String> getRecentSearchTerms(String userId) {
        return medicineSearchHistoryService.getRecentSearchTerms(userId);
    }
}

package com.project.foradhd.domain.medicine.web.mapper;

import com.project.foradhd.domain.medicine.persistence.entity.Medicine;
import com.project.foradhd.domain.medicine.persistence.entity.MedicineBookmark;
import com.project.foradhd.domain.medicine.web.dto.MedicineDto;
import com.project.foradhd.domain.medicine.web.dto.response.MedicineBookmarkResponse;
import com.project.foradhd.domain.medicine.web.dto.response.MedicineResponse;
import com.project.foradhd.domain.medicine.web.dto.response.MedicineSearchResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

import java.util.List;

@Mapper(componentModel = "spring")
public interface MedicineMapper {
    Medicine toEntity(MedicineDto dto);

    @Mapping(source = "id", target = "medicineId")
    MedicineDto toDto(Medicine entity);

    List<Medicine> toEntityList(List<MedicineDto> dtoList);

    List<MedicineResponse> toDtoList(List<Medicine> entityList);

    MedicineSearchResponse toResponseDto(Medicine entity);
    List<MedicineSearchResponse> toResponseDtoList(List<Medicine> entityList);

    @Mapping(source = "bookmark.id", target = "id")
    @Mapping(source = "bookmark.medicine.itemName", target = "name")
    @Mapping(source = "bookmark.medicine.itemEngName", target = "engName")
    @Mapping(source = "bookmark.medicine.entpName", target = "manufacturer")
    @Mapping(source = "bookmark.medicine.itemImage", target = "images")
    @Mapping(source = "bookmark.createdAt", target = "bookmarkedAt", qualifiedByName = "toEpochTime")
    MedicineBookmarkResponse toResponseDto(MedicineBookmark bookmark);
    @Named("toEpochTime")
    default long toEpochTime(java.time.LocalDateTime createdAt) {
        return createdAt != null ? createdAt.toEpochSecond(java.time.ZoneOffset.UTC) : 0L;
    }

    @Mapping(source = "itemImage", target = "itemImage")
    @Mapping(source = "itemName", target = "itemName")
    @Mapping(source = "itemEngName", target = "itemEngName")
    @Mapping(source = "entpName", target = "entpName")
    @Mapping(source = "id", target = "medicineId")
    MedicineSearchResponse.MedicineSearchListResponse toMedicineSearchListResponse(Medicine medicine);
}

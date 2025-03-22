package com.project.foradhd.domain.medicine.business.service;

import com.project.foradhd.domain.medicine.persistence.entity.Medicine;
import com.project.foradhd.domain.medicine.persistence.enums.IngredientType;
import com.project.foradhd.domain.medicine.persistence.enums.TabletType;
import com.project.foradhd.domain.medicine.web.dto.MedicineDto;
import jakarta.persistence.Table;

import java.io.IOException;
import java.util.List;

public interface MedicineService {
    void saveMedicine(String itemname) throws IOException;
    String fetchMedicineInfo(String itemname) throws IOException;
    MedicineDto parseMedicine(String json);
    List<MedicineDto> getSortedMedicines(String sortOption, String userId);
    List<Medicine> searchByItemName(String itemName, String userId);
    MedicineDto getMedicineById(Long id, String userId);
    List<MedicineDto> getMedicinesByIngredientType(IngredientType ingredientType);
    List<Medicine> searchByFormCodeNameShapeColorAndTabletType(String formCodeName, String shape, String color1, TabletType tabletType);
    List<String> getRecentSearchTerms(String userId);
}
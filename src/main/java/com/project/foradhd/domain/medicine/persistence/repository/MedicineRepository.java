package com.project.foradhd.domain.medicine.persistence.repository;

import com.project.foradhd.domain.medicine.persistence.entity.Medicine;
import com.project.foradhd.domain.medicine.persistence.enums.IngredientType;
import com.project.foradhd.domain.medicine.persistence.enums.TabletType;
import com.project.foradhd.domain.medicine.web.dto.MedicineDto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MedicineRepository extends JpaRepository<Medicine, Long> {
    List<Medicine> findAllByOrderByItemNameAsc();
    List<Medicine> findAllByOrderByRatingDesc();
    List<Medicine> findAllByOrderByRatingAsc();
    List<Medicine> findByItemNameContainingOrderByItemNameAsc(String ingredient);
    List<Medicine> findAllByFormCodeNameOrDrugShapeOrColorClass1(String formCodeName, String drugShape, String color1);
    List<Medicine> findByItemNameContaining(String itemName);
    MedicineDto getMedicineById(Long id);
    List<Medicine> findAllByIngredientTypeOrderByItemNameAsc(IngredientType ingredientType);
    List<Medicine> findAllByFormCodeNameOrDrugShapeOrColorClass1OrTabletType(
            String formCodeName, String drugShape, String color1, TabletType tabletType
    );
    @Query("SELECT m FROM Medicine m JOIN MedicineBookmark mb ON m.id = mb.medicine.id WHERE mb.user.id = :userId AND mb.deleted = false")
    List<Medicine> findMedicinesByUserFavorites(@Param("userId") String userId);
    @Query("SELECT m FROM Medicine m WHERE m.itemName IN :ingredientNames ORDER BY m.itemName ASC")
    List<Medicine> findByIngredientNames(@Param("ingredientNames") List<String> ingredientNames);

    @Query("""
    SELECT m FROM Medicine m
    WHERE (:formCodeName IS NULL OR m.formCodeName = :formCodeName)
    AND (:drugShape IS NULL OR m.drugShape = :drugShape)
    AND (:color1 IS NULL OR 
         (m.colorClass1 IS NOT NULL AND m.colorClass1 = :color1) OR 
         (m.colorClass2 IS NOT NULL AND m.colorClass2 = :color1))
    AND (:tabletType IS NULL OR m.tabletType = :tabletType)
""")
    List<Medicine> findByAllAttributes(
            @Param("formCodeName") String formCodeName,
            @Param("drugShape") String drugShape,
            @Param("color1") String color1,
            @Param("tabletType") TabletType tabletType
    );
}

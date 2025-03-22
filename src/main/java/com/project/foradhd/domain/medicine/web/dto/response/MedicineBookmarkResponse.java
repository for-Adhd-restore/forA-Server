package com.project.foradhd.domain.medicine.web.dto.response;

import com.project.foradhd.global.paging.web.dto.response.PagingResponse;
import lombok.*;

import java.util.List;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MedicineBookmarkResponse {
    private Long id;
    private String name;
    private String engName;
    private String manufacturer;
    private String images;
    private long bookmarkedAt;
    private long medicineId;

    @Getter
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class PagedMedicineBookmarkResponse {
        private List<MedicineBookmarkResponse> data;
        private PagingResponse paging;
    }
}

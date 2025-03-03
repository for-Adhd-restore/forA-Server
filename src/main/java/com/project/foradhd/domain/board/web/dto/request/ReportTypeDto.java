package com.project.foradhd.domain.board.web.dto.request;
import com.project.foradhd.domain.board.persistence.enums.Report;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class ReportTypeDto {
    private Report reportType;  // Enum 타입
}

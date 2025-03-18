package com.project.foradhd.domain.board.web.dto.request;

import com.project.foradhd.domain.board.persistence.enums.HandleReport;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.web.bind.annotation.RequestBody;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class HandleReportRequest {

    private String email;
    private Long postId;
    private HandleReport handleReportType;

}

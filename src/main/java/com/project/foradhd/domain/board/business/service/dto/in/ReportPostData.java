package com.project.foradhd.domain.board.business.service.dto.in;

import com.project.foradhd.domain.board.persistence.entity.Post;
import com.project.foradhd.domain.board.persistence.enums.Report;
import java.util.HashMap;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ReportPostData {

    private Post post;

    private HashMap<Report, Integer> reportTypeCounts;

}

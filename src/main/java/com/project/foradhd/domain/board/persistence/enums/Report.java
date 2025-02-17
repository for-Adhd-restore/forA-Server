package com.project.foradhd.domain.board.persistence.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
public enum Report {
    INSULT_AND_DEGRADE("특정인에 대한 욕설 및 비하"),
    MISINFORMATION("잘못 된 정보"),
    PERSONAL_INFO_LEAK("개인정보 유출"),
    COMMERCIAL_ADVERTISEMENT("상업적 광고 및 판매글"),
    HATEFUL_CONTENT("타인에게 혐오감을 주는 게시글");

    private final String description;
    Report(String description) {
        this.description = description;
    }
}

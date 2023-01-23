package org.anotherclass.colortherock.domain.videoboard.response;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Builder
@Data
public class VideoBoardDetailResponse {
    private Long videoBoardId;
    private String nickname;
    private String title;
    private LocalDateTime writtenTime;

}

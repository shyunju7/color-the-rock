package org.anotherclass.colortherock.domain.videoboard.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.anotherclass.colortherock.domain.member.entity.Member;
import org.anotherclass.colortherock.domain.member.entity.MemberDetails;
import org.anotherclass.colortherock.domain.videoboard.request.SuccessPostUpdateRequest;
import org.anotherclass.colortherock.domain.videoboard.request.SuccessVideoUploadRequest;
import org.anotherclass.colortherock.domain.videoboard.request.VideoBoardSearchRequest;
import org.anotherclass.colortherock.domain.videoboard.response.VideoBoardDetailResponse;
import org.anotherclass.colortherock.domain.videoboard.response.VideoBoardSummaryResponse;
import org.anotherclass.colortherock.domain.videoboard.service.VideoBoardService;
import org.anotherclass.colortherock.global.common.BaseResponse;
import org.anotherclass.colortherock.global.error.GlobalErrorCode;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.util.List;

@RestController
@Slf4j
@RequiredArgsConstructor
@RequestMapping("/api/video")
public class VideoBoardController {

    private final VideoBoardService videoBoardService;

    /**
     * 완등 영상 게시글 전체 리스트 조회
     *
     * @param condition 현재 페이지의 마지막 아이디, 암장, 색상 검색 조건을 담고 있는 객체
     * @param pageable  Pageable 객체 (컨트롤러에서 생성)
     */
    @GetMapping("/board")
    @Operation(description = "완등 영상 전체 리스트 조회 API")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "완등 영상 목록 조회 성공", content = @Content(schema = @Schema(implementation = VideoBoardSummaryResponse.class)))
    })
    public BaseResponse<List<VideoBoardSummaryResponse>> getVideoList
    (VideoBoardSearchRequest condition, @PageableDefault(size = 16) Pageable pageable) {
        List<VideoBoardSummaryResponse> successVideoList = videoBoardService.getSuccessVideos(condition, pageable);
        return new BaseResponse<>(successVideoList);
    }

    /**
     * 내 운동기록 영상에서 가져와 완등 영상 게시판에 글 올리기
     */
    @PostMapping("/board")
    @Operation(description = "완등 영상 게시글 올리기(내 운동기록 동영상에서 영상 가져오기)")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "운동 영상 올리기 성공", content = @Content(schema = @Schema(implementation = Long.class))),
            @ApiResponse(responseCode = "404", description = "유저 정보를 찾을 수 없음"),
            @ApiResponse(responseCode = "404", description = "해당하는 영상을 찾을 수 없음")
    })
    public BaseResponse<Long> uploadMySuccessVideo(@AuthenticationPrincipal MemberDetails memberDetails, @Valid SuccessVideoUploadRequest successVideoUploadRequest) {
        Member member = memberDetails.getMember();
        Long videoBoardId = videoBoardService.uploadMySuccessVideoPost(member.getId(), successVideoUploadRequest);
        return new BaseResponse<>(videoBoardId);
    }

    @GetMapping("/board/detail")
    @Operation(description = "완등 영상 게시글 상세보기 API")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "완등 영상 상세 조회 성공", content = @Content(schema = @Schema(implementation = VideoBoardDetailResponse.class))),
            @ApiResponse(responseCode = "404", description = "해당하는 영상 게시글을 찾을 수 없음")
    })
    public BaseResponse<VideoBoardDetailResponse> getVideoDetail(@NotNull Long videoBoardId) {
        VideoBoardDetailResponse videoDetail = videoBoardService.getVideoDetail(videoBoardId);
        return new BaseResponse<>(videoDetail);
    }

    @PutMapping("/board/detail")
    @Operation(description = "완등 영상 게시글 수정하기 API")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "완등 영상 게시글 수정 성공"),
            @ApiResponse(responseCode = "404", description = "해당하는 영상 게시글을 찾을 수 없음"),
            @ApiResponse(responseCode = "404", description = "작성자와 유저 정보가 일치하지 않음")
    })
    public BaseResponse<?> updateSuccessPost(@AuthenticationPrincipal MemberDetails memberDetails, @Valid SuccessPostUpdateRequest successPostUpdateRequest) {
        Member member = memberDetails.getMember();
        videoBoardService.updateSuccessPost(member.getId(), successPostUpdateRequest);
        return new BaseResponse<>(GlobalErrorCode.SUCCESS);
    }

    @DeleteMapping("board/detail")
    @Operation(description = "완등 영상 게시글 삭제하기 API")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "완등 영상 게시글 삭제 성공"),
            @ApiResponse(responseCode = "404", description = "해당하는 영상 게시글을 찾을 수 없음"),
            @ApiResponse(responseCode = "404", description = "작성자와 유저 정보가 일치하지 않음")
    })
    public BaseResponse<?> deleteSuccessPost(@AuthenticationPrincipal MemberDetails memberDetails, @Valid Long videoBoardId) {
        Member member = memberDetails.getMember();
        videoBoardService.deleteSuccessPost(member.getId(), videoBoardId);
        return new BaseResponse<>(GlobalErrorCode.SUCCESS);
    }

    @GetMapping("board/mypost")
    @Operation(description = "내 완등 영상 게시글 목록 조회하기 API")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "내 완등 영상 글 목록 불러오기 성공")
    })
    public BaseResponse<List<VideoBoardSummaryResponse>> getMySuccessVideoPosts
            (@AuthenticationPrincipal MemberDetails memberDetails, Long storeId, @PageableDefault(size = 8, sort = "id") Pageable pageable) {
        Member member = memberDetails.getMember();
        List<VideoBoardSummaryResponse> mySuccessPosts = videoBoardService.getMySuccessVideoPosts(member.getId(), storeId, pageable);
        return new BaseResponse<>(mySuccessPosts);
    }


}
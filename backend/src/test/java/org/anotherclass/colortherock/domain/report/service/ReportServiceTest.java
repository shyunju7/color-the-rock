package org.anotherclass.colortherock.domain.report.service;

import org.anotherclass.colortherock.domain.member.entity.Member;
import org.anotherclass.colortherock.domain.member.repository.MemberRepository;
import org.anotherclass.colortherock.domain.report.entity.Report;
import org.anotherclass.colortherock.domain.report.exception.ReportOneselfException;
import org.anotherclass.colortherock.domain.report.repository.ReportReadRepository;
import org.anotherclass.colortherock.domain.report.repository.ReportRepository;
import org.anotherclass.colortherock.domain.report.request.PostReportRequest;
import org.anotherclass.colortherock.domain.video.entity.Video;
import org.anotherclass.colortherock.domain.video.repository.VideoRepository;
import org.anotherclass.colortherock.domain.videoboard.entity.VideoBoard;
import org.anotherclass.colortherock.domain.videoboard.exception.PostNotFoundException;
import org.anotherclass.colortherock.domain.videoboard.repository.VideoBoardRepository;
import org.anotherclass.colortherock.global.error.GlobalBaseException;
import org.anotherclass.colortherock.global.error.GlobalErrorCode;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
class ReportServiceTest {

    @Autowired
    private MemberRepository memberRepository;
    @Autowired
    private VideoRepository videoRepository;
    @Autowired
    private VideoBoardRepository videoBoardRepository;
    @Autowired
    private ReportService reportService;
    @Autowired
    private ReportRepository reportRepository;
    @Autowired
    private ReportReadRepository reportReadRepository;

    private ArrayList<Long> memberIds;
    private ArrayList<Long> videoBoardIds;

    @BeforeEach
    void setData() {
        memberIds = new ArrayList<>();
        videoBoardIds = new ArrayList<>();
        // Member, Video, VideoBoard ??????
        for (int i = 0; i < 10; i++) {
            Member member = new Member(i + "@rock.com", i + "user", Member.RegistrationId.kakao);
            memberRepository.save(member);
            memberIds.add(member.getId());
            Video video = Video.builder()
                    .shootingDate(LocalDate.parse("2022-01-30"))
                    .level(1)
                    .gymName("???????????????")
                    .s3URL("url")
                    .videoName("name")
                    .isSuccess(true)
                    .thumbnailURL("url")
                    .thumbnailName("name")
                    .color("??????")
                    .member(member)
                    .build();
            videoRepository.save(video);
            VideoBoard videoBoard = VideoBoard.builder()
                    .title("??? ?????????")
                    .isHidden(false)
                    .video(video)
                    .member(member)
                    .build();
            videoBoardRepository.save(videoBoard);
            videoBoardIds.add(videoBoard.getId());
        }

        // ?????? ??????
        for (int i = 1; i < 3; i++) {
            VideoBoard videoBoard = videoBoardRepository.findById(videoBoardIds.get(0))
                    .orElseThrow(() -> new PostNotFoundException(GlobalErrorCode.POST_NOT_FOUND));
            Member member = memberRepository.findById(memberIds.get(i))
                    .orElseThrow(() -> new GlobalBaseException(GlobalErrorCode.USER_NOT_FOUND));
            for (int j = 0; j < 2; j++) {
                Report report = new Report("TYPE_A", member, videoBoard);
                reportRepository.save(report);
            }
        }
    }

    @Test
    @DisplayName("????????? ?????? ??????")
    void reportPost() {
        // given
        Member member = memberRepository.findById(memberIds.get(3))
                .orElseThrow(() -> new GlobalBaseException(GlobalErrorCode.USER_NOT_FOUND));
        Long videoBoardId = videoBoardIds.get(0);
        String categoryName = "TYPE_B";
        PostReportRequest request = new PostReportRequest(videoBoardId, categoryName);
        // when
        reportService.reportPost(member, request);
        // then
        Long cnt = reportReadRepository.countReport(videoBoardId);
        assertEquals(3, cnt);
    }

    @Test
    @DisplayName("?????? 5??? ????????? ?????? ????????? hidden ??????")
    void hideReportPost() {
        // when
        VideoBoard videoBoard = videoBoardRepository.findById(videoBoardIds.get(0))
                .orElseThrow(() -> new PostNotFoundException(GlobalErrorCode.POST_NOT_FOUND));
        for (int i = 3; i < 6; i++) {
            Member member = memberRepository.findById(memberIds.get(i))
                    .orElseThrow(() -> new GlobalBaseException(GlobalErrorCode.USER_NOT_FOUND));
            PostReportRequest request = new PostReportRequest(videoBoardIds.get(0), "TYPE_A");
            reportService.reportPost(member, request);
        }
        // then
        Long cnt = reportReadRepository.countReport(videoBoard.getId());
        assertEquals(5, cnt);
        assertTrue(videoBoard.getIsHidden());
    }

    @Test
    @DisplayName("?????? 5??? ?????????????????? ?????? ????????? ???????????? ?????? ??????")
    void notEnoughReport() {
        // when
        VideoBoard videoBoard = videoBoardRepository.findById(videoBoardIds.get(0))
                .orElseThrow(() -> new PostNotFoundException(GlobalErrorCode.POST_NOT_FOUND));
        for (int i = 3; i < 5; i++) {
            Member member = memberRepository.findById(memberIds.get(i))
                    .orElseThrow(() -> new GlobalBaseException(GlobalErrorCode.USER_NOT_FOUND));
            PostReportRequest request = new PostReportRequest(videoBoardIds.get(0), "TYPE_A");
            reportService.reportPost(member, request);
        }
        // then
        Long cnt = reportReadRepository.countReport(videoBoard.getId());
        assertEquals(4, cnt);
        assertFalse(videoBoard.getIsHidden());
    }

    @Test
    @DisplayName("???????????? ????????? ?????? ?????? ??????")
    void reportOneself() {
        // when
        Member member = memberRepository.findById(memberIds.get(0))
                .orElseThrow(() -> new PostNotFoundException(GlobalErrorCode.POST_NOT_FOUND));
        PostReportRequest request = new PostReportRequest(videoBoardIds.get(0), "TYPE_A");
        // then
        try {
            reportService.reportPost(member, request);
            fail("Expected MemberReportException");
        } catch (ReportOneselfException e) {
            // Assert that the exception is the expected exception
            Assertions.assertThat(e.getMessage()).isEqualTo("?????? ???????????? ????????? ??? ????????????.");
        }


    }

}
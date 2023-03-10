package org.anotherclass.colortherock.domain.videocomment.controller;

import org.anotherclass.colortherock.IntegrationTest;
import org.anotherclass.colortherock.domain.member.entity.Member;
import org.anotherclass.colortherock.domain.video.entity.Video;
import org.anotherclass.colortherock.domain.videoboard.entity.VideoBoard;
import org.anotherclass.colortherock.domain.videocomment.entity.VideoComment;
import org.anotherclass.colortherock.domain.videocomment.repository.VideoCommentRepository;
import org.anotherclass.colortherock.domain.videocomment.request.CommentUpdateRequest;
import org.anotherclass.colortherock.domain.videocomment.request.NewCommentRequest;
import org.anotherclass.colortherock.domain.videocomment.response.CommentListResponse;
import org.anotherclass.colortherock.domain.videocomment.response.MyCommentListResponse;
import org.anotherclass.colortherock.global.common.BaseResponse;
import org.anotherclass.colortherock.global.security.jwt.JwtTokenUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.web.servlet.MockMvc;

import javax.persistence.EntityManager;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.anotherclass.colortherock.global.security.jwt.JwtTokenUtils.BEARER_PREFIX;
import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

class VideoCommentControllerTest extends IntegrationTest {

    @Autowired
    EntityManager em;

    @Autowired
    MockMvc mockMvc;
    Member member;
    private Video video;
    private VideoBoard videoBoard;
    String url = "http://localhost:8080/api/videoboard/";

    @Autowired
    VideoCommentRepository videoCommentRepository;
    @Autowired
    JwtTokenUtils jwtTokenUtils;
    private String token;
    List<VideoComment> videoComments;

    private final Integer commentSize = 5;

    @BeforeEach()
    void setup() {

        videoComments = new ArrayList<>();
        member = Member.builder()
                .nickname("??????")
                .email("?????????")
                .registrationId(Member.RegistrationId.google).build();
        em.persist(member);


        video = Video.builder()
                .thumbnailURL("url")
                .videoName("??????")
                .s3URL("s3")
                .color("??????")
                .level(3)
                .gymName("??????")
                .isSuccess(true)
                .shootingDate(LocalDate.now())
                .member(member)
                .build();
        em.persist(video);
        videoBoard = VideoBoard.builder()
                .video(video)
                .title("??????")
                .member(member)
                .build();
        em.persist(videoBoard);

        for (int i = 0; i < commentSize; i++) {
            VideoComment comment = VideoComment.builder()
                    .videoBoard(videoBoard)
                    .content("??????")
                    .member(member)
                    .build();
            videoComments.add(comment);
            em.persist(comment);
        }

        em.flush();
        em.clear();
        token = BEARER_PREFIX + jwtTokenUtils.createTokens(member, List.of(new SimpleGrantedAuthority("ROLE_MEMBER")));
    }

    @Test
    @DisplayName("?????? ?????? ??????")
    void getCommentList() throws Exception {
        url += "comment";
        MockHttpServletResponse response = mockMvc.perform(
                        get(url)
                                .contentType(MediaType.APPLICATION_JSON)
                                .param("videoBoardId", String.valueOf(videoBoard.getId()))
                                .param("storeId", String.valueOf(-1))
                )
                .andReturn()
                .getResponse();

        BaseResponse<List<CommentListResponse>> arrayList = objectMapper.readValue(response.getContentAsString(), BaseResponse.class);
        Assertions.assertEquals(commentSize, arrayList.getResult().size());
    }

    @Test
    @DisplayName("?????? ???????????? ?????? ??????")
    void getCommentSlice() throws Exception {
        url += "comment";
        MockHttpServletResponse response = mockMvc.perform(
                        get(url)
                                .contentType(MediaType.APPLICATION_JSON)
                                .param("videoBoardId", String.valueOf(videoBoard.getId()))
                                .param("storeId", String.valueOf(-1))
                )
                .andDo(print())
                .andReturn()
                .getResponse();

        BaseResponse<List<CommentListResponse>> arrayList = objectMapper.readValue(response.getContentAsString(), BaseResponse.class);
        Assertions.assertEquals(commentSize, arrayList.getResult().size());
    }


    @Test
    @DisplayName("?????? ?????? ")
    void addComment() throws Exception {
        url += "comment";
        NewCommentRequest request = new NewCommentRequest(videoBoard.getId(), "???????????? ????????? ??????~!");
        mockMvc.perform(
                        post(url)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsBytes(request))
                                .header(HttpHeaders.AUTHORIZATION, token)

                )
                .andExpect(jsonPath("$.status", is(200)));
//        url += "comment";
        MockHttpServletResponse response = mockMvc.perform(
                        get(url)
                                .contentType(MediaType.APPLICATION_JSON)
                                .param("videoBoardId", String.valueOf(videoBoard.getId()))
                                .param("storeId", String.valueOf(-1))
                )
                .andDo(print())
                .andReturn()
                .getResponse();

        BaseResponse<List<CommentListResponse>> arrayList = objectMapper.readValue(response.getContentAsString(), BaseResponse.class);
        Assertions.assertEquals(commentSize + 1, arrayList.getResult().size());
    }


    @Test
    @DisplayName("?????? ??????")
    void updateComment() throws Exception {
        url += "comment";
        Long commentId = videoComments.get(0).getId();
        CommentUpdateRequest request = new CommentUpdateRequest(commentId, "???????????????");
        mockMvc.perform(
                        put(url)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsBytes(request))
                                .header(HttpHeaders.AUTHORIZATION, token)

                ).andDo(print())
                .andExpect(jsonPath("$.status", is(200)));

        Optional<VideoComment> byId = videoCommentRepository.findById(commentId);
        VideoComment videoComment = byId.orElseThrow();
        String content = videoComment.getContent();
        System.out.println(content);
        Assertions.assertEquals("???????????????", content);
    }

    @Test
    @DisplayName("????????? ??????")
    void deleteComment() throws Exception {
        url += "comment";
        Long commentId = videoComments.get(0).getId();
        mockMvc.perform(
                        delete(url)
                                .contentType(MediaType.APPLICATION_JSON)
                                .param("commentId", String.valueOf(commentId))
                                .header(HttpHeaders.AUTHORIZATION, token)

                ).andDo(print())
                .andExpect(jsonPath("$.status", is(200)));

        Optional<VideoComment> byId = videoCommentRepository.findById(commentId);
        Assertions.assertTrue(byId.isEmpty());
    }

    @Test
    @DisplayName("?????? ????????? ??????")
    void getMyCommentList() throws Exception {
        url += "mycomment";
        MockHttpServletResponse response = mockMvc.perform(
                        get(url)
                                .contentType(MediaType.APPLICATION_JSON)
                                .header(HttpHeaders.AUTHORIZATION, token)
                                .param("storeId", String.valueOf(-1))

                ).andDo(print())
                .andExpect(jsonPath("$.status", is(200)))
                .andReturn()
                .getResponse();
        BaseResponse<List<MyCommentListResponse>> listBaseResponse = this.convertToBaseResponse(response);
        Assertions.assertEquals(5, listBaseResponse.getResult().size());

    }
}
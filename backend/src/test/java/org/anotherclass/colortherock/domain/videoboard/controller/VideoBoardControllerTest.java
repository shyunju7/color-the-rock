package org.anotherclass.colortherock.domain.videoboard.controller;

import org.anotherclass.colortherock.IntegrationTest;
import org.anotherclass.colortherock.domain.member.entity.Member;
import org.anotherclass.colortherock.domain.memberrecord.response.VideoListResponse;
import org.anotherclass.colortherock.domain.video.entity.Video;
import org.anotherclass.colortherock.domain.videoboard.entity.VideoBoard;
import org.anotherclass.colortherock.domain.videoboard.repository.VideoBoardRepository;
import org.anotherclass.colortherock.domain.videoboard.request.SuccessPostUpdateRequest;
import org.anotherclass.colortherock.domain.videoboard.response.VideoBoardSummaryResponse;
import org.anotherclass.colortherock.global.common.BaseResponse;
import org.anotherclass.colortherock.global.security.jwt.JwtTokenUtils;
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
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

class VideoBoardControllerTest extends IntegrationTest {

    @Autowired
    EntityManager em;
    @Autowired
    MockMvc mockMvc;
    Member member;
    String url = "http://localhost:8080/api/video/board/";

    @Autowired
    VideoBoardRepository videoBoardRepository;
    @Autowired
    JwtTokenUtils jwtTokenUtils;
    private String token;

    ArrayList<Long> videoBoardIds;

    private static final Integer PAGE_SIZE = 16;
    private static final Integer MY_PAGE_SIZE = 8;

    @BeforeEach
    void setup() {
        member = Member.builder()
                .nickname("yeji")
                .email("yeji@email.com")
                .registrationId(Member.RegistrationId.kakao).build();
        em.persist(member);
        videoBoardIds = new ArrayList<>();

        for (int i = 0; i < 30; i++) {
            Video video = Video.builder()
                    .thumbnailURL("url")
                    .videoName("??????")
                    .s3URL("s3")
                    .color("??????" + i)
                    .level(3)
                    .gymName("??????????????? ?????????" + i)
                    .isSuccess(true)
                    .shootingDate(LocalDate.of(2023, 2, 9))
                    .member(member)
                    .isPosted(false)
                    .build();
            em.persist(video);
            if (i % 2 == 0) {
                VideoBoard videoBoard = VideoBoard.builder()
                        .video(video)
                        .title("??????" + i)
                        .isHidden(false)
                        .member(member)
                        .build();
                em.persist(videoBoard);
                videoBoard.getVideo().videoPosted();
                videoBoardIds.add(videoBoard.getId());
            }
        }
        token = BEARER_PREFIX + jwtTokenUtils.createTokens(member, List.of(new SimpleGrantedAuthority("ROLE_MEMBER")));
    }

    @Test
    @DisplayName("?????? ?????? ????????? ?????? ??????")
    void getSuccessVideoPosts() throws Exception {
        MockHttpServletResponse response = mockMvc.perform(
                        get(url)
                                .contentType(MediaType.APPLICATION_JSON)
                                .param("storeId", String.valueOf(-1))
                )
                .andReturn()
                .getResponse();

        BaseResponse<List<VideoBoardSummaryResponse>> arrayList = objectMapper.readValue(response.getContentAsString(), BaseResponse.class);
        assertEquals(PAGE_SIZE, arrayList.getResult().size());
    }

    @Test
    @DisplayName("?????? ?????? ????????? ???????????? ??????")
    void getSuccessPostsSlice() throws Exception {
        Long videoBoardId = videoBoardIds.get(2);
        mockMvc.perform(
                        get(url)
                                .contentType(MediaType.APPLICATION_JSON)
                                .param("storeId", String.valueOf(videoBoardId))
                )
                .andExpect(jsonPath("$.status", is(200)));
    }

    @Test
    @DisplayName("?????? ?????? ????????? ????????? ??????")
    void getSuccessPostsByGym() throws Exception {
        MockHttpServletResponse response = mockMvc.perform(
                        get(url)
                                .contentType(MediaType.APPLICATION_JSON)
                                .param("storeId", String.valueOf(-1))
                                .param("gymName", "??????????????? ?????????26")

                )
                .andReturn()
                .getResponse();
        BaseResponse<List<VideoBoardSummaryResponse>> arrayList = objectMapper.readValue(response.getContentAsString(), BaseResponse.class);
        assertEquals(1, arrayList.getResult().size());
    }

    @Test
    @DisplayName("?????? ?????? ????????? ????????? ??????")
    void getSuccessPostsByColor() throws Exception {
        MockHttpServletResponse response = mockMvc.perform(
                        get(url)
                                .contentType(MediaType.APPLICATION_JSON)
                                .param("storeId", String.valueOf(-1))
                                .param("color", "??????2")
                )
                .andReturn()
                .getResponse();
        BaseResponse<List<VideoBoardSummaryResponse>> arrayList = objectMapper.readValue(response.getContentAsString(), BaseResponse.class);
        assertEquals(1, arrayList.getResult().size());
    }

    @Test
    @DisplayName("??? ???????????? ????????? ??? ????????? ????????? ?????? ??????")
    void getMySuccessVideo() throws Exception {
        url += "myvideo";
        MockHttpServletResponse response = mockMvc.perform(
                get(url)
                        .contentType(MediaType.APPLICATION_JSON)
                        .param("storeId", String.valueOf(-1))
                        .param("shootingDate", "2023-02-09")
                        .header(HttpHeaders.AUTHORIZATION, token)
        ).andReturn().getResponse();
        BaseResponse<List<VideoListResponse>> arrayList = objectMapper.readValue(response.getContentAsString(), BaseResponse.class);
        assertEquals(15, arrayList.getResult().size());
    }


    @Test
    @DisplayName("?????? ?????? ????????? ?????? ??????")
    void getSuccessPostDetail() throws Exception {
        url += "detail";
        mockMvc.perform(
                        get(url)
                                .contentType(MediaType.APPLICATION_JSON)
                                .param("videoBoardId", String.valueOf(videoBoardIds.get(0)))
                )
                .andExpect(jsonPath("$.result.title", is("??????0")));
    }

    @Test
    @DisplayName("?????? ?????? ????????? ?????? ??????")
    void updateSuccessPost() throws Exception {
        url += "detail";
        Long videoBoardId = videoBoardIds.get(0);
        String newTitle = "????????? ??????";
        Integer newLevel = 5;
        String newColor = "??????";
        String newGymName = "???????????? ?????????";
        SuccessPostUpdateRequest request = new SuccessPostUpdateRequest(videoBoardId, newTitle, newLevel, newColor, newGymName);
        mockMvc.perform(
                        put(url)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsBytes(request))
                                .header(HttpHeaders.AUTHORIZATION, token)

                ).andDo(print())
                .andExpect(jsonPath("$.status", is(200)));

        Optional<VideoBoard> byId = videoBoardRepository.findById(videoBoardId);
        VideoBoard videoBoard = byId.orElseThrow();
        assertEquals(newTitle, videoBoard.getTitle());
        assertEquals(newLevel, videoBoard.getVideo().getLevel());
        assertEquals(newColor, videoBoard.getVideo().getColor());
        assertEquals(newGymName, videoBoard.getVideo().getGymName());
    }

    @Test
    @DisplayName("?????? ?????? ????????? ??????")
    void deleteSuccessPost() throws Exception {
        url += "detail";
        Long videoBoardId = videoBoardIds.get(0);
        mockMvc.perform(
                delete(url)
                        .contentType(MediaType.APPLICATION_JSON)
                        .param("videoBoardId", String.valueOf(videoBoardId))
                        .header(HttpHeaders.AUTHORIZATION, token)
        ).andExpect(jsonPath("$.status", is(200)));

        Optional<VideoBoard> byId = videoBoardRepository.findById(videoBoardId);
        assertTrue(byId.isEmpty());
    }

    @Test
    @DisplayName("??? ?????? ?????? ?????? storeId -1????????? ????????? 16????????? ??????")
    void getMySuccessPostList() throws Exception {
        url += "mypost";
        MockHttpServletResponse response = mockMvc.perform(
                        get(url)
                                .contentType(MediaType.APPLICATION_JSON)
                                .header(HttpHeaders.AUTHORIZATION, token)
                                .param("storeId", String.valueOf(-1))
                ).andDo(print())
                .andExpect(jsonPath("$.status", is(200)))
                .andReturn().getResponse();

        BaseResponse<List<VideoBoardSummaryResponse>> arrayList = objectMapper.readValue(response.getContentAsString(), BaseResponse.class);
        assertEquals(MY_PAGE_SIZE, arrayList.getResult().size());
    }


}
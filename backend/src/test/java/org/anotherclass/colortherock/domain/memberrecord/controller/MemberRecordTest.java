package org.anotherclass.colortherock.domain.memberrecord.controller;

import org.anotherclass.colortherock.IntegrationTest;
import org.anotherclass.colortherock.domain.member.entity.Member;
import org.anotherclass.colortherock.domain.member.repository.MemberRepository;
import org.anotherclass.colortherock.domain.memberrecord.entity.MemberRecord;
import org.anotherclass.colortherock.domain.memberrecord.repository.RecordRepository;
import org.anotherclass.colortherock.domain.memberrecord.service.RecordService;
import org.anotherclass.colortherock.domain.video.entity.Video;
import org.anotherclass.colortherock.domain.video.repository.VideoRepository;
import org.anotherclass.colortherock.domain.video.request.UploadVideoRequest;
import org.anotherclass.colortherock.global.redis.RefreshTokenRepository;
import org.anotherclass.colortherock.global.security.jwt.JwtTokenUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.io.FileInputStream;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.List;

import static org.anotherclass.colortherock.global.security.jwt.JwtTokenUtils.BEARER_PREFIX;
import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SuppressWarnings("NonAsciiCharacters")
class MemberRecordTest extends IntegrationTest {

    @Autowired
    MemberRepository memberRepository;
    @Autowired
    RefreshTokenRepository refreshTokenRepository;
    @Autowired
    RedisTemplate<String, String> redisTemplate;
    public static final String AUTHORIZATION_HEADER = BEARER_PREFIX;
    @Autowired
    JwtTokenUtils jwtTokenUtils;
    @Autowired
    VideoRepository videoRepository;
    @Autowired
    RecordRepository recordRepository;
    @Autowired
    RecordService recordService;
    @Autowired
    MockMvc mockMvc;
    String url = "http://localhost:8080/api";
    Member member;
    String token;
    Video video;
    Long videoId;

    @BeforeEach
    void setMemberAndToken() {
        // Member ?????? ??? token ??????
        member = new Member("johan@rock.com", "??????", Member.RegistrationId.google);
        Member savedMember = memberRepository.save(member);
        token = jwtTokenUtils.createTokens(savedMember, List.of(new SimpleGrantedAuthority("ROLE_MEMBER")));
        // ?????? ??????
        for (int i = 1; i <= 9; i++) {
            video = UploadVideoRequest.builder()
                    .shootingDate(LocalDate.parse("2023-01-17"))
                    .level(i)
                    .gymName("???????????? ??????")
                    .isSuccess(true)
                    .color("??????").build().toEntity(savedMember, "s3URL", "thumbURL", "videoName", "thumbName", false);
            Video save = videoRepository.save(video);
            videoId = save.getId();
            video = UploadVideoRequest.builder()
                    .shootingDate(LocalDate.parse("2023-01-17"))
                    .level(i)
                    .gymName("???????????? ??????")
                    .isSuccess(true)
                    .color("??????").build().toEntity(savedMember, "s3URL", "thumbURL", "videoName", "thumbName", false);
            videoRepository.save(video);
        }
        // member record ??????
        MemberRecord record = MemberRecord.builder().successCount(18).videoCount(18).member(member).build();
        recordRepository.save(record);
    }

    @Test
    @DisplayName("[GET]?????? ?????? ?????? ??? ?????? ??????")
    void ??????9???_????????????() throws Exception {
        mockMvc.perform(
                        get(url + "/record/color")
                                .header("Authorization", AUTHORIZATION_HEADER + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result").isArray())
                .andExpect(jsonPath("$.result[0].level").isNumber());
    }

    @Test
    @DisplayName("[GET]????????? ?????? ?????? ?????? ?????? ??????")
    void ??????9???_?????????_????????????() throws Exception {
        mockMvc.perform(
                        get(url + "/record/color/2023-01-17")
                                .header("Authorization", AUTHORIZATION_HEADER + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result").isArray())
                .andExpect(jsonPath("$.result[0].level").isNumber())
                .andExpect(jsonPath("$.result[0].success").value(2));
    }

    @Test
    @DisplayName("[GET]????????? ?????? ?????? ?????? ?????? ?????? - ??????")
    void ??????9???_?????????_????????????_??????() throws Exception {
        mockMvc.perform(
                        get(url + "/record/color/2023-13-17")
                                .header("Authorization", AUTHORIZATION_HEADER + token))
                .andExpect(jsonPath("$.status", is(400)));
    }

    @Test
    @DisplayName("[GET]?????? ?????? ?????? ?????? ?????? ??????")
    void ?????????_????????????_??????() throws Exception {
        MemberRecord record = recordRepository.findByMember(member);
        int originalVideoCount = record.getVideoCount();
        int originalSuccessCount = record.getSuccessCount();
        recordService.addVideoCount(member, true);
        recordService.addVideoCount(member, true);
        recordService.addVideoCount(member, false);
        mockMvc.perform(
                        get(url + "/record/total")
                                .header("Authorization", AUTHORIZATION_HEADER + token))
                .andExpect(jsonPath("$.status", is(200)))
                .andExpect(jsonPath("$.result.videoCount").value(originalVideoCount + 3))
                .andExpect(jsonPath("$.result.successCount").value(originalSuccessCount + 2));
    }

    @Test
    @DisplayName("[GET]????????? ????????? ??????/?????? ?????? ??????")
    void ?????????_??????_??????_????????????() throws Exception {

        MultiValueMap<String, String> info = new LinkedMultiValueMap<>();

        info.add("videoId", "-1");
        info.add("shootingDate", "2023-01-17");
        info.add("isSuccess", "true");

        mockMvc.perform(
                        get(url + "/record/videos")
                                .header("Authorization", AUTHORIZATION_HEADER + token)
                                .params(info))
                .andExpect(jsonPath("$.status", is(200)));
    }

    @Test
    @DisplayName("[GET] ?????? ?????? ??????")
    void ??????_??????_??????() throws Exception {
        mockMvc.perform(
                        get(url + "/record/video/" + videoId)
                                .header("Authorization", AUTHORIZATION_HEADER + token)
                ).andExpect(jsonPath("$.status", is(200)))
                .andExpect(jsonPath("$.result.level").isNumber());
    }

    @Test
    @DisplayName("[GET] ?????? ?????? ?????? - ??????(?????? ???????????? ?????? ??????)")
    void ??????_??????_??????_??????() throws Exception {
        mockMvc.perform(
                get(url + "/record/video/-1")
                        .header("Authorization", AUTHORIZATION_HEADER + token)
        ).andExpect(jsonPath("$.status", is(404)));
    }

    @Test
    @DisplayName("[POST] ?????? ?????? ?????????")
    void ??????_??????_?????????_??????() throws Exception {
        MockMultipartFile newVideo = new MockMultipartFile("newVideo", "video.mp4", "mp4", new FileInputStream("src/test/resources/video/test_recording.mp4"));
        String content = "{" +
                "\"shootingDate\": \"2023-01-17\"," +
                "\"level\": 1," +
                "\"color\": \"??????\"," +
                "\"gymName\": \"???????????????\"," +
                "\"isSuccess\": true }";
        MockMultipartFile json = new MockMultipartFile("uploadVideoRequest", "jsondata", "application/json", content.getBytes(StandardCharsets.UTF_8));
        mockMvc.perform(multipart(url + "/record/video")
                        .file(newVideo)
                        .file(json)
                        .contentType(MediaType.MULTIPART_FORM_DATA)
                        .accept(MediaType.APPLICATION_JSON)
                        .characterEncoding("UTF-8")
                        .header("Authorization", AUTHORIZATION_HEADER + token))
                .andExpect(jsonPath("$.status", is(200)));

    }

    @Test
    @DisplayName("[DELETE] ?????? ?????? ??????")
    void ??????_??????() throws Exception {
        mockMvc.perform(
                        delete(url + "/record/video/" + videoId)
                                .header("Authorization", AUTHORIZATION_HEADER + token))
                .andExpect(jsonPath("$.status", is(200)));
    }

    @Test
    @DisplayName("[GET] ?????? ?????? ?????? ??????")
    void ????????????_??????() throws Exception {
        video = UploadVideoRequest.builder()
                .shootingDate(LocalDate.parse("2023-01-18"))
                .level(1)
                .gymName("???????????? ??????")
                .isSuccess(true)
                .color("??????").build().toEntity(member);
        videoRepository.save(video);
        mockMvc.perform(
                        get(url + "/record/visit")
                                .header("Authorization", AUTHORIZATION_HEADER + token))
                .andExpect(jsonPath("$.status", is(200)))
                .andExpect(jsonPath("$.result.totalCount").isNumber())
                .andExpect(jsonPath("$.result.data").isArray())
                .andExpect(jsonPath("$.result.data[0].gymName", is("???????????? ??????")))
                .andExpect(jsonPath("$.result.data[0].count", is(2)));
    }

    @Test
    @DisplayName("[GET] ????????? ?????? ?????? ?????? ??????")
    void ??????????????????_??????() throws Exception {
        video = UploadVideoRequest.builder()
                .shootingDate(LocalDate.parse("2023-01-18"))
                .level(1)
                .gymName("???????????? ??????")
                .isSuccess(true)
                .color("??????").build().toEntity(member);
        videoRepository.save(video);
        mockMvc.perform(
                        get(url + "/record/calendar/2023-01")
                                .header("Authorization", AUTHORIZATION_HEADER + token))
                .andExpect(jsonPath("$.status", is(200)))
                .andExpect(jsonPath("$.result").isArray())
                .andExpect(jsonPath("$.result[0].date", is("2023-01-17")));
    }

}

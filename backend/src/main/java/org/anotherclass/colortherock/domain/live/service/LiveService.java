package org.anotherclass.colortherock.domain.live.service;

import io.openvidu.java.client.*;
import org.anotherclass.colortherock.domain.live.entity.Live;
import org.anotherclass.colortherock.domain.live.exception.RecordingStartBadRequestException;
import org.anotherclass.colortherock.domain.live.exception.SessionNotFountException;
import org.anotherclass.colortherock.domain.live.repository.LiveReadRepository;
import org.anotherclass.colortherock.domain.live.repository.LiveRepository;
import org.anotherclass.colortherock.domain.live.request.CreateLiveRequest;
import org.anotherclass.colortherock.domain.live.request.RecordingSaveRequest;
import org.anotherclass.colortherock.domain.live.request.RecordingStartRequest;
import org.anotherclass.colortherock.domain.live.request.RecordingStopRequest;
import org.anotherclass.colortherock.domain.live.response.LiveListResponse;
import org.anotherclass.colortherock.domain.member.entity.Member;
import org.anotherclass.colortherock.domain.member.entity.MemberDetails;
import org.anotherclass.colortherock.domain.member.repository.MemberRepository;
import org.anotherclass.colortherock.domain.video.entity.Video;
import org.anotherclass.colortherock.domain.video.repository.VideoRepository;
import org.anotherclass.colortherock.domain.video.service.S3Service;
import org.jcodec.api.JCodecException;
import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class LiveService {

    private final S3Service s3Service;
    private final LiveRepository liveRepository;
    private final LiveReadRepository liveReadRepository;
    private final MemberRepository memberRepository;
    private final VideoRepository videoRepository;

    private final OpenVidu openVidu;

    @Value("${RECORDING_PATH}") String dir;

    public LiveService(LiveRepository liveRepository,
                       MemberRepository memberRepository,
                       VideoRepository videoRepository,
                       S3Service s3Service,
                       LiveReadRepository liveReadRepository, @Value("${OPENVIDU_URL}") String OPENVIDU_URL,
                       @Value("${OPENVIDU_SECRET}") String OPENVIDU_SECRET) {
        this.s3Service = s3Service;
        this.liveRepository = liveRepository;
        this.memberRepository = memberRepository;
        this.videoRepository = videoRepository;
        this.liveReadRepository = liveReadRepository;
        this.openVidu = new OpenVidu(OPENVIDU_URL, OPENVIDU_SECRET);
    }

    public String createLiveRoom(MemberDetails memberDetails, CreateLiveRequest request) {
        Long id = memberDetails.getMember().getId();
        Member member = memberRepository.findById(id).orElseThrow();
        Session session;
        // TODO 어떤 오류가 나는지 불명
        try {
            session = openVidu.createSession();
        } catch (OpenViduJavaClientException | OpenViduHttpException e) {
            throw new RuntimeException(e);
        }
        String sessionId = session.getSessionId();
        Live live = request.toEntity(sessionId, member);
        liveRepository.save(live);
        try {
            Connection connection = session.createConnection(new ConnectionProperties.Builder().role(OpenViduRole.PUBLISHER).build());
            return connection.getToken();
        } catch (OpenViduJavaClientException | OpenViduHttpException e) {
            throw new RuntimeException(e);
        }
    }

    public String joinLiveRoom(String sessionId) {
        Session activeSession = openVidu.getActiveSession(sessionId);
        if (activeSession == null) {
            throw new SessionNotFountException();
        }
        try {
            Connection connection = activeSession.createConnection(new ConnectionProperties.Builder().role(OpenViduRole.SUBSCRIBER).build());
            return connection.getToken();
        } catch (OpenViduJavaClientException | OpenViduHttpException e) {
            throw new RuntimeException(e);
        }
    }

    public String recordingStart(String sessionId, RecordingStartRequest request) {

        Session activeSession = openVidu.getActiveSession(sessionId);
        if (activeSession == null) {
            throw new SessionNotFountException();
        }
        String token = request.getToken();
        Connection connection = activeSession.getConnection(token);
        OpenViduRole role = connection.getRole();

        if (role.equals(OpenViduRole.PUBLISHER)) {
            try {
                Recording recording = openVidu.startRecording(sessionId);
                return recording.getId();
            } catch (OpenViduJavaClientException | OpenViduHttpException e) {
                throw new RuntimeException(e);
            }
        }
        throw new RecordingStartBadRequestException();
    }

    public void recordingStop(RecordingStopRequest request) {

        try {
            openVidu.stopRecording(request.getRecordingId());
        } catch (OpenViduJavaClientException | OpenViduHttpException e) {
            throw new RuntimeException(e);
        }
        throw new RecordingStartBadRequestException();
    }

    @Transactional
    public void recordingSave(MemberDetails memberDetails, String sessionId, RecordingSaveRequest request) throws IOException, JCodecException {
        dir += "/" + request.getRecordingId() + "/" + request.getRecordingId() + ".webm";
        String videoName = DateTime.now() + request.getRecordingId() + ".webm";
        String s3Url = s3Service.uploadFromOV(dir, videoName);
        Member member = memberRepository.findById(memberDetails.getMember().getId()).orElseThrow();
        // 썸네일 추가
        String thumbnailName = "Thumb"+DateTime.now() + request.getRecordingId() + ".JPEG";
        String thumbnailURL = s3Service.uploadThumbnailFromOV(dir, thumbnailName);
        // 비디오 객체 생성
        Video video = request.toEntity(s3Url, thumbnailURL, member);
        videoRepository.save(video);
    }

    @Transactional(readOnly = true)
    public List<LiveListResponse> getLiveList(Long liveId, Pageable pageable) {
        Slice<Live> slices = liveReadRepository.searchBySlice(liveId, pageable);

        if(slices.isEmpty()) return new ArrayList<>();

        return slices.toList().stream()
                .map(live ->
                        LiveListResponse.builder()
                                .id(live.getId())
                                .title(live.getTitle())
                                .memberId(live.getMember().getId())
                                .memberName(live.getMember().getNickname())
                                .gymName(live.getGymName())
                                .sessionId(live.getSessionId())
                                .participantNum(
                                        openVidu.getActiveSession(live.getSessionId()).getActiveConnections().size()
                                ).build())
                .collect(Collectors.toList());
    }
}
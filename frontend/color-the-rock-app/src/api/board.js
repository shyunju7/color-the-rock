import { defaultInstance } from "./utils/index";

// board(성공 영상 모음) API 작성
export const BoardApi = {
  // 완등 영상 글 상세보기(GET요청)
  getBoardDetail: ({ videoBoardId }) =>
    defaultInstance.get(`/video/board/detail`, {
      params: {
        videoBoardId,
      },
    }),

  // 완등 영상 글 수정하기(PUT 요청)
  PutBoardDetail: (videoBoardId, title) =>
    defaultInstance.put("/video/board/detail", {
      videoBoardId,
      title,
    }),

  // 완등 영상 글 삭제하기(DELETE 요청)
  DeleteBoardDetail: ({ videoBoardId }) =>
    defaultInstance.delete("/video/board/detail", {
      params: {
        videoBoardId,
      },
    }),

  // 전체 완등 영상 전체 리스트 조회
  // 이거 뭐임??
  getAllVideo: ({ storeId, color, gymName }) =>
    defaultInstance.get(`/video/baord`, {
      params: {
        storeId,
        color,
        gymName,
      },
    }),

  // 완등 영상 게시글 올리기(내 운동기록 동영상에서 영상 가져오기)
  postRegisterRecordVideo: ({ videoId, title }) =>
    defaultInstance.post(
      "/video/board",
      {
        videoId,
        title,
      },
      {
        headers: {
          "Content-Type": "multipart/form-data",
        },
      }
    ),

  // 완등 영상 게시글 올리기(로컬 파일에서 영상 가져오기)
  postRegisterLocalVideo: (formData) =>
    defaultInstance.post("/video/board/local", formData, {
      headers: {
        "Content-Type": "multipart/form-data",
      },
    }),

  // 내 완등 영상 게시글 목록 조회 요청
  getMypostVideo: ({ storeId }) =>
    defaultInstance.get("/video/board/mypost", {
      params: {
        storeId,
      },
    }),

  // 영상 댓글 조회 요청
  getVideoBoardComment: ({ storeId, videoBoardId }) => {
    defaultInstance.get("/videoboard/comment", {
      params: {
        storeId,
        videoBoardId,
      },
      headers: {
        "Content-Type": "multipart/form-data",
      },
    });
  },
  // 영상 댓글 수정 요청
  putVideoBoardComment: ({ commentId, content }) =>
    defaultInstance.put("/videoboard/comment", {
      commentId,
      content,
    }),

  // 영상 댓글 작성 요청
  postVideoBoardComment: ({ videoBoardId, content }) =>
    defaultInstance.post("/videoboard/comment", {
      videoBoardId,
      content,
    }),

  // 영상 댓글 삭제 요청
  // value 수정
  deleteVideoBoardComment: ({ value }) =>
    defaultInstance.delete("videoboard/comment", {
      params: {
        value,
      },
    }),

  // 내 영상 댓글 조회 요청
  getVideoBoardmyComment: ({ storeId }) =>
    defaultInstance.get("/videoboard/mycomment", {
      params: {
        storeId,
      },
    }),
};

export default BoardApi;

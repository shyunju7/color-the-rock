import React, { useEffect, useState } from "react";
import Thumbnail from "../../components/Common/Thumbnail";
import Title from "../../components/Common/Title";
import * as S from "./style";
import { HiPencil } from "react-icons/hi";
import BoardSearchBar from "../../components/Board/BoardSearch";
import boardApi from "../../api/board";
import { useNavigate } from "react-router-dom";
import { useSelector } from "react-redux";
import Loading from "../../components/Common/Loading";

const Board = () => {
  const navigate = useNavigate();
  const isLogin = useSelector((state) => state.users.isLogin);
  const [result, setResult] = useState([]);
  const [storeId, setStoreId] = useState(-1);
  const [isShowRegisterModal, setShowRegisterModal] = useState(false);
  const [isLoading, setLoading] = useState(false);
  const getBoardList = () => {
    setLoading(true);
    const requestData = {
      storeId: -1,
      color: "",
      gymName: "",
    };
    boardApi
      .getAllVideo(requestData)
      .then(({ data: { status, result: _result } }) => {
        if (status === 200) {
          console.log("statusCode : 200", _result);
          setResult(_result);
          setStoreId(_result[_result.length - 1].videoBoardId);
        }
      })
      .catch((error) => console.log(error))
      .finally(() => {
        setTimeout(() => setLoading(false), 200);
      });
  };

  const handleProcessRegister = (e) => {
    const { target } = e;

    if (target.nodeName === "DIV") {
      setShowRegisterModal(false);
      return;
    }

    if (target.value === "local") {
      console.log("로컬에서 가져오기 ");
    } else {
      console.log("클라우드에서 가져오기 ");
    }
    setShowRegisterModal(false);
  };

  const handleOnClickRegisterBoard = () => {
    if (isLogin) {
      setShowRegisterModal(true);
    } else {
      alert("로그인 후, 이용해주세요!");
    }
  };

  useEffect(() => {
    getBoardList();
  }, []);

  const handleOnClickItem = (id) => {
    navigate(`/board/detail/${id}`);
  };

  return (
    <S.Container id="board-container">
      <Title>완등 영상 보기</Title>
      <S.Description>완등 영상을 게시하고 피드백을 받아보세요!</S.Description>
      <BoardSearchBar
        setResult={setResult}
        storeId={storeId}
        setStoreId={setStoreId}
        setLoading={setLoading}
      />
      {isLoading && <Loading />}
      {result && result.length > 0 ? (
        <S.ThumbnailList>
          {result.map((item) => (
            <Thumbnail
              key={item.videoBoardId}
              id={item.videoBoardId}
              title={item.title}
              userNickname={item.userNickname}
              gymName={item.gymName}
              imgUrl={item.thumbnailURL}
              isLive={false}
              color={item.color}
              createdDate={item.createdDate}
              onClick={handleOnClickItem}
            />
          ))}
        </S.ThumbnailList>
      ) : (
        <S.Message>등록된 게시글이 없어요!</S.Message>
      )}
      <S.RegisterButton onClick={handleOnClickRegisterBoard}>
        <HiPencil size="24px" color="#C250D6" />
      </S.RegisterButton>

      {isShowRegisterModal && (
        <S.RegisterModal onClick={handleProcessRegister}>
          <S.ModalButtonWrapper>
            <S.ModalButton value="local" to="/board/form">
              <S.GradientText>로컬에서 가져오기</S.GradientText>
            </S.ModalButton>
            <S.ModalButton value="cloud" to="/board/s3form">
              <S.GradientText>내 운동기록에서 가져오기</S.GradientText>
            </S.ModalButton>
          </S.ModalButtonWrapper>
        </S.RegisterModal>
      )}
    </S.Container>
  );
};
export default Board;
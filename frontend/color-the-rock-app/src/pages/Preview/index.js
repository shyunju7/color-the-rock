import React, { useEffect, useState } from "react";
import * as S from "./style";
import { useSearchParams } from "react-router-dom";
import { recordApi } from "../../api/record";
const Preview = () => {
  const [searchParams] = useSearchParams();
  const videoId = searchParams.get("videoId");
  const [result, setResult] = useState([]);

  // CALL API
  useEffect(() => {
    recordApi
      .getOneRecordVideo(videoId)
      .then(({ data: { status, result: _result } }) => {
        if (status === 200) {
          setResult(_result);
        }
      });
  }, []);

  useEffect(() => {
    console.log("preview result ? ", result);
  }, [result]);

  return (
    <S.Container controls>
      <S.Video src={result.s3URL} type="video/mp4" autoPlay controls></S.Video>
    </S.Container>
  );
};
export default Preview;
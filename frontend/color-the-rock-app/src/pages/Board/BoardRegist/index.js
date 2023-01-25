import React, {useState} from 'react'
import { Mobile, Desktop } from "../../../components/layout/Template"
import * as S from "./style";
import  { FiArrowLeft } from "react-icons/fi";
import DatePicker from "react-datepicker"
import "react-datepicker/dist/react-datepicker.css";
import SubTitleForm from "../../../components/Board/SubTitleForm"
import UploadForm from "../../../components/Board/UploadForm"
import InputComp from '../../../components/Board/InputComp';
import SelectButton from '../../../components/Board/SelectButton';
import DatePickBtn from '../../../components/Board/DatePickBtn';
import KakaoMapBtn from '../../../components/Board/KakaoMapBtn';
import RegistBtn from '../../../components/Board/RegistBtn' 
import { useNavigate } from 'react-router-dom'  

export default function BoardRegist() {
  const navigate = useNavigate();
  const [startDate, setStartDate] = useState();
  const [title, setTitle] = useState("");
  const [level, setLevel] = useState("");
  const [color, setColor] = useState("");
  const [crag, setCrag] = useState("");
  const [location, setLocation] = useState("");

  const onClickHandler = () => {
    navigate("/");
  }

  return (
    <div>
      <Desktop></Desktop>

      <Mobile>
        <S.Container>

          <S.CloseBtnContainer>
            <S.CloseBtn>
              <FiArrowLeft onClick={onClickHandler}/>
            </S.CloseBtn>
          </S.CloseBtnContainer>
          
           <S.ContentWrap>
            <S.Content>
              <SubTitleForm title="동영상"></SubTitleForm>
              <UploadForm></UploadForm>
              <SubTitleForm title="글등록"></SubTitleForm>
              <InputComp placeholder="제목을 입력해주세요."></InputComp>
              <S.SelectWrap>
                  <SelectButton></SelectButton>
                  <SelectButton></SelectButton>
              </S.SelectWrap>
              <KakaoMapBtn location={location} setLocation={setLocation}/>
              <DatePickBtn
                startDate={startDate}
                setStartDate={setStartDate}
              ></DatePickBtn>
              <RegistBtn btnName="등록하기"></RegistBtn>
            </S.Content>
          </S.ContentWrap>
          
          {/*  <S.TitleContainer>
              <S.Title>동영상</S.Title>
            </S.TitleContainer>
            
            <S.UploadContainer>
              <S.Upload>

              </S.Upload>
            </S.UploadContainer>

            <S.TitleContainer>
              <S.Title>글등록</S.Title>
            </S.TitleContainer>

            <S.ContentContainer>

            </S.ContentContainer>
            */}
        </S.Container>
      </Mobile>
    </div>
  )
}

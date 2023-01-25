import React from "react";
import {
  BrowserRouter as Router,
  Route,
  Routes,
  Outlet,
} from "react-router-dom";
import Intro from "./pages/Intro";
import Login from "./pages/Login";
import Streaming from "./pages/Streaming";
import BoardList from "./pages/Board";
import Record from "./pages/Record";
import Header from "./components/layout/Header";
import Oauth from "./components/LogIn/index";
import Signup from "./pages/Signup/index"
import BoardRegist from "./pages/Board/BoardRegist/index"
import StreamingForm from "./pages/StreamingForm"
const Layout = () => {
  return (
    <div>
      <Header />
      <Outlet />
    </div>
  );
};

const AppRouter = () => {
  // 로그인 여부에 따라 router 설정
  return (
    <Router>
      <Routes>
        <Route path="/" element={<Layout />}>
          <Route index element={<Intro />} />
          <Route path="/board" element={<BoardList />} />
          <Route path="/record" element={<Record />} />
          <Route path="/streaming" element={<Streaming />} />
        </Route>

        <Route path="/login" element={<Login />}/>
        <Route path="/signup" element={<Signup />}/>
        <Route path="/regist" element={<BoardRegist/>}/>
        <Route path="/oauth" element={<Oauth />} />  
        <Route path="/streaming/:streamingId" element={<Streaming />} />
        <Route path="/streamingform" element={<StreamingForm />} />
      </Routes>
      {/* <Footer /> */}
    </Router>
  );
};

export default AppRouter;

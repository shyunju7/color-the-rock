import axios from "axios";

// url 설정 수정 필요
const BASE_URL = "http://www.colortherock.com";

const defaultApi = (url, option) => {
  const instance = axios.create({
    baseURL: BASE_URL,
    ...option,
  });
  return instance;
};


// 인증 토큰 관련 api 처리
// 로그인 처리시 변경해야할 로직
const authApi = (url, option) => {
  const accessToken = sessionStorage.getItem("");
  const instance = axios.create({
    baseURL: BASE_URL,
    headers: {
      Authorization: ``,
    },
    ...option,
  });

  // if(accessToken == null) 
  //   instance.defaults.headers.common['Authorization'] = accessToken;

  return instance;
};


defaultApi().interceptors.request.use(
  function (config) {
    const accessToken = sessionStorage.getItem("accessToken");

    // 요청시 AccessToken 계속 보내주기
    if (!accessToken) {
      config.headers.Authorization = null;
      // config.headers.refreshToken = null;
      return config;
    }

    if(config.headers && accessToken) {
      config.headers.Authorization =`Bearer ${accessToken}`;
      // config.headers.refreshToken = `Bearer ${refreshToken}`;
      return config;
    }
  }
);

defaultApi().interceptors.response.use(
  // 2xx 응답이 오면 return;
  (response) => {
    return response;
  },

  // error 가 오면 
  async (error) => {
    // error에 담겨있는 config와 response 구조 분해 할당
    const {
      config,
      response: { status },
    } = error;

    // token 만료시 401 error
    if (status === 401) {
      const originalRequest = config;
      const refreshToken = await sessionStorage.getItem("refreshToken");
      
      // refreshToken이 있는 경우에만 재요청 시도
      if(refreshToken) {
        // token refresh 요청
        const token = sessionStorage.getItem("token");
        const data = await axios.post(
          `http://colortherock.com/refresh`, // token refresh api
          {     
            "accessToken": `Bearer ${token}`,
            "refreshToken": `Bearer ${refreshToken}`,},
          // 이거 맞아???
          {
            // header에 넣지?? 흠....
            'Content-Type' : 'application/json',
          }
        );
        
        const accessToken = data;
        await sessionStorage.setItem(["accessToken", accessToken]);

        originalRequest.headers.Authorization = `Bearer ${accessToken}`;
        return axios(originalRequest)
      }
    }

    console.log("response error", error);
    return Promise.reject(error);
  }
)

export const defaultInstance = defaultApi(BASE_URL);
export const AuthInstance = authApi(BASE_URL);
import React from "react";
import ReactDOM from "react-dom/client";
import App from "./App";
import store from "./stores/store";
import { Provider } from "react-redux";
import GlobalStyles from "./styles/GlobalStyles";
import { CookiesProvider } from "react-cookie";
import { persistStore } from "redux-persist";
import { PersistGate } from "redux-persist/integration/react";
import ReactGA from "react-ga";

const TRACKING_ID = process.env.REACT_APP_GOOGLE_ANALYTICS_TRACKING_ID;
ReactGA.initialize(TRACKING_ID);
const persistor = persistStore(store);

const root = ReactDOM.createRoot(document.getElementById("root"));
root.render(
  <CookiesProvider>
    <Provider store={store}>
      <PersistGate loading={null} persistor={persistor}>
        <GlobalStyles />
        <App />
      </PersistGate>
    </Provider>
  </CookiesProvider>
);

import React, { useEffect, useState } from "react";
import { useLocation } from "react-router-dom";
import ReactGA from "react-ga";
const RouteChangeTracker = () => {
  const location = useLocation();
  const [isInitialized, setInitialized] = useState(false);

  useEffect(() => {
    if (!window.location.href.includes("colortherock")) {
      ReactGA.initialize(process.env.REACT_APP_GOOGLE_ANALYTICS_TRACKING_ID);
    }
    setInitialized(true);
  }, []);

  useEffect(() => {
    if (isInitialized) {
      ReactGA.pageview(location.pathname + location.search);
    }
  }, [isInitialized, location]);
};

export default RouteChangeTracker;

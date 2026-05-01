import { useEffect, useState } from "react";
import { appRouteChangeEvent } from "../utils/router";

export function usePathname() {
  const [pathname, setPathname] = useState(() => window.location.pathname);

  useEffect(() => {
    const handleRouteChange = () => setPathname(window.location.pathname);

    window.addEventListener("popstate", handleRouteChange);
    window.addEventListener(appRouteChangeEvent, handleRouteChange);

    return () => {
      window.removeEventListener("popstate", handleRouteChange);
      window.removeEventListener(appRouteChangeEvent, handleRouteChange);
    };
  }, []);

  return pathname;
}

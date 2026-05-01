import { useEffect, useState } from "react";
import { homeFallback } from "../data/homeFallback";
import { getHomeContent } from "../services/contentApi";
import type { HomeViewModel } from "../types/home";

export type HomeDataState = {
  data: HomeViewModel;
  isLoading: boolean;
  isFallback: boolean;
  error: string | null;
};

export function useHomeData(): HomeDataState {
  const [state, setState] = useState<HomeDataState>({
    data: homeFallback,
    isLoading: true,
    isFallback: true,
    error: null,
  });

  useEffect(() => {
    let isMounted = true;

    getHomeContent()
      .then((data) => {
        if (!isMounted) {
          return;
        }

        setState({
          data,
          isLoading: false,
          isFallback: false,
          error: null,
        });
      })
      .catch((error: unknown) => {
        if (!isMounted) {
          return;
        }

        setState({
          data: homeFallback,
          isLoading: false,
          isFallback: true,
          error: error instanceof Error ? error.message : "Unknown home API error",
        });
      });

    return () => {
      isMounted = false;
    };
  }, []);

  return state;
}

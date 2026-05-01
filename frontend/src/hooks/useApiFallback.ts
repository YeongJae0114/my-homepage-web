import { useEffect, useState } from "react";

export type ApiFallbackState<T> = {
  data: T;
  isLoading: boolean;
  isFallback: boolean;
  error: string | null;
};

export function useApiFallback<T>(fetcher: () => Promise<T>, fallback: T): ApiFallbackState<T> {
  const [state, setState] = useState<ApiFallbackState<T>>({
    data: fallback,
    isLoading: true,
    isFallback: true,
    error: null,
  });

  useEffect(() => {
    let isMounted = true;

    fetcher()
      .then((data) => {
        if (!isMounted) {
          return;
        }

        setState({ data, isLoading: false, isFallback: false, error: null });
      })
      .catch((error: unknown) => {
        if (!isMounted) {
          return;
        }

        setState({
          data: fallback,
          isLoading: false,
          isFallback: true,
          error: error instanceof Error ? error.message : "Unknown API error",
        });
      });

    return () => {
      isMounted = false;
    };
  }, [fallback, fetcher]);

  return state;
}

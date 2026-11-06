import { useCallback, useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";
import { ApiError } from "../services/apiClient";
import { useOwner } from "./useOwner";

export type PortfolioResource<T> = {
  data: T | null;
  error: string | null;
  loading: boolean;
  reload: () => Promise<void>;
};

/**
 * Loads owner data with no fallback (fake asset numbers must never render).
 * A 401 means the session expired: refresh owner state and go to the login page.
 * `loader` must be stable (module-level function or useCallback).
 */
export function usePortfolioResource<T>(loader: () => Promise<T>): PortfolioResource<T> {
  const handleError = usePortfolioErrorHandler();
  const [data, setData] = useState<T | null>(null);
  const [error, setError] = useState<string | null>(null);
  const [loading, setLoading] = useState(true);

  const reload = useCallback(async () => {
    setLoading(true);
    try {
      setData(await loader());
      setError(null);
    } catch (loadError) {
      setError(handleError(loadError));
    } finally {
      setLoading(false);
    }
  }, [loader, handleError]);

  useEffect(() => {
    void reload();
  }, [reload]);

  return { data, error, loading, reload };
}

/** Returns a user-facing message; redirects to the login page on an expired session. */
export function usePortfolioErrorHandler(): (error: unknown) => string {
  const { refresh } = useOwner();
  const navigate = useNavigate();

  return useCallback(
    (error: unknown) => {
      if (error instanceof ApiError && error.status === 401) {
        void refresh().then(() => navigate("/owner", { replace: true }));
        return "로그인이 만료되었습니다.";
      }
      return error instanceof Error ? error.message : "요청을 처리하지 못했습니다.";
    },
    [refresh, navigate],
  );
}

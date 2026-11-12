import { useCallback, useEffect, useRef, useState } from "react";
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
export function usePortfolioResource<T>(loader: () => Promise<T>, refreshIntervalMs = 0): PortfolioResource<T> {
  const handleError = usePortfolioErrorHandler();
  const [data, setData] = useState<T | null>(null);
  const [error, setError] = useState<string | null>(null);
  const [loading, setLoading] = useState(true);
  const request = useRef({ id: 0, pending: false, mounted: false });

  const reload = useCallback(async () => {
    const state = request.current;
    if (!state.mounted) return;
    const id = ++state.id;
    state.pending = true;
    setLoading(true);
    try {
      const result = await loader();
      if (id !== state.id) return;
      setData(result);
      setError(null);
    } catch (loadError) {
      if (id !== state.id) return;
      setError(handleError(loadError));
    } finally {
      if (id === state.id) {
        state.pending = false;
        setLoading(false);
      }
    }
  }, [loader, handleError]);

  useEffect(() => {
    const state = request.current;
    state.mounted = true;
    void reload();
    const refreshVisible = () => {
      if (document.visibilityState === "visible" && !state.pending) void reload();
    };
    const timer = refreshIntervalMs > 0 ? window.setInterval(refreshVisible, refreshIntervalMs) : null;
    if (timer != null) document.addEventListener("visibilitychange", refreshVisible);
    return () => {
      state.mounted = false;
      state.id++;
      state.pending = false;
      if (timer != null) {
        window.clearInterval(timer);
        document.removeEventListener("visibilitychange", refreshVisible);
      }
    };
  }, [reload, refreshIntervalMs]);

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

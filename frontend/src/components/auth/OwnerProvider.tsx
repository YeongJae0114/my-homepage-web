import { useCallback, useEffect, useMemo, useState, type ReactNode } from "react";
import { OwnerContext } from "../../hooks/useOwner";
import { fetchOwnerStatus } from "../../services/authApi";

export function OwnerProvider({ children }: { children: ReactNode }) {
  const [owner, setOwner] = useState(false);
  const [loading, setLoading] = useState(true);

  const refresh = useCallback(async () => {
    try {
      setOwner(await fetchOwnerStatus());
    } catch {
      // Backend unreachable: treat as a public visitor.
      setOwner(false);
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => {
    void refresh();
  }, [refresh]);

  const value = useMemo(() => ({ owner, loading, refresh }), [owner, loading, refresh]);

  return <OwnerContext.Provider value={value}>{children}</OwnerContext.Provider>;
}

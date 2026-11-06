import type { ReactNode } from "react";
import { Navigate } from "react-router-dom";
import { useOwner } from "../../hooks/useOwner";
import { Container } from "../common/Container";

/**
 * UX guard only: hides owner pages from visitors. The backend enforces access (401 on /api/portfolio/**).
 * Visitors go to "/" rather than "/owner" so the login path is not revealed.
 */
export function RequireOwner({ children }: { children: ReactNode }) {
  const { owner, loading } = useOwner();

  if (loading) {
    return (
      <Container className="py-20">
        <p className="text-sm text-zinc-500">Loading…</p>
      </Container>
    );
  }

  if (!owner) {
    return <Navigate to="/" replace />;
  }

  return children;
}

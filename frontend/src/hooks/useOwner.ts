import { createContext, useContext } from "react";

export type OwnerState = {
  owner: boolean;
  loading: boolean;
  /** Re-reads /api/auth/me; also re-issues the CSRF cookie (needed again after logout). */
  refresh: () => Promise<void>;
};

export const OwnerContext = createContext<OwnerState | null>(null);

export function useOwner(): OwnerState {
  const state = useContext(OwnerContext);

  if (!state) {
    throw new Error("useOwner must be used inside OwnerProvider");
  }

  return state;
}

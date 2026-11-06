import { fetchJson, sendRequest } from "./apiClient";

type OwnerStatusResponse = {
  owner: boolean;
};

export async function fetchOwnerStatus(): Promise<boolean> {
  const response = await fetchJson<OwnerStatusResponse>("/auth/me");
  return response.owner;
}

export function loginOwner(username: string, password: string): Promise<void> {
  return sendRequest("/auth/login", {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify({ username, password }),
  });
}

export function logoutOwner(): Promise<void> {
  return sendRequest("/auth/logout", { method: "POST" });
}

const productionApiBaseUrl = "https://api.zerojae175-dev.shop/api";
const defaultApiBaseUrl = import.meta.env.PROD ? productionApiBaseUrl : "/api";

export async function fetchJson<T>(path: string, init?: RequestInit): Promise<T> {
  const apiBaseUrl = import.meta.env.VITE_API_BASE_URL ?? defaultApiBaseUrl;
  const url = `${apiBaseUrl.replace(/\/$/, "")}/${path.replace(/^\//, "")}`;
  const response = await fetch(url, {
    headers: {
      Accept: "application/json",
      ...init?.headers,
    },
    ...init,
  });

  if (!response.ok) {
    throw new Error(`API request failed: ${response.status} ${response.statusText}`);
  }

  return response.json() as Promise<T>;
}

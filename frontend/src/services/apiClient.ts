const defaultApiBaseUrl = "/api";
const csrfProtectedMethods = new Set(["POST", "PUT", "PATCH", "DELETE"]);

export class ApiError extends Error {
  readonly status: number;
  /** Per-field messages from a 400 problem detail (owner forms show them next to the inputs). */
  readonly fieldErrors: Record<string, string>;

  constructor(status: number, message: string, fieldErrors: Record<string, string> = {}) {
    super(message);
    this.name = "ApiError";
    this.status = status;
    this.fieldErrors = fieldErrors;
  }
}

async function errorFrom(response: Response): Promise<ApiError> {
  const fallback = `API request failed: ${response.status} ${response.statusText}`;
  try {
    const body = (await response.json()) as { detail?: string; errors?: Record<string, string> };
    return new ApiError(response.status, body.detail ?? fallback, body.errors ?? {});
  } catch {
    return new ApiError(response.status, fallback);
  }
}

function readCookie(name: string): string | undefined {
  const entry = document.cookie.split("; ").find((cookie) => cookie.startsWith(`${name}=`));
  return entry ? decodeURIComponent(entry.slice(name.length + 1)) : undefined;
}

async function request(path: string, init?: RequestInit): Promise<Response> {
  const apiBaseUrl = import.meta.env.VITE_API_BASE_URL ?? defaultApiBaseUrl;
  const url = `${apiBaseUrl.replace(/\/$/, "")}/${path.replace(/^\//, "")}`;
  const headers = new Headers(init?.headers);

  if (!headers.has("Accept")) {
    headers.set("Accept", "application/json");
  }

  // Spring Security issues XSRF-TOKEN via GET /api/auth/me; echo it on state-changing requests.
  const csrfToken = csrfProtectedMethods.has((init?.method ?? "GET").toUpperCase()) ? readCookie("XSRF-TOKEN") : undefined;
  if (csrfToken) {
    headers.set("X-XSRF-TOKEN", csrfToken);
  }

  const response = await fetch(url, { ...init, headers });

  if (!response.ok) {
    throw await errorFrom(response);
  }

  return response;
}

export async function fetchJson<T>(path: string, init?: RequestInit): Promise<T> {
  const response = await request(path, init);
  return response.json() as Promise<T>;
}

/** For endpoints that answer 204 No Content. */
export async function sendRequest(path: string, init?: RequestInit): Promise<void> {
  await request(path, init);
}

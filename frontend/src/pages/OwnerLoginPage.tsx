import { useState, type FormEvent } from "react";
import { Navigate, useNavigate } from "react-router-dom";
import { Card } from "../components/common/Card";
import { Container } from "../components/common/Container";
import { useOwner } from "../hooks/useOwner";
import { ApiError } from "../services/apiClient";
import { loginOwner } from "../services/authApi";

const inputClassName =
  "w-full rounded-md border border-white/10 bg-surface-900 px-3 py-2.5 text-sm text-zinc-100 outline-none transition focus:border-emerald-300/50";

function loginErrorMessage(error: unknown): string {
  if (error instanceof ApiError && error.status === 401) {
    return "아이디 또는 비밀번호가 올바르지 않습니다.";
  }
  if (error instanceof ApiError && error.status === 429) {
    return "로그인 시도가 너무 많습니다. 15분 후 다시 시도하세요.";
  }
  return "로그인에 실패했습니다. 잠시 후 다시 시도하세요.";
}

/** Hidden owner login. Not linked anywhere; hiding it is UX, the backend enforces access. */
export function OwnerLoginPage() {
  const { owner, loading, refresh } = useOwner();
  const navigate = useNavigate();
  const [username, setUsername] = useState("");
  const [password, setPassword] = useState("");
  const [error, setError] = useState<string | null>(null);
  const [submitting, setSubmitting] = useState(false);

  if (!loading && owner) {
    return <Navigate to="/portfolio" replace />;
  }

  async function handleSubmit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    setSubmitting(true);
    setError(null);

    try {
      await loginOwner(username, password);
      await refresh();
      navigate("/portfolio", { replace: true });
    } catch (loginError) {
      setError(loginErrorMessage(loginError));
      // A 403 means the CSRF cookie was missing; refresh re-issues it for the next attempt.
      await refresh();
    } finally {
      setPassword("");
      setSubmitting(false);
    }
  }

  return (
    <Container className="grid min-h-[70vh] place-items-center py-16">
      <meta name="robots" content="noindex, nofollow" />
      <Card className="w-full max-w-sm p-6 sm:p-8">
        <p className="font-mono text-xs font-semibold uppercase tracking-[0.18em] text-emerald-200">Owner</p>
        <h1 className="mt-3 text-xl font-semibold text-zinc-50">Sign in</h1>

        <form className="mt-6 grid gap-4" onSubmit={handleSubmit}>
          <label className="grid gap-1.5 text-sm text-zinc-400">
            Username
            <input
              className={inputClassName}
              name="username"
              autoComplete="username"
              required
              value={username}
              onChange={(event) => setUsername(event.target.value)}
            />
          </label>
          <label className="grid gap-1.5 text-sm text-zinc-400">
            Password
            <input
              className={inputClassName}
              type="password"
              name="password"
              autoComplete="current-password"
              required
              value={password}
              onChange={(event) => setPassword(event.target.value)}
            />
          </label>

          {error ? (
            <p className="text-sm text-rose-300" role="alert">
              {error}
            </p>
          ) : null}

          <button
            type="submit"
            disabled={submitting}
            className="inline-flex min-h-11 items-center justify-center rounded-md border border-emerald-300/40 bg-emerald-300 px-4 py-2 text-sm font-semibold text-surface-950 transition hover:bg-emerald-200 disabled:cursor-not-allowed disabled:opacity-60"
          >
            {submitting ? "Signing in…" : "Sign in"}
          </button>
        </form>
      </Card>
    </Container>
  );
}

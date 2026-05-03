import { useCallback, useEffect, useMemo, useRef, useState, type FormEvent, type KeyboardEvent } from "react";
import { Badge } from "../components/common/Badge";
import { Card } from "../components/common/Card";
import { Container } from "../components/common/Container";
import { cn } from "../utils/cn";
import { getLlmServerStatus, sendPrompt, wakeLocalGpuServer } from "../services/llmApi";
import type { LlmMessage, LlmServerStatus } from "../types/llm";

const statusMeta: Record<LlmServerStatus, { label: string; tone: "neutral" | "cyan" | "emerald" | "amber"; dotClassName: string }> = {
  offline: {
    label: "Offline",
    tone: "neutral",
    dotClassName: "bg-zinc-500",
  },
  waking: {
    label: "Waking Up...",
    tone: "amber",
    dotClassName: "bg-amber-300 shadow-[0_0_18px_rgba(252,211,77,.45)]",
  },
  online: {
    label: "Online",
    tone: "emerald",
    dotClassName: "bg-emerald-300 shadow-[0_0_18px_rgba(110,231,183,.55)]",
  },
};

const initialMessages: LlmMessage[] = [
  {
    id: "welcome",
    role: "assistant",
    content: "Local GPU 서버가 online 상태가 되면 문서, 코드, 운영 기록에 대해 질문할 수 있습니다.",
    createdAt: new Date().toISOString(),
  },
];

function createMessage(role: LlmMessage["role"], content: string): LlmMessage {
  return {
    id: globalThis.crypto?.randomUUID?.() ?? `${role}-${Date.now()}-${Math.random().toString(16).slice(2)}`,
    role,
    content,
    createdAt: new Date().toISOString(),
  };
}

function formatTime(value: string) {
  return new Intl.DateTimeFormat("ko-KR", {
    hour: "2-digit",
    minute: "2-digit",
  }).format(new Date(value));
}

export function LocalLLMChatPage() {
  const [isAuthenticated, setIsAuthenticated] = useState(false);
  const [password, setPassword] = useState("");
  const [authError, setAuthError] = useState<string | null>(null);
  const [serverStatus, setServerStatus] = useState<LlmServerStatus>("offline");
  const [isStatusLoading, setIsStatusLoading] = useState(false);
  const [isWaking, setIsWaking] = useState(false);
  const [isSending, setIsSending] = useState(false);
  const [prompt, setPrompt] = useState("");
  const [messages, setMessages] = useState<LlmMessage[]>(initialMessages);
  const messageEndRef = useRef<HTMLDivElement | null>(null);
  const accessKey = import.meta.env.VITE_LOCAL_LLM_ACCESS_KEY;
  const activeStatus = statusMeta[serverStatus];
  const canSend = isAuthenticated && serverStatus === "online" && prompt.trim().length > 0 && !isSending;

  const inputPlaceholder = useMemo(() => {
    if (!isAuthenticated) {
      return "인증 후 사용할 수 있습니다.";
    }

    if (serverStatus !== "online") {
      return "서버가 online 상태가 되면 질문할 수 있습니다.";
    }

    return "로컬 문서, 코드, 서버 운영 기록에 대해 질문하기";
  }, [isAuthenticated, serverStatus]);

  const appendSystemMessage = useCallback((content: string) => {
    setMessages((current) => [...current, createMessage("system", content)]);
  }, []);

  const refreshStatus = useCallback(async () => {
    if (!isAuthenticated) {
      return;
    }

    setIsStatusLoading(true);

    try {
      const status = await getLlmServerStatus();
      setServerStatus(status);
    } catch (error) {
      setServerStatus("offline");
      appendSystemMessage(error instanceof Error ? `서버 상태 조회 실패: ${error.message}` : "서버 상태 조회에 실패했습니다.");
    } finally {
      setIsStatusLoading(false);
    }
  }, [appendSystemMessage, isAuthenticated]);

  useEffect(() => {
    if (isAuthenticated) {
      refreshStatus();
    }
  }, [isAuthenticated, refreshStatus]);

  useEffect(() => {
    messageEndRef.current?.scrollIntoView({ behavior: "smooth", block: "end" });
  }, [messages]);

  const handleAuthSubmit = (event: FormEvent<HTMLFormElement>) => {
    event.preventDefault();

    if (!accessKey) {
      setAuthError("VITE_LOCAL_LLM_ACCESS_KEY 환경변수가 설정되어 있지 않습니다.");
      return;
    }

    if (password !== accessKey) {
      setAuthError("패스워드가 올바르지 않습니다.");
      return;
    }

    setAuthError(null);
    setPassword("");
    setIsAuthenticated(true);
  };

  const handleWake = async () => {
    if (!isAuthenticated || isWaking) {
      return;
    }

    setIsWaking(true);
    setServerStatus("waking");

    try {
      const nextStatus = await wakeLocalGpuServer();
      setServerStatus(nextStatus);
      appendSystemMessage("WOL 요청을 전송했습니다. 서버가 준비되면 상태를 다시 확인해 주세요.");
    } catch (error) {
      setServerStatus("offline");
      appendSystemMessage(error instanceof Error ? `WOL 요청 실패: ${error.message}` : "WOL 요청에 실패했습니다.");
    } finally {
      setIsWaking(false);
    }
  };

  const handleSubmitPrompt = async (event?: FormEvent<HTMLFormElement>) => {
    event?.preventDefault();

    if (!canSend) {
      return;
    }

    const userMessage = createMessage("user", prompt.trim());
    const nextMessages = [...messages, userMessage];
    setMessages(nextMessages);
    setPrompt("");
    setIsSending(true);

    try {
      const response = await sendPrompt(nextMessages);
      setMessages((current) => [...current, response]);
    } catch (error) {
      setMessages((current) => [
        ...current,
        createMessage("system", error instanceof Error ? `LLM 응답 실패: ${error.message}` : "LLM 응답을 가져오지 못했습니다."),
      ]);
    } finally {
      setIsSending(false);
    }
  };

  const handlePromptKeyDown = (event: KeyboardEvent<HTMLTextAreaElement>) => {
    if (event.key !== "Enter" || event.shiftKey) {
      return;
    }

    event.preventDefault();
    handleSubmitPrompt();
  };

  return (
    <section className="relative min-h-[calc(100vh-4rem)] overflow-hidden bg-surface-950">
      <div className="absolute inset-x-0 top-0 h-80 bg-[radial-gradient(circle_at_50%_0%,rgba(103,232,249,0.14),transparent_64%)]" aria-hidden="true" />
      <Container className="relative py-8 sm:py-10">
        <div className={cn("transition duration-300", !isAuthenticated && "pointer-events-none select-none blur-sm")}>
          <Card className="overflow-hidden">
            <div className="flex flex-wrap items-center justify-between gap-4 border-b border-white/10 px-4 py-4 sm:px-5">
              <div className="min-w-0">
                <p className="font-mono text-xs uppercase tracking-[0.16em] text-cyan-200">Local LLM Chat</p>
                <h1 className="mt-1 break-words text-xl font-semibold text-zinc-50 sm:text-2xl">Private GPU Workspace</h1>
              </div>
              <div className="flex flex-wrap items-center gap-2">
                <Badge tone={activeStatus.tone}>
                  <span className={cn("mr-2 h-2 w-2 rounded-full", activeStatus.dotClassName)} />
                  {activeStatus.label}
                </Badge>
                <button
                  type="button"
                  className="min-h-10 rounded-md border border-white/10 bg-white/[0.06] px-3 py-2 text-sm font-semibold text-zinc-50 transition hover:border-cyan-200/30 hover:bg-cyan-200/10 disabled:cursor-not-allowed disabled:opacity-50"
                  disabled={!isAuthenticated || isStatusLoading}
                  onClick={refreshStatus}
                >
                  {isStatusLoading ? "Checking..." : "Refresh"}
                </button>
                <button
                  type="button"
                  className="min-h-10 rounded-md border border-emerald-300/40 bg-emerald-300 px-3 py-2 text-sm font-semibold text-surface-950 transition hover:bg-emerald-200 disabled:cursor-not-allowed disabled:opacity-50"
                  disabled={!isAuthenticated || isWaking || serverStatus === "online"}
                  onClick={handleWake}
                >
                  {isWaking ? "Booting..." : "Boot Local GPU"}
                </button>
              </div>
            </div>

            <div className="grid min-h-[58vh] content-start gap-4 px-4 py-5 sm:px-5">
              {messages.map((message) => {
                const isUser = message.role === "user";
                const isSystem = message.role === "system";

                return (
                  <div key={message.id} className={cn("flex", isUser ? "justify-end" : "justify-start")}>
                    <div
                      className={cn(
                        "max-w-[min(100%,760px)] rounded-lg border px-4 py-3 shadow-glow",
                        isUser && "border-emerald-300/25 bg-emerald-300/[0.12] text-emerald-50",
                        !isUser && !isSystem && "border-white/10 bg-white/[0.05] text-zinc-100",
                        isSystem && "border-amber-300/25 bg-amber-300/[0.08] text-amber-50",
                      )}
                    >
                      <div className="mb-2 flex items-center justify-between gap-3">
                        <span className="font-mono text-xs uppercase text-zinc-500">{message.role}</span>
                        <span className="font-mono text-xs text-zinc-500">{formatTime(message.createdAt)}</span>
                      </div>
                      <p className="whitespace-pre-wrap break-words text-sm leading-7">{message.content}</p>
                    </div>
                  </div>
                );
              })}
              <div ref={messageEndRef} />
            </div>

            <form className="sticky bottom-0 border-t border-white/10 bg-surface-950/90 p-4 backdrop-blur-xl sm:p-5" onSubmit={handleSubmitPrompt}>
              <div className="mx-auto flex max-w-4xl items-end gap-3 rounded-lg border border-white/10 bg-white/[0.04] p-2">
                <textarea
                  value={prompt}
                  onChange={(event) => setPrompt(event.target.value)}
                  onKeyDown={handlePromptKeyDown}
                  disabled={!isAuthenticated || serverStatus !== "online" || isSending}
                  rows={1}
                  className="max-h-40 min-h-12 flex-1 resize-none bg-transparent px-3 py-3 text-sm leading-6 text-zinc-100 outline-none placeholder:text-zinc-600 disabled:cursor-not-allowed"
                  placeholder={inputPlaceholder}
                />
                <button
                  type="submit"
                  className="min-h-11 rounded-md border border-emerald-300/40 bg-emerald-300 px-4 py-2 text-sm font-semibold text-surface-950 transition hover:bg-emerald-200 disabled:cursor-not-allowed disabled:opacity-50"
                  disabled={!canSend}
                >
                  {isSending ? "Sending" : "Send"}
                </button>
              </div>
            </form>
          </Card>
        </div>
      </Container>

      {!isAuthenticated ? (
        <div className="fixed inset-0 z-50 grid place-items-center bg-surface-950/70 px-4 backdrop-blur-md">
          <Card className="w-full max-w-md p-5">
            <Badge tone="cyan">Auth Lock</Badge>
            <h2 className="mt-4 text-2xl font-semibold text-zinc-50">Local LLM 접근 인증</h2>
            <p className="mt-3 text-sm leading-6 text-zinc-400">
              이 페이지는 실제 로컬 GPU 서버와 연결되는 작업 공간입니다. 권한이 있는 사용자만 접근할 수 있습니다.
            </p>
            <form className="mt-5 grid gap-3" onSubmit={handleAuthSubmit}>
              <label className="grid gap-2">
                <span className="text-sm font-medium text-zinc-300">Access password</span>
                <input
                  type="password"
                  value={password}
                  onChange={(event) => setPassword(event.target.value)}
                  className="min-h-11 rounded-md border border-white/10 bg-surface-900 px-3 text-sm text-zinc-100 outline-none transition focus:border-cyan-200/40"
                  placeholder="Enter access key"
                  autoFocus
                />
              </label>
              {authError ? <p className="text-sm text-amber-200">{authError}</p> : null}
              <button
                type="submit"
                className="min-h-11 rounded-md border border-emerald-300/40 bg-emerald-300 px-4 py-2 text-sm font-semibold text-surface-950 transition hover:bg-emerald-200"
              >
                Unlock Workspace
              </button>
            </form>
          </Card>
        </div>
      ) : null}
    </section>
  );
}

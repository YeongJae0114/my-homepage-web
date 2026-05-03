export type LlmServerStatus = "offline" | "waking" | "online";

export type LlmModelStatus = "offline" | "loading" | "ready" | "thinking";

export type RetrievalSource = {
  id: string;
  title: string;
  url?: string;
  excerpt: string;
  score: number;
};

export type LlmMessage = {
  id: string;
  role: "user" | "assistant" | "system";
  content: string;
  createdAt: string;
};

export type LlmStatusResponse = {
  status: LlmServerStatus;
};

export type LlmWakeResponse = {
  status: LlmServerStatus;
};

export type LlmChatRequest = {
  messages: LlmMessage[];
};

export type LlmChatResponse = {
  message: LlmMessage;
};

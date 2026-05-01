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

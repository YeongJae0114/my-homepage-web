import { fetchJson } from "./apiClient";
import type {
  LlmChatRequest,
  LlmChatResponse,
  LlmMessage,
  LlmModelStatus,
  LlmServerStatus,
  LlmStatusResponse,
  LlmWakeResponse,
  RetrievalSource,
} from "../types/llm";

const jsonHeaders = {
  "Content-Type": "application/json",
};

export async function getModelStatus(): Promise<LlmModelStatus> {
  return "offline";
}

export async function getLlmServerStatus(): Promise<LlmServerStatus> {
  const response = await fetchJson<LlmStatusResponse>("/llm/status");

  return response.status;
}

export async function wakeLocalGpuServer(): Promise<LlmServerStatus> {
  const response = await fetchJson<LlmWakeResponse>("/llm/wake", {
    method: "POST",
    headers: jsonHeaders,
  });

  return response.status;
}

export async function searchLocalKnowledge(_query: string): Promise<RetrievalSource[]> {
  void _query;
  return [];
}

export async function sendPrompt(messages: LlmMessage[]): Promise<LlmMessage> {
  const response = await fetchJson<LlmChatResponse>("/llm/chat", {
    method: "POST",
    headers: jsonHeaders,
    body: JSON.stringify({ messages } satisfies LlmChatRequest),
  });

  return response.message;
}

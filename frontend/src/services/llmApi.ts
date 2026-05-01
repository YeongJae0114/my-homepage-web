import type { LlmMessage, LlmModelStatus, RetrievalSource } from "../types/llm";

export async function getModelStatus(): Promise<LlmModelStatus> {
  return "offline";
}

export async function searchLocalKnowledge(_query: string): Promise<RetrievalSource[]> {
  void _query;
  return [];
}

export async function sendPrompt(_messages: LlmMessage[]): Promise<LlmMessage> {
  void _messages;
  return {
    id: "placeholder-response",
    role: "assistant",
    content: "Local LLM integration is not connected yet.",
    createdAt: new Date().toISOString(),
  };
}

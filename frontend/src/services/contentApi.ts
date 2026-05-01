import { mediaItems } from "../data/media";
import { posts } from "../data/posts";
import type { HomeApiResponse } from "../types/home";
import { mapHomeApiToViewModel } from "../adapters/homeAdapter";
import { fetchJson } from "./apiClient";

export async function getContentSnapshot() {
  return {
    posts,
    mediaItems,
  };
}

export async function getHomeContent() {
  const response = await fetchJson<HomeApiResponse>("/home");
  return mapHomeApiToViewModel(response);
}

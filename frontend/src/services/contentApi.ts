import { mediaItems } from "../data/media";
import { posts } from "../data/posts";

export async function getContentSnapshot() {
  return {
    posts,
    mediaItems,
  };
}

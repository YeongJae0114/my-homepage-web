import type { Post } from "../types/content";
import type { VelogPost } from "../types/velog";

const velogBaseUrl = "https://velog.io";

export function mapVelogPostToPost(post: VelogPost, username: string): Post {
  return {
    id: post.id,
    title: post.title,
    summary: post.short_description,
    publishedAt: post.released_at.slice(0, 10),
    tags: post.tags,
    url: `${velogBaseUrl}/@${username}/${post.url_slug}`,
  };
}

export function mapVelogPostsToPosts(posts: VelogPost[], username: string): Post[] {
  return posts.map((post) => mapVelogPostToPost(post, username));
}

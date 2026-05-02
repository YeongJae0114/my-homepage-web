import type { Post } from "../types/content";
import type { VelogBlogProfile, VelogPost, VelogUserProfile } from "../types/velog";

const velogBaseUrl = "https://velog.io";
const maxSummaryLength = 200;

function truncateSummary(value: string) {
  const normalized = value.replace(/\s+/g, " ").trim();

  if (normalized.length <= maxSummaryLength) {
    return normalized;
  }

  return `${normalized.slice(0, maxSummaryLength)}...`;
}

export function mapVelogPostToPost(post: VelogPost, username: string): Post {
  return {
    id: post.id,
    title: post.title,
    summary: truncateSummary(post.short_description),
    publishedAt: post.released_at.slice(0, 10),
    tags: post.tags,
    url: `${velogBaseUrl}/@${username}/${post.url_slug}`,
  };
}

export function mapVelogPostsToPosts(posts: VelogPost[], username: string): Post[] {
  return posts.map((post) => mapVelogPostToPost(post, username));
}

export function mapVelogUserToProfile(user: VelogUserProfile): VelogBlogProfile {
  return {
    username: user.username,
    displayName: user.profile.display_name,
    shortBio: user.profile.short_bio,
    thumbnail: user.profile.thumbnail,
    title: user.velog_config.title ?? `${user.username}'s Velog`,
  };
}

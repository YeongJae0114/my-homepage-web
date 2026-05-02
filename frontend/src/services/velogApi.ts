import { mapVelogPostsToPosts } from "../adapters/velogAdapter";
import type { VelogPostsResponse } from "../types/velog";

const defaultVelogApiUrl = "/velog/graphql";
const defaultVelogUsername = "yjl8628";
const defaultPostLimit = 10;

const postsQuery = `
  query Posts($username: String!, $limit: Int) {
    posts(username: $username, limit: $limit) {
      id
      title
      short_description
      url_slug
      tags
      released_at
    }
  }
`;

export async function getVelogPosts() {
  const username = import.meta.env.VITE_VELOG_USERNAME ?? defaultVelogUsername;
  const limit = Number(import.meta.env.VITE_VELOG_POST_LIMIT ?? defaultPostLimit);
  const endpoint = import.meta.env.VITE_VELOG_API_URL ?? defaultVelogApiUrl;

  const response = await fetch(endpoint, {
    method: "POST",
    headers: {
      "Content-Type": "application/json",
      Accept: "application/json",
    },
    body: JSON.stringify({
      query: postsQuery,
      variables: {
        username,
        limit,
      },
    }),
  });

  if (!response.ok) {
    throw new Error(`Velog API request failed: ${response.status} ${response.statusText}`);
  }

  const json = (await response.json()) as VelogPostsResponse;

  if (json.errors?.length) {
    throw new Error(json.errors.map((error) => error.message).join(", "));
  }

  return mapVelogPostsToPosts(json.data?.posts ?? [], username);
}

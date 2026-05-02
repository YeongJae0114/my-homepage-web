import { mapVelogPostsToPosts, mapVelogUserToProfile } from "../adapters/velogAdapter";
import type { VelogPostsResponse, VelogUserResponse } from "../types/velog";

const defaultVelogApiUrl = "/velog/graphql";
const defaultVelogUsername = "yjl8628";
const defaultPostLimit = 10;

const postsQuery = `
  query Posts($username: String!, $limit: Int, $cursor: ID) {
    posts(username: $username, limit: $limit, cursor: $cursor) {
      id
      title
      short_description
      url_slug
      tags
      released_at
    }
  }
`;

const userQuery = `
  query User($username: String!) {
    user(username: $username) {
      id
      username
      profile {
        display_name
        short_bio
        thumbnail
      }
      velog_config {
        title
        logo_image
      }
    }
  }
`;

function getVelogConfig() {
  const username = import.meta.env.VITE_VELOG_USERNAME ?? defaultVelogUsername;
  const limit = Number(import.meta.env.VITE_VELOG_POST_LIMIT ?? defaultPostLimit);
  const endpoint = import.meta.env.VITE_VELOG_API_URL ?? defaultVelogApiUrl;

  return { username, limit, endpoint };
}

async function requestVelog<T>(body: object, endpoint: string): Promise<T> {
  const response = await fetch(endpoint, {
    method: "POST",
    headers: {
      "Content-Type": "application/json",
      Accept: "application/json",
    },
    body: JSON.stringify(body),
  });

  if (!response.ok) {
    throw new Error(`Velog API request failed: ${response.status} ${response.statusText}`);
  }

  return response.json() as Promise<T>;
}

export async function getVelogPostsPage(cursor?: string | null) {
  const { username, limit, endpoint } = getVelogConfig();
  const json = await requestVelog<VelogPostsResponse>(
    {
      query: postsQuery,
      variables: {
        username,
        limit,
        cursor,
      },
    },
    endpoint,
  );

  if (json.errors?.length) {
    throw new Error(json.errors.map((error) => error.message).join(", "));
  }

  const rawPosts = json.data?.posts ?? [];

  return {
    posts: mapVelogPostsToPosts(rawPosts, username),
    nextCursor: rawPosts.at(-1)?.id ?? null,
    hasMore: rawPosts.length >= limit,
  };
}

export async function getVelogProfile() {
  const { username, endpoint } = getVelogConfig();
  const json = await requestVelog<VelogUserResponse>(
    {
      query: userQuery,
      variables: {
        username,
      },
    },
    endpoint,
  );

  if (json.errors?.length) {
    throw new Error(json.errors.map((error) => error.message).join(", "));
  }

  if (!json.data?.user) {
    throw new Error("Velog user profile is empty");
  }

  return mapVelogUserToProfile(json.data.user);
}

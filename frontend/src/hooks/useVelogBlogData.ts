import { useCallback, useEffect, useState } from "react";
import { blogPageFallback } from "../data/pageFallbacks";
import { getVelogPostsPage, getVelogProfile } from "../services/velogApi";
import type { BlogPageViewModel } from "../types/pages";
import type { VelogBlogProfile } from "../types/velog";

type VelogBlogDataState = {
  data: BlogPageViewModel;
  profile: VelogBlogProfile | null;
  isLoading: boolean;
  isLoadingMore: boolean;
  isFallback: boolean;
  hasMore: boolean;
  error: string | null;
  loadMore: () => Promise<void>;
};

export function useVelogBlogData(): VelogBlogDataState {
  const [data, setData] = useState<BlogPageViewModel>(blogPageFallback);
  const [profile, setProfile] = useState<VelogBlogProfile | null>(null);
  const [cursor, setCursor] = useState<string | null>(null);
  const [hasMore, setHasMore] = useState(false);
  const [isLoading, setIsLoading] = useState(true);
  const [isLoadingMore, setIsLoadingMore] = useState(false);
  const [isFallback, setIsFallback] = useState(true);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    let isMounted = true;

    async function loadInitialData() {
      try {
        const [profileResult, postsResult] = await Promise.all([getVelogProfile(), getVelogPostsPage()]);

        if (!isMounted) {
          return;
        }

        setProfile(profileResult);
        setData({
          posts: postsResult.posts,
          mediaItems: blogPageFallback.mediaItems,
        });
        setCursor(postsResult.nextCursor);
        setHasMore(postsResult.hasMore);
        setIsFallback(false);
        setError(null);
      } catch (unknownError) {
        if (!isMounted) {
          return;
        }

        setData(blogPageFallback);
        setProfile(null);
        setCursor(null);
        setHasMore(false);
        setIsFallback(true);
        setError(unknownError instanceof Error ? unknownError.message : "Unknown Velog API error");
      } finally {
        if (isMounted) {
          setIsLoading(false);
        }
      }
    }

    loadInitialData();

    return () => {
      isMounted = false;
    };
  }, []);

  const loadMore = useCallback(async () => {
    if (!cursor || isLoadingMore || !hasMore) {
      return;
    }

    setIsLoadingMore(true);

    try {
      const result = await getVelogPostsPage(cursor);

      setData((current) => ({
        ...current,
        posts: [...current.posts, ...result.posts],
      }));
      setCursor(result.nextCursor);
      setHasMore(result.hasMore);
      setError(null);
    } catch (unknownError) {
      setError(unknownError instanceof Error ? unknownError.message : "Unknown Velog pagination error");
    } finally {
      setIsLoadingMore(false);
    }
  }, [cursor, hasMore, isLoadingMore]);

  return {
    data,
    profile,
    isLoading,
    isLoadingMore,
    isFallback,
    hasMore,
    error,
    loadMore,
  };
}

import { BlogPreview } from "../components/sections/BlogPreview";
import { MediaPreview } from "../components/sections/MediaPreview";
import { useVelogBlogData } from "../hooks/useVelogBlogData";

export function BlogPage() {
  const { data, profile, isLoading, isLoadingMore, hasMore, loadMore } = useVelogBlogData();

  return (
    <>
      <BlogPreview
        posts={data.posts}
        profile={profile}
        isLoading={isLoading}
        isLoadingMore={isLoadingMore}
        hasMore={hasMore}
        onLoadMore={loadMore}
      />
      <MediaPreview items={data.mediaItems} />
    </>
  );
}

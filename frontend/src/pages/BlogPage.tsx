import { BlogPreview } from "../components/sections/BlogPreview";
import { MediaPreview } from "../components/sections/MediaPreview";
import { useBlogPageData } from "../hooks/usePageData";

export function BlogPage() {
  const { data } = useBlogPageData();

  return (
    <>
      <BlogPreview posts={data.posts} />
      <MediaPreview items={data.mediaItems} />
    </>
  );
}

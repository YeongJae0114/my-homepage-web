import type { Post } from "../../types/content";
import type { VelogBlogProfile } from "../../types/velog";
import { siteConfig } from "../../config/site";
import { Badge } from "../common/Badge";
import { Button } from "../common/Button";
import { Card } from "../common/Card";
import { Container } from "../common/Container";
import { SectionTitle } from "../common/SectionTitle";

type BlogPreviewProps = {
  posts: Post[];
  profile: VelogBlogProfile | null;
  isLoading?: boolean;
  isLoadingMore?: boolean;
  hasMore?: boolean;
  onLoadMore?: () => Promise<void>;
};

export function BlogPreview({
  posts,
  profile,
  isLoading = false,
  isLoadingMore = false,
  hasMore = false,
  onLoadMore,
}: BlogPreviewProps) {
  const authorName = profile?.displayName ?? siteConfig.nickname;
  const authorInitial = authorName.slice(0, 1).toUpperCase();

  return (
    <section id="blog" className="section-shell bg-surface-900">
      <Container>
        <div className="mb-10 grid gap-6 lg:grid-cols-[1fr_auto] lg:items-end">
          <SectionTitle
            eyebrow="Blog"
            title="기술 기록과 트러블슈팅 노트"
            description="Velog GraphQL API에서 가져온 최신 글을 1단 리스트로 보여줍니다."
          />
          {profile ? (
            <Card className="flex min-w-0 items-center gap-4 p-4 lg:max-w-sm">
              {profile.thumbnail ? (
                <img
                  src={profile.thumbnail}
                  alt={`${profile.displayName} profile`}
                  className="h-14 w-14 rounded-md border border-white/10 object-cover"
                />
              ) : null}
              <div className="min-w-0">
                <p className="truncate font-semibold text-zinc-50">{profile.displayName}</p>
                <p className="mt-1 line-clamp-2 text-sm text-zinc-400">{profile.title}</p>
              </div>
            </Card>
          ) : null}
        </div>

        <div className="grid gap-4">
          {posts.map((post) => (
            <a key={post.id} href={post.url} className="block">
              <Card interactive className="grid gap-5 p-5 sm:p-6 lg:grid-cols-[1fr_auto] lg:items-start">
                <div className="min-w-0">
                  <div className="flex flex-wrap items-center gap-3">
                    <time
                      dateTime={post.publishedAt}
                      className="inline-flex rounded-md border border-emerald-300/20 bg-emerald-300/[0.08] px-3 py-1.5 font-mono text-sm font-semibold text-emerald-100"
                    >
                      {post.publishedAt}
                    </time>
                    <div className="flex min-w-0 items-center gap-2 text-sm text-zinc-400">
                      {profile?.thumbnail ? (
                        <img
                          src={profile.thumbnail}
                          alt={`${authorName} profile`}
                          className="h-7 w-7 shrink-0 rounded-md border border-white/10 object-cover"
                        />
                      ) : (
                        <span className="grid h-7 w-7 shrink-0 place-items-center rounded-md border border-white/10 bg-white/[0.06] font-mono text-xs font-semibold text-zinc-100">
                          {authorInitial}
                        </span>
                      )}
                      <span className="min-w-0 truncate font-medium text-zinc-300">{authorName}</span>
                    </div>
                  </div>
                  <h3 className="mt-3 break-words text-lg font-semibold leading-snug text-zinc-50 sm:text-xl">{post.title}</h3>
                  <p className="mt-3 text-sm leading-6 text-zinc-400">{post.summary}</p>
                </div>
                <div className="flex flex-wrap gap-2 lg:max-w-xs lg:justify-end">
                  {post.tags.slice(0, 6).map((tag) => (
                    <Badge key={tag}>{tag}</Badge>
                  ))}
                </div>
              </Card>
            </a>
          ))}
        </div>

        <div className="mt-8 flex justify-center">
          {hasMore ? (
            <Button
              href="#"
              variant="secondary"
              aria-disabled={isLoadingMore}
              onClick={(event) => {
                event.preventDefault();
                onLoadMore?.();
              }}
            >
              {isLoadingMore ? "불러오는 중..." : "더보기"}
            </Button>
          ) : (
            <p className="text-sm text-zinc-500">{isLoading ? "글을 불러오는 중입니다." : "표시할 수 있는 글을 모두 불러왔습니다."}</p>
          )}
        </div>
      </Container>
    </section>
  );
}

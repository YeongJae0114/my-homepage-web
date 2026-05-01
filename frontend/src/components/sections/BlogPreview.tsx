import type { Post } from "../../types/content";
import { Badge } from "../common/Badge";
import { Card } from "../common/Card";
import { Container } from "../common/Container";
import { SectionTitle } from "../common/SectionTitle";

type BlogPreviewProps = {
  posts: Post[];
};

export function BlogPreview({ posts }: BlogPreviewProps) {
  return (
    <section id="blog" className="section-shell bg-surface-900">
      <Container>
        <SectionTitle eyebrow="Blog" title="기술 기록과 트러블슈팅 노트" description="Markdown, CMS, 외부 블로그 API로 바뀌어도 Post 타입과 contentApi만 교체하면 되는 구조입니다." />
        <div className="grid gap-4 md:grid-cols-3">
          {posts.map((post) => (
            <a key={post.id} href={post.url} className="block">
              <Card interactive className="h-full p-5">
                <p className="font-mono text-xs text-zinc-500">{post.publishedAt}</p>
                <h3 className="mt-3 text-lg font-semibold leading-snug text-zinc-50">{post.title}</h3>
                <p className="mt-3 text-sm leading-6 text-zinc-400">{post.summary}</p>
                <div className="mt-5 flex flex-wrap gap-2">
                  {post.tags.map((tag) => (
                    <Badge key={tag}>{tag}</Badge>
                  ))}
                </div>
              </Card>
            </a>
          ))}
        </div>
      </Container>
    </section>
  );
}

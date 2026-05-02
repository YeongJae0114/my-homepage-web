import type { RetrievalSource } from "../../types/llm";

type SourceListProps = {
  sources: RetrievalSource[];
};

export function SourceList({ sources }: SourceListProps) {
  return (
    <ul className="grid gap-2">
      {sources.map((source) => (
        <li key={source.id} className="break-words rounded-md border border-white/10 px-3 py-2 text-sm text-zinc-300">
          {source.title}
        </li>
      ))}
    </ul>
  );
}

import type { RetrievalSource } from "../../types/llm";
import { Card } from "../common/Card";

type RetrievalResultCardProps = {
  source: RetrievalSource;
};

export function RetrievalResultCard({ source }: RetrievalResultCardProps) {
  return (
    <Card className="p-4">
      <h3 className="font-semibold text-zinc-50">{source.title}</h3>
      <p className="mt-2 text-sm leading-6 text-zinc-400">{source.excerpt}</p>
      <p className="mt-3 font-mono text-xs text-zinc-500">score {source.score.toFixed(2)}</p>
    </Card>
  );
}

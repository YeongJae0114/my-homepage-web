import { Card } from "../common/Card";
import { Badge } from "../common/Badge";

export function ModelStatusCard() {
  return (
    <Card className="p-5">
      <div className="flex items-center justify-between gap-4">
        <div>
          <p className="font-mono text-xs uppercase text-zinc-500">model status</p>
          <h3 className="mt-2 text-lg font-semibold text-zinc-50">Local model not connected</h3>
        </div>
        <Badge>planned</Badge>
      </div>
      <p className="mt-3 text-sm leading-6 text-zinc-400">
        llmApi 연결 후 모델 상태, 검색 결과, 출처 목록을 이 영역에 연결할 수 있습니다.
      </p>
    </Card>
  );
}

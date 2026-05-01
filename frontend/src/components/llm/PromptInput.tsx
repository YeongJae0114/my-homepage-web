export function PromptInput() {
  return (
    <label className="block">
      <span className="mb-2 block text-sm font-medium text-zinc-300">Prompt</span>
      <textarea
        className="min-h-28 w-full rounded-md border border-white/10 bg-surface-900 px-3 py-2 text-sm text-zinc-100"
        placeholder="서버 운영 기록이나 코드에 대해 질문하기"
      />
    </label>
  );
}

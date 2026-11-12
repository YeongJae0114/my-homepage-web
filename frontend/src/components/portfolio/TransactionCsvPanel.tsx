import { useState, type ChangeEvent } from "react";
import { usePortfolioErrorHandler } from "../../hooks/usePortfolioResource";
import { ApiError } from "../../services/apiClient";
import { portfolioApi, type Account, type Instrument, type TransactionCsvFile, type TransactionCsvImport } from "../../services/portfolioApi";
import { ActionButton, Field, FormMessage, Panel, TextInput } from "./fields";

const MAX_FILE_BYTES = 5 * 1024 * 1024;

async function readCsv(file: File): Promise<string> {
  if (file.size > MAX_FILE_BYTES) {
    throw new Error("CSV 파일을 5MB 이하로 나눠서 가져오세요.");
  }
  const bytes = await file.arrayBuffer();
  try {
    return new TextDecoder("utf-8", { fatal: true }).decode(bytes);
  } catch {
    try {
      return new TextDecoder("euc-kr", { fatal: true }).decode(bytes);
    } catch {
      throw new Error("파일을 읽을 수 없습니다. CSV UTF-8 형식으로 다시 저장하세요.");
    }
  }
}

function downloadCsv({ filename, content }: TransactionCsvFile) {
  const url = URL.createObjectURL(new Blob([content], { type: "text/csv;charset=utf-8" }));
  const link = document.createElement("a");
  link.href = url;
  link.download = filename;
  document.body.append(link);
  link.click();
  link.remove();
  window.setTimeout(() => URL.revokeObjectURL(url), 1000);
}

type TransactionCsvPanelProps = {
  accounts: Account[];
  instruments: Instrument[];
  onImported: () => Promise<void>;
};

type PendingImport = { filename: string; content: string; preview: TransactionCsvImport };

export function TransactionCsvPanel({ accounts, instruments, onImported }: TransactionCsvPanelProps) {
  const handleError = usePortfolioErrorHandler();
  const [busy, setBusy] = useState(false);
  const [pending, setPending] = useState<PendingImport | null>(null);
  const [message, setMessage] = useState<{ error?: string; success?: string }>({});

  function showError(error: unknown) {
    const fieldError = error instanceof ApiError ? error.fieldErrors.content : undefined;
    setMessage({ error: fieldError ?? handleError(error) });
  }

  async function download(load: () => Promise<TransactionCsvFile>) {
    setBusy(true);
    setMessage({});
    try {
      downloadCsv(await load());
    } catch (error) {
      showError(error);
    } finally {
      setBusy(false);
    }
  }

  async function selectFile(event: ChangeEvent<HTMLInputElement>) {
    const file = event.target.files?.[0];
    event.target.value = "";
    if (!file) return;

    setPending(null);
    setMessage({});
    setBusy(true);
    try {
      const content = await readCsv(file);
      if (!content.trim()) throw new Error("CSV 파일이 비어 있습니다.");
      const preview = await portfolioApi.previewTransactionsCsv(content);
      setPending({ filename: file.name, content, preview });
    } catch (error) {
      showError(error);
    } finally {
      setBusy(false);
    }
  }

  async function apply() {
    if (!pending) return;
    setBusy(true);
    setMessage({});
    try {
      const result = await portfolioApi.importTransactionsCsv(pending.content);
      setPending(null);
      setMessage({ success: "거래 " + result.newRows + "행을 저장했습니다. 기존 기록 " + result.duplicateRows + "행을 건너뛰었습니다." });
      await onImported();
    } catch (error) {
      showError(error);
    } finally {
      setBusy(false);
    }
  }

  return (
    <Panel
      title="거래 CSV"
      description="양식에 거래를 입력하고 파일을 선택하세요. 미리보기에서 확인한 뒤 저장할 수 있습니다."
      actions={
        <>
          <ActionButton disabled={busy} onClick={() => void download(portfolioApi.getTransactionCsvTemplate)}>양식 다운로드</ActionButton>
          <ActionButton disabled={busy} onClick={() => void download(portfolioApi.exportTransactionsCsv)}>전체 거래 내보내기</ActionButton>
        </>
      }
    >
      <div className="grid gap-4">
        <details className="text-sm text-zinc-400">
          <summary className="w-fit cursor-pointer text-zinc-200">양식 작성 방법과 계좌·종목 코드</summary>
          <div className="mt-3 grid gap-2">
            <p>거래일은 2026-01-15처럼 입력하세요. 계좌에는 아래의 등록 ID를, 종목에는 KRX/069500처럼 시장과 종목코드를 함께 입력하세요.</p>
            <p>유형은 기초잔고, 매수, 매도, 입금, 출금, 이체, 분배금·배당, 이자, 수수료, 세금, 대출 실행, 원금 상환 중 하나를 입력하세요.</p>
            <p>매수·매도는 수량과 단가를 입력하세요. 외화 종목은 체결환율도 필요합니다. 매매 정산금액은 비우면 자동 계산합니다.</p>
            <p>이체는 계좌에 보내는 계좌 ID를, 입금계좌에 받는 계좌 ID를 입력하세요. 양식의 열 제목과 순서를 유지하고 CSV UTF-8로 저장하세요.</p>
            <div className="grid gap-3 sm:grid-cols-2">
              <div>
                <p className="font-medium text-zinc-200">사이트 계좌 ID</p>
                <ul className="mt-1 max-h-28 overflow-auto">
                  {accounts.map((account) => <li key={account.id}>{account.id} · {account.name}</li>)}
                </ul>
              </div>
              <div>
                <p className="font-medium text-zinc-200">종목 코드</p>
                <ul className="mt-1 max-h-28 overflow-auto">
                  {instruments.map((instrument) => <li key={instrument.id}>{instrument.market}/{instrument.symbol} · {instrument.name}</li>)}
                </ul>
              </div>
            </div>
          </div>
        </details>
        <Field label="CSV 가져오기" hint="UTF-8 또는 한글 Windows CSV · 최대 5MB">
          <TextInput type="file" accept=".csv,text/csv" disabled={busy} onChange={(event) => void selectFile(event)} />
        </Field>
        <FormMessage error={message.error} success={message.success} />
        {pending ? (
          <div className="grid gap-3">
            <p className="break-all text-sm text-zinc-200" role="status">
              {pending.filename} · 새 거래 {pending.preview.newRows}행 · 기존 기록 {pending.preview.duplicateRows}행
            </p>
            {pending.preview.rows.length ? (
              <div className="overflow-x-auto">
                <table className="w-full min-w-[580px] text-left text-sm">
                  <thead className="text-xs text-zinc-400">
                    <tr className="border-b border-white/10">
                      <th className="py-2 pr-2" scope="col">행</th>
                      <th className="py-2 pr-2" scope="col">거래일</th>
                      <th className="py-2 pr-2" scope="col">계좌</th>
                      <th className="py-2 pr-2" scope="col">유형·종목</th>
                      <th className="py-2 pr-2 text-right" scope="col">정산금액</th>
                      <th className="py-2" scope="col">상태</th>
                    </tr>
                  </thead>
                  <tbody>
                    {pending.preview.rows.map((row) => (
                      <tr className="border-b border-white/5 text-zinc-300" key={row.line}>
                        <td className="py-2 pr-2">{row.line}</td>
                        <td className="py-2 pr-2 font-mono">{row.tradeDate}</td>
                        <td className="py-2 pr-2">{row.account}{row.toAccount ? " → " + row.toAccount : ""}</td>
                        <td className="py-2 pr-2">{row.type}{row.instrument ? " · " + row.instrument : ""}</td>
                        <td className="py-2 pr-2 text-right font-mono tabular-nums">{row.amount}원</td>
                        <td className="py-2">{row.duplicate ? "건너뜀" : "저장 예정"}</td>
                      </tr>
                    ))}
                  </tbody>
                </table>
              </div>
            ) : <p className="text-sm text-zinc-400">파일에 입력된 거래가 없습니다.</p>}
            {pending.preview.newRows + pending.preview.duplicateRows > pending.preview.rows.length ? (
              <p className="text-sm text-zinc-400">처음 {pending.preview.rows.length}행의 미리보기입니다.</p>
            ) : null}
            <div className="flex flex-wrap gap-2">
              <ActionButton variant="primary" disabled={busy || pending.preview.newRows === 0} onClick={() => void apply()}>
                {busy ? "처리 중…" : "새 거래 저장"}
              </ActionButton>
              <ActionButton variant="ghost" disabled={busy} onClick={() => setPending(null)}>취소</ActionButton>
            </div>
          </div>
        ) : busy ? <p className="text-sm text-zinc-400" role="status">CSV 처리 중…</p> : null}
      </div>
    </Panel>
  );
}

import { useCallback, useEffect, useMemo, useState, type FormEvent } from "react";
import { usePortfolioErrorHandler, usePortfolioResource } from "../../hooks/usePortfolioResource";
import { ApiError } from "../../services/apiClient";
import { portfolioApi, type Transaction, type TransactionInput, type TransactionType } from "../../services/portfolioApi";
import {
  accountTypeLabels,
  allowedTransactionTypes,
  formatKrw,
  formatNumber,
  parseNumber,
  todayInSeoul,
  transactionTypeLabels,
} from "./labels";
import { ActionButton, EmptyState, Field, FormMessage, NumberInput, Panel, SelectInput, TextInput } from "./fields";
import { TransactionCsvPanel } from "./TransactionCsvPanel";

const loadAccounts = () => portfolioApi.getAccounts();
const loadInstruments = () => portfolioApi.getInstruments();
const PAGE_SIZE = 30;

type TxForm = {
  accountId: string;
  type: TransactionType | "";
  tradeDate: string;
  openingKind: "cash" | "position";
  instrumentId: string;
  quantity: string;
  price: string;
  fxRate: string;
  amount: string;
  fee: string;
  tax: string;
  memo: string;
  useLatestPrice: boolean;
};

const blankForm = (accountId = ""): TxForm => ({
  accountId,
  type: "",
  tradeDate: todayInSeoul(),
  openingKind: "position",
  instrumentId: "",
  quantity: "",
  price: "",
  fxRate: "",
  amount: "",
  fee: "",
  tax: "",
  memo: "",
  useLatestPrice: true,
});

const LOCK_HINT = "다음 스냅샷(평일 18:00)에 반영되어 잠겼습니다. 정정은 반대 거래(예: 매수↔매도, 입금↔출금)로 입력하세요.";

type TransactionsPanelProps = {
  initialAccountId: number | null;
};

export function TransactionsPanel({ initialAccountId }: TransactionsPanelProps) {
  const { data: accounts } = usePortfolioResource(loadAccounts);
  const { data: instruments } = usePortfolioResource(loadInstruments);
  const handleError = usePortfolioErrorHandler();
  const [filterAccountId, setFilterAccountId] = useState<number | null>(initialAccountId);
  const [page, setPage] = useState(0);
  const loadTransactions = useCallback(
    () => portfolioApi.getTransactions({ accountId: filterAccountId ?? undefined, page, size: PAGE_SIZE }),
    [filterAccountId, page],
  );
  const { data: transactions, error: loadError, reload } = usePortfolioResource(loadTransactions);

  const [mode, setMode] = useState<"single" | "transfer">("single");
  const [form, setForm] = useState<TxForm>(() => blankForm(initialAccountId ? String(initialAccountId) : ""));
  const [editingId, setEditingId] = useState<number | null>(null);
  const [keepEntering, setKeepEntering] = useState(false);
  const [fieldErrors, setFieldErrors] = useState<Record<string, string>>({});
  const [message, setMessage] = useState<{ error?: string; success?: string }>({});
  const [saving, setSaving] = useState(false);

  const accountById = useMemo(() => new Map(accounts?.map((account) => [account.id, account]) ?? []), [accounts]);
  const instrumentById = useMemo(() => new Map(instruments?.map((instrument) => [instrument.id, instrument]) ?? []), [instruments]);

  const account = accountById.get(Number(form.accountId));
  const allowedTypes = account ? allowedTransactionTypes(account.accountType) : [];
  const type = form.type && allowedTypes.includes(form.type) ? form.type : (allowedTypes[0] ?? "");
  const positionEntry = type === "BUY" || type === "SELL" || type === "DIVIDEND" || (type === "OPENING" && form.openingKind === "position" && !!account?.investment);
  const priced = type === "BUY" || type === "SELL" || (type === "OPENING" && positionEntry);
  const instrument = instrumentById.get(Number(form.instrumentId));
  const foreign = !!instrument && instrument.tradingCurrency !== "KRW";

  const automaticOpening = !editingId && type === "OPENING" && positionEntry
    && form.tradeDate === todayInSeoul() && form.useLatestPrice;

  useEffect(() => {
    if (initialAccountId) {
      setFilterAccountId(initialAccountId);
      setForm((current) => ({ ...current, accountId: String(initialAccountId) }));
    }
  }, [initialAccountId]);

  function update<K extends keyof TxForm>(key: K, value: TxForm[K]) {
    setForm((current) => ({ ...current, [key]: value }));
  }

  function startEdit(transaction: Transaction) {
    setMode("single");
    setEditingId(transaction.id);
    setFieldErrors({});
    setMessage({});
    setForm({
      accountId: String(transaction.accountId),
      type: transaction.type,
      tradeDate: transaction.tradeDate,
      openingKind: transaction.instrumentId ? "position" : "cash",
      instrumentId: transaction.instrumentId ? String(transaction.instrumentId) : "",
      quantity: transaction.quantity?.toString() ?? "",
      price: transaction.price?.toString() ?? "",
      fxRate: transaction.fxRate && transaction.fxRate !== 1 ? transaction.fxRate.toString() : "",
      amount: transaction.amount.toString(),
      fee: transaction.fee ? transaction.fee.toString() : "",
      tax: transaction.tax ? transaction.tax.toString() : "",
      memo: transaction.memo ?? "",
      useLatestPrice: false,
    });
    window.scrollTo({ top: 0, behavior: "smooth" });
  }

  function cancelEdit() {
    setEditingId(null);
    setForm(blankForm(form.accountId));
  }

  async function handleSubmit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    if (!account || !type) {
      setMessage({ error: "계좌와 거래 유형을 선택하세요." });
      return;
    }
    const input: TransactionInput = {
      accountId: account.id,
      type,
      tradeDate: form.tradeDate,
      instrumentId: positionEntry && form.instrumentId ? Number(form.instrumentId) : null,
      quantity: priced ? parseNumber(form.quantity) : null,
      price: priced && !automaticOpening ? parseNumber(form.price) : null,
      fxRate: priced && foreign && !automaticOpening ? parseNumber(form.fxRate) : null,
      amount: automaticOpening ? null : parseNumber(form.amount),
      fee: automaticOpening ? null : parseNumber(form.fee),
      tax: automaticOpening ? null : parseNumber(form.tax),
      memo: form.memo || null,
      useLatestPrice: automaticOpening,
    };

    setSaving(true);
    setFieldErrors({});
    try {
      const saved = editingId
        ? await portfolioApi.updateTransaction(editingId, input)
        : await portfolioApi.createTransaction(input);
      const what = instrument && positionEntry ? `${instrument.name} ` : "";
      setMessage({ success: `${saved.tradeDate} ${what}${transactionTypeLabels[saved.type]} ${formatKrw(saved.amount)} ${editingId ? "수정" : "저장"}` });
      setEditingId(null);
      setForm((current) =>
        keepEntering && !editingId
          ? { ...blankForm(current.accountId), type: current.type, tradeDate: current.tradeDate, openingKind: current.openingKind }
          : blankForm(current.accountId),
      );
      await reload();
    } catch (error) {
      setFieldErrors(error instanceof ApiError ? error.fieldErrors : {});
      setMessage({ error: handleError(error) });
    } finally {
      setSaving(false);
    }
  }

  async function handleDelete(transaction: Transaction) {
    const label = `${transaction.tradeDate} ${transactionTypeLabels[transaction.type]} ${formatKrw(transaction.amount)}`;
    const pair = transaction.transferGroupId ? "\n이체는 출금·입금 양쪽이 함께 삭제됩니다." : "";
    if (!window.confirm(`${label} 거래를 삭제할까요?${pair}`)) return;
    try {
      await portfolioApi.deleteTransaction(transaction.id);
      setMessage({ success: `${label} 삭제` });
      await reload();
    } catch (error) {
      setMessage({ error: handleError(error) });
    }
  }

  const accountOptions = accounts?.filter((candidate) => !candidate.closedAt) ?? [];
  const activeInstruments = instruments?.filter((candidate) => candidate.active) ?? [];
  const totalPages = transactions ? Math.max(1, Math.ceil(transactions.totalElements / PAGE_SIZE)) : 1;

  return (
    <div className="grid gap-6">
      <Panel
        title={editingId ? "거래 수정" : "거래 입력"}
        description="매매·입출금이 생기면 한 건씩 기록하세요. 저장 즉시 수량과 잔액에 반영됩니다. 스냅샷(평일 18:00)에 반영되기 전에는 아래 내역에서 수정·삭제할 수 있습니다."
        actions={
          editingId ? null : (
            <div className="flex rounded-md border border-white/10 p-0.5 text-sm">
              {(["single", "transfer"] as const).map((value) => (
                <button
                  key={value}
                  type="button"
                  className={`rounded px-3 py-1.5 ${mode === value ? "bg-white/10 text-zinc-50" : "text-zinc-400"}`}
                  onClick={() => setMode(value)}
                >
                  {value === "single" ? "일반 거래" : "계좌 간 이체"}
                </button>
              ))}
            </div>
          )
        }
      >
        <div className="grid gap-4">
          <FormMessage error={message.error} success={message.success} />
          {mode === "transfer" && !editingId ? (
            <TransferForm accounts={accountOptions} onSaved={async (text) => { setMessage({ success: text }); await reload(); }} onError={(error) => setMessage({ error: handleError(error) })} />
          ) : (
            <form className="grid gap-4" onSubmit={handleSubmit}>
              <div className="grid gap-4 sm:grid-cols-2 lg:grid-cols-4">
                <Field label="계좌">
                  <SelectInput required value={form.accountId} disabled={!!editingId} onChange={(event) => update("accountId", event.target.value)}>
                    <option value="">선택</option>
                    {accountOptions.map((candidate) => (
                      <option key={candidate.id} value={candidate.id}>
                        {candidate.name} ({accountTypeLabels[candidate.accountType]})
                      </option>
                    ))}
                  </SelectInput>
                </Field>
                <Field label="거래 유형">
                  <SelectInput value={type} disabled={!account || allowedTypes.length === 0} onChange={(event) => update("type", event.target.value as TransactionType)}>
                    {allowedTypes.map((value) => (
                      <option key={value} value={value}>
                        {transactionTypeLabels[value]}
                      </option>
                    ))}
                  </SelectInput>
                </Field>
                <Field label="거래일" error={fieldErrors.tradeDate}>
                  <TextInput type="date" required max={todayInSeoul()} value={form.tradeDate} onChange={(event) => update("tradeDate", event.target.value)} />
                </Field>
                {type === "OPENING" && account?.investment ? (
                  <Field label="기초잔고 종류">
                    <SelectInput value={form.openingKind} onChange={(event) => update("openingKind", event.target.value as TxForm["openingKind"])}>
                      <option value="position">보유 종목</option>
                      <option value="cash">예수금 (현금)</option>
                    </SelectInput>
                  </Field>
                ) : null}
              </div>

              {type === "OPENING" && positionEntry && !editingId && form.tradeDate === todayInSeoul() ? (
                <div className="rounded-md border border-white/10 p-3 text-sm text-zinc-300">
                  <label className="flex items-center gap-2">
                    <input type="checkbox" checked={form.useLatestPrice} onChange={(event) => update("useLatestPrice", event.target.checked)} />
                    최근 시세를 시작 기준가로 사용
                  </label>
                  <p className="mt-2 text-xs text-zinc-400">종목과 수량만 입력하면 저장된 최근 시세·환율을 사용합니다. 시세가 없으면 API로 조회합니다. 과거 매입 손익은 포함하지 않고 이 기준가 이후의 변화를 추적합니다. 직접 입력하려면 선택을 해제하세요.</p>
                </div>
              ) : null}

              {account?.accountType === "INSURANCE" ? (
                <p className="text-sm text-zinc-400">보험은 거래가 아니라 계좌 탭의 '해약환급금 입력'으로 기록합니다.</p>
              ) : null}
              {account?.accountType === "LOAN" ? (
                <p className="text-sm text-zinc-400">대출 시작 잔액은 '대출 실행'으로 입력하세요.</p>
              ) : null}

              {account && type ? (
                <div className="grid gap-4 sm:grid-cols-2 lg:grid-cols-4">
                  {positionEntry ? (
                    <Field label="종목" hint="목록에 없으면 종목 탭에서 먼저 등록" className="sm:col-span-2">
                      <SelectInput required value={form.instrumentId} onChange={(event) => update("instrumentId", event.target.value)}>
                        <option value="">선택</option>
                        {activeInstruments.map((candidate) => (
                          <option key={candidate.id} value={candidate.id}>
                            {candidate.name} ({candidate.symbol})
                          </option>
                        ))}
                      </SelectInput>
                    </Field>
                  ) : null}
                  {priced ? (
                    <>
                      <Field label="수량" error={fieldErrors.quantity}>
                        <NumberInput required value={form.quantity} onChange={(event) => update("quantity", event.target.value)} />
                      </Field>
                      {!automaticOpening ? <Field label={`${type === "OPENING" ? "평단 또는 시작 기준가" : "체결가"}${foreign ? ` (${instrument?.tradingCurrency})` : " (원)"}`} hint={type === "OPENING" ? "매입 평단을 모르면 오늘 가격으로 시작할 수 있습니다" : undefined} error={fieldErrors.price}>
                        <NumberInput required value={form.price} onChange={(event) => update("price", event.target.value)} />
                      </Field> : null}
                      {foreign && !automaticOpening ? (
                        <Field label={type === "OPENING" ? "매입 평균 환율" : "체결 환율"} hint="1달러당 원" error={fieldErrors.fxRate}>
                          <NumberInput required value={form.fxRate} placeholder="1,380" onChange={(event) => update("fxRate", event.target.value)} />
                        </Field>
                      ) : null}
                    </>
                  ) : null}
                  {type === "BUY" || type === "SELL" ? (
                    <Field label="수수료 (원)" error={fieldErrors.fee}>
                      <NumberInput value={form.fee} placeholder="0" onChange={(event) => update("fee", event.target.value)} />
                    </Field>
                  ) : null}
                  {type === "SELL" || type === "DIVIDEND" ? (
                    <Field label={type === "DIVIDEND" ? "원천징수 세액 (원)" : "세금 (원)"} error={fieldErrors.tax}>
                      <NumberInput value={form.tax} placeholder="0" onChange={(event) => update("tax", event.target.value)} />
                    </Field>
                  ) : null}
                  {!automaticOpening ? <Field
                    label={priced ? "정산금액 (선택)" : type === "DIVIDEND" ? "입금액 (세후, 원)" : "금액 (원)"}
                    hint={priced ? "비우면 서버에서 수량·가격·환율로 계산합니다. 증권사 정산금액이 있으면 입력하세요" : undefined}
                    error={fieldErrors.amount}
                  >
                    <NumberInput required={!priced} value={form.amount} onChange={(event) => update("amount", event.target.value)} />
                  </Field> : null}
                  <Field label="메모" className="sm:col-span-2">
                    <TextInput value={form.memo} maxLength={automaticOpening ? 480 : 500} onChange={(event) => update("memo", event.target.value)} />
                  </Field>
                </div>
              ) : null}

              <div className="flex flex-wrap items-center gap-3">
                <ActionButton type="submit" variant="primary" disabled={saving || !account || !type}>
                  {saving ? "저장 중…" : editingId ? "수정 저장" : "저장"}
                </ActionButton>
                {editingId ? (
                  <ActionButton variant="ghost" onClick={cancelEdit}>
                    취소
                  </ActionButton>
                ) : (
                  <label className="flex items-center gap-2 text-sm text-zinc-400">
                    <input type="checkbox" checked={keepEntering} onChange={(event) => setKeepEntering(event.target.checked)} />
                    저장 후 같은 계좌·유형·날짜로 계속 입력
                  </label>
                )}
              </div>
            </form>
          )}
        </div>
      </Panel>

      <TransactionCsvPanel accounts={accounts ?? []} instruments={instruments ?? []} onImported={reload} />

      <Panel
        title="거래 내역"
        actions={
          <SelectInput
            className="w-auto"
            value={filterAccountId ?? ""}
            onChange={(event) => {
              setFilterAccountId(event.target.value ? Number(event.target.value) : null);
              setPage(0);
            }}
          >
            <option value="">전체 계좌</option>
            {accounts?.map((candidate) => (
              <option key={candidate.id} value={candidate.id}>
                {candidate.name}
              </option>
            ))}
          </SelectInput>
        }
      >
        <FormMessage error={loadError} />
        {transactions && transactions.items.length === 0 ? <EmptyState>거래가 없습니다.</EmptyState> : null}
        {transactions && transactions.items.length > 0 ? (
          <div className="overflow-x-auto">
            <table className="w-full min-w-[720px] text-left text-sm">
              <thead className="text-xs text-zinc-500">
                <tr className="border-b border-white/10">
                  <th className="py-2 pr-3 font-medium">거래일</th>
                  <th className="py-2 pr-3 font-medium">계좌</th>
                  <th className="py-2 pr-3 font-medium">유형</th>
                  <th className="py-2 pr-3 font-medium">종목</th>
                  <th className="py-2 pr-3 text-right font-medium">수량</th>
                  <th className="py-2 pr-3 text-right font-medium">단가</th>
                  <th className="py-2 pr-3 text-right font-medium">금액</th>
                  <th className="py-2 font-medium" />
                </tr>
              </thead>
              <tbody>
                {transactions.items.map((transaction) => (
                  <tr key={transaction.id} className="border-b border-white/5 align-top">
                    <td className="py-2 pr-3 font-mono text-zinc-300">{transaction.tradeDate}</td>
                    <td className="py-2 pr-3 text-zinc-300">{accountById.get(transaction.accountId)?.name ?? `#${transaction.accountId}`}</td>
                    <td className="py-2 pr-3 text-zinc-100">{transactionTypeLabels[transaction.type]}</td>
                    <td className="py-2 pr-3 text-zinc-300">
                      {transaction.instrumentId ? instrumentById.get(transaction.instrumentId)?.name ?? `#${transaction.instrumentId}` : ""}
                      {transaction.memo ? <span className="block text-xs text-zinc-500">{transaction.memo}</span> : null}
                    </td>
                    <td className="py-2 pr-3 text-right font-mono tabular-nums">{transaction.quantity != null ? formatNumber(transaction.quantity) : ""}</td>
                    <td className="py-2 pr-3 text-right font-mono tabular-nums">{transaction.price != null ? formatNumber(transaction.price) : ""}</td>
                    <td className="py-2 pr-3 text-right font-mono tabular-nums text-zinc-100">{formatKrw(transaction.amount)}</td>
                    <td className="py-2 text-right">
                      {transaction.editable ? (
                        <div className="flex justify-end gap-1">
                          {transaction.transferGroupId ? null : (
                            <ActionButton variant="ghost" className="min-h-8 px-2" onClick={() => startEdit(transaction)}>
                              수정
                            </ActionButton>
                          )}
                          <ActionButton variant="ghost" className="min-h-8 px-2 text-rose-300" onClick={() => handleDelete(transaction)}>
                            삭제
                          </ActionButton>
                        </div>
                      ) : (
                        <span className="cursor-help text-xs text-zinc-500" title={LOCK_HINT}>
                          잠김
                        </span>
                      )}
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        ) : null}
        {totalPages > 1 ? (
          <div className="mt-4 flex items-center justify-end gap-2 text-sm text-zinc-400">
            <ActionButton variant="ghost" disabled={page === 0} onClick={() => setPage(page - 1)}>
              이전
            </ActionButton>
            {page + 1} / {totalPages}
            <ActionButton variant="ghost" disabled={page + 1 >= totalPages} onClick={() => setPage(page + 1)}>
              다음
            </ActionButton>
          </div>
        ) : null}
      </Panel>
    </div>
  );
}

type TransferFormProps = {
  accounts: { id: number; name: string; accountType: string }[];
  onSaved: (message: string) => Promise<void>;
  onError: (error: unknown) => void;
};

function TransferForm({ accounts, onSaved, onError }: TransferFormProps) {
  const transferable = accounts.filter((account) => account.accountType !== "LOAN" && account.accountType !== "INSURANCE");
  const [fromId, setFromId] = useState("");
  const [toId, setToId] = useState("");
  const [date, setDate] = useState(todayInSeoul());
  const [amount, setAmount] = useState("");
  const [memo, setMemo] = useState("");

  async function handleSubmit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    const value = parseNumber(amount);
    if (value == null) return;
    try {
      await portfolioApi.createTransfer({ fromAccountId: Number(fromId), toAccountId: Number(toId), tradeDate: date, amount: value, memo: memo || null });
      setAmount("");
      setMemo("");
      await onSaved(`${date} 이체 ${formatKrw(value)} 저장`);
    } catch (error) {
      onError(error);
    }
  }

  return (
    <form className="grid gap-4" onSubmit={handleSubmit}>
      <div className="grid gap-4 sm:grid-cols-2 lg:grid-cols-4">
        <Field label="출금 계좌">
          <SelectInput required value={fromId} onChange={(event) => setFromId(event.target.value)}>
            <option value="">선택</option>
            {transferable.map((account) => (
              <option key={account.id} value={account.id}>
                {account.name}
              </option>
            ))}
          </SelectInput>
        </Field>
        <Field label="입금 계좌">
          <SelectInput required value={toId} onChange={(event) => setToId(event.target.value)}>
            <option value="">선택</option>
            {transferable
              .filter((account) => String(account.id) !== fromId)
              .map((account) => (
                <option key={account.id} value={account.id}>
                  {account.name}
                </option>
              ))}
          </SelectInput>
        </Field>
        <Field label="이체일">
          <TextInput type="date" required max={todayInSeoul()} value={date} onChange={(event) => setDate(event.target.value)} />
        </Field>
        <Field label="금액 (원)">
          <NumberInput required value={amount} onChange={(event) => setAmount(event.target.value)} />
        </Field>
        <Field label="메모" className="sm:col-span-2 lg:col-span-4">
          <TextInput value={memo} maxLength={500} placeholder="예: 급여통장 → ISA 납입" onChange={(event) => setMemo(event.target.value)} />
        </Field>
      </div>
      <div>
        <ActionButton type="submit" variant="primary">
          이체 저장
        </ActionButton>
      </div>
    </form>
  );
}

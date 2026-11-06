import { useCallback, useState, type FormEvent } from "react";
import { usePortfolioErrorHandler, usePortfolioResource } from "../../hooks/usePortfolioResource";
import { ApiError } from "../../services/apiClient";
import { portfolioApi, type Account, type AccountInput, type AccountType, type RepaymentType } from "../../services/portfolioApi";
import {
  accountTypeLabels,
  formatKrw,
  formatNumber,
  parseNumber,
  repaymentTypeLabels,
  todayInSeoul,
} from "./labels";
import { ActionButton, EmptyState, Field, FormMessage, NumberInput, Panel, SelectInput, TextInput } from "./fields";

const loadAccounts = () => portfolioApi.getAccounts();

const RATE_TYPES: AccountType[] = ["TIME_DEPOSIT", "INSTALLMENT_SAVINGS", "CMA", "LOAN"];
const MONTHLY_TYPES: AccountType[] = ["INSTALLMENT_SAVINGS", "LOAN"];
const DATED_TYPES: AccountType[] = ["TIME_DEPOSIT", "INSTALLMENT_SAVINGS", "LOAN", "INSURANCE"];

type AccountForm = {
  name: string;
  institution: string;
  accountType: AccountType;
  accountNumberLast4: string;
  interestRate: string;
  monthlyPayment: string;
  repaymentType: RepaymentType | "";
  openedAt: string;
  maturityDate: string;
  closedAt: string;
  memo: string;
};

const emptyForm: AccountForm = {
  name: "",
  institution: "",
  accountType: "BROKERAGE",
  accountNumberLast4: "",
  interestRate: "",
  monthlyPayment: "",
  repaymentType: "",
  openedAt: "",
  maturityDate: "",
  closedAt: "",
  memo: "",
};

function toForm(account: Account): AccountForm {
  return {
    name: account.name,
    institution: account.institution,
    accountType: account.accountType,
    accountNumberLast4: account.accountNumberLast4 ?? "",
    interestRate: account.interestRate?.toString() ?? "",
    monthlyPayment: account.monthlyPayment?.toString() ?? "",
    repaymentType: account.repaymentType ?? "",
    openedAt: account.openedAt ?? "",
    maturityDate: account.maturityDate ?? "",
    closedAt: account.closedAt ?? "",
    memo: account.memo ?? "",
  };
}

function toInput(form: AccountForm, displayOrder: number | null): AccountInput {
  const type = form.accountType;
  return {
    name: form.name,
    institution: form.institution,
    accountType: type,
    accountNumberLast4: form.accountNumberLast4 || null,
    interestRate: RATE_TYPES.includes(type) ? parseNumber(form.interestRate) : null,
    monthlyPayment: MONTHLY_TYPES.includes(type) ? parseNumber(form.monthlyPayment) : null,
    repaymentType: type === "LOAN" && form.repaymentType ? form.repaymentType : null,
    openedAt: DATED_TYPES.includes(type) ? form.openedAt || null : null,
    maturityDate: DATED_TYPES.includes(type) ? form.maturityDate || null : null,
    closedAt: form.closedAt || null,
    displayOrder,
    memo: form.memo || null,
  };
}

type AccountsPanelProps = {
  onAddTransaction: (accountId: number) => void;
};

export function AccountsPanel({ onAddTransaction }: AccountsPanelProps) {
  const { data: accounts, error: loadError, reload } = usePortfolioResource(loadAccounts);
  const handleError = usePortfolioErrorHandler();
  const [editing, setEditing] = useState<Account | "new" | null>(null);
  const [form, setForm] = useState<AccountForm>(emptyForm);
  const [fieldErrors, setFieldErrors] = useState<Record<string, string>>({});
  const [message, setMessage] = useState<{ error?: string; success?: string }>({});
  const [created, setCreated] = useState<Account | null>(null);
  const [valuationFor, setValuationFor] = useState<Account | null>(null);
  const [saving, setSaving] = useState(false);

  function startNew() {
    setEditing("new");
    setForm(emptyForm);
    setFieldErrors({});
    setMessage({});
    setCreated(null);
  }

  function startEdit(account: Account) {
    setEditing(account);
    setForm(toForm(account));
    setFieldErrors({});
    setMessage({});
    setCreated(null);
  }

  function update<K extends keyof AccountForm>(key: K, value: AccountForm[K]) {
    setForm((current) => ({ ...current, [key]: value }));
  }

  async function handleSubmit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    setSaving(true);
    setFieldErrors({});
    try {
      if (editing === "new") {
        const account = await portfolioApi.createAccount(toInput(form, accounts?.length ?? 0));
        setCreated(account);
        setMessage({ success: `${account.name} 계좌를 만들었습니다.` });
      } else if (editing) {
        await portfolioApi.updateAccount(editing.id, toInput(form, editing.displayOrder));
        setMessage({ success: "수정했습니다." });
      }
      setEditing(null);
      await reload();
    } catch (error) {
      setFieldErrors(error instanceof ApiError ? error.fieldErrors : {});
      setMessage({ error: handleError(error) });
    } finally {
      setSaving(false);
    }
  }

  const type = form.accountType;

  return (
    <div className="grid gap-6">
      <Panel
        title="계좌"
        description="증권·ISA·연금 계좌, 예적금, 보험, 대출을 등록합니다. 해지한 계좌는 삭제 대신 해지일을 입력하세요."
        actions={editing ? null : <ActionButton variant="primary" onClick={startNew}>+ 계좌 추가</ActionButton>}
      >
        <div className="grid gap-4">
          <FormMessage error={message.error ?? loadError} success={message.success} />
          {created ? (
            <div className="flex flex-wrap items-center gap-2 text-sm text-zinc-300">
              다음 단계:
              {created.accountType === "INSURANCE" ? (
                <ActionButton onClick={() => setValuationFor(created)}>해약환급금 입력</ActionButton>
              ) : (
                <ActionButton onClick={() => onAddTransaction(created.id)}>
                  {created.accountType === "LOAN" ? "대출 잔액 입력" : "기초잔고·거래 입력"}
                </ActionButton>
              )}
            </div>
          ) : null}

          {editing ? (
            <form className="grid gap-4 rounded-lg border border-white/10 bg-white/[0.02] p-4" onSubmit={handleSubmit}>
              <div className="grid gap-4 sm:grid-cols-2 lg:grid-cols-3">
                <Field label="계좌 유형" error={fieldErrors.accountType}>
                  <SelectInput value={type} onChange={(event) => update("accountType", event.target.value as AccountType)}>
                    {Object.entries(accountTypeLabels).map(([value, label]) => (
                      <option key={value} value={value}>
                        {label}
                      </option>
                    ))}
                  </SelectInput>
                </Field>
                <Field label="별칭" error={fieldErrors.name}>
                  <TextInput required value={form.name} placeholder="예: ISA 중개형" onChange={(event) => update("name", event.target.value)} />
                </Field>
                <Field label="기관" error={fieldErrors.institution}>
                  <TextInput required value={form.institution} placeholder="예: 한국투자증권" onChange={(event) => update("institution", event.target.value)} />
                </Field>
                <Field label="계좌번호 끝 4자리" hint="전체 번호는 저장하지 않습니다" error={fieldErrors.accountNumberLast4}>
                  <TextInput
                    value={form.accountNumberLast4}
                    inputMode="numeric"
                    maxLength={4}
                    pattern="\d{4}"
                    onChange={(event) => update("accountNumberLast4", event.target.value.replace(/\D/g, ""))}
                  />
                </Field>
                {RATE_TYPES.includes(type) ? (
                  <Field label="연 금리 (%)" error={fieldErrors.interestRate}>
                    <NumberInput value={form.interestRate} placeholder="3.5" onChange={(event) => update("interestRate", event.target.value)} />
                  </Field>
                ) : null}
                {MONTHLY_TYPES.includes(type) ? (
                  <Field label={type === "LOAN" ? "월 상환액" : "월 납입액"} error={fieldErrors.monthlyPayment}>
                    <NumberInput value={form.monthlyPayment} onChange={(event) => update("monthlyPayment", event.target.value)} />
                  </Field>
                ) : null}
                {type === "LOAN" ? (
                  <Field label="상환 방식">
                    <SelectInput value={form.repaymentType} onChange={(event) => update("repaymentType", event.target.value as RepaymentType | "")}>
                      <option value="">선택 안 함</option>
                      {Object.entries(repaymentTypeLabels).map(([value, label]) => (
                        <option key={value} value={value}>
                          {label}
                        </option>
                      ))}
                    </SelectInput>
                  </Field>
                ) : null}
                {DATED_TYPES.includes(type) ? (
                  <>
                    <Field label={type === "LOAN" ? "대출일" : "가입일"} hint="예상 이자 계산에 사용">
                      <TextInput type="date" value={form.openedAt} onChange={(event) => update("openedAt", event.target.value)} />
                    </Field>
                    <Field label="만기일">
                      <TextInput type="date" value={form.maturityDate} onChange={(event) => update("maturityDate", event.target.value)} />
                    </Field>
                  </>
                ) : null}
                {editing !== "new" ? (
                  <Field label="해지일" hint="해지한 계좌만 입력">
                    <TextInput type="date" value={form.closedAt} onChange={(event) => update("closedAt", event.target.value)} />
                  </Field>
                ) : null}
                <Field label="메모" className="sm:col-span-2 lg:col-span-3">
                  <TextInput value={form.memo} maxLength={500} onChange={(event) => update("memo", event.target.value)} />
                </Field>
              </div>
              <div className="flex flex-wrap gap-2">
                <ActionButton type="submit" variant="primary" disabled={saving}>
                  {saving ? "저장 중…" : editing === "new" ? "계좌 만들기" : "수정 저장"}
                </ActionButton>
                <ActionButton variant="ghost" onClick={() => setEditing(null)}>
                  취소
                </ActionButton>
              </div>
            </form>
          ) : null}

          {accounts && accounts.length === 0 ? (
            <EmptyState>아직 계좌가 없습니다. 먼저 보유 중인 계좌를 추가하세요.</EmptyState>
          ) : null}
          <ul className="grid gap-2">
            {accounts?.map((account) => (
              <li
                key={account.id}
                className="flex flex-wrap items-center justify-between gap-3 rounded-md border border-white/10 bg-white/[0.02] px-4 py-3"
              >
                <div className="min-w-0">
                  <p className="flex flex-wrap items-center gap-2 font-medium text-zinc-100">
                    {account.name}
                    <span className="rounded border border-white/10 px-1.5 py-0.5 text-xs text-zinc-400">
                      {accountTypeLabels[account.accountType]}
                    </span>
                    {account.closedAt ? <span className="text-xs text-zinc-500">해지 {account.closedAt}</span> : null}
                  </p>
                  <p className="mt-1 text-xs text-zinc-500">
                    {account.institution}
                    {account.accountNumberLast4 ? ` · ****${account.accountNumberLast4}` : ""}
                    {account.interestRate != null ? ` · 연 ${formatNumber(account.interestRate)}%` : ""}
                    {account.monthlyPayment != null ? ` · 월 ${formatKrw(account.monthlyPayment)}` : ""}
                    {account.maturityDate ? ` · 만기 ${account.maturityDate}` : ""}
                  </p>
                </div>
                <div className="flex flex-wrap gap-2">
                  {account.accountType === "INSURANCE" ? (
                    <ActionButton onClick={() => setValuationFor(account)}>해약환급금 입력</ActionButton>
                  ) : (
                    <ActionButton onClick={() => onAddTransaction(account.id)}>거래 입력</ActionButton>
                  )}
                  <ActionButton variant="ghost" onClick={() => startEdit(account)}>
                    수정
                  </ActionButton>
                </div>
              </li>
            ))}
          </ul>
        </div>
      </Panel>

      {valuationFor ? <InsuranceValuation key={valuationFor.id} account={valuationFor} onClose={() => setValuationFor(null)} /> : null}
    </div>
  );
}

function InsuranceValuation({ account, onClose }: { account: Account; onClose: () => void }) {
  const loadValuations = useCallback(() => portfolioApi.getValuations(account.id), [account.id]);
  const { data: valuations, reload } = usePortfolioResource(loadValuations);
  const handleError = usePortfolioErrorHandler();
  const [date, setDate] = useState(todayInSeoul());
  const [amount, setAmount] = useState("");
  const [message, setMessage] = useState<{ error?: string; success?: string }>({});

  async function handleSubmit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    const value = parseNumber(amount);
    if (value == null) {
      setMessage({ error: "금액을 입력하세요." });
      return;
    }
    try {
      await portfolioApi.saveValuation(account.id, { valuationDate: date, amount: value, memo: null });
      setAmount("");
      setMessage({ success: `${date} 해약환급금 ${formatKrw(value)} 저장` });
      await reload();
    } catch (error) {
      setMessage({ error: handleError(error) });
    }
  }

  return (
    <Panel
      title={`${account.name} 해약환급금`}
      description="보험사 앱이나 내보험다보여에서 확인한 현재 해약환급금을 입력합니다. 같은 날짜는 덮어씁니다."
      actions={<ActionButton variant="ghost" onClick={onClose}>닫기</ActionButton>}
    >
      <form className="grid gap-4 sm:grid-cols-[1fr_1fr_auto] sm:items-end" onSubmit={handleSubmit}>
        <Field label="기준일">
          <TextInput type="date" required max={todayInSeoul()} value={date} onChange={(event) => setDate(event.target.value)} />
        </Field>
        <Field label="해약환급금 (원)">
          <NumberInput required value={amount} onChange={(event) => setAmount(event.target.value)} />
        </Field>
        <ActionButton type="submit" variant="primary">
          저장
        </ActionButton>
      </form>
      <div className="mt-4 grid gap-3">
        <FormMessage error={message.error} success={message.success} />
        <ul className="grid gap-1 text-sm text-zinc-400">
          {valuations?.slice(0, 6).map((valuation) => (
            <li key={valuation.id} className="flex justify-between border-b border-white/5 py-1.5">
              <span>{valuation.valuationDate}</span>
              <span className="font-mono tabular-nums text-zinc-200">{formatKrw(valuation.amount)}</span>
            </li>
          ))}
        </ul>
      </div>
    </Panel>
  );
}

import { useState, type FormEvent } from "react";
import { usePortfolioErrorHandler, usePortfolioResource } from "../../hooks/usePortfolioResource";
import { ApiError } from "../../services/apiClient";
import {
  portfolioApi,
  type AssetClass,
  type Instrument,
  type InstrumentInput,
  type InstrumentType,
  type Market,
  type PriceSource,
  type Region,
  type TaxType,
} from "../../services/portfolioApi";
import { assetClassLabels, instrumentTypeLabels, marketLabels, regionLabels, taxTypeLabels } from "./labels";
import { ActionButton, EmptyState, Field, FormMessage, Panel, SelectInput, TextInput } from "./fields";

const loadInstruments = () => portfolioApi.getInstruments();

const emptyInstrument: InstrumentInput = {
  symbol: "",
  name: "",
  market: "KRX",
  instrumentType: "ETF",
  underlyingAssetClass: "EQUITY",
  exposureRegion: "KR",
  fxHedged: false,
  taxType: "DOMESTIC_EQUITY_ETF",
  priceSource: "KIS",
  active: true,
};

/** Sensible tax type for the listing; the owner can still override it. */
function defaultTaxType(market: Market, type: InstrumentType, region: Region, assetClass: AssetClass): TaxType {
  if (market !== "KRX") return "FOREIGN_STOCK";
  if (type === "STOCK") return "DOMESTIC_STOCK";
  return region === "KR" && assetClass === "EQUITY" ? "DOMESTIC_EQUITY_ETF" : "OTHER_ETF";
}

export function InstrumentsPanel() {
  const { data: instruments, error: loadError, reload } = usePortfolioResource(loadInstruments);
  const handleError = usePortfolioErrorHandler();
  const [editing, setEditing] = useState<Instrument | "new" | null>(null);
  const [form, setForm] = useState<InstrumentInput>(emptyInstrument);
  const [fieldErrors, setFieldErrors] = useState<Record<string, string>>({});
  const [message, setMessage] = useState<{ error?: string; success?: string }>({});
  const [saving, setSaving] = useState(false);

  function start(target: Instrument | "new") {
    setEditing(target);
    setForm(target === "new" ? emptyInstrument : { ...target });
    setFieldErrors({});
    setMessage({});
  }

  /** Changing listing or classification re-derives the defaults that depend on it. */
  function update(patch: Partial<InstrumentInput>) {
    setForm((current) => {
      const next = { ...current, ...patch };
      if (patch.market) {
        next.exposureRegion = patch.market === "KRX" ? "KR" : "US";
      }
      if (patch.market || patch.instrumentType || patch.exposureRegion || patch.underlyingAssetClass) {
        next.taxType = defaultTaxType(next.market, next.instrumentType, next.exposureRegion, next.underlyingAssetClass);
      }
      if (patch.name !== undefined && /\(H\)/i.test(patch.name)) {
        next.fxHedged = true;
      }
      return next;
    });
  }

  async function handleSubmit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    setSaving(true);
    setFieldErrors({});
    try {
      if (editing === "new") {
        const instrument = await portfolioApi.createInstrument(form);
        setMessage({ success: `${instrument.name} (${instrument.symbol}) 등록` });
      } else if (editing) {
        await portfolioApi.updateInstrument(editing.id, form);
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

  const foreignListed = form.market !== "KRX";

  return (
    <Panel
      title="종목"
      description="보유하거나 거래할 주식·ETF를 등록합니다. 국내 상장 해외 ETF는 '실제 투자 지역'을 해외로 선택하세요."
      actions={editing ? null : <ActionButton variant="primary" onClick={() => start("new")}>+ 종목 추가</ActionButton>}
    >
      <div className="grid gap-4">
        <FormMessage error={message.error ?? loadError} success={message.success} />

        {editing ? (
          <form className="grid gap-4 rounded-lg border border-white/10 bg-white/[0.02] p-4" onSubmit={handleSubmit}>
            <div className="grid gap-4 sm:grid-cols-2 lg:grid-cols-3">
              <Field label="시장" error={fieldErrors.market}>
                <SelectInput value={form.market} onChange={(event) => update({ market: event.target.value as Market })}>
                  {Object.entries(marketLabels).map(([value, label]) => (
                    <option key={value} value={value}>
                      {label}
                    </option>
                  ))}
                </SelectInput>
              </Field>
              <Field
                label={foreignListed ? "티커" : "종목코드"}
                hint={foreignListed ? "예: AAPL, VOO" : "6자리, 예: 069500"}
                error={fieldErrors.symbol}
              >
                <TextInput
                  required
                  value={form.symbol}
                  maxLength={foreignListed ? 10 : 6}
                  className="font-mono uppercase"
                  onChange={(event) => update({ symbol: event.target.value.toUpperCase() })}
                />
              </Field>
              <Field label="종목명" hint="이름에 (H)가 있으면 환헤지로 자동 체크" error={fieldErrors.name}>
                <TextInput required value={form.name} placeholder="예: TIGER 미국S&P500" onChange={(event) => update({ name: event.target.value })} />
              </Field>
              <Field label="상품 유형">
                <SelectInput value={form.instrumentType} onChange={(event) => update({ instrumentType: event.target.value as InstrumentType })}>
                  {Object.entries(instrumentTypeLabels).map(([value, label]) => (
                    <option key={value} value={value}>
                      {label}
                    </option>
                  ))}
                </SelectInput>
              </Field>
              <Field label="실제 투자 지역" hint="상장 시장이 아니라 기초자산 기준">
                <SelectInput value={form.exposureRegion} onChange={(event) => update({ exposureRegion: event.target.value as Region })}>
                  {Object.entries(regionLabels).map(([value, label]) => (
                    <option key={value} value={value}>
                      {label}
                    </option>
                  ))}
                </SelectInput>
              </Field>
              <Field label="기초자산">
                <SelectInput
                  value={form.underlyingAssetClass}
                  onChange={(event) => update({ underlyingAssetClass: event.target.value as AssetClass })}
                >
                  {Object.entries(assetClassLabels).map(([value, label]) => (
                    <option key={value} value={value}>
                      {label}
                    </option>
                  ))}
                </SelectInput>
              </Field>
              <Field label="과세 유형" hint="시장·유형에 맞춰 자동 선택, 필요하면 변경">
                <SelectInput value={form.taxType} onChange={(event) => setForm({ ...form, taxType: event.target.value as TaxType })}>
                  {Object.entries(taxTypeLabels).map(([value, label]) => (
                    <option key={value} value={value}>
                      {label}
                    </option>
                  ))}
                </SelectInput>
              </Field>
              <Field label="시세 출처" hint="KIS 키 등록 전에는 시세 탭에서 직접 입력">
                <SelectInput value={form.priceSource} onChange={(event) => setForm({ ...form, priceSource: event.target.value as PriceSource })}>
                  <option value="KIS">자동 (KIS)</option>
                  <option value="MANUAL">직접 입력만</option>
                </SelectInput>
              </Field>
              <div className="grid content-end gap-3 pb-1 text-sm text-zinc-300">
                <label className="flex items-center gap-2">
                  <input type="checkbox" checked={form.fxHedged} onChange={(event) => setForm({ ...form, fxHedged: event.target.checked })} />
                  환헤지 상품
                </label>
                <label className="flex items-center gap-2">
                  <input type="checkbox" checked={form.active} onChange={(event) => setForm({ ...form, active: event.target.checked })} />
                  사용 중 (해제하면 목록에서 비활성)
                </label>
              </div>
            </div>
            <div className="flex flex-wrap gap-2">
              <ActionButton type="submit" variant="primary" disabled={saving}>
                {saving ? "저장 중…" : editing === "new" ? "종목 등록" : "수정 저장"}
              </ActionButton>
              <ActionButton variant="ghost" onClick={() => setEditing(null)}>
                취소
              </ActionButton>
            </div>
          </form>
        ) : null}

        {instruments && instruments.length === 0 ? <EmptyState>등록된 종목이 없습니다.</EmptyState> : null}
        <ul className="grid gap-2">
          {instruments?.map((instrument) => (
            <li
              key={instrument.id}
              className="flex flex-wrap items-center justify-between gap-3 rounded-md border border-white/10 bg-white/[0.02] px-4 py-3"
            >
              <div className="min-w-0">
                <p className="flex flex-wrap items-center gap-2 font-medium text-zinc-100">
                  {instrument.name}
                  <span className="font-mono text-xs text-zinc-500">{instrument.symbol}</span>
                  {!instrument.active ? <span className="text-xs text-zinc-500">비활성</span> : null}
                </p>
                <p className="mt-1 text-xs text-zinc-500">
                  {marketLabels[instrument.market]} · {instrumentTypeLabels[instrument.instrumentType]} ·{" "}
                  {regionLabels[instrument.exposureRegion]} {assetClassLabels[instrument.underlyingAssetClass]}
                  {instrument.fxHedged ? " · 환헤지" : ""} · {instrument.tradingCurrency}
                </p>
              </div>
              <ActionButton variant="ghost" onClick={() => start(instrument)}>
                수정
              </ActionButton>
            </li>
          ))}
        </ul>
      </div>
    </Panel>
  );
}

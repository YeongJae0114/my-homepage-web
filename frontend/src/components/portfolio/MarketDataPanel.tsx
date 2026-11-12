import { useState, type FormEvent } from "react";
import { usePortfolioErrorHandler, usePortfolioResource } from "../../hooks/usePortfolioResource";
import { portfolioApi, type SourceStatus } from "../../services/portfolioApi";
import { formatNumber, parseNumber, todayInSeoul } from "./labels";
import { ActionButton, EmptyState, Field, FormMessage, NumberInput, Panel, TextInput } from "./fields";

const loadDashboard = async () => {
  const [dashboard, instruments] = await Promise.all([portfolioApi.getDashboard("1M"), portfolioApi.getInstruments()]);
  return { ...dashboard, instruments };
};
const loadIntegrations = () => portfolioApi.getIntegrations();

export function MarketDataPanel() {
  const { data: dashboard, error: loadError, reload } = usePortfolioResource(loadDashboard);
  const { data: integrations, reload: reloadIntegrations } = usePortfolioResource(loadIntegrations);
  const handleError = usePortfolioErrorHandler();
  const [date, setDate] = useState(todayInSeoul());
  const [prices, setPrices] = useState<Record<number, string>>({});
  const [rates, setRates] = useState<Record<string, string>>({});
  const [message, setMessage] = useState<{ error?: string; success?: string }>({});
  const [saving, setSaving] = useState(false);

  // One row per instrument even when it sits in several accounts.
  const holdings = new Map(dashboard?.holdings.map((holding) => [holding.instrumentId, holding]) ?? []);
  const instruments = dashboard?.instruments.filter((instrument) => instrument.active || holdings.has(instrument.id)).map((instrument) => ({
    instrumentId: instrument.id, name: instrument.name, symbol: instrument.symbol, tradingCurrency: instrument.tradingCurrency,
    closePrice: holdings.get(instrument.id)?.closePrice ?? null,
    priceDate: holdings.get(instrument.id)?.priceDate ?? null,
    priceStale: holdings.get(instrument.id)?.priceStale ?? false,
  })) ?? [];
  const currentRates = new Map(dashboard?.freshness.fxRates.map((rate) => [rate.currency, rate]) ?? []);
  const currencies = [...new Set([
    ...instruments.filter((holding) => holding.tradingCurrency !== "KRW").map((holding) => holding.tradingCurrency),
    ...currentRates.keys(),
  ])].sort();

  async function handleSubmit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    const priceItems = Object.entries(prices)
      .map(([instrumentId, text]) => ({ instrumentId: Number(instrumentId), closePrice: parseNumber(text) }))
      .filter((item): item is { instrumentId: number; closePrice: number } => item.closePrice != null);
    const rateItems = Object.entries(rates)
      .map(([currency, text]) => ({ currency, rateToKrw: parseNumber(text) }))
      .filter((item): item is { currency: string; rateToKrw: number } => item.rateToKrw != null);
    if (priceItems.length === 0 && rateItems.length === 0) {
      setMessage({ error: "입력한 값이 없습니다." });
      return;
    }

    setSaving(true);
    try {
      if (priceItems.length > 0) await portfolioApi.savePrices(date, priceItems);
      if (rateItems.length > 0) await portfolioApi.saveFxRates(date, rateItems);
      setPrices({});
      setRates({});
      setMessage({ success: `${date} 기준 시세 ${priceItems.length}건, 환율 ${rateItems.length}건 저장` });
      await reload();
    } catch (error) {
      setMessage({ error: handleError(error) });
    } finally {
      setSaving(false);
    }
  }

  async function handleSnapshot() {
    if (!window.confirm("지금 스냅샷을 저장할까요?\n지금까지 입력한 거래는 잠겨서 더 이상 수정·삭제할 수 없습니다.")) return;
    try {
      const snapshot = await portfolioApi.captureSnapshot();
      setMessage({ success: `${snapshot.snapshotDate} 스냅샷 저장 (순자산 ₩${formatNumber(snapshot.netWorth)})` });
      await reloadIntegrations();
    } catch (error) {
      setMessage({ error: handleError(error) });
    }
  }

  return (
    <div className="grid gap-6">
      <Panel
        title="시세·환율 입력"
        description="KIS·수출입은행 키를 등록하기 전에는 여기서 종가와 환율을 입력합니다. 자동 수집 후에도 잘못된 값을 정정할 때 쓰며, 직접 입력한 값이 자동 값보다 우선합니다."
      >
        <form className="grid gap-5" onSubmit={handleSubmit}>
          <FormMessage error={message.error ?? loadError} success={message.success} />
          <Field label="기준일 (종가 날짜)" className="max-w-xs">
            <TextInput type="date" required max={todayInSeoul()} value={date} onChange={(event) => setDate(event.target.value)} />
          </Field>

          {dashboard && instruments.length === 0 ? <EmptyState>종목 탭에서 먼저 종목을 등록하세요. 기초잔고 등록 전에도 시세와 환율을 입력할 수 있습니다.</EmptyState> : null}
          {instruments.length > 0 ? (
            <div className="overflow-x-auto">
              <table className="w-full min-w-[560px] text-left text-sm">
                <thead className="text-xs text-zinc-500">
                  <tr className="border-b border-white/10">
                    <th className="py-2 pr-3 font-medium">종목</th>
                    <th className="py-2 pr-3 text-right font-medium">현재 반영 시세</th>
                    <th className="py-2 pr-3 font-medium">시세 날짜</th>
                    <th className="w-44 py-2 font-medium">새 종가</th>
                  </tr>
                </thead>
                <tbody>
                  {instruments.map((holding) => (
                    <tr key={holding.instrumentId} className="border-b border-white/5">
                      <td className="py-2 pr-3 text-zinc-100">
                        {holding.name} <span className="font-mono text-xs text-zinc-500">{holding.symbol}</span>
                      </td>
                      <td className="py-2 pr-3 text-right font-mono tabular-nums">
                        {formatNumber(holding.closePrice)} {holding.tradingCurrency === "KRW" ? "원" : holding.tradingCurrency}
                      </td>
                      <td className="py-2 pr-3 text-xs">
                        {holding.priceDate ?? <span className="text-amber-300">보유 평가 시세 없음</span>}
                        {holding.priceDate && holding.priceStale ? <span className="ml-1 text-amber-300">오래됨</span> : null}
                      </td>
                      <td className="py-2">
                        <NumberInput
                          aria-label={`${holding.name} 새 종가`}
                          value={prices[holding.instrumentId] ?? ""}
                          placeholder={holding.closePrice == null ? "가격 입력" : String(holding.closePrice)}
                          onChange={(event) => setPrices({ ...prices, [holding.instrumentId]: event.target.value })}
                        />
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          ) : null}

          {currencies.length > 0 ? (
            <div className="grid gap-3 sm:grid-cols-2 lg:grid-cols-3">
              {currencies.map((currency) => {
                const current = currentRates.get(currency);
                return (
                  <Field
                    key={currency}
                    label={`${currency} 환율 (1${currency}당 원)`}
                    hint={current ? `현재 ${formatNumber(current.rateToKrw)} (${current.rateDate})` : "최근 환율을 직접 입력할 수 있습니다"}
                  >
                    <NumberInput value={rates[currency] ?? ""} onChange={(event) => setRates({ ...rates, [currency]: event.target.value })} />
                  </Field>
                );
              })}
            </div>
          ) : null}

          <div>
            <ActionButton type="submit" variant="primary" disabled={saving}>
              {saving ? "저장 중…" : "입력한 값 저장"}
            </ActionButton>
          </div>
        </form>
      </Panel>

      <Panel
        title="자동 수집·스냅샷"
        description="스냅샷은 평일 18:00에 자동 저장되고, 그 전에 입력한 거래는 잠깁니다. 초기 입력을 마친 뒤 직접 저장해도 됩니다."
        actions={<ActionButton onClick={handleSnapshot}>지금 스냅샷 저장</ActionButton>}
      >
        <div className="grid gap-3 sm:grid-cols-2">
          <IntegrationLine name="KIS 시세" status={integrations?.kis} />
          <IntegrationLine name="수출입은행 환율" status={integrations?.kexim} />
        </div>
      </Panel>
    </div>
  );
}

function IntegrationLine({ name, status }: { name: string; status?: SourceStatus }) {
  return (
    <div className="rounded-md border border-white/10 px-4 py-3 text-sm">
      <p className="flex items-center gap-2 font-medium text-zinc-100">
        <span aria-hidden="true" className={`h-2 w-2 rounded-full ${status?.configured ? "bg-emerald-300" : "bg-zinc-600"}`} />
        {name}: {status?.configured ? "키 설정됨" : "키 미설정 (직접 입력)"}
      </p>
      {status?.lastResult ? (
        <p className="mt-1 text-xs text-zinc-500">
          {status.lastResult}
          {status.lastRunAt ? ` · ${status.lastRunAt.slice(0, 16).replace("T", " ")}` : ""}
        </p>
      ) : null}
    </div>
  );
}

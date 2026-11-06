import { useCallback, useState } from "react";
import { usePortfolioResource } from "../../hooks/usePortfolioResource";
import { portfolioApi, type AssetClass, type Dashboard, type HistoryRange, type Region } from "../../services/portfolioApi";
import { MetricCard } from "../common/MetricCard";
import { cn } from "../../utils/cn";
import { AllocationDonut, NetWorthChart, type DonutSlice } from "./charts";
import {
  accountTypeLabels,
  assetClassLabels,
  assetGroupLabels,
  formatKrw,
  formatNumber,
  formatPercent,
  profitClassName,
  regionLabels,
} from "./labels";
import { EmptyState, FormMessage, Panel } from "./fields";

const MASK = "₩•••••";
const RANGES: HistoryRange[] = ["1M", "3M", "6M", "1Y", "ALL"];
const DONUT_VIEWS = [
  { id: "group", label: "자산군" },
  { id: "region", label: "지역" },
  { id: "currency", label: "통화" },
  { id: "assetClass", label: "기초자산" },
] as const;
type DonutView = (typeof DONUT_VIEWS)[number]["id"];

/** Group view covers every asset; the exposure views cover securities only (look-through of ETFs). */
function slicesFor(data: Dashboard, view: DonutView): DonutSlice[] {
  if (view === "group") {
    return data.allocation.map((item) => ({ key: item.group, label: assetGroupLabels[item.group], amount: item.amount, weightPercent: item.weightPercent }));
  }
  const items = view === "region" ? data.exposure.byRegion : view === "currency" ? data.exposure.byCurrency : data.exposure.byAssetClass;
  return items.map((item) => ({
    key: item.key,
    label:
      view === "region"
        ? regionLabels[item.key as Region] ?? item.key
        : view === "assetClass"
          ? assetClassLabels[item.key as AssetClass] ?? item.key
          : item.key === "MULTI"
            ? "복수 통화"
            : item.key,
    amount: item.amount,
    weightPercent: item.weightPercent,
  }));
}

function SegmentedControl<T extends string>({ options, value, onChange }: { options: readonly { id: T; label: string }[]; value: T; onChange: (value: T) => void }) {
  return (
    <div className="flex rounded-md border border-white/10 p-0.5 text-xs">
      {options.map((option) => (
        <button
          key={option.id}
          type="button"
          aria-pressed={value === option.id}
          className={cn("rounded px-2.5 py-1", value === option.id ? "bg-white/10 text-zinc-50" : "text-zinc-400 hover:text-zinc-100")}
          onClick={() => onChange(option.id)}
        >
          {option.label}
        </button>
      ))}
    </div>
  );
}

type SummaryPanelProps = {
  onStart: () => void;
};

export function SummaryPanel({ onStart }: SummaryPanelProps) {
  const [range, setRange] = useState<HistoryRange>("1Y");
  const [donutView, setDonutView] = useState<DonutView>("group");
  const loadDashboard = useCallback(() => portfolioApi.getDashboard(range), [range]);
  const { data, error, loading } = usePortfolioResource(loadDashboard);
  const [hidden, setHidden] = useState(() => {
    try {
      return localStorage.getItem("portfolio.hideAmounts") === "1";
    } catch {
      return false;
    }
  });

  function toggleHidden() {
    setHidden((value) => {
      try {
        localStorage.setItem("portfolio.hideAmounts", value ? "0" : "1");
      } catch {
        // storage unavailable: the toggle still works for this visit
      }
      return !value;
    });
  }

  const money = (value: number | null | undefined) => (hidden ? MASK : formatKrw(value));

  if (loading && !data) {
    return <p className="text-sm text-zinc-500">불러오는 중…</p>;
  }
  if (!data) {
    return <FormMessage error={error} />;
  }

  const empty = data.accounts.length === 0;
  const { summary, performance } = data;

  return (
    <div className="grid gap-6">
      <div className="flex flex-wrap items-center justify-between gap-3 text-xs text-zinc-500">
        <span>
          시세 기준 {data.freshness.latestPriceDate ?? "없음"}
          {data.freshness.staleHoldings > 0 ? (
            <span className="ml-2 text-amber-300">시세 오래된 종목 {data.freshness.staleHoldings}개</span>
          ) : null}
        </span>
        <button type="button" className="rounded-md border border-white/10 px-2.5 py-1 text-zinc-300 hover:bg-white/[0.06]" onClick={toggleHidden}>
          {hidden ? "금액 보기" : "금액 숨기기"}
        </button>
      </div>

      {empty ? (
        <Panel title="시작하기" description="세 단계면 대시보드가 채워집니다.">
          <ol className="grid gap-2 text-sm text-zinc-300">
            <li>1. 계좌 탭에서 보유 계좌(증권·ISA·연금·예적금·보험·대출)를 추가합니다.</li>
            <li>2. 종목 탭에서 보유 ETF·주식을 등록합니다.</li>
            <li>3. 거래 탭에서 오늘 기준 기초잔고(보유 수량·평단, 예수금, 예적금 잔액)를 입력합니다.</li>
          </ol>
          <button type="button" className="mt-4 text-sm font-semibold text-emerald-200 hover:underline" onClick={onStart}>
            계좌 추가하러 가기 →
          </button>
        </Panel>
      ) : null}

      <div className="grid grid-cols-2 gap-3 lg:grid-cols-4">
        <MetricCard label="총자산" value={money(summary.totalAssets)} detail={summary.monthOverMonthRate != null ? `전월 대비 ${formatPercent(summary.monthOverMonthRate)}` : "전월 비교 데이터 없음"} />
        <MetricCard label="총부채" value={money(summary.totalLiabilities)} detail={`${data.accounts.filter((account) => account.group === "LIABILITY").length}건`} />
        <MetricCard label="순자산" value={money(summary.netWorth)} detail={summary.monthOverMonthChange != null ? `전월 대비 ${money(summary.monthOverMonthChange)}` : "—"} />
        <MetricCard
          label={performance.xirrAnnualized === false ? "수익률 (기간)" : "수익률 (XIRR 연)"}
          value={formatPercent(performance.xirr)}
          detail={`TWR ${formatPercent(performance.twr)} · 분배금 ${money(performance.dividendIncome)}`}
        />
      </div>

      <div className="grid gap-6 lg:grid-cols-2">
        <Panel
          title="비중"
          description={donutView === "group" ? "부채를 뺀 총자산 기준" : "보유 종목 기준 실제 노출 (국내 상장 해외 ETF 포함)"}
          actions={<SegmentedControl options={DONUT_VIEWS} value={donutView} onChange={setDonutView} />}
        >
          <AllocationDonut slices={slicesFor(data, donutView)} hidden={hidden} />
        </Panel>
        <Panel
          title="순자산 추이"
          description="선: 순자산 · 영역: 총자산 (오늘은 실시간 값)"
          actions={<SegmentedControl options={RANGES.map((id) => ({ id, label: id }))} value={range} onChange={setRange} />}
        >
          <NetWorthChart points={data.history} hidden={hidden} />
        </Panel>
      </div>

      <Panel title="보유 종목" description="평가손익: 상승 빨강 · 하락 파랑. 해외 종목은 주가·환율 효과로 나눠 표시합니다.">
        {data.holdings.length === 0 ? (
          <EmptyState>보유 종목이 없습니다.</EmptyState>
        ) : (
          <div className="overflow-x-auto">
            <table className="w-full min-w-[760px] text-left text-sm">
              <thead className="text-xs text-zinc-500">
                <tr className="border-b border-white/10">
                  <th className="py-2 pr-3 font-medium">종목</th>
                  <th className="py-2 pr-3 font-medium">계좌</th>
                  <th className="py-2 pr-3 text-right font-medium">수량</th>
                  <th className="py-2 pr-3 text-right font-medium">평단 / 현재가</th>
                  <th className="py-2 pr-3 text-right font-medium">평가금액</th>
                  <th className="py-2 pr-3 text-right font-medium">평가손익</th>
                  <th className="py-2 text-right font-medium">비중</th>
                </tr>
              </thead>
              <tbody>
                {data.holdings.map((holding) => (
                  <tr key={`${holding.accountId}-${holding.instrumentId}`} className="border-b border-white/5">
                    <td className="py-2 pr-3 text-zinc-100">
                      {holding.name}
                      <span className="block font-mono text-xs text-zinc-500">
                        {holding.symbol}
                        {holding.priceStale ? <span className="ml-1 text-amber-300">시세 오래됨</span> : null}
                      </span>
                    </td>
                    <td className="py-2 pr-3 text-zinc-400">{holding.accountName}</td>
                    <td className="py-2 pr-3 text-right font-mono tabular-nums">{formatNumber(holding.quantity)}</td>
                    <td className="py-2 pr-3 text-right font-mono tabular-nums text-zinc-400">
                      {formatNumber(holding.avgPriceLocal)} / <span className="text-zinc-100">{formatNumber(holding.closePrice)}</span>
                    </td>
                    <td className="py-2 pr-3 text-right font-mono tabular-nums text-zinc-100">{money(holding.marketValueKrw)}</td>
                    <td className={cn("py-2 pr-3 text-right font-mono tabular-nums", profitClassName(holding.unrealizedProfit))}>
                      {money(holding.unrealizedProfit)} <span className="text-xs">({formatPercent(holding.returnRate)})</span>
                      {holding.fxEffect != null && !hidden ? (
                        <span className="block text-xs text-zinc-500">
                          주가 {formatKrw(holding.priceEffect)} · 환율 {formatKrw(holding.fxEffect)}
                        </span>
                      ) : null}
                    </td>
                    <td className="py-2 text-right font-mono tabular-nums text-zinc-400">{formatPercent(holding.weightPercent).replace("+", "")}</td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}
      </Panel>

      {data.upcomingMaturities.length > 0 ? (
        <Panel title="90일 내 만기">
          <ul className="grid gap-2 text-sm">
            {data.upcomingMaturities.map((maturity) => (
              <li key={maturity.accountId} className="flex flex-wrap justify-between gap-2 rounded-md border border-white/10 px-4 py-3">
                <span className="text-zinc-100">
                  {maturity.name} <span className="text-xs text-zinc-500">{accountTypeLabels[maturity.accountType]}</span>
                </span>
                <span className="text-zinc-400">
                  {maturity.maturityDate} (D-{maturity.daysLeft})
                  {maturity.expectedInterestAfterTax != null ? ` · 예상 세후 이자 ${money(maturity.expectedInterestAfterTax)}` : ""}
                </span>
              </li>
            ))}
          </ul>
        </Panel>
      ) : null}
    </div>
  );
}

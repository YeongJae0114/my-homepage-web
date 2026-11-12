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

function collectedTime(value: string | null) {
  if (!value) return "없음";
  return `${new Date(value).toLocaleString("ko-KR", { timeZone: "Asia/Seoul", month: "numeric", day: "numeric", hour: "2-digit", minute: "2-digit", hour12: false })} KST`;
}

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
  onRecord: () => void;
};

export function SummaryPanel({ onStart, onRecord }: SummaryPanelProps) {
  const [range, setRange] = useState<HistoryRange>("1Y");
  const [donutView, setDonutView] = useState<DonutView>("group");
  const loadDashboard = useCallback(async () => {
    const [dashboard, integrations] = await Promise.all([portfolioApi.getDashboard(range), portfolioApi.getIntegrations()]);
    return { ...dashboard, integrations };
  }, [range]);
  const { data, error, loading, reload } = usePortfolioResource(loadDashboard, 60_000);
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
  const changeMoney = (value: number | null | undefined) => !hidden && value != null && value > 0 ? `+${money(value)}` : money(value);

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
          화면 갱신 {collectedTime(data.calculatedAt)} · 표시 중 1분마다 갱신
          {data.freshness.staleHoldings > 0 ? (
            <span className="ml-2 text-amber-300">시세 확인 필요 {data.freshness.staleHoldings}건</span>
          ) : null}
        </span>
        <div className="flex flex-wrap gap-2">
          <button type="button" className="rounded-md border border-white/10 px-2.5 py-1 text-zinc-300 hover:bg-white/[0.06] disabled:opacity-50" disabled={loading} onClick={() => void reload()}>
            {loading ? "갱신 중…" : "새로고침"}
          </button>
          <button type="button" className="rounded-md border border-white/10 px-2.5 py-1 text-zinc-300 hover:bg-white/[0.06]" onClick={toggleHidden}>
            {hidden ? "금액 보기" : "금액 숨기기"}
          </button>
          <button type="button" className="rounded-md bg-emerald-300/10 px-2.5 py-1 text-emerald-200 hover:bg-emerald-300/20" onClick={onRecord}>매매·입출금 기록</button>
        </div>
      </div>
      {error ? <FormMessage error={`갱신하지 못해 이전 값을 표시합니다. ${error}`} /> : null}

      <div className="rounded-md border border-white/10 px-4 py-3 text-xs leading-6 text-zinc-400">
        <p>국내·미국 주식은 정규장 중 5분마다 수집합니다. 최근 시세 저장 {collectedTime(data.freshness.latestCollectedAt)}.</p>
        <p>{data.integrations.kis.configured ? data.integrations.kis.lastResult ?? "시세 API 키 설정됨 · 첫 수집 대기 중" : "시세 API 키가 없어 자동 수집이 꺼져 있습니다. 시세·환율 탭에서 수동으로 입력할 수 있습니다."}
          {data.integrations.kis.configured && data.integrations.kis.lastRunAt ? ` · ${collectedTime(data.integrations.kis.lastRunAt)}` : ""}</p>
        <p>수집 시각은 거래소 체결 시각과 다릅니다. 해외 시세 제공 지연과 하루 단위 환율로 평가금액에 차이가 있을 수 있습니다.</p>
        {data.freshness.fxRates.length > 0 ? <p>적용 환율 {data.freshness.fxRates.map((fx) => `${fx.currency} ${formatNumber(fx.rateToKrw)}원 (${fx.rateDate})`).join(" · ")}</p> : null}
      </div>

      {empty ? (
        <Panel title="시작하기" description="세 단계면 대시보드가 채워집니다.">
          <ol className="grid gap-2 text-sm text-zinc-300">
            <li>1. 계좌 탭에서 보유 계좌(증권·ISA·연금·예적금·보험·대출)를 추가합니다.</li>
            <li>2. 종목 탭에서 보유 ETF·주식을 등록합니다.</li>
            <li>3. 거래 탭에서 오늘 기준 보유 수량과 현금 잔액을 등록합니다. 이후 매매·입출금이 생길 때 한 건씩 기록하세요.</li>
          </ol>
          <button type="button" className="mt-4 text-sm font-semibold text-emerald-200 hover:underline" onClick={onStart}>
            계좌 추가하러 가기 →
          </button>
        </Panel>
      ) : null}

      <div className="grid grid-cols-2 gap-3 lg:grid-cols-4">
        <MetricCard label="현재 추정 순자산" value={money(summary.netWorth)} detail="총자산에서 부채를 뺀 금액" />
        <MetricCard label="총자산" value={money(summary.totalAssets)} detail={`부채 ${money(summary.totalLiabilities)}`} />
        <MetricCard
          label="오늘 순자산 변화"
          value={changeMoney(summary.dayChange?.netWorthChange)}
          detail={summary.dayChange ? `${summary.dayChange.baseDate} 기록 대비 · 입출금 포함` : "이전 날짜의 기록이 쌓이면 표시"}
          valueClassName={profitClassName(summary.dayChange?.netWorthChange ?? null)}
        />
        <MetricCard
          label="이번 달 순자산 변화"
          value={changeMoney(summary.monthChange?.netWorthChange)}
          detail={summary.monthChange ? `${summary.monthChange.baseDate} 기록 대비 · 입출금 포함` : "전월 기록이 쌓이면 표시"}
          valueClassName={profitClassName(summary.monthChange?.netWorthChange ?? null)}
        />
      </div>

      <Panel title="투자로 얼마나 달라졌나요?" description="증권·ISA·연금저축·IRP 계좌의 변화를 입출금과 구분합니다. 이 계좌들 사이의 이체는 입출금에서 제외합니다.">
        <div className="grid gap-3 sm:grid-cols-2">
          {([{ label: "오늘", change: summary.dayChange }, { label: "이번 달", change: summary.monthChange }] as const).map(({ label, change }) => (
            <MetricCard key={label} label={`${label} 입출금 제외 투자 증감`} value={changeMoney(change?.investmentGain)}
              valueClassName={profitClassName(change?.investmentGain ?? null)}
              detail={change ? `순입금 ${changeMoney(change.netContribution)} · ${change.baseDate} 기록 대비` : "비교할 기록이 아직 없습니다"} />
          ))}
        </div>
        <p className="mt-3 text-xs text-zinc-500">시세·환율 변화와 배당·수수료를 포함한 추정치입니다. 과거 거래를 뒤늦게 등록하면 비교값에 차이가 날 수 있습니다.</p>
      </Panel>

      <div className="grid gap-6 lg:grid-cols-2">
        <Panel
          title="비중"
          description={donutView === "group" ? "부채 차감 전 총자산 기준" : "보유 종목 기준 실제 노출 (국내 상장 해외 ETF 포함)"}
          actions={<SegmentedControl options={DONUT_VIEWS} value={donutView} onChange={setDonutView} />}
        >
          <AllocationDonut slices={slicesFor(data, donutView)} hidden={hidden} />
        </Panel>
        <Panel
          title="순자산 추이"
          description="선: 순자산 · 영역: 총자산 (오늘은 최근 수집 시세로 평가)"
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
                  <th className="py-2 pr-3 text-right font-medium">기준 평단 / 최근 가격</th>
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
                        {holding.priceStale ? <span className="ml-1 text-amber-300">{holding.priceDate ? "수집 지연·오래된 시세" : "시세 없음 · 기준가 평가"}</span> : null}
                      </span>
                      {holding.priceDate ? <span className="block text-xs text-zinc-500">{holding.priceSource === "MANUAL" ? "수동" : "자동"} · 시세 기록일 {holding.priceDate} · 저장 {collectedTime(holding.priceCollectedAt)}</span> : null}
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

      <details className="rounded-lg border border-white/10 p-4 text-sm">
        <summary className="cursor-pointer text-zinc-300">수익률과 누적 손익 자세히 보기</summary>
        <div className="mt-4 grid gap-3 sm:grid-cols-2 lg:grid-cols-4">
          <MetricCard label={performance.xirrAnnualized === false ? "XIRR 기간 수익률" : "XIRR 연 수익률"} value={formatPercent(performance.xirr)} detail={`투자금 유입 시점 반영 · ${performance.since ?? "기록 없음"}부터`} />
          <MetricCard label="TWR" value={formatPercent(performance.twr)} detail="기록 시점 사이의 수익률 연결" />
          <MetricCard label="실현손익" value={money(performance.realizedProfit)} detail={`평가손익 ${money(performance.unrealizedProfit)}`} />
          <MetricCard label="분배금·배당" value={money(performance.dividendIncome)} detail={`누적 투자금 ${money(performance.netContribution)}`} />
        </div>
        <p className="mt-3 text-xs text-zinc-500">기초잔고의 기준가를 바탕으로 계산합니다. 과거 매입 평단을 넣었다면 등록 전 손익도 포함되므로 증권사 기간 수익률과 다를 수 있습니다.</p>
      </details>

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

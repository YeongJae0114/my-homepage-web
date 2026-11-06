import { useState } from "react";
import { useNavigate } from "react-router-dom";
import { Container } from "../components/common/Container";
import { AccountsPanel } from "../components/portfolio/AccountsPanel";
import { InstrumentsPanel } from "../components/portfolio/InstrumentsPanel";
import { MarketDataPanel } from "../components/portfolio/MarketDataPanel";
import { SummaryPanel } from "../components/portfolio/SummaryPanel";
import { TransactionsPanel } from "../components/portfolio/TransactionsPanel";
import { useOwner } from "../hooks/useOwner";
import { logoutOwner } from "../services/authApi";
import { cn } from "../utils/cn";

const tabs = [
  { id: "summary", label: "요약" },
  { id: "transactions", label: "거래 입력" },
  { id: "accounts", label: "계좌" },
  { id: "instruments", label: "종목" },
  { id: "market", label: "시세·환율" },
] as const;

type TabId = (typeof tabs)[number]["id"];

/** Owner-only. Charts arrive with the dashboard step; this page already covers every input. */
export function PortfolioPage() {
  const { refresh } = useOwner();
  const navigate = useNavigate();
  const [tab, setTab] = useState<TabId>("summary");
  const [transactionAccountId, setTransactionAccountId] = useState<number | null>(null);
  const [loggingOut, setLoggingOut] = useState(false);

  async function handleLogout() {
    setLoggingOut(true);
    try {
      await logoutOwner();
    } finally {
      await refresh();
      navigate("/", { replace: true });
    }
  }

  function openTransactions(accountId: number) {
    setTransactionAccountId(accountId);
    setTab("transactions");
  }

  return (
    <Container className="py-10 sm:py-14">
      <meta name="robots" content="noindex, nofollow" />
      <div className="mb-6 flex flex-wrap items-end justify-between gap-4">
        <div>
          <p className="font-mono text-xs font-semibold uppercase tracking-[0.18em] text-emerald-200">Owner only</p>
          <h1 className="mt-2 text-2xl font-semibold text-zinc-50 sm:text-3xl">Investment Portfolio</h1>
        </div>
        <button
          type="button"
          onClick={handleLogout}
          disabled={loggingOut}
          className="rounded-md border border-white/10 bg-white/[0.06] px-3.5 py-2 text-sm font-semibold text-zinc-50 transition hover:bg-white/10 disabled:opacity-60"
        >
          {loggingOut ? "Signing out…" : "Sign out"}
        </button>
      </div>

      <nav className="mb-6 flex gap-1 overflow-x-auto border-b border-white/10" aria-label="Portfolio sections">
        {tabs.map((item) => (
          <button
            key={item.id}
            type="button"
            onClick={() => setTab(item.id)}
            aria-current={tab === item.id ? "page" : undefined}
            className={cn(
              "-mb-px shrink-0 border-b-2 px-3.5 py-2.5 text-sm font-medium transition",
              tab === item.id ? "border-emerald-300 text-emerald-100" : "border-transparent text-zinc-400 hover:text-zinc-100",
            )}
          >
            {item.label}
          </button>
        ))}
      </nav>

      {tab === "summary" ? <SummaryPanel onStart={() => setTab("accounts")} /> : null}
      {tab === "transactions" ? <TransactionsPanel initialAccountId={transactionAccountId} /> : null}
      {tab === "accounts" ? <AccountsPanel onAddTransaction={openTransactions} /> : null}
      {tab === "instruments" ? <InstrumentsPanel /> : null}
      {tab === "market" ? <MarketDataPanel /> : null}
    </Container>
  );
}

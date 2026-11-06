import type {
  AccountType,
  AssetClass,
  AssetGroup,
  InstrumentType,
  Market,
  Region,
  RepaymentType,
  TaxType,
  TransactionType,
} from "../../services/portfolioApi";

export const accountTypeLabels: Record<AccountType, string> = {
  BROKERAGE: "증권 위탁",
  ISA: "ISA",
  PENSION_SAVINGS: "연금저축",
  IRP: "IRP",
  BANK: "입출금",
  CMA: "CMA",
  TIME_DEPOSIT: "정기예금",
  INSTALLMENT_SAVINGS: "적금",
  INSURANCE: "보험",
  LOAN: "대출",
};

export const transactionTypeLabels: Record<TransactionType, string> = {
  OPENING: "기초잔고",
  DEPOSIT: "입금",
  WITHDRAW: "출금",
  TRANSFER_IN: "이체 입금",
  TRANSFER_OUT: "이체 출금",
  BUY: "매수",
  SELL: "매도",
  DIVIDEND: "분배금·배당",
  INTEREST: "이자",
  FEE: "수수료",
  TAX: "세금",
  LOAN_DRAW: "대출 실행",
  LOAN_REPAY: "원금 상환",
};

/** Types the backend accepts per account (transfers have their own form; insurance uses valuations). */
export function allowedTransactionTypes(accountType: AccountType): TransactionType[] {
  if (accountType === "LOAN") return ["LOAN_DRAW", "LOAN_REPAY"];
  if (accountType === "INSURANCE") return [];
  if (["BROKERAGE", "ISA", "PENSION_SAVINGS", "IRP"].includes(accountType)) {
    return ["BUY", "SELL", "DIVIDEND", "OPENING", "DEPOSIT", "WITHDRAW", "INTEREST", "FEE", "TAX"];
  }
  return ["DEPOSIT", "WITHDRAW", "INTEREST", "OPENING", "FEE", "TAX"];
}

export const repaymentTypeLabels: Record<RepaymentType, string> = {
  EQUAL_PAYMENT: "원리금균등",
  EQUAL_PRINCIPAL: "원금균등",
  BULLET: "만기일시",
};

export const marketLabels: Record<Market, string> = { KRX: "국내(KRX)", NASDAQ: "NASDAQ", NYSE: "NYSE", AMEX: "AMEX" };
export const instrumentTypeLabels: Record<InstrumentType, string> = { ETF: "ETF", STOCK: "주식" };
export const assetClassLabels: Record<AssetClass, string> = {
  EQUITY: "주식",
  BOND: "채권",
  COMMODITY: "원자재",
  REIT: "리츠",
  CASH_LIKE: "현금성",
  MIXED: "혼합",
};
export const regionLabels: Record<Region, string> = {
  KR: "한국",
  US: "미국",
  GLOBAL: "글로벌",
  DM: "선진국",
  EM: "신흥국",
  CN: "중국",
  JP: "일본",
  OTHER: "기타",
};
export const taxTypeLabels: Record<TaxType, string> = {
  DOMESTIC_EQUITY_ETF: "국내주식형 ETF (매매차익 비과세)",
  OTHER_ETF: "기타 ETF (매매차익 배당소득세)",
  DOMESTIC_STOCK: "국내 주식",
  FOREIGN_STOCK: "해외 주식 (양도소득세)",
};
export const assetGroupLabels: Record<AssetGroup, string> = {
  CASH_EQUIVALENT: "현금성",
  INVESTMENT: "투자",
  PENSION_INSURANCE: "연금·보장",
  LIABILITY: "부채",
};

const krwFormat = new Intl.NumberFormat("ko-KR", { maximumFractionDigits: 0 });
const numberFormat = new Intl.NumberFormat("ko-KR", { maximumFractionDigits: 8 });

export function formatKrw(value: number | null | undefined): string {
  return value == null ? "—" : `${value < 0 ? "-" : ""}₩${krwFormat.format(Math.abs(value))}`;
}

export function formatNumber(value: number | null | undefined): string {
  return value == null ? "—" : numberFormat.format(value);
}

export function formatPercent(value: number | null | undefined): string {
  return value == null ? "—" : `${value > 0 ? "+" : ""}${value.toFixed(2)}%`;
}

/** Korean convention: gains red, losses blue. */
export function profitClassName(value: number | null | undefined): string {
  if (value == null || value === 0) return "text-zinc-300";
  return value > 0 ? "text-rose-400" : "text-sky-400";
}

export function todayInSeoul(): string {
  return new Intl.DateTimeFormat("sv-SE", { timeZone: "Asia/Seoul" }).format(new Date());
}

/** Accepts "1,234,567" or "1234.5"; empty -> null. */
export function parseNumber(text: string): number | null {
  const cleaned = text.replace(/,/g, "").trim();
  if (cleaned === "") return null;
  const value = Number(cleaned);
  return Number.isFinite(value) ? value : null;
}

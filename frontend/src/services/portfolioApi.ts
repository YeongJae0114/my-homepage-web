import { fetchJson, sendRequest } from "./apiClient";

export type AccountType =
  | "BROKERAGE"
  | "ISA"
  | "PENSION_SAVINGS"
  | "IRP"
  | "BANK"
  | "CMA"
  | "TIME_DEPOSIT"
  | "INSTALLMENT_SAVINGS"
  | "INSURANCE"
  | "LOAN";
export type RepaymentType = "EQUAL_PAYMENT" | "EQUAL_PRINCIPAL" | "BULLET";
export type TransactionType =
  | "OPENING"
  | "DEPOSIT"
  | "WITHDRAW"
  | "TRANSFER_IN"
  | "TRANSFER_OUT"
  | "BUY"
  | "SELL"
  | "DIVIDEND"
  | "INTEREST"
  | "FEE"
  | "TAX"
  | "LOAN_DRAW"
  | "LOAN_REPAY";
export type Market = "KRX" | "NASDAQ" | "NYSE" | "AMEX";
export type InstrumentType = "ETF" | "STOCK";
export type AssetClass = "EQUITY" | "BOND" | "COMMODITY" | "REIT" | "CASH_LIKE" | "MIXED";
export type Region = "KR" | "US" | "GLOBAL" | "DM" | "EM" | "CN" | "JP" | "OTHER";
export type TaxType = "DOMESTIC_EQUITY_ETF" | "OTHER_ETF" | "DOMESTIC_STOCK" | "FOREIGN_STOCK";
export type PriceSource = "KIS" | "MANUAL";
export type AssetGroup = "CASH_EQUIVALENT" | "INVESTMENT" | "PENSION_INSURANCE" | "LIABILITY";

export type AccountInput = {
  name: string;
  institution: string;
  accountType: AccountType;
  accountNumberLast4: string | null;
  interestRate: number | null;
  monthlyPayment: number | null;
  repaymentType: RepaymentType | null;
  openedAt: string | null;
  maturityDate: string | null;
  closedAt: string | null;
  displayOrder: number | null;
  memo: string | null;
};
export type Account = AccountInput & { id: number; investment: boolean; displayOrder: number };

export type Valuation = { id: number; accountId: number; valuationDate: string; amount: number; memo: string | null };

export type InstrumentInput = {
  symbol: string;
  name: string;
  market: Market;
  instrumentType: InstrumentType;
  underlyingAssetClass: AssetClass;
  exposureRegion: Region;
  fxHedged: boolean;
  taxType: TaxType;
  priceSource: PriceSource;
  active: boolean;
};
export type Instrument = InstrumentInput & { id: number; tradingCurrency: string };

export type TransactionInput = {
  accountId: number;
  type: TransactionType;
  tradeDate: string;
  instrumentId: number | null;
  quantity: number | null;
  price: number | null;
  fxRate: number | null;
  amount: number | null;
  fee: number | null;
  tax: number | null;
  memo: string | null;
  useLatestPrice?: boolean;
};
export type Transaction = TransactionInput & {
  id: number;
  amount: number;
  fee: number;
  tax: number;
  transferGroupId: string | null;
  editable: boolean;
};
export type TransactionPage = { items: Transaction[]; page: number; size: number; totalElements: number };
export type TransactionCsvFile = { filename: string; content: string };
export type TransactionCsvImport = {
  newRows: number;
  duplicateRows: number;
  rows: {
    line: number;
    tradeDate: string;
    account: string;
    type: string;
    instrument: string | null;
    amount: string;
    toAccount: string | null;
    duplicate: boolean;
  }[];
};
export type TransferInput = { fromAccountId: number; toAccountId: number; tradeDate: string; amount: number; memo: string | null };

export type Holding = {
  instrumentId: number;
  symbol: string;
  name: string;
  market: Market;
  tradingCurrency: string;
  accountId: number;
  accountName: string;
  quantity: number;
  avgPriceLocal: number;
  closePrice: number;
  priceDate: string | null;
  priceStale: boolean;
  priceSource: "KIS" | "MANUAL" | null;
  priceCollectedAt: string | null;
  marketValueKrw: number;
  costKrw: number;
  unrealizedProfit: number;
  returnRate: number | null;
  priceEffect: number | null;
  fxEffect: number | null;
  weightPercent: number | null;
};
export type ExposureItem = { key: string; amount: number; weightPercent: number | null };
export type HistoryRange = "1M" | "3M" | "6M" | "1Y" | "ALL";
export type PeriodChange = {
  baseDate: string;
  netWorthChange: number;
  investmentChange: number;
  netContribution: number;
  investmentGain: number;
};
export type Dashboard = {
  summary: {
    totalAssets: number;
    totalLiabilities: number;
    netWorth: number;
    monthOverMonthChange: number | null;
    monthOverMonthRate: number | null;
    dayChange: PeriodChange | null;
    monthChange: PeriodChange | null;
  };
  performance: {
    xirr: number | null;
    xirrAnnualized: boolean | null;
    since: string | null;
    twr: number | null;
    twrSince: string | null;
    unrealizedProfit: number;
    realizedProfit: number;
    dividendIncome: number;
    netContribution: number;
  };
  allocation: { group: AssetGroup; amount: number; weightPercent: number | null }[];
  exposure: Record<"byRegion" | "byCurrency" | "byAssetClass", ExposureItem[]>;
  history: { date: string; totalAssets: number; totalLiabilities: number; netWorth: number }[];
  holdings: Holding[];
  accounts: { id: number; name: string; institution: string; accountType: AccountType; group: AssetGroup; valueKrw: number }[];
  upcomingMaturities: {
    accountId: number;
    name: string;
    accountType: AccountType;
    maturityDate: string;
    daysLeft: number;
    expectedInterestAfterTax: number | null;
  }[];
  freshness: {
    oldestPriceDate: string | null;
    latestPriceDate: string | null;
    staleHoldings: number;
    latestCollectedAt: string | null;
    fxRates: { currency: string; rateToKrw: number; rateDate: string }[];
  };
  calculatedAt: string;
};
export type SourceStatus = { configured: boolean; lastRunAt: string | null; lastResult: string | null };
export type Integrations = { kis: SourceStatus; kexim: SourceStatus };

function json(method: "POST" | "PUT", body: unknown): RequestInit {
  return { method, headers: { "Content-Type": "application/json" }, body: JSON.stringify(body) };
}

export const portfolioApi = {
  getAccounts: () => fetchJson<Account[]>("/portfolio/accounts"),
  createAccount: (input: AccountInput) => fetchJson<Account>("/portfolio/accounts", json("POST", input)),
  updateAccount: (id: number, input: AccountInput) => fetchJson<Account>(`/portfolio/accounts/${id}`, json("PUT", input)),
  getValuations: (accountId: number) => fetchJson<Valuation[]>(`/portfolio/accounts/${accountId}/valuations`),
  saveValuation: (accountId: number, input: { valuationDate: string; amount: number; memo: string | null }) =>
    fetchJson<Valuation>(`/portfolio/accounts/${accountId}/valuations`, json("POST", input)),

  getInstruments: () => fetchJson<Instrument[]>("/portfolio/instruments"),
  createInstrument: (input: InstrumentInput) => fetchJson<Instrument>("/portfolio/instruments", json("POST", input)),
  updateInstrument: (id: number, input: InstrumentInput) =>
    fetchJson<Instrument>(`/portfolio/instruments/${id}`, json("PUT", input)),

  getTransactions: (params: { accountId?: number; page?: number; size?: number }) => {
    const query = new URLSearchParams();
    if (params.accountId) query.set("accountId", String(params.accountId));
    query.set("page", String(params.page ?? 0));
    query.set("size", String(params.size ?? 50));
    return fetchJson<TransactionPage>(`/portfolio/transactions?${query}`);
  },
  getTransactionCsvTemplate: () => fetchJson<TransactionCsvFile>("/portfolio/transactions/csv/template"),
  exportTransactionsCsv: () => fetchJson<TransactionCsvFile>("/portfolio/transactions/csv/export"),
  previewTransactionsCsv: (content: string) =>
    fetchJson<TransactionCsvImport>("/portfolio/transactions/csv/preview", json("POST", { content })),
  importTransactionsCsv: (content: string) =>
    fetchJson<TransactionCsvImport>("/portfolio/transactions/csv/import", json("POST", { content })),
  createTransaction: (input: TransactionInput) => fetchJson<Transaction>("/portfolio/transactions", json("POST", input)),
  updateTransaction: (id: number, input: TransactionInput) =>
    fetchJson<Transaction>(`/portfolio/transactions/${id}`, json("PUT", input)),
  deleteTransaction: (id: number) => sendRequest(`/portfolio/transactions/${id}`, { method: "DELETE" }),
  createTransfer: (input: TransferInput) => fetchJson<Transaction[]>("/portfolio/transfers", json("POST", input)),

  getDashboard: (range = "1Y") => fetchJson<Dashboard>(`/portfolio/dashboard?range=${range}`),
  savePrices: (date: string, items: { instrumentId: number; closePrice: number }[]) =>
    sendRequest("/portfolio/prices", json("PUT", { date, items })),
  saveFxRates: (date: string, items: { currency: string; rateToKrw: number }[]) =>
    sendRequest("/portfolio/fx-rates", json("PUT", { date, items })),
  captureSnapshot: () => fetchJson<{ snapshotDate: string; netWorth: number }>("/portfolio/snapshots", { method: "POST" }),
  getIntegrations: () => fetchJson<Integrations>("/portfolio/integrations"),
};

# 투자 포트폴리오(Owner 전용 자산 대시보드) 설계안

> 상태: **장중 시세 수집과 자산 추적 정책 반영** · 갱신일 2026-11-12
> 대상 저장소: `my-homepage-web` (backend: Spring Boot 4.0.6 / Java 17, frontend: React 19 + Vite + TS + Tailwind)

---

## 0. 확정 정책 요약

| 영역 | 결정 |
|---|---|
| 인증 | 소유자 1명, 환경변수 계정(bcrypt 해시), **서버 세션 쿠키**, 히든 로그인 `/owner`, 2FA는 v2 |
| 데이터 원본 | **거래 원장(Transaction Ledger)**. 보유 수량·평단·잔액은 거래를 다시 합산해 계산 |
| 데이터 구조 | 계좌 / 종목 마스터 / 거래 / 수동 평가 / 시세 이력 / 환율 이력 / 일별 스냅샷 |
| 시작 방식 | **기초잔고(OPENING)**로 현금과 보유 수량을 등록. 매입 평단을 모르면 최근 시세와 환율을 추적 시작 기준으로 사용 |
| 주력 자산 | **국내 ETF**(주력), 국내 주식, 해외 주식(가끔). 가상자산은 v2 |
| 시세 | **KIS Open API**로 국내·미국 보유 종목을 정규장 중 5분마다 수집 |
| 환율 | **한국수출입은행 환율 API** (영업일 1회) |
| API 키 | **연동 코드는 지금 개발, 키는 나중에 환경변수로 주입.** 키가 없으면 수집을 건너뛰고 수동 입력으로 운영 |
| 자산 추적 | 추정 순자산, 오늘·이번 달 증감, 투자 계좌 순입금과 입출금 제외 증감. XIRR·TWR은 상세 보기 |
| 거래 입력 | 첫 등록·일괄 입력용 CSV, 평소 매매·입출금은 거래 화면에서 한 건씩 기록 |
| 스냅샷 | **평일 18:00 KST** 자동 저장 + 수동 저장 버튼 |
| 금액 | 전부 `BigDecimal` / `numeric`, 기준 통화 KRW, 계산은 서버에서만 |
| 화면 | 기존 다크 테마, 손익 색 **한국식(상승 빨강·하락 파랑)**, 금액 숨김 토글, Recharts |
| 세금 | v1은 **세전** 표시. 과세 유형만 저장하고 세후 계산은 v2 |

---

## 1. 기존 코드베이스 분석

### 1.1 프론트엔드

| 항목 | 현재 상태 | 근거 |
|---|---|---|
| 라우터 | `BrowserRouter` + `App.tsx`의 `<Routes>` 단일 목록 (react-router-dom 7) | `frontend/src/main.tsx`, `frontend/src/App.tsx` |
| 네비게이션 | `config/navigation.ts`의 정적 배열 → `Header.tsx`가 `enabled` 필터 + `order` 정렬 | `frontend/src/components/layout/Header.tsx:11` |
| 레이아웃 | `Header` / `main` / `Footer` 고정 셸, 다크 테마 `bg-surface-950`, emerald/cyan 포인트 | `tailwind.config.js`, `Card.tsx` |
| 재사용 컴포넌트 | `Card`, `MetricCard`(label/value/detail), `Badge`, `StatusBadge`, `SectionTitle`, `Container` | `components/common/*` |
| API 클라이언트 | `fetchJson()` 한 개, base `/api` | `services/apiClient.ts` |
| 데이터 훅 | `useApiFallback`: **API 실패 시 정적 fallback 데이터를 보여줌** | `hooks/useApiFallback.ts` |
| 차트 라이브러리 | **없음** | `package.json` |
| 배포/프록시 | Vercel이 `/api/*` → `https://api.zerojae.cloud/api/*` 로 rewrite, 로컬은 Vite proxy가 같은 곳으로 | `vercel.json`, `vite.config.ts` |

> 브라우저 입장에서 API는 **항상 같은 Origin**(`/api`)입니다. 쿠키 기반 인증이 CORS나 서드파티 쿠키 문제 없이 그대로 동작합니다.

### 1.2 백엔드

| 항목 | 현재 상태 |
|---|---|
| 인증/인가 | **없음.** `spring-boot-starter-security` 미포함 |
| 공개 API | `/api/pages/**`, `/api/monitoring/**`, `/api/llm/{status,wake,chat}` 전부 무인증 |
| 기타 공개 엔드포인트 | Swagger(`/swagger-ui.html`, `/api-docs`), Actuator(`health,info,metrics,prometheus`) |
| 구조 컨벤션 | `domain/{name}/{controller,dto,entity,repository,service}`, Service 인터페이스 + `Impl`, DTO는 `record`, 엔티티는 Lombok `@Builder` + protected 생성자 |
| DB | PostgreSQL + Flyway (`V1`~`V10`), `ddl-auto: validate` |
| 외부 수집 패턴 | **`MonitoringCollectScheduler` + `NodeExporterClient`**: 스케줄러가 외부 HTTP를 주기 호출해 DB에 저장. **시세 수집이 이 패턴을 그대로 따름** |
| 배포 | GitHub Actions → Docker Hub → SSH로 `.env.production` 생성 후 `compose up` |

### 1.3 설계에 영향을 주는 발견 사항

1. **Spring Security를 추가하면 기본값이 "전부 잠금"**입니다. 기존 공개 API가 깨지지 않도록 `permitAll` 범위를 명시해야 합니다.
2. `useApiFallback`을 포트폴리오에 쓰면 **API 실패 시 가짜 자산 데이터가 표시**됩니다. 포트폴리오는 fallback 없이 에러나 로그인 상태만 표시합니다.
3. `fetchJson`은 `...init`을 `headers` 뒤에 펼쳐서, `init.headers`를 넘기면 `Accept`가 사라집니다. CSRF 헤더를 추가할 때 순서를 고쳐야 합니다.
4. 로컬 Vite proxy가 **운영 백엔드**를 가리킵니다. 로컬 개발은 로컬 백엔드로 전환합니다.
5. (범위 밖, 참고) `/api/llm/wake`도 누구나 호출할 수 있습니다. 인증이 생기면 Owner 전용으로 옮길 수 있습니다.

---

## 2. 1인 Owner 전용 인증·보안

### 2.1 방식: Spring Security + 환경변수 계정 1개 + 서버 세션(HttpOnly 쿠키)

| 선택지 | 판단 |
|---|---|
| **세션 쿠키 (채택)** | 같은 Origin 구조라 그대로 동작. JS가 토큰에 접근할 수 없어 XSS로 탈취되지 않음. 로그아웃은 세션 무효화로 즉시 끝남 |
| JWT | 사용자 1명, 서버 1대라 stateless의 이점이 없고, 저장·갱신·폐기 로직만 늘어남 → 미채택 |
| 회원 테이블 | 불필요. `InMemoryUserDetailsManager`에 환경변수로 1명 등록 |

```
OWNER_USERNAME=...
OWNER_PASSWORD_HASH={bcrypt}$2a$12$...   # 평문 비밀번호는 어디에도 저장하지 않음
```

### 2.2 인증 흐름

```
POST /api/auth/login {username,password}
  ├ 실패: 401 (+ 실패 카운트)          ※ 아이디/비밀번호 중 무엇이 틀렸는지 구분하지 않음
  └ 성공: 세션 ID 재발급(세션 고정 공격 방지) → 204 + Set-Cookie: JSESSIONID
GET  /api/auth/me      → {"owner": true|false}  (항상 200)
POST /api/auth/logout  → 세션 invalidate, 204
```

```yaml
server:
  servlet:
    session:
      timeout: 2h
      cookie: { http-only: true, secure: true, same-site: strict }   # Domain 미지정 → host-only
```

- 세션 저장소는 메모리입니다. 컨테이너 재시작 시 재로그인이 필요하며, 1인 용도라 수용 가능합니다.
- **CSRF:** `http.csrf(csrf -> csrf.spa())` (`XSRF-TOKEN` 쿠키 / `X-XSRF-TOKEN` 헤더). 프론트는 변경 요청에만 헤더를 붙입니다.
- **무차별 대입:** 계정 단위로 5회 실패 시 15분 잠금(429). Vercel 경유 구조라 IP 판정을 신뢰할 수 없어 전역 카운터로 합니다.
  - `// ponytail:` 공격자가 고의로 15분 잠글 수는 있음(데이터 유출은 없음). 문제가 되면 2FA 도입 후 완화.

### 2.3 접근 제어

**백엔드 SecurityFilterChain**

```
/api/auth/login, /api/auth/me   → permitAll
/api/auth/logout                → authenticated
/api/portfolio/**               → hasRole("OWNER")
그 외 기존 경로 전부            → permitAll   (기존 공개 동작 유지)
```

- `formLogin`과 `httpBasic`은 끕니다. 미인증은 **401 JSON**(`HttpStatusEntryPoint`), 권한 부족은 403입니다.
- `springdoc.paths-to-exclude: /api/portfolio/**, /api/auth/**`로 운영 Swagger에서 숨깁니다.
- 포트폴리오 응답에는 `Cache-Control: no-store`가 붙습니다 (Security 기본값).

**프론트엔드**

| 장치 | 내용 |
|---|---|
| `useOwner()` | 앱 시작 시 `/api/auth/me` 1회 호출, Context로 공유 |
| 메뉴 숨김 | `NavigationItem.ownerOnly`를 추가하고, Header 필터에 `!ownerOnly \|\| owner` 조건을 더함 |
| Route Guard | `<RequireOwner>`: 비로그인이면 **`/`로 리다이렉트** (`/owner` 경로 존재를 드러내지 않음) |
| 코드 분리 | 포트폴리오 페이지와 Recharts를 `React.lazy`로 분리. 일반 방문자는 대시보드 번들을 받지 않음 |
| 세션 만료 | 포트폴리오 API가 401을 받으면 owner를 false로 바꾸고 `/owner`로 이동 |
| 히든 로그인 | `/owner`는 어디에도 링크하지 않고 `noindex`. **숨김은 UX일 뿐, 보안은 백엔드가 담당** |

---

## 3. 데이터 모델

### 3.1 설계 원칙

- **거래 원장이 유일한 원본**입니다. 보유 수량, 평균단가, 현금 잔액, 대출 잔액은 거래를 시간순으로 다시 합산해서 구합니다.
- 시세, 환율, 스냅샷은 **원장 위에 얹는 계산 재료와 결과**입니다. 과거 거래를 수정하면 스냅샷을 다시 계산합니다.
- 실제 자산 데이터는 저장소에 커밋하지 않습니다. 시드 데이터는 가상 값만 씁니다. 계좌번호는 끝 4자리만 저장합니다.

> **구현 반영 (2단계, `V11__create_portfolio_ledger_tables.sql`)**
> - 다른 도메인과 섞이지 않도록 실제 테이블명에 `portfolio_` 접두어를 붙였습니다: `portfolio_accounts`, `portfolio_instruments`, `portfolio_transactions`, `portfolio_manual_valuations`.
> - 대출 계좌에는 `LOAN_DRAW` / `LOAN_REPAY`만 허용합니다. 시작 잔액도 `LOAN_DRAW`로 기록하므로 `OPENING`은 대출 계좌에 쓰지 않습니다.
> - 종목의 거래 통화는 시장에서 자동으로 정합니다 (KRX → KRW, 미국 거래소 → USD).
> - 수동 평가는 v1에서 보험 계좌에만 허용합니다.
> - 이체는 전용 API로 출금·입금 쌍을 한 번에 만들고, 한쪽을 지우면 양쪽이 함께 지워집니다. 이체는 수정할 수 없고 삭제 후 재등록합니다.
> - 모든 거래 변경 후 해당 계좌의 원장을 다시 재생해서, 초과 매도나 초과 상환이 생기면 400으로 거부하고 롤백합니다 (과거 거래 수정·삭제 포함).

### 3.2 엔티티 관계

```
accounts 1 ──< transactions >── 0..1 instruments 1 ──< prices
    │                                                    fx_rates (통화별)
    └──< manual_valuations (보험 해약환급금 등)
portfolio_snapshots (일별, 전체 포트폴리오 단위)
```

### 3.3 계좌 (`accounts`)

| 컬럼 | 타입 | 비고 |
|---|---|---|
| id | bigint identity | |
| name | varchar(100) not null | "ISA 중개형", "급여통장" |
| institution | varchar(100) not null | 증권사·은행·보험사 |
| account_type | varchar(30) not null | 아래 enum |
| account_number_last4 | char(4) | 끝 4자리만 |
| interest_rate | numeric(7,4) | 연 %. 예적금·대출 |
| monthly_payment | numeric(19,2) | 적금 월납입액, 대출 월상환액 |
| repayment_type | varchar(30) | 대출: EQUAL_PAYMENT(원리금균등) / EQUAL_PRINCIPAL(원금균등) / BULLET(만기일시) |
| opened_at / maturity_date / closed_at | date | |
| display_order, memo, created_at, updated_at | | |

**계좌 유형과 자산군 매핑**

| AccountType | 자산군 (대시보드 그룹) | 수익률(XIRR/TWR) 범위 |
|---|---|---|
| `BROKERAGE` (일반 위탁), `ISA` | 종목은 **투자**, 예수금은 **현금성** | ✅ 포함 |
| `PENSION_SAVINGS`, `IRP` | 계좌 전체를 **연금·보장** (인출 제한이 있어 계좌 단위로 묶음) | ✅ 포함 |
| `BANK`(입출금), `CMA`, `TIME_DEPOSIT`(예금), `INSTALLMENT_SAVINGS`(적금) | **현금성** | ❌ |
| `INSURANCE` | **연금·보장** (해약환급금 기준) | ❌ |
| `LOAN` | **부채** | ❌ |
| `CRYPTO_EXCHANGE` | 투자 | v2 |

### 3.4 종목 마스터 (`instruments`)

ETF 중심 포트폴리오라 **"어디에 상장됐는가"와 "실제로 무엇에 투자하는가"를 분리**합니다.

| 컬럼 | 타입 | 예시: TIGER 미국S&P500 |
|---|---|---|
| id | bigint identity | |
| symbol | varchar(20) not null | `360750` (KRX 6자리) / `AAPL` |
| name | varchar(100) not null | TIGER 미국S&P500 |
| market | varchar(10) not null | `KRX` / `NASDAQ` / `NYSE` / `AMEX` |
| trading_currency | char(3) not null | `KRW` |
| instrument_type | varchar(10) not null | `ETF` / `STOCK` |
| underlying_asset_class | varchar(20) not null | `EQUITY` / `BOND` / `COMMODITY` / `REIT` / `CASH_LIKE` / `MIXED` |
| exposure_region | varchar(10) not null | `US` (KR / US / GLOBAL / DM / EM / CN / JP / OTHER) |
| fx_hedged | boolean not null | `false` (종목명에 `(H)`가 있으면 true) |
| tax_type | varchar(30) not null | `OTHER_ETF` (DOMESTIC_EQUITY_ETF / OTHER_ETF / DOMESTIC_STOCK / FOREIGN_STOCK) |
| price_source | varchar(10) not null default `KIS` | `KIS` / `MANUAL` |
| active | boolean not null | |

unique `(market, symbol)`

**통화 노출 규칙:** 해외 직접 보유분은 `trading_currency`를 따릅니다. 국내 상장 해외 ETF 중 환노출형은 기초자산 통화(US → USD)로, 환헤지형은 KRW로 집계합니다.

### 3.5 거래 원장 (`transactions`)

| 컬럼 | 타입 | 비고 |
|---|---|---|
| id | bigint identity | |
| account_id | bigint not null FK | |
| type | varchar(20) not null | 아래 표 |
| trade_date | date not null | KST 기준, 미래 날짜 불가 |
| instrument_id | bigint FK | BUY/SELL/DIVIDEND/OPENING(종목)일 때 |
| quantity | numeric(28,8) | |
| price | numeric(19,4) | **거래 통화 기준** 체결가 |
| fx_rate | numeric(19,6) not null default 1 | 거래 통화 → KRW 체결 환율 |
| amount | numeric(19,2) not null | **실제 정산금액(KRW, 항상 양수)**. 부호는 type이 결정. 기본값은 자동 계산하고, 증권사 정산금액과 다르면 수정 가능 |
| fee | numeric(19,2) not null default 0 | |
| tax | numeric(19,2) not null default 0 | 거래세, 원천징수세 |
| transfer_group_id | uuid | 내 계좌 간 이체 쌍을 연결 |
| memo, created_at, updated_at | | |

**거래 유형**

| type | 현금 | 보유 수량 | 외부 현금흐름 (XIRR) | 설명 |
|---|---|---|---|---|
| `OPENING` | +amount 또는 종목 수량·평단 설정 | 설정 | ✅ 기준일 유입 | 기초잔고 |
| `DEPOSIT` / `WITHDRAW` | + / − | | ✅ | 외부 입출금 (급여 등) |
| `TRANSFER_IN` / `TRANSFER_OUT` | + / − | | 수익률 범위 경계를 넘을 때만 ✅ | 내 계좌 간 이동. 쌍으로 기록 |
| `BUY` | −(qty×price×fx + fee) | +qty | | |
| `SELL` | +(qty×price×fx − fee − tax) | −qty | | 보유 수량 초과 매도 불가 |
| `DIVIDEND` | +amount (세후 입금액) | | | ETF 분배금, 배당. `tax` = 원천징수 |
| `INTEREST` | +amount | | | 예적금·CMA 이자 |
| `FEE` / `TAX` | − | | | 별도 청구분 |
| `LOAN_DRAW` / `LOAN_REPAY` | 부채 +/− | | | 대출 실행 / 원금 상환 |

- DB check 제약: BUY/SELL은 `instrument_id`, `quantity > 0`, `price > 0` 필수. `amount >= 0`.
- `// ponytail:` 해외주식은 **원화 결제 기준**(BUY/SELL에 체결 환율 기록)으로 처리하고, 외화 예수금은 v2입니다. 달러 예수금을 장기 보유하게 되면 통화별 현금 잔액을 추가합니다.

### 3.6 수동 평가 (`manual_valuations`)

거래로 계산할 수 없는 가치를 입력합니다 (보험 해약환급금 등). 현금흐름이 아니므로 원장과 분리합니다.

| account_id | valuation_date | amount | memo |
|---|---|---|---|

unique `(account_id, valuation_date)`. 평가 시점에는 해당 일자 이전의 최신 값을 씁니다.

### 3.7 시세·환율 이력

**`prices`**: PK `(instrument_id, price_date)`

| 컬럼 | 비고 |
|---|---|
| close_price numeric(19,4) | 장중에는 같은 날짜 행을 계속 갱신, 장 마감 후 종가로 확정 |
| source varchar(10) | `KIS` / `MANUAL` |
| is_final boolean | 종가 확정 여부 |
| captured_at timestamp | |

**`fx_rates`**: PK `(currency, rate_date)`. 컬럼은 `rate_to_krw numeric(19,6)`(외화 1단위당 원화), `source`(`KEXIM`/`MANUAL`), `captured_at`.

- 수출입은행은 JPY를 100엔 단위로 주므로 **저장 전에 1단위로 정규화**합니다.
- **같은 날짜에 자동 값과 수동 값이 둘 다 있으면 수동 값이 우선**합니다 (소유자의 정정으로 간주).
- 평가 기준 시세는 "평가일 이전의 가장 최근 값"입니다. 7일 이상 지난 값이면 화면에 "시세 오래됨" 배지를 붙입니다.

### 3.8 일별 스냅샷 (`portfolio_snapshots`)

| 컬럼 | 비고 |
|---|---|
| snapshot_date date **unique** | |
| cash_equivalent, investment, pension_insurance | 자산군별 평가액 |
| total_assets, total_liabilities, net_worth | |
| scope_value | 수익률 범위(투자 계좌) 평가액 |
| scope_net_flow | 직전 스냅샷 이후 수익률 범위의 순 외부 현금흐름 |
| price_as_of date | 사용된 시세 중 가장 오래된 날짜 |
| computed_at timestamp | |

- **스케줄:** 평일 18:00 KST (시간외 거래 종료 후. 공식 종가는 15:30에 확정)에 국내 당일 종가와 미국 전일 종가로 계산합니다.
- **휴장일 처리:** 직전 스냅샷 이후 시세·환율·거래·평가 중 아무것도 바뀌지 않았으면 저장하지 않습니다. 휴장일 달력을 따로 관리하지 않습니다.
- **수동 저장:** "지금 스냅샷 저장" 버튼. 같은 날짜면 덮어씁니다.
- **고정 (변경된 정책):** 한 번 찍힌 스냅샷은 다시 계산하지 않습니다 (오늘 스냅샷만 다시 찍기 가능).
  - **수정 가능 범위:** 마지막 스냅샷 이후에 입력한 거래·보험 평가만 수정·삭제할 수 있습니다. 그 전 입력은 잠기고, 정정은 반대 거래로 합니다. 스냅샷이 하나도 없는 초기 입력 기간에는 전부 수정할 수 있습니다.
  - **과거 날짜 입력:** 새로 입력하는 것은 언제나 가능하며, 이미 찍힌 스냅샷에는 반영되지 않고 다음 스냅샷부터 반영됩니다.
  - **TWR 보정 (`V13`):** 스냅샷마다 그때까지의 누적 투자금(`scope_cumulative_flow`)을 저장하고, 구간 순입금 = 이번 누적 − 직전 누적으로 계산합니다. 나중에 과거 날짜로 넣은 입금이 수익으로 잡히지 않습니다.
  - 시세·환율 수동 정정은 언제든 가능하며 현재 평가에만 영향을 줍니다.

---

## 4. 계산 로직

모든 계산은 서버의 순수 계산 클래스 두 개에서 합니다.
- `HoldingsCalculator`: 원장 재생, 평가
- `ReturnCalculator`: XIRR, TWR

프론트는 받은 값을 표시만 합니다.

### 4.1 보유 종목 (원장 재생, 이동평균법)

거래를 `(trade_date, id)` 순서로 재생합니다. 국내 증권사 앱의 평단 표시와 같은 방식입니다.

```
BUY  : qty      += q
       cost_krw += q × price × fx + fee          (매수 수수료는 평단에 포함)
       cost_loc += q × price                     (현지통화 평단 표시용)
SELL : avg_krw   = cost_krw / qty
       realized += (q × price × fx − fee − tax) − avg_krw × q
       cost_krw −= avg_krw × q ;  cost_loc −= (cost_loc / qty) × q ;  qty −= q
```

### 4.2 평가

```
평가금액(KRW)  = qty × close(평가일) × fx(평가일)          (KRW 종목은 fx = 1)
평가손익       = 평가금액 − cost_krw
수익률(%)      = 평가손익 / cost_krw × 100                 (cost 0이면 null)

해외 종목 손익 분해 (주가 효과 + 환율 효과 = 평가손익)
  avg_loc  = cost_loc / qty,   avg_fx = cost_krw / cost_loc
  주가 효과 = qty × (close − avg_loc) × fx_now
  환율 효과 = qty × avg_loc × (fx_now − avg_fx)

계좌 현금 = Σ type별 부호 × amount
보험 등   = 최신 manual_valuation
부채 잔액 = Σ LOAN_DRAW − Σ LOAN_REPAY
```

### 4.3 포트폴리오 지표

```
총자산      = Σ 자산 평가액 (부채 제외)
총부채      = Σ 부채 잔액
순자산      = 총자산 − 총부채
자산군 비중 = 그룹 평가액 / 총자산 × 100
노출 비중   = 지역별·통화별·기초자산별 평가액 / 수익률 범위 종목 평가액 합 × 100
평가손익    = Σ (평가금액 − cost_krw)
실현손익    = Σ realized   (기간 필터 가능)
분배금 수익 = Σ DIVIDEND.amount
월 증감     = NW(이번 달 마지막 스냅샷) − NW(지난달 마지막 스냅샷)
```

### 4.4 XIRR (상세 수익률)

**범위:** 투자 계좌(`BROKERAGE`, `ISA`, `PENSION_SAVINGS`, `IRP`)

**현금흐름 (투자자 관점)**
- `OPENING`, `DEPOSIT`, 범위 밖에서 들어온 `TRANSFER_IN`: **음수** (내가 넣은 돈)
- `WITHDRAW`, 범위 밖으로 나간 `TRANSFER_OUT`: **양수**
- 평가일 현재 `scope_value`: 마지막 **양수** 흐름

```
Σ CF_i / (1 + r)^((d_i − d_0) / 365) = 0  을 만족하는 r
```

- 풀이: Newton-Raphson 방식으로 구하고, 수렴하지 않으면 이분법 `[-0.99, 10]`으로 대체합니다. 허용오차 1e-7, 최대 100회.
- 이자율 풀이에는 `double`을 써도 됩니다. 금액은 `BigDecimal`로 유지합니다.
- **투자 기간이 1년 미만이면 연환산하지 않고** 기간 수익률 `(1 + r)^(days/365) − 1`로 표시합니다. 응답에 `annualized: false`를 넣습니다.
- 범위 안의 계좌끼리 옮긴 돈(예: 일반 → ISA)은 현금흐름에서 **제외**합니다.

### 4.5 TWR (보조 수익률)

스냅샷 사이 구간 수익률을 연결합니다.

```
r_i = (V_i − V_{i-1} − F_i) / (V_{i-1} + 0.5 × F_i)     # 구간 내 흐름은 중간 시점으로 가정 (Modified Dietz)
TWR = Π (1 + r_i) − 1
```

- `V` = `scope_value`, `F` = `scope_net_flow`입니다.
- 일별 스냅샷이면 구간이 하루라서 근사 오차가 사실상 없습니다. 키가 없어 스냅샷 간격이 벌어진 기간도 같은 식으로 근사 처리합니다.

### 4.6 예적금 부가정보 (표시용)

```
만기 D-day         = maturity_date − today
정기예금 세후 이자 ≈ 원금 × 금리 × (기간일수 / 365) × (1 − 0.154)
적금(단리) 세후 이자 ≈ 월납입액 × 금리/12 × n(n+1)/2 × (1 − 0.154)       (n = 납입 개월 수)
```

### 4.7 반올림

- 내부 계산은 scale 8로 합니다.
- 응답의 KRW 금액은 정수(`HALF_UP`), 비율은 소수 2자리, 현지통화 가격은 소수 4자리입니다.

---

## 5. 시세·환율 연동 (키는 나중에 주입하는 구조)

### 5.1 원칙

- **연동 코드는 지금 모두 구현**합니다. 키는 환경변수로만 받습니다.
- **키가 비어 있으면** 수집 스케줄러가 "미설정"을 1회 로그로 남기고 매 회차를 건너뜁니다. 그동안 시세와 환율은 수동 입력으로 운영합니다. 앱 기동은 실패하지 않습니다.
- 키를 등록하고 재배포하면 **코드 변경 없이** 자동 수집이 시작됩니다.
- 기존 `MonitoringCollectScheduler` + `NodeExporterClient` 패턴(Spring `RestClient`, timeout 설정)을 그대로 따릅니다.

```yaml
portfolio:
  kis:
    base-url: ${KIS_BASE_URL:https://openapi.koreainvestment.com:9443}
    app-key: ${KIS_APP_KEY:}
    app-secret: ${KIS_APP_SECRET:}
  kexim:
    base-url: ${KEXIM_BASE_URL:https://oapi.koreaexim.go.kr}
    api-key: ${KEXIM_API_KEY:}
```

키 등록 경로: GitHub Secrets → `backend-ci-cd.yml`의 `deploy.env` 생성 단계 → `.env.production`. 기존 `DB_PASSWORD`와 같은 방식입니다.

> **구현 반영 (3단계, `V12__create_portfolio_market_data_tables.sql`)**
> - 테이블: `portfolio_prices`, `portfolio_fx_rates`, `portfolio_snapshots`. 시세·환율은 종목(통화)·날짜당 1행이고, **수집기는 MANUAL 행을 덮어쓰지 않습니다** (같은 날 자동·수동 값을 둘 다 두지 않고 1행으로 단순화).
> - 요약 화면에서 `GET /api/portfolio/dashboard`로 평가액, `GET /api/portfolio/integrations`로 마지막 수집 결과를 조회합니다.
> - `portfolio.schedule.kr-prices`는 KST 평일 09:00~15:40에 5분 간격으로 수집합니다. `portfolio.schedule.us-intraday`는 뉴욕 평일 09:30~16:10에 5분 간격으로 수집하며 서머타임을 적용합니다. 두 시장 모두 정규장 종료 이후 수집분을 `is_final`로 기록합니다.
> - `portfolio.schedule.us-prices`는 화~토 07:00 KST에 미국 장 마감 후 한 번 더 조회합니다. 미국 자동 수집 행은 뉴욕 날짜를 사용합니다. 환율은 평일 11:30 KST, 스냅샷은 평일 18:00 KST가 기본입니다.
> - 시세가 없는 종목은 원장의 기준 평단으로 평가하고 배지를 표시합니다. 외화 종목에 적용할 환율이 없으면 매입 평균 환율을 사용합니다.
> - KEXIM은 키가 URL에 들어가므로, 요청 실패 메시지에서 키를 가린 뒤에만 로그에 남깁니다.
> - 키 없이 검증: 응답 예시 픽스처로 `MockRestServiceServer` 테스트를 했고, 키 미설정 시 수집이 건너뛰어지는 것도 테스트로 확인했습니다. **실제 응답 필드는 키 발급 후 스모크 테스트로 확인이 필요합니다.**

### 5.2 KIS (국내·해외 시세)

| 구성요소 | 역할 |
|---|---|
| `KisClient` | 접근 토큰 캐시와 국내·미국 현재가 조회 |
| `PortfolioCollectScheduler` | 국내·미국 장중 시세, 장 마감 후 시세, 일별 환율 및 스냅샷 |

**보안 정책**
- KIS 실전 앱키는 **주문도 낼 수 있는 키**입니다. 코드에는 **주문·잔고 API를 아예 넣지 않고** 시세 조회만 구현합니다.
- 키와 토큰은 로그에 남기지 않습니다. 응답 로그에서도 마스킹합니다.
- 계좌번호는 시세 조회에 필요 없으므로 설정에 넣지 않습니다.

**운영 정책**
- 정기 수집 대상은 `price_source = KIS`인 활성 보유 종목입니다. 종목별로 순차 호출하며 호출 사이에 간격을 둡니다.
- 실패하면 해당 종목만 건너뜁니다. 직전 시세를 유지하고, 오래되면 배지로 알립니다. 한 종목이 실패해도 전체 수집은 멈추지 않습니다.
- 시세 날짜는 수집에 적용한 시장 날짜이며 저장 시각은 KST로 표시합니다. 공급사 체결 시각이나 시세 지연 시간은 응답에 포함되지 않습니다. 평일 정규장 기준으로 수집하며 별도의 휴장·조기 폐장 달력은 사용하지 않습니다.
- 오늘 날짜의 보유 종목 `OPENING`에 `useLatestPrice: true`를 보내면 서버가 최근 시세와 환율로 기준금액을 계산합니다. 저장된 값이 없을 때 키가 있으면 조회를 시도하고, 사용 가능한 값이 없으면 직접 입력하도록 안내합니다. 7일보다 오래된 가격이나 환율도 직접 확인을 안내합니다. 기준가는 거래 가격과 메모에 기록되며 CSV로 내보낼 수 있습니다. 이 방식의 손익은 등록 기준가부터 계산합니다.

### 5.3 한국수출입은행 (환율)

- `KeximFxClient`: 영업일 11:30 KST에 1회, 보유 통화(USD 등)만 저장합니다.
- 응답의 환율 값은 쉼표가 들어간 문자열이므로 파싱하고, JPY(100)은 1단위로 정규화합니다.
- 휴일에는 빈 응답이 오므로 저장하지 않고, 직전 값을 씁니다.

### 5.4 키 없이 검증하는 방법

- 각 클라이언트는 **공식 문서의 응답 예시를 JSON 픽스처로 저장**하고, `MockRestServiceServer`로 파싱과 정규화를 테스트합니다.
- 엔드포인트 경로, TR ID, 응답 필드명은 **구현 시점에 공식 문서로 다시 확인**합니다.
- 키를 발급받으면 실제 응답으로 스모크 테스트(종목 1개, 통화 1개)를 하고 픽스처와 비교합니다.

---

## 6. API 및 DTO

### 6.1 엔드포인트 (`/api/portfolio/**`는 전부 Owner 전용)

| Method | Path | 설명 |
|---|---|---|
| POST | `/api/auth/login` · `/api/auth/logout` | 로그인 / 로그아웃 |
| GET | `/api/auth/me` | `{owner}` |
| GET | `/api/portfolio/dashboard?range=1Y` | 대시보드 전체 1회 호출 (range: 1M/3M/6M/1Y/ALL) |
| GET / POST / PUT | `/api/portfolio/accounts[/{id}]` | 계좌. 삭제 대신 `closed_at`으로 해지 |
| GET / POST / PUT | `/api/portfolio/instruments[/{id}]` | 종목 마스터 |
| GET | `/api/portfolio/transactions?accountId&from&to&type` | 거래 내역 (페이징) |
| POST / PUT / DELETE | `/api/portfolio/transactions[/{id}]` | 거래 입력·수정·삭제. 저장 후 영향받는 날짜부터 스냅샷 재계산 |
| POST | `/api/portfolio/transfers` | 내 계좌 간 이체 (TRANSFER_OUT / TRANSFER_IN 쌍 생성) |
| GET | `/api/portfolio/transactions/csv/{template,export}` | 거래 CSV 양식 또는 전체 원장 다운로드용 파일 (`filename`, `content`) |
| POST | `/api/portfolio/transactions/csv/{preview,import}` | 거래 CSV 미리보기 또는 일괄 저장 (`content`) |
| GET | `/api/portfolio/holdings` | 원가 기준 보유 현황 (수량·평단·원가·실현손익·분배금, 계좌별 현금·대출 잔액). 2단계 구현 |
| PUT | `/api/portfolio/prices` | 시세 일괄 수동 입력 `{date, items:[{instrumentId, closePrice}]}` |
| PUT | `/api/portfolio/fx-rates` | 환율 수동 입력 `{date, items:[{currency, rateToKrw}]}` |
| POST | `/api/portfolio/accounts/{id}/valuations` | 수동 평가 (보험 등) |
| POST | `/api/portfolio/snapshots` | 오늘 스냅샷 저장/덮어쓰기 |
| GET | `/api/portfolio/integrations` | KIS·KEXIM 설정 여부와 최근 수집 시각/결과 (키 값은 절대 반환하지 않음) |

### 거래 원장 CSV

- 거래 입력 탭에서 양식을 내려받아 작성하고, 미리보기에서 저장할 행과 기존 기록을 확인한 뒤 가져옵니다. 파일은 최대 5MB이며 UTF-8과 한글 Windows CSV를 읽습니다. 다운로드는 UTF-8 BOM과 CRLF를 사용합니다.
- 계좌·종목을 먼저 등록합니다. `계좌`에는 사이트 계좌 ID를, `종목`에는 `KRX/069500`처럼 시장과 종목코드를 입력합니다. 이체는 한 행에 출금 계좌와 입금 계좌를 적고, 원장에는 두 거래로 저장합니다.
- 서버가 원장 검증을 통과한 파일을 한 트랜잭션으로 저장합니다. 기존 원장과 내용이 같은 행은 기존 건수만큼 건너뜁니다. 같은 조건의 체결이 여러 건인 파일은 행 수를 보존합니다. 정산금액·수수료·세금은 원 단위로 입력합니다.
- 내보내기에는 전체 거래 원장이 포함됩니다. 등록 계좌·종목, 시세·환율, 보험 평가, 스냅샷은 별도 데이터입니다.

### 6.2 대시보드 응답

```java
public record PortfolioDashboardResponse(
    SummaryResponse summary,
    PerformanceResponse performance,
    List<AllocationResponse> allocation,          // 자산군 비중
    ExposureResponse exposure,                    // 지역·통화·기초자산 노출
    List<DailyNetWorthResponse> history,          // range 내 일별 (프론트에서 월별로 묶기 가능)
    List<HoldingResponse> holdings,
    List<AccountSummaryResponse> accounts,
    List<MaturityResponse> upcomingMaturities,    // 90일 내 만기
    DataFreshnessResponse freshness,              // 시세·환율 기준일, 오래된 종목 수
    OffsetDateTime calculatedAt
) {}

public record SummaryResponse(
    BigDecimal totalAssets, BigDecimal totalLiabilities, BigDecimal netWorth,
    BigDecimal monthOverMonthChange, BigDecimal monthOverMonthRate   // 비교 대상이 없으면 null
) {}

public record PerformanceResponse(
    BigDecimal xirr, boolean xirrAnnualized, BigDecimal twr, LocalDate since,
    BigDecimal unrealizedProfit, BigDecimal realizedProfit, BigDecimal dividendIncome,
    BigDecimal netContribution                     // 순 납입액 (넣은 돈 − 뺀 돈)
) {}

public record AllocationResponse(AssetGroup group, BigDecimal amount, BigDecimal weightPercent) {}

public record ExposureResponse(
    List<ExposureItem> byRegion, List<ExposureItem> byCurrency, List<ExposureItem> byAssetClass
) {}
public record ExposureItem(String key, BigDecimal amount, BigDecimal weightPercent) {}

public record DailyNetWorthResponse(
    LocalDate date, BigDecimal totalAssets, BigDecimal totalLiabilities, BigDecimal netWorth
) {}

public record HoldingResponse(
    Long instrumentId, String symbol, String name, String market, String tradingCurrency,
    String exposureRegion, boolean fxHedged, Long accountId, String accountName,
    BigDecimal quantity, BigDecimal avgPriceLocal, BigDecimal closePrice, LocalDate priceDate,
    boolean priceStale, BigDecimal marketValueKrw, BigDecimal costKrw,
    BigDecimal unrealizedProfit, BigDecimal returnRate,
    BigDecimal priceEffect, BigDecimal fxEffect,   // 해외 종목만, 아니면 null
    BigDecimal weightPercent
) {}

public record TransactionRequest(
    @NotNull Long accountId, @NotNull TransactionType type,
    @NotNull @PastOrPresent LocalDate tradeDate, Long instrumentId,
    @Positive BigDecimal quantity, @Positive BigDecimal price,
    @Positive BigDecimal fxRate, @PositiveOrZero BigDecimal amount,   // null이면 자동 계산
    @PositiveOrZero BigDecimal fee, @PositiveOrZero BigDecimal tax,
    UUID transferGroupId, @Size(max = 500) String memo
) {}
// 유형별 필수값과 보유 수량 초과 매도는 서비스에서 검증 → 400 + 필드별 메시지
```

### 6.3 백엔드 패키지 (기존 컨벤션)

```
config/SecurityConfig.java, config/OwnerProperties.java
domain/auth/{controller/AuthController, service/LoginAttemptGuard}
domain/portfolio/
  controller/   PortfolioDashboardController, AccountController, InstrumentController,
                TransactionController, MarketDataController
  entity/       Account, Instrument, Transaction, ManualValuation, Price, FxRate, PortfolioSnapshot (+ enums)
  repository/
  service/      HoldingsCalculator, ReturnCalculator      ← 순수 계산 클래스 (단위 테스트 핵심)
                PortfolioQueryService(Impl), TransactionService(Impl), SnapshotService(Impl)
  client/       KisTokenProvider, KisQuoteClient, KeximFxClient
  scheduler/    PriceCollectScheduler, FxCollectScheduler, SnapshotScheduler
  config/       PortfolioProperties (kis, kexim, snapshot)
```

---

## 7. UI/UX

### 7.1 톤앤매너

- 배경 `surface-950`, 기존 `Card`·`MetricCard`, 포인트 emerald/cyan을 그대로 씁니다.
- 숫자는 `font-mono tabular-nums`로 자릿수를 정렬합니다.
- 자산군 색: 현금성 emerald, 투자 cyan, 연금·보장 violet, 부채 rose.
- 손익 색: **상승 빨강(rose-400) / 하락 파랑(sky-400)**.
- 신규 의존성은 **Recharts 3.x** 하나뿐이고, lazy chunk에만 포함합니다.

### 7.2 레이아웃 (`/portfolio`)

- 요약 상단에 현재 추정 순자산, 총자산·부채, 오늘과 이번 달의 순자산 변화를 표시합니다. 매매·입출금 기록 버튼은 거래 입력 탭을 엽니다.
- 오늘은 오늘 이전의 마지막 스냅샷, 이번 달은 지난달 말까지의 마지막 스냅샷과 비교합니다. 비교 날짜를 표시하고, 기준 스냅샷이 없으면 증감을 비워 둡니다.
- 투자 계좌의 입출금 제외 증감은 `현재 투자 계좌 가치 - 기준 스냅샷 가치 - 구간 순입금`으로 계산합니다. 순입금은 누적 투자금 차이로 계산하여 나중에 입력한 과거 입금도 반영합니다. 투자 계좌끼리의 이체는 순입금에서 제외합니다.
- 보유 종목별 시세 기록일·저장 시각과 적용 환율을 표시합니다. 시세가 없거나 7일보다 오래된 경우, 또는 개장 후 15분이 지난 정규장 중 최근 자동 수집이 15분을 넘은 경우 시세 확인 배지를 표시합니다.
- 요약 화면이 보이는 동안 1분마다 서버의 평가값을 다시 읽고, 숨겨진 탭에서 돌아오면 즉시 갱신합니다. 새 조회가 실패하면 이전 평가액과 마지막으로 갱신한 시각을 함께 표시합니다.

### 7.3 위젯과 화면

| 요소 | 구현 | 데이터 |
|---|---|---|
| 자산 증감 | `MetricCard` 재사용, 순자산과 투자 증감 표시 | `summary` |
| 비중 도넛 | Recharts `PieChart`. 탭으로 자산군/지역/통화/기초자산 전환 | `allocation`, `exposure` |
| 순자산 추이 | Recharts `ComposedChart` (`Line` + `Area`) | `history` |
| 보유 종목 | 테이블, 계좌별 그룹 토글, 시세 오래됨 배지 | `holdings` |
| 거래 입력 탭 | 거래 유형별 단건 입력과 내역 수정·삭제, 초기 일괄 등록용 CSV | `TransactionRequest` |
| 시세 입력 탭 | 등록 종목의 날짜·가격과 환율 입력. 기초잔고 등록 전에도 입력 가능 | `PUT /prices`, `PUT /fx-rates` |
| 계좌·종목 관리 | 간단한 목록 + 폼 모달 | CRUD API |

### 7.4 UX 디테일

- **금액 숨김:** 금액을 `₩•••••`로 마스킹하고 비율만 보여줍니다. 설정은 localStorage에 기억합니다.
- **빈 상태:** 스냅샷이 2개 미만이면 차트 대신 "내일부터 추이가 쌓입니다"를 표시합니다.
- **상세 수익률:** 요약의 접이식 영역에서 XIRR·TWR과 누적 손익을 봅니다. 1년 미만 XIRR은 "기간 수익률"로 표시합니다.
- **반응형:** KPI는 모바일 2×2, lg에서 4열. 차트는 세로 스택에서 2열로. 테이블은 카드 안에서 가로 스크롤.
- **로그인 페이지(`/owner`):** 가운데 `Card` 하나에 아이디/비밀번호와 실패 메시지만 둡니다.

---

## 8. 구현 단계 (승인 후 진행)

| 단계 | 범위 | 검증 |
|---|---|---|
| **1. 인증** | security starter, `SecurityConfig`, `AuthController`, 로그인 잠금, `/owner`, `useOwner`, `RequireOwner`, nav `ownerOnly`, `apiClient` CSRF·헤더 순서 수정, `OWNER_*` 시크릿, 로컬 proxy 전환 | `@WebMvcTest`: 미인증 `/api/portfolio/**` → 401, 기존 공개 API → 200, 로그인 후 → 200, 5회 실패 → 429 |
| **2. 원장** | `V11` (accounts, instruments, transactions, manual_valuations), 엔티티, CRUD API, `HoldingsCalculator` | 이동평균·실현손익·환율 분해·초과매도 거부 단위 테스트 |
| **3. 시세·스냅샷·수익률** | `V12` (prices, fx_rates, portfolio_snapshots), 수동 입력 API, `SnapshotService`(재계산 포함), `ReturnCalculator`, **KIS·KEXIM 클라이언트와 스케줄러 (키 없으면 skip)**, `/integrations` | XIRR(엑셀 XIRR 결과와 비교)·TWR·1년 미만 표기 테스트, 픽스처 기반 클라이언트 파싱 테스트, 키 미설정 시 기동 성공 및 skip 확인 |
| **4. 대시보드 UI** | Recharts, KPI·도넛·추이·탭 테이블·거래 모달·시세 입력, 금액 숨김 | `npm run build`, 브라우저 확인 (모바일 포함) |
| **5. 키 활성화** (계좌 개설 후) | GitHub Secrets에 `KIS_APP_KEY`, `KIS_APP_SECRET`, `KEXIM_API_KEY` 등록 → 재배포 | 종목 1개·통화 1개 스모크 테스트, `/integrations` 상태 확인 |

수출입은행 키는 증권 계좌가 필요 없으므로 KIS보다 먼저 발급해서 5단계를 나눠 진행할 수 있습니다.

### v2 이후

가상자산, 증권사별 CSV 자동 변환, 세후 수익 계산, 위험 지표(변동성·MDD), 벤치마크 비교, 목표 비중과 리밸런싱, 유동 순자산, 외화 예수금, TOTP 2FA, 변경 이력(audit log), Spring Session JDBC.

---

## 9. 소유자가 준비할 것

### 9.1 계정·키 (5단계 전까지만 준비하면 됨)

1. 한국투자증권 비대면 계좌 개설 → KIS Developers에서 Open API 신청 → 실전투자 앱키·시크릿 발급
2. 한국수출입은행 환율 API 인증키 신청 (계좌 불필요)
3. 키는 채팅이나 저장소에 넣지 않고 **GitHub Secrets에 직접 등록**합니다.

### 9.2 기초 데이터 (기준일 1회)

| 대상 | 준비할 값 |
|---|---|
| 계좌 | 기관, 계좌 유형, 별칭, 끝 4자리 |
| 증권 계좌 (일반·ISA·연금저축·IRP) | 기준일 보유 종목, 수량, 평균단가, 예수금. 해외 종목은 매입환율 (모르면 기준일 환율로 대체) |
| 예금·적금·CMA | 원금 또는 잔액, 금리, 가입일, 만기일, 적금 월납입액 |
| 보험 | 납입 누계, 현재 해약환급금 (보험사 앱 / 내보험다보여) |
| 연금 | 납입 누계 (통합연금포털에서 확인 가능) |
| 대출 | 잔액, 금리, 상환방식, 월상환액, 만기일 |

이후에는 매수·매도·분배금·입출금을 거래 입력 폼이나 CSV 양식으로 기록합니다.

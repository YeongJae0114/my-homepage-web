# Velog Blog API Integration

블로그 페이지(`/blog`)는 다음 순서로 데이터를 가져옵니다.

```text
1. POST /velog/graphql
2. src/data/pageFallbacks.ts
```

즉 블로그 글 목록은 Mock API를 거치지 않고 Velog GraphQL에서 직접 가져옵니다. Velog 요청이 실패하면 기존 정적 fallback 데이터로 화면이 유지됩니다.

## Environment Variables

기본값은 이미 `yjl8628`로 설정되어 있습니다.

필요할 때 `frontend/.env.local`에 아래 값을 추가할 수 있습니다.

```bash
VITE_VELOG_USERNAME=yjl8628
VITE_VELOG_POST_LIMIT=10
VITE_VELOG_API_URL=/velog/graphql
```

개발 서버에서는 Vite proxy가 `/velog/graphql` 요청을 `https://v2.velog.io/graphql`로 전달합니다.

## Velog Request

```http
POST /velog/graphql
Content-Type: application/json
Accept: application/json
```

```json
{
  "query": "query Posts($username: String!, $limit: Int, $cursor: ID) { posts(username: $username, limit: $limit, cursor: $cursor) { id title short_description url_slug tags released_at } }",
  "variables": {
    "username": "yjl8628",
    "limit": 10,
    "cursor": null
  }
}
```

다음 페이지 요청 시 `cursor`에는 이전 응답의 마지막 게시글 `id`를 넣습니다.

```json
{
  "query": "query Posts($username: String!, $limit: Int, $cursor: ID) { posts(username: $username, limit: $limit, cursor: $cursor) { id title short_description url_slug tags released_at } }",
  "variables": {
    "username": "yjl8628",
    "limit": 10,
    "cursor": "9c30dcdc-182c-4da4-afdc-3233221c4e8e"
  }
}
```

## Velog Profile Request

```json
{
  "query": "query User($username: String!) { user(username: $username) { id username profile { display_name short_bio thumbnail } velog_config { title logo_image } } }",
  "variables": {
    "username": "yjl8628"
  }
}
```

## Velog Response Shape

```json
{
  "data": {
    "posts": [
      {
        "id": "9c30dcdc-182c-4da4-afdc-3233221c4e8e",
        "title": "[본인인증] CI와 DI 개념 정리 및 차이 이해하기",
        "short_description": "회원가입이나 본인인증 기능을 붙이다 보면...",
        "url_slug": "본인인증-CI와-DI-개념-정리-및-차이-이해하기",
        "tags": ["ci", "di", "본인인증"],
        "released_at": "2026-04-08T00:32:25.668Z"
      }
    ]
  }
}
```

## Frontend Mapping

Velog post는 프론트 내부 `Post` 타입으로 변환됩니다.

```ts
{
  id: post.id,
  title: post.title,
  summary: post.short_description,
  publishedAt: post.released_at.slice(0, 10),
  tags: post.tags,
  url: `https://velog.io/@${username}/${post.url_slug}`
}
```

`short_description`은 공백을 정리한 뒤 200자를 초과하면 `...`을 붙여 표시합니다.

관련 파일:

```text
src/types/velog.ts
src/adapters/velogAdapter.ts
src/services/velogApi.ts
src/services/pageApi.ts
```

## Spring Boot Migration Note

브라우저에서 Velog GraphQL을 직접 호출할 때 CORS 정책에 막힐 수 있습니다. 현재 개발 환경은 Vite proxy(`/velog/graphql`)를 사용합니다. 실제 운영에서는 Spring Boot backend에서 Velog GraphQL을 proxy 또는 scheduled sync 방식으로 가져오고, 프론트에는 별도 블로그 endpoint로 내려주는 구성이 더 안정적입니다.

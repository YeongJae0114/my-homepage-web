# Velog Blog API Integration

블로그 페이지(`/blog`)는 다음 순서로 데이터를 가져옵니다.

```text
1. GET /api/blog
2. POST https://v2.velog.io/graphql
3. src/data/pageFallbacks.ts
```

즉 Mock 서버가 아직 없거나 `/api/blog`가 실패하면 Velog GraphQL에서 글 목록을 가져옵니다. Velog 요청도 실패하면 기존 정적 fallback 데이터로 화면이 유지됩니다.

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
  "query": "query Posts($username: String!, $limit: Int) { posts(username: $username, limit: $limit) { id title short_description url_slug tags released_at } }",
  "variables": {
    "username": "yjl8628",
    "limit": 10
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

관련 파일:

```text
src/types/velog.ts
src/adapters/velogAdapter.ts
src/services/velogApi.ts
src/services/pageApi.ts
```

## Spring Boot Migration Note

브라우저에서 Velog GraphQL을 직접 호출할 때 CORS 정책에 막힐 수 있습니다. 실제 운영에서는 Spring Boot backend에서 Velog GraphQL을 proxy 또는 scheduled sync 방식으로 가져오고, 프론트에는 기존 `/api/blog` 응답 형식으로 내려주는 구성이 더 안정적입니다.

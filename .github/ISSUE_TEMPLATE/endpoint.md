---
name: API Endpoint 요청
about: 새로운 API Endpoint 요청 또는 수정사항을 제안합니다.
title: "[API]: "
labels: api, backend
assignees: ''
---

# [이슈의 핵심 문제를 간단명료하게 제목으로 작성하세요.]

- [이슈에 대해 프론트엔드 개발자 및 신입 개발자가 이해할 수 있도록 간략하게 설명합니다. 프롬프트 실행 시 이 힌트는 삭제됩니다.]

## API 정의

### 인터페이스

- **Method:** [필요한 HTTP 메서드를 입력하세요: GET, POST, PUT, PATCH, DELETE 중 선택]
- **URL:** `[엔드포인트 URL을 정확히 입력하세요. 예시: /assets/{param1}/images/{param2}?page={page}&size={size}]`
- **Content-Type:** `application/json`

### Path Parameters (모든 Path Parameter를 필수로 명시하세요.)

| 이름 | 타입 | 필수 여부 | 설명                          |
|------|------|----------|-------------------------------|
| `[param 이름]` | `[데이터 타입]` | ✅ 필수 | `[파라미터의 설명을 명확히 입력하세요.]` |

### Query Parameters (필요 없는 경우 표만 유지하고 내용을 지우세요.)

| 이름   | 타입   | 필수 여부 | 기본값 | 설명                               |
|--------|--------|----------|--------|------------------------------------|
| `[파라미터 이름]` | `[타입]` | `[✅필수/❌선택]` | `[기본값]` | `[파라미터 설명을 입력하세요.]` |

### Body Parameters (Body가 필요 없으면 표만 유지하고 내용을 지우세요.)

| 이름 | 타입 | 필수 여부 | 설명                             |
|------|------|----------|----------------------------------|
| `[필드 이름]` | `[타입]` | `[✅필수/❌선택]` | `[필드 설명을 입력하세요.]` |

## 요청 예시

```http
[Method] [URL] HTTP/2.0
Host: api.example.com
Content-Type: application/json

{
  "[필드 이름]": "[샘플 값]"
}
```

## 응답 예시

### 성공 응답 - 200 OK

```http
HTTP/2.0 200 OK
Content-Type: application/json

{
  "[필드 이름]": "[성공 응답 예시 값]"
}
```

### 오류 응답 예시 (실제 작성 시 관련 없는 오류 응답 예시는 지우세요.)

- 400 Bad Request: 요청값 유효성 문제 발생
```http
HTTP/2.0 400 Bad Request
Content-Type: application/json

{
  "[필드 이름]": "[필수 필드 누락 등의 오류 메시지를 입력하세요.]"
}
```

- 404 Not Found: 리소스 없음
```http
HTTP/2.0 404 Not Found
Content-Type: application/json

{
  "code": "RESOURCE_NOT_FOUND",
  "message": "[리소스가 없는 경우의 메시지를 입력하세요.]"
}
```

- 500 Internal Server Error: 서버 오류
```http
HTTP/2.0 500 Internal Server Error
Content-Type: application/json

{
  "code": "INTERNAL_SERVER_ERROR",
  "message": "[서버 오류 메시지를 입력하세요.]"
}
```

## 구현 체크리스트 (필요한 항목만 남기고 나머지는 삭제하세요.)

- [ ] `[HTTP 메서드와 엔드포인트]` 구현
- [ ] 요청 파라미터 및 Body 검증
- [ ] 성공 시 200 OK 반환
- [ ] 리소스가 없으면 404 반환
- [ ] 유효하지 않은 요청 시 400 반환
- [ ] 서버 오류 처리 시 500 반환

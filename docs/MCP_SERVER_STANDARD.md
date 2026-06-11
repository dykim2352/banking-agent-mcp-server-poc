# MCP Server 표준 설계

## 목적

은행 Agent가 내부 시스템을 호출할 때 필요한 MCP Tool, Resource, Prompt의 정의 방식을 표준화한다.

이 문서는 개별 Tool 구현보다 상위의 공통 기준을 정리한다. 목표는 다음과 같다.

- Tool/Resource/Prompt 메타데이터 구조 통일
- 권한, 감사 로그, 에러 코드 정책 통일
- 동기 처리와 비동기 Job 처리 기준 분리
- 레거시 연동 실패를 MCP 응답으로 일관되게 변환

## 설계 범위

| 범위 | 설명 |
|---|---|
| Tool 표준 | Agent가 실행할 수 있는 기능의 이름, 입력값, 권한, 감사 정책 |
| Resource 표준 | Agent가 참조할 수 있는 스키마, 정책 문서, 운영 정보 |
| Prompt 표준 | 반복 사용할 수 있는 업무 프롬프트의 이름, 인자, 템플릿 |
| 접근 제어 | 역할과 세부 권한 기반의 Tool/Resource 접근 정책 |
| 감사 로그 | 성공, 실패, 권한 거부 요청의 공통 기록 기준 |
| 에러 코드 | MCP 응답에 포함할 도메인 오류 코드 기준 |

## 구성요소 표준

### Tools

Agent가 실행할 수 있는 기능이다.

표준 필드:

| Field | Required | 설명 |
|---|---|---|
| `name` | Y | MCP client가 `tools/call`에서 사용하는 고유 Tool 이름. 예: `account_summary` |
| `description` | Y | Agent 또는 사용자가 Tool의 목적을 이해할 수 있는 짧은 설명 |
| `inputSchema` | Y | Tool 호출에 필요한 입력값 JSON schema. 필수 파라미터와 타입을 정의한다. |
| `requiredRole` | Y | Tool 호출에 필요한 최소 역할. 예: `USER`, `ADVISOR`, `ADMIN` |
| `requiredPermission` | Y | 역할보다 세밀한 접근 제어를 위한 권한 코드. 예: `TOOL_ACCOUNT_SUMMARY_CALL` |
| `auditPolicy` | Y | 호출 성공, 실패, 권한 거부를 감사 로그에 남길지에 대한 정책 |
| `executionType` | Y | `SYNC` 또는 `ASYNC`. 즉시 응답 Tool인지 비동기 Job 접수 Tool인지 구분한다. |
| `timeoutMillis` | N | 외부 연동 또는 Tool 처리 제한 시간. PoC에서는 명시 정책 중심으로 둔다. |
| `errorCodeMapping` | Y | 예외를 MCP/도메인 에러 코드로 변환하는 기준 |

처리 기준:

- 조회성 Tool은 동기 응답을 기본으로 한다.
- 오래 걸리거나 재시도가 필요한 작업은 비동기 Job으로 접수한다.
- 모든 Tool 호출은 감사 로그 대상이다.
- 실제 고객명, 실제 계좌번호, 실제 전문 포맷은 사용하지 않는다.

### Resources

Agent가 참조할 수 있는 데이터 또는 스키마이다.

표준 필드:

| Field | Required | 설명 |
|---|---|---|
| `uri` | Y | Resource를 식별하는 고유 URI. 예: `banking://schemas/account` |
| `name` | Y | MCP Inspector 또는 client 화면에 표시되는 Resource 이름 |
| `description` | Y | Resource가 제공하는 정보의 목적과 사용 범위 |
| `mimeType` | Y | Resource 내용 형식. 예: `application/json`, `text/markdown` |
| `requiredRole` | Y | Resource read에 필요한 최소 역할 |
| `requiredPermission` | Y | Resource별 세부 읽기 권한 |
| `cachePolicy` | N | 정적 스키마, 정책 문서처럼 캐시 가능한지에 대한 기준 |

### Prompts

반복 사용 가능한 업무 프롬프트이다.

표준 필드:

| Field | Required | 설명 |
|---|---|---|
| `name` | Y | MCP client가 `prompts/get`에서 사용하는 고유 Prompt 이름 |
| `description` | Y | Prompt가 어떤 업무 상황에 쓰이는지 설명 |
| `arguments` | N | Prompt 템플릿에 주입할 입력값 목록과 타입 |
| `template` | Y | Agent에게 전달할 재사용 가능한 업무 지시문 |
| `domain` | N | 상담, 상품 추천, 거래 검토처럼 Prompt가 속한 업무 영역 |

## 접근 제어 표준

| 항목 | 기준 |
|---|---|
| 인증 | Mock API key 또는 Bearer token으로 사용자와 역할을 식별한다. |
| 역할 | `USER`, `ADVISOR`, `ADMIN` |
| 권한 | Tool/Resource별 세부 permission으로 확장 가능하게 구성한다. |
| 거부 응답 | 권한이 없으면 `ACCESS_DENIED`, 인증 정보가 없으면 `UNAUTHENTICATED`로 구분한다. |

## 처리 방식 표준

| 방식 | 기준 | 예시 |
|---|---|---|
| 동기 처리 | 즉시 응답 가능한 조회/추천성 Tool | `account_summary`, `card_transaction_search` |
| 비동기 처리 | Job 상태 추적이 필요한 작업 | `customer_ticket_create_async` |
| 상태 조회 | 비동기 Job의 상태와 결과 조회 | `async_job_status` |

비동기 Tool은 요청 접수 시 `agent_jobs`에 `PENDING` 상태를 저장하고, Worker 처리 결과에 따라 `COMPLETED` 또는 `FAILED`로 변경한다.

## 표준 에러코드

| Code | 의미 |
|---|---|
| TOOL_NOT_FOUND | 알 수 없는 tool |
| RESOURCE_NOT_FOUND | 알 수 없는 resource |
| VALIDATION_ERROR | 입력값 오류 |
| LEGACY_TIMEOUT | 레거시 시스템 타임아웃 |
| LEGACY_NOT_FOUND | 레거시 데이터 없음 |
| ACCESS_DENIED | 권한 없음 |

## 감사 로그 표준

| Field | Required | 설명 |
|---|---|---|
| `correlationId` | Y | 하나의 MCP 요청과 내부 처리, Worker 처리까지 연결하는 추적 ID |
| `userId` | Y | mock 인증에서 식별한 사용자 ID. 인증 실패 시 `anonymous` |
| `actionType` | Y | 감사 대상 유형. 예: `TOOL`, `RESOURCE` |
| `target` | Y | Tool 이름 또는 Resource URI |
| `requestSummary` | N | 민감정보를 제외한 요청 요약 |
| `status` | Y | `SUCCESS`, `FAILED`, `DENIED` |
| `elapsedMillis` | N | 처리 소요 시간 |
| `errorCode` | N | 실패 또는 권한 거부 시 도메인 에러 코드 |
| `createdAt` | Y | 감사 로그 생성 시각 |

# Kafka Topic 설계

## 목적

MCP 서버와 Worker 서비스 사이의 비동기 Job 메시지 구조를 표준화한다.

목표:

- 비동기 Job 요청 topic 정의
- event payload 필드 표준화
- Job 상태 추적 기준 정의
- 업무 실패와 메시지 소비 실패 분리

## 설계 범위

| 범위 | 설명 |
|---|---|
| Topic | 비동기 Job 요청을 전달하는 Kafka topic |
| Event | Worker가 Job을 처리하기 위해 필요한 payload |
| Job 상태 | `agent_jobs` 테이블의 상태 전이 기준 |
| Dead Letter | 정상 처리할 수 없는 메시지 저장 기준 |

## Topic 표준

| Topic | Producer | Consumer | 용도 |
|---|---|---|---|
| `agent.job.requested` | `mcp-server` | `worker-service` | 비동기 Tool 요청을 Worker Job으로 전달 |

## Event 표준

| Field | Type | Required | 설명 |
|---|---|---|---|
| `jobId` | string UUID | Y | `agent_jobs.job_id`와 연결되는 비동기 작업 ID |
| `correlationId` | string | N | MCP 요청과 Worker 처리를 연결하는 추적 ID |
| `toolName` | string | Y | Worker가 실행할 Tool 이름 |
| `arguments` | object | Y | Tool 실행 인자 |
| `requestedAt` | ISO-8601 instant | Y | Job 요청 event 생성 시각 |

예시:

```json
{
  "jobId": "mock-generated-job-id",
  "correlationId": "mock-correlation-id",
  "toolName": "customer_ticket_create",
  "arguments": {
    "customerId": "CUST-1001",
    "title": "Mock title",
    "description": "Mock description"
  },
  "requestedAt": "2026-06-10T13:00:00Z"
}
```

## Job 상태 표준

| 상태 | 변경 주체 | 설명 |
|---|---|---|
| `PENDING` | `mcp-server` | 비동기 Tool 요청 접수 |
| `RUNNING` | `worker-service` | Worker가 메시지를 소비하고 처리 시작 |
| `COMPLETED` | `worker-service` | Worker 처리 성공 |
| `FAILED` | `worker-service` | Worker 처리 중 업무 실패 |

## 실패 처리 표준

| 실패 유형 | 저장 위치 | 설명 |
|---|---|---|
| 업무 처리 실패 | `agent_jobs.status = FAILED` | 유효한 Job을 처리했지만 레거시 호출 또는 업무 로직이 실패한 경우 |
| 메시지 소비 실패 | `kafka_dead_letters` | payload 파싱 실패, 알 수 없는 `jobId`, 반복 소비 실패 |

## Dead Letter 표준

| Field | 설명 |
|---|---|
| `topic` | 실패한 원본 topic |
| `messageKey` | 실패한 message key |
| `payload` | 실패한 원본 payload |
| `errorMessage` | 실패 사유 |
| `retryCount` | 소비 재시도 횟수 |
| `createdAt` | Dead Letter 저장 시각 |

## 확장 후보

| 항목 | 설명 |
|---|---|
| DLT topic | `agent.job.requested.dlt` 같은 topic 기반 Dead Letter |
| Backoff | fixed 또는 exponential backoff |
| ErrorHandler | Spring Kafka `DefaultErrorHandler` 또는 `@RetryableTopic` |
| Alerting | Dead Letter 적재 또는 연속 실패 시 운영 알림 |
| Producer callback | Kafka `send()` 비동기 실패 처리 |
| Serialization metric | 직렬화 실패 metric 또는 fallback 처리 |

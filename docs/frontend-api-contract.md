# TechMatch Frontend API Contract

## Base Rules

- Base URL: `/api`
- Content-Type: `application/json`
- Auth header: `Authorization: Bearer <accessToken>`
- Request trace header: `X-Request-Id` is returned by backend on every response
- Success response shape:

```json
{
  "success": true,
  "code": 0,
  "message": "success",
  "data": {},
  "timestamp": 1746950400000,
  "requestId": "7e3f2dbe5f774c7a9e8601a35c5be6e1"
}
```

- Error response shape:

```json
{
  "success": false,
  "code": 401,
  "message": "unauthorized",
  "data": null,
  "timestamp": 1746950400000,
  "requestId": "7e3f2dbe5f774c7a9e8601a35c5be6e1"
}
```

## Auth APIs

### `POST /api/auth/register`

Request:

```json
{
  "username": "alice",
  "password": "123456",
  "nickname": "Alice"
}
```

Response `data`:

```json
{
  "accessToken": "jwt-token",
  "tokenType": "Bearer",
  "expiresIn": 86400,
  "user": {
    "userId": 1,
    "username": "alice",
    "nickname": "Alice"
  }
}
```

### `POST /api/auth/login`

Request:

```json
{
  "username": "alice",
  "password": "123456"
}
```

Response `data`: same as register

### `GET /api/auth/me`

Headers:

```http
Authorization: Bearer <accessToken>
```

Response `data`:

```json
{
  "userId": 1,
  "username": "alice",
  "nickname": "Alice"
}
```

## System APIs

### `GET /api/system/ping`

Frontend can use this for environment connectivity checks before login.

### `GET /api/system/infra`

Only for backend/dev diagnostics. Frontend production pages usually do not need it.

## Resume APIs

### `POST /api/resumes/upload`

Headers:

```http
Authorization: Bearer <accessToken>
Content-Type: multipart/form-data
```

Form fields:

- `file`: PDF or DOCX resume file, max 5 MB

Response `data`:

```json
{
  "resumeId": 101,
  "fileName": "alice-resume.pdf",
  "status": "COMPLETED",
  "chunkCount": 6
}
```

### `GET /api/resumes`

Headers:

```http
Authorization: Bearer <accessToken>
```

Response `data`:

```json
[
  {
    "resumeId": 101,
    "fileName": "alice-resume.pdf",
    "fileType": "pdf",
    "fileSize": 182736,
    "status": "COMPLETED",
    "createTime": "2026-05-13T16:30:00"
  }
]
```

### `GET /api/resumes/{resumeId}`

Headers:

```http
Authorization: Bearer <accessToken>
```

Response `data`:

```json
{
  "resumeId": 101,
  "fileName": "alice-resume.pdf",
  "fileType": "pdf",
  "fileSize": 182736,
  "status": "COMPLETED",
  "storageUrl": "http://localhost:9000/techmatch/resumes/1/uuid.pdf",
  "rawText": "Alice ...",
  "structured": {
    "basics": {
      "name": "Alice",
      "email": "alice@example.com",
      "phone": "+86 13800000000",
      "summary": "..."
    },
    "skills": ["java", "spring boot", "postgresql"],
    "education": [],
    "workExperiences": [],
    "projects": [],
    "highlights": ["Built matching service"]
  },
  "parseError": null,
  "createTime": "2026-05-13T16:30:00",
  "chunks": [
    {
      "chunkIndex": 0,
      "chunkText": "Alice ...",
      "metadataJson": "{\"startOffset\":0,\"endOffset\":800,\"length\":768}",
      "embedding": [0.21, -0.08, 0.43],
      "embeddingDimensions": 3
    }
  ]
}
```

### `GET /api/resumes/search/chunks`

Headers:

```http
Authorization: Bearer <accessToken>
```

Query params:

- `query`: required, natural language retrieval query
- `topK`: optional, default `5`, max `20`

Response `data`:

```json
[
  {
    "chunkId": 9001,
    "resumeId": 101,
    "fileName": "alice-resume.pdf",
    "chunkIndex": 2,
    "chunkText": "Built Spring Boot matching service ...",
    "metadataJson": "{\"startOffset\":820,\"endOffset\":1600,\"length\":745}",
    "distance": 0.1234,
    "similarity": 0.8766
  }
]
```

## Job APIs

### `POST /api/jobs`

Headers:

```http
Authorization: Bearer <accessToken>
Content-Type: application/json
```

Request:

```json
{
  "title": "高级后端工程师",
  "company": "Nebula Systems",
  "location": "上海 / 混合办公",
  "rawText": "我们正在招聘..."
}
```

Response `data`:

```json
{
  "jobId": 201,
  "title": "高级后端工程师",
  "status": "COMPLETED",
  "chunkCount": 4
}
```

### `GET /api/jobs`

Headers:

```http
Authorization: Bearer <accessToken>
```

Response `data`:

```json
[
  {
    "jobId": 201,
    "title": "高级后端工程师",
    "company": "Nebula Systems",
    "location": "上海 / 混合办公",
    "status": "COMPLETED",
    "createTime": "2026-05-13T20:15:00"
  }
]
```

### `GET /api/jobs/{jobId}`

Headers:

```http
Authorization: Bearer <accessToken>
```

Response `data`:

```json
{
  "jobId": 201,
  "title": "高级后端工程师",
  "company": "Nebula Systems",
  "location": "上海 / 混合办公",
  "status": "COMPLETED",
  "rawText": "我们正在招聘...",
  "structured": {
    "title": "高级后端工程师",
    "company": "Nebula Systems",
    "location": "上海 / 混合办公",
    "summary": "我们正在招聘...",
    "requiredSkills": ["java", "spring boot", "redis"],
    "preferredSkills": ["docker", "kubernetes"],
    "responsibilities": ["负责招聘智能工作流相关后端服务建设"],
    "requirements": ["5 年以上后端工程经验"],
    "businessKeywords": ["招聘", "saas"]
  },
  "parseError": null,
  "createTime": "2026-05-13T20:15:00",
  "chunks": [
    {
      "chunkId": 7001,
      "chunkIndex": 0,
      "chunkText": "我们正在招聘...",
      "metadataJson": "{\"startOffset\":0,\"endOffset\":800,\"length\":312}",
      "embedding": [0.18, -0.11, 0.27],
      "embeddingDimensions": 3
    }
  ]
}
```

## Match APIs

### `POST /api/matches`

Headers:

```http
Authorization: Bearer <accessToken>
Content-Type: application/json
```

Request:

```json
{
  "resumeId": 101,
  "jobDescriptionId": 201
}
```

Response `data`:

```json
{
  "taskId": 301,
  "resumeId": 101,
  "jobDescriptionId": 201,
  "taskType": "MATCH_ANALYSIS",
  "status": "PENDING",
  "currentNode": "QUEUED",
  "createTime": "2026-05-14T10:30:00"
}
```

Notes:

- Backend will automatically start the agent workflow after the task is created.
- The returned `status` may already be `RUNNING`, `COMPLETED`, or `FAILED`, not only `PENDING`.

### `GET /api/matches/{taskId}`

Headers:

```http
Authorization: Bearer <accessToken>
```

Response `data`:

```json
{
  "taskId": 301,
  "resumeId": 101,
  "jobDescriptionId": 201,
  "taskType": "MATCH_ANALYSIS",
  "status": "RUNNING",
  "currentNode": "EvidenceVerificationNode",
  "errorMessage": null,
  "startedAt": "2026-05-14T10:30:02",
  "finishedAt": null,
  "createTime": "2026-05-14T10:30:00"
}
```

### `GET /api/matches/{taskId}/timeline`

Headers:

```http
Authorization: Bearer <accessToken>
```

Response `data`:

```json
[
  {
    "eventId": 5001,
    "nodeName": "MatchTaskCreate",
    "status": "CREATED",
    "inputSummary": null,
    "outputSummary": "Match task created for resumeId=101 and jobDescriptionId=201",
    "modelName": null,
    "promptTokens": null,
    "completionTokens": null,
    "durationMs": null,
    "retryCount": 0,
    "errorMessage": null,
    "metadataJson": null,
    "createTime": "2026-05-14T10:30:00"
  },
  {
    "eventId": 5002,
    "nodeName": "RetrievalNode",
    "status": "SUCCESS",
    "inputSummary": "resume chunks + job chunks",
    "outputSummary": "TopK evidence chunks selected",
    "modelName": "text-embedding-3-small",
    "promptTokens": 0,
    "completionTokens": 0,
    "durationMs": 148,
    "retryCount": 0,
    "errorMessage": null,
    "metadataJson": "{\"topK\":5}",
    "createTime": "2026-05-14T10:30:03"
  }
]
```

Status conventions:

- Task status: `PENDING`, `RUNNING`, `COMPLETED`, `FAILED`
- Timeline status: `CREATED`, `SUCCESS`, `FAILED`

### `GET /api/matches/{taskId}/report`

Headers:

```http
Authorization: Bearer <accessToken>
```

Response `data`:

```json
{
  "taskId": 301,
  "resumeId": 101,
  "jobDescriptionId": 201,
  "candidateName": "Alice",
  "jobTitle": "高级后端工程师",
  "totalScore": 78.5,
  "confidence": 0.74,
  "matchLevel": "中等匹配",
  "recommendation": "建议补充核验后进入面试",
  "summary": "综合得分 78.5，当前判断为中等匹配",
  "dimensionScores": [
    {
      "dimension": "TECH_SKILL",
      "score": 28,
      "maxScore": 35,
      "confidence": 0.82,
      "reason": "必备技能命中 4/5，其中有证据支撑 3 项"
    }
  ],
  "strengths": ["候选人具备 spring boot"],
  "risks": ["岗位要求“Kubernetes”缺少可靠简历证据，需转为面试验证问题"],
  "suggestions": ["优先围绕缺失技能和薄弱项目证据设计面试追问"],
  "interviewQuestions": [
    {
      "type": "风险验证题",
      "question": "请结合具体项目说明：岗位要求“Kubernetes”缺少可靠简历证据，需转为面试验证问题",
      "target": "Kubernetes",
      "difficulty": "中等"
    }
  ]
}
```

### `GET /api/matches/{taskId}/questions`

Headers:

```http
Authorization: Bearer <accessToken>
```

Response `data`:

```json
[
  {
    "questionId": 501,
    "type": "TECH_BASIC",
    "question": "请结合你最近一次使用 Redis 的经历，说明核心原理、排障思路，以及你如何把它用在实际项目里。",
    "target": "Redis",
    "difficulty": "中等",
    "sourceRisk": "技术栈核验",
    "feedback": {
      "feedbackId": 801,
      "questionId": 501,
      "score": 4,
      "feedbackType": "证据充分",
      "notes": "能够解释缓存一致性，但对哨兵切换细节还可以继续追问。",
      "confidenceDelta": 0.075,
      "createTime": "2026-05-15T10:20:00",
      "updateTime": "2026-05-15T10:20:00"
    }
  }
]
```

### `POST /api/matches/{taskId}/feedback`

Headers:

```http
Authorization: Bearer <accessToken>
Content-Type: application/json
```

Request:

```json
{
  "questionId": 501,
  "score": 4,
  "feedbackType": "证据充分",
  "notes": "能够解释缓存一致性，但对哨兵切换细节还可以继续追问。"
}
```

Response `data`:

```json
{
  "taskId": 301,
  "questionId": 501,
  "feedbackId": 801,
  "confidence": 0.8125,
  "matchLevel": "强匹配",
  "recommendation": "建议优先推进后续面试",
  "feedbackSummary": {
    "feedbackCount": 3,
    "averageScore": 4.33,
    "confidenceDelta": 0.0525,
    "adjustedMatchLevel": "强匹配",
    "adjustedRecommendation": "建议优先推进后续面试",
    "note": "面试反馈整体积极，提升了候选人与岗位匹配结论的可信度"
  }
}
```

### `GET /api/matches/{taskId}/graph`

Headers:

```http
Authorization: Bearer <accessToken>
```

Response `data`:

```json
{
  "taskId": 301,
  "nodes": [
    {
      "nodeId": 1,
      "nodeType": "JD_REQUIREMENT",
      "nodeKey": "jd:kubernetes",
      "title": "kubernetes",
      "content": "kubernetes",
      "status": null,
      "metadataJson": null
    }
  ],
  "edges": [
    {
      "edgeId": 11,
      "fromNodeId": 1,
      "toNodeId": 3,
      "edgeType": "MISSING_EVIDENCE",
      "metadataJson": "{\"reason\":\"未找到绑定到简历的有效证据\"}"
    }
  ],
  "chains": [
    {
      "requirement": "kubernetes",
      "evidence": null,
      "status": "缺失证据",
      "risk": "岗位要求“kubernetes”缺少可靠简历证据，需转为面试验证问题",
      "question": "请结合具体项目说明：岗位要求“kubernetes”缺少可靠简历证据，需转为面试验证问题"
    }
  ]
}
```

## Frontend Integration Suggestions

- Store `accessToken` after login and attach it through an HTTP interceptor.
- When backend returns `401`, clear local token and redirect to login page.
- Use `requestId` when reporting backend errors for easier log tracing.
- Swagger UI is available at `/swagger-ui.html`.

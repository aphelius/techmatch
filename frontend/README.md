# TechMatch 前端

## 启动

```bash
npm install
npm run dev
```

默认开发地址：`http://localhost:5173`

## 后端代理

Vite 会把 `/api/*` 代理到：

```text
http://localhost:8080
```

所以请先启动 Spring Boot 后端，再打开 Vite 页面。

## 当前已联调页面

- `登录页`：真实对接 `/api/auth/*`
- `仪表盘`：真实对接 `/api/resumes` 和 `/api/system/infra`
- `简历页`：真实对接上传、列表、详情与 chunk 检索

## 当前使用 Mock 的页面

这些页面已经按 Stitch 设计实现，但在对应后端阶段完成前，先使用本地 mock 数据展示：

- `岗位 JD`
- `匹配分析`
- `报告`
- `证据图谱`
- `Agent 时间线`
- `面试反馈`

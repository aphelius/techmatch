export function formatBytes(bytes: number) {
  if (!Number.isFinite(bytes)) {
    return "-";
  }
  if (bytes < 1024) {
    return `${bytes} B`;
  }
  const units = ["KB", "MB", "GB"];
  let value = bytes / 1024;
  let index = 0;
  while (value >= 1024 && index < units.length - 1) {
    value /= 1024;
    index += 1;
  }
  return `${value.toFixed(1)} ${units[index]}`;
}

export function formatDate(value?: string) {
  if (!value) {
    return "-";
  }
  const date = new Date(value);
  if (Number.isNaN(date.getTime())) {
    return value;
  }
  return new Intl.DateTimeFormat("zh-CN", {
    year: "numeric",
    month: "short",
    day: "numeric",
    hour: "2-digit",
    minute: "2-digit"
  }).format(date);
}

export function titleFromPath(pathname: string) {
  if (pathname === "/") return "仪表盘";
  if (pathname.startsWith("/resumes")) return "简历管理";
  if (pathname.startsWith("/jobs")) return "岗位描述";
  if (pathname.startsWith("/analysis")) return "创建匹配分析";
  if (pathname.startsWith("/reports")) return "匹配报告";
  if (pathname.startsWith("/evidence-graph")) return "证据图谱";
  if (pathname.startsWith("/timeline")) return "Agent 时间线";
  if (pathname.startsWith("/feedback")) return "面试反馈";
  return "TechMatch";
}

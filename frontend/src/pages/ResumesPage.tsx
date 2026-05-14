import { useEffect, useMemo, useState } from "react";
import { api } from "../services/api";
import type { ResumeDetail, ResumeListItem, SimilarChunk } from "../types/api";
import { formatBytes, formatDate } from "../utils/format";
import { Badge, EmptyState, SectionCard } from "../components/Ui";

export function ResumesPage() {
  const [resumes, setResumes] = useState<ResumeListItem[]>([]);
  const [selectedResumeId, setSelectedResumeId] = useState<number | null>(null);
  const [selectedResume, setSelectedResume] = useState<ResumeDetail | null>(null);
  const [query, setQuery] = useState("Spring Boot Redis 高并发");
  const [results, setResults] = useState<SimilarChunk[]>([]);
  const [uploading, setUploading] = useState(false);
  const [searching, setSearching] = useState(false);
  const [loadingList, setLoadingList] = useState(true);
  const [loadingDetail, setLoadingDetail] = useState(false);
  const [error, setError] = useState("");

  async function loadResumes() {
    setLoadingList(true);
    try {
      const data = await api.listResumes();
      setResumes(data);
      if (!selectedResumeId && data[0]) {
        setSelectedResumeId(data[0].resumeId);
      }
    } catch (exception) {
      setError(exception instanceof Error ? exception.message : "加载简历列表失败");
    } finally {
      setLoadingList(false);
    }
  }

  useEffect(() => {
    void loadResumes();
  }, []);

  useEffect(() => {
    if (!selectedResumeId) {
      setSelectedResume(null);
      return;
    }
    setLoadingDetail(true);
    api
      .getResume(selectedResumeId)
      .then(setSelectedResume)
      .catch((exception) => {
        setError(exception instanceof Error ? exception.message : "加载简历详情失败");
      })
      .finally(() => setLoadingDetail(false));
  }, [selectedResumeId]);

  const selectedSummary = useMemo(() => selectedResume?.structured?.basics?.summary || "暂未提取到简历摘要。", [
    selectedResume
  ]);

  async function handleUpload(fileList: FileList | null) {
    const file = fileList?.[0];
    if (!file) return;

    setUploading(true);
    setError("");
    try {
      const uploaded = await api.uploadResume(file);
      await loadResumes();
      setSelectedResumeId(uploaded.resumeId);
    } catch (exception) {
      setError(exception instanceof Error ? exception.message : "上传失败");
    } finally {
      setUploading(false);
    }
  }

  async function handleSearch() {
    setSearching(true);
    setError("");
    try {
      const data = await api.searchChunks(query, 6);
      setResults(data);
    } catch (exception) {
      setError(exception instanceof Error ? exception.message : "检索失败");
    } finally {
      setSearching(false);
    }
  }

  return (
    <div className="stack-lg">
      {error ? <div className="form-error">{error}</div> : null}

      <div className="content-grid two-one">
        <SectionCard title="上传简历" subtitle="直接联调后端上传与解析链路">
          <label className="upload-dropzone">
            <span className="material-symbols-outlined">upload_file</span>
            <strong>{uploading ? "正在上传并解析..." : "将简历拖到这里，或点击浏览文件"}</strong>
            <span>支持 PDF / DOCX，后端当前限制单文件 5 MB</span>
            <input
              type="file"
              accept=".pdf,.docx"
              onChange={(event) => void handleUpload(event.target.files)}
              hidden
            />
            <span className="primary-button">浏览文件</span>
          </label>
        </SectionCard>

        <SectionCard title="语义检索" subtitle="基于 pgvector 余弦相似度检索简历 chunk">
          <div className="form-grid compact">
            <label>
              <span>检索语句</span>
              <input value={query} onChange={(event) => setQuery(event.target.value)} />
            </label>
            <button className="primary-button" disabled={searching} onClick={() => void handleSearch()}>
              {searching ? "检索中..." : "开始检索"}
            </button>
          </div>
          <div className="stack-sm">
            {results.length === 0 ? (
              <EmptyState title="还没有检索结果" description="试试输入 `Redis 一致性` 或 `Spring Boot` 这样的查询。" />
            ) : (
              results.map((item) => (
                <div className="result-card" key={item.chunkId}>
                  <div className="result-topline">
                    <strong>{item.fileName}</strong>
                    <Badge tone="info">相似度 {(item.similarity * 100).toFixed(1)}%</Badge>
                  </div>
                  <p>{item.chunkText}</p>
                  <div className="table-meta">Chunk #{item.chunkIndex} · 距离 {item.distance.toFixed(4)}</div>
                </div>
              ))
            )}
          </div>
        </SectionCard>
      </div>

      <div className="content-grid two-one">
        <SectionCard
          title="简历库"
          subtitle="已上传的候选人文件与解析状态"
          action={loadingList ? <Badge tone="info">刷新中</Badge> : undefined}
        >
          {resumes.length === 0 ? (
            <EmptyState title="还没有上传简历" description="先上传一份简历，就能看到结构化解析结果。" />
          ) : (
            <div className="table-list">
              {resumes.map((resume) => (
                <button
                  className={`table-row selectable ${selectedResumeId === resume.resumeId ? "selected" : ""}`}
                  key={resume.resumeId}
                  onClick={() => setSelectedResumeId(resume.resumeId)}
                >
                  <div>
                    <div className="table-title">{resume.fileName}</div>
                    <div className="table-meta">
                      {resume.fileType.toUpperCase()} · {formatBytes(resume.fileSize)} · {formatDate(resume.createTime)}
                    </div>
                  </div>
                  <Badge tone={resume.status === "COMPLETED" ? "success" : "warning"}>{resume.status}</Badge>
                </button>
              ))}
            </div>
          )}
        </SectionCard>

        <SectionCard
          title="解析详情"
          subtitle="结构化抽取、原始文本预览与 chunk 诊断"
          action={loadingDetail ? <Badge tone="info">加载中</Badge> : undefined}
        >
          {!selectedResume ? (
            <EmptyState title="请选择一份简历" description="从左侧列表中选择简历，查看解析结果。" />
          ) : (
            <div className="stack-md">
              <div className="detail-hero">
                <div>
                  <h4>{selectedResume.structured.basics.name || selectedResume.fileName}</h4>
                  <p>{selectedSummary}</p>
                </div>
                <Badge tone={selectedResume.status === "COMPLETED" ? "success" : "warning"}>{selectedResume.status}</Badge>
              </div>

              <div className="tag-list">
                {selectedResume.structured.skills.length > 0 ? (
                  selectedResume.structured.skills.map((skill) => (
                    <span className="tag" key={skill}>
                      {skill}
                    </span>
                  ))
                ) : (
                  <span className="table-meta">暂未提取到技能标签。</span>
                )}
              </div>

              <div className="info-grid">
                <div className="mini-panel">
                  <strong>邮箱</strong>
                  <span>{selectedResume.structured.basics.email || "-"}</span>
                </div>
                <div className="mini-panel">
                  <strong>电话</strong>
                  <span>{selectedResume.structured.basics.phone || "-"}</span>
                </div>
                <div className="mini-panel">
                  <strong>切片数</strong>
                  <span>{selectedResume.chunks.length}</span>
                </div>
                <div className="mini-panel">
                  <strong>入库时间</strong>
                  <span>{formatDate(selectedResume.createTime)}</span>
                </div>
              </div>

              <div>
                <h4 className="subsection-title">原始文本预览</h4>
                <pre className="code-block">{selectedResume.rawText || "暂无原始文本。"}</pre>
              </div>

              <div>
                <h4 className="subsection-title">Chunk 摘要</h4>
                <div className="stack-sm">
                  {selectedResume.chunks.slice(0, 4).map((chunk) => (
                    <div className="result-card" key={`${chunk.chunkIndex}-${chunk.chunkId ?? chunk.chunkIndex}`}>
                      <div className="result-topline">
                        <strong>Chunk #{chunk.chunkIndex}</strong>
                        <span className="table-meta">{chunk.embeddingDimensions} 维</span>
                      </div>
                      <p>{chunk.chunkText}</p>
                    </div>
                  ))}
                </div>
              </div>
            </div>
          )}
        </SectionCard>
      </div>
    </div>
  );
}

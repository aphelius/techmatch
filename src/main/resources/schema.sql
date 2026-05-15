CREATE EXTENSION IF NOT EXISTS vector;

CREATE TABLE IF NOT EXISTS app_user (
    id BIGSERIAL PRIMARY KEY,
    username VARCHAR(64) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    nickname VARCHAR(64),
    create_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS resume (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    file_name VARCHAR(255) NOT NULL,
    file_type VARCHAR(16) NOT NULL,
    file_size BIGINT NOT NULL,
    storage_bucket VARCHAR(128) NOT NULL,
    storage_object_key VARCHAR(255) NOT NULL,
    storage_url VARCHAR(512),
    status VARCHAR(32) NOT NULL,
    raw_text TEXT,
    structured_json TEXT,
    parse_error VARCHAR(1000),
    create_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_resume_user FOREIGN KEY (user_id) REFERENCES app_user (id)
);

CREATE INDEX IF NOT EXISTS idx_resume_user_id ON resume (user_id);

CREATE TABLE IF NOT EXISTS document_chunk (
    id BIGSERIAL PRIMARY KEY,
    resume_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    chunk_index INT NOT NULL,
    chunk_text TEXT NOT NULL,
    metadata_json TEXT,
    embedding vector,
    create_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_document_chunk_resume FOREIGN KEY (resume_id) REFERENCES resume (id),
    CONSTRAINT fk_document_chunk_user FOREIGN KEY (user_id) REFERENCES app_user (id)
);

CREATE INDEX IF NOT EXISTS idx_document_chunk_resume_id ON document_chunk (resume_id);
CREATE INDEX IF NOT EXISTS idx_document_chunk_user_id ON document_chunk (user_id);

CREATE TABLE IF NOT EXISTS job_description (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    title VARCHAR(255) NOT NULL,
    company VARCHAR(255),
    location VARCHAR(255),
    raw_text TEXT NOT NULL,
    structured_json TEXT,
    status VARCHAR(32) NOT NULL,
    parse_error VARCHAR(1000),
    create_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_job_description_user FOREIGN KEY (user_id) REFERENCES app_user (id)
);

CREATE INDEX IF NOT EXISTS idx_job_description_user_id ON job_description (user_id);

CREATE TABLE IF NOT EXISTS job_description_chunk (
    id BIGSERIAL PRIMARY KEY,
    job_description_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    chunk_index INT NOT NULL,
    chunk_text TEXT NOT NULL,
    metadata_json TEXT,
    embedding vector,
    create_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_job_description_chunk_job FOREIGN KEY (job_description_id) REFERENCES job_description (id),
    CONSTRAINT fk_job_description_chunk_user FOREIGN KEY (user_id) REFERENCES app_user (id)
);

CREATE INDEX IF NOT EXISTS idx_job_description_chunk_job_id ON job_description_chunk (job_description_id);
CREATE INDEX IF NOT EXISTS idx_job_description_chunk_user_id ON job_description_chunk (user_id);

CREATE TABLE IF NOT EXISTS agent_task (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    resume_id BIGINT NOT NULL,
    job_description_id BIGINT NOT NULL,
    task_type VARCHAR(64) NOT NULL,
    status VARCHAR(32) NOT NULL,
    current_node VARCHAR(128),
    error_message VARCHAR(1000),
    report_json TEXT,
    started_at TIMESTAMP,
    finished_at TIMESTAMP,
    create_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_agent_task_user FOREIGN KEY (user_id) REFERENCES app_user (id),
    CONSTRAINT fk_agent_task_resume FOREIGN KEY (resume_id) REFERENCES resume (id),
    CONSTRAINT fk_agent_task_job FOREIGN KEY (job_description_id) REFERENCES job_description (id)
);

CREATE INDEX IF NOT EXISTS idx_agent_task_user_id ON agent_task (user_id);
CREATE INDEX IF NOT EXISTS idx_agent_task_resume_id ON agent_task (resume_id);
CREATE INDEX IF NOT EXISTS idx_agent_task_job_description_id ON agent_task (job_description_id);
CREATE INDEX IF NOT EXISTS idx_agent_task_status ON agent_task (status);
ALTER TABLE agent_task ADD COLUMN IF NOT EXISTS report_json TEXT;

CREATE TABLE IF NOT EXISTS agent_timeline_event (
    id BIGSERIAL PRIMARY KEY,
    task_id BIGINT NOT NULL,
    node_name VARCHAR(128) NOT NULL,
    status VARCHAR(32) NOT NULL,
    input_summary VARCHAR(1000),
    output_summary VARCHAR(1000),
    model_name VARCHAR(128),
    prompt_tokens INT,
    completion_tokens INT,
    duration_ms BIGINT,
    retry_count INT NOT NULL DEFAULT 0,
    error_message VARCHAR(1000),
    metadata_json TEXT,
    create_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_agent_timeline_event_task FOREIGN KEY (task_id) REFERENCES agent_task (id)
);

CREATE INDEX IF NOT EXISTS idx_agent_timeline_event_task_id ON agent_timeline_event (task_id);
CREATE INDEX IF NOT EXISTS idx_agent_timeline_event_status ON agent_timeline_event (status);

CREATE TABLE IF NOT EXISTS evidence_node (
    id BIGSERIAL PRIMARY KEY,
    task_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    node_type VARCHAR(64) NOT NULL,
    node_key VARCHAR(255) NOT NULL,
    title VARCHAR(255) NOT NULL,
    content TEXT,
    status VARCHAR(32),
    metadata_json TEXT,
    create_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_evidence_node_task FOREIGN KEY (task_id) REFERENCES agent_task (id),
    CONSTRAINT fk_evidence_node_user FOREIGN KEY (user_id) REFERENCES app_user (id)
);

CREATE INDEX IF NOT EXISTS idx_evidence_node_task_id ON evidence_node (task_id);
CREATE INDEX IF NOT EXISTS idx_evidence_node_user_id ON evidence_node (user_id);
CREATE INDEX IF NOT EXISTS idx_evidence_node_type ON evidence_node (node_type);

CREATE TABLE IF NOT EXISTS evidence_edge (
    id BIGSERIAL PRIMARY KEY,
    task_id BIGINT NOT NULL,
    from_node_id BIGINT NOT NULL,
    to_node_id BIGINT NOT NULL,
    edge_type VARCHAR(64) NOT NULL,
    metadata_json TEXT,
    create_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_evidence_edge_task FOREIGN KEY (task_id) REFERENCES agent_task (id),
    CONSTRAINT fk_evidence_edge_from FOREIGN KEY (from_node_id) REFERENCES evidence_node (id),
    CONSTRAINT fk_evidence_edge_to FOREIGN KEY (to_node_id) REFERENCES evidence_node (id)
);

CREATE INDEX IF NOT EXISTS idx_evidence_edge_task_id ON evidence_edge (task_id);
CREATE INDEX IF NOT EXISTS idx_evidence_edge_type ON evidence_edge (edge_type);

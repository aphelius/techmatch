package com.techmatch.resume.repository;

import com.pgvector.PGvector;
import com.techmatch.config.ResumeProperties;
import com.techmatch.resume.dto.ResumeChunkSearchResponse;
import com.techmatch.resume.entity.DocumentChunkEntity;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.ConnectionCallback;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

@Repository
@RequiredArgsConstructor
public class PgvectorDocumentChunkRepository {

    private final JdbcTemplate jdbcTemplate;
    private final ResumeProperties resumeProperties;

    @PostConstruct
    public void initialize() {
        jdbcTemplate.execute("CREATE EXTENSION IF NOT EXISTS vector");
        jdbcTemplate.execute("ALTER TABLE document_chunk ADD COLUMN IF NOT EXISTS embedding vector");
        jdbcTemplate.execute("ALTER TABLE document_chunk DROP COLUMN IF EXISTS embedding_json");
        jdbcTemplate.execute("CREATE INDEX IF NOT EXISTS idx_document_chunk_resume_id ON document_chunk (resume_id)");
        jdbcTemplate.execute("CREATE INDEX IF NOT EXISTS idx_document_chunk_user_id ON document_chunk (user_id)");
        jdbcTemplate.execute(buildCosineIndexSql());
    }

    public void insert(DocumentChunkEntity entity) {
        withRegisteredTypes(connection -> {
            try (PreparedStatement statement = connection.prepareStatement("""
                    INSERT INTO document_chunk
                    (resume_id, user_id, chunk_index, chunk_text, metadata_json, embedding, create_time)
                    VALUES (?, ?, ?, ?, ?, ?, ?)
                    """)) {
                statement.setLong(1, entity.getResumeId());
                statement.setLong(2, entity.getUserId());
                statement.setInt(3, entity.getChunkIndex());
                statement.setString(4, entity.getChunkText());
                statement.setString(5, entity.getMetadataJson());
                statement.setObject(6, toPgvector(entity.getEmbedding()));
                statement.setTimestamp(7, Timestamp.valueOf(entity.getCreateTime()));
                statement.executeUpdate();
            }
            return null;
        });
    }

    public List<DocumentChunkEntity> findByResumeIdAndUserId(Long resumeId, Long userId) {
        return withRegisteredTypes(connection -> {
            String sql = """
                    SELECT id, resume_id, user_id, chunk_index, chunk_text, metadata_json, embedding, create_time
                    FROM document_chunk
                    WHERE resume_id = ? AND user_id = ?
                    ORDER BY chunk_index ASC
                    """;
            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setLong(1, resumeId);
                statement.setLong(2, userId);
                try (var resultSet = statement.executeQuery()) {
                    List<DocumentChunkEntity> result = new ArrayList<>();
                    while (resultSet.next()) {
                        result.add(mapChunk(resultSet));
                    }
                    return result;
                }
            }
        });
    }

    public List<ResumeChunkSearchResponse> searchByUserId(Long userId, List<Double> queryEmbedding, int topK) {
        return withRegisteredTypes(connection -> {
            String sql = """
                    SELECT dc.id,
                           dc.resume_id,
                           r.file_name,
                           dc.chunk_index,
                           dc.chunk_text,
                           dc.metadata_json,
                           dc.embedding <=> ? AS distance
                    FROM document_chunk dc
                    JOIN resume r ON r.id = dc.resume_id
                    WHERE dc.user_id = ?
                      AND dc.embedding IS NOT NULL
                      AND vector_dims(dc.embedding) = ?
                    ORDER BY dc.embedding <=> ?
                    LIMIT ?
                    """;
            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                PGvector vector = toPgvector(queryEmbedding);
                statement.setObject(1, vector);
                statement.setLong(2, userId);
                statement.setInt(3, queryEmbedding.size());
                statement.setObject(4, vector);
                statement.setInt(5, topK);
                try (var resultSet = statement.executeQuery()) {
                    List<ResumeChunkSearchResponse> result = new ArrayList<>();
                    while (resultSet.next()) {
                        double distance = resultSet.getDouble("distance");
                        result.add(ResumeChunkSearchResponse.builder()
                                .chunkId(resultSet.getLong("id"))
                                .resumeId(resultSet.getLong("resume_id"))
                                .fileName(resultSet.getString("file_name"))
                                .chunkIndex(resultSet.getInt("chunk_index"))
                                .chunkText(resultSet.getString("chunk_text"))
                                .metadataJson(resultSet.getString("metadata_json"))
                                .distance(distance)
                                .similarity(1 - distance)
                                .build());
                    }
                    return result;
                }
            }
        });
    }

    private String buildCosineIndexSql() {
        int dimensions = resumeProperties.getEmbedding().getDimensions();
        return """
                CREATE INDEX IF NOT EXISTS idx_document_chunk_embedding_cosine
                ON document_chunk
                USING hnsw ((embedding::vector(%d)) vector_cosine_ops)
                WHERE embedding IS NOT NULL AND vector_dims(embedding) = %d
                """.formatted(dimensions, dimensions);
    }

    private DocumentChunkEntity mapChunk(java.sql.ResultSet resultSet) throws java.sql.SQLException {
        PGvector vector = (PGvector) resultSet.getObject("embedding");
        DocumentChunkEntity entity = new DocumentChunkEntity();
        entity.setId(resultSet.getLong("id"));
        entity.setResumeId(resultSet.getLong("resume_id"));
        entity.setUserId(resultSet.getLong("user_id"));
        entity.setChunkIndex(resultSet.getInt("chunk_index"));
        entity.setChunkText(resultSet.getString("chunk_text"));
        entity.setMetadataJson(resultSet.getString("metadata_json"));
        entity.setEmbedding(toList(vector));
        entity.setCreateTime(resultSet.getTimestamp("create_time").toLocalDateTime());
        return entity;
    }

    private <T> T withRegisteredTypes(ConnectionCallback<T> callback) {
        return jdbcTemplate.execute((ConnectionCallback<T>) connection -> {
            PGvector.registerTypes(connection);
            return callback.doInConnection(connection);
        });
    }

    private PGvector toPgvector(List<Double> values) {
        float[] data = new float[values.size()];
        for (int index = 0; index < values.size(); index++) {
            data[index] = values.get(index).floatValue();
        }
        return new PGvector(data);
    }

    private List<Double> toList(PGvector vector) {
        if (vector == null) {
            return List.of();
        }
        float[] values = vector.toArray();
        List<Double> result = new ArrayList<>(values.length);
        for (float value : values) {
            result.add((double) value);
        }
        return result;
    }
}

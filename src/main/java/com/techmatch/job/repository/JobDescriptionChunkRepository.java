package com.techmatch.job.repository;

import com.pgvector.PGvector;
import com.techmatch.config.ResumeProperties;
import com.techmatch.job.entity.JobDescriptionChunkEntity;
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
public class JobDescriptionChunkRepository {

    private final JdbcTemplate jdbcTemplate;
    private final ResumeProperties resumeProperties;

    @PostConstruct
    public void initialize() {
        jdbcTemplate.execute("CREATE EXTENSION IF NOT EXISTS vector");
        jdbcTemplate.execute("ALTER TABLE job_description_chunk ADD COLUMN IF NOT EXISTS embedding vector");
        jdbcTemplate.execute("CREATE INDEX IF NOT EXISTS idx_job_description_chunk_job_id ON job_description_chunk (job_description_id)");
        jdbcTemplate.execute("CREATE INDEX IF NOT EXISTS idx_job_description_chunk_user_id ON job_description_chunk (user_id)");
        jdbcTemplate.execute(buildCosineIndexSql());
    }

    public void insert(JobDescriptionChunkEntity entity) {
        withRegisteredTypes(connection -> {
            try (PreparedStatement statement = connection.prepareStatement("""
                    INSERT INTO job_description_chunk
                    (job_description_id, user_id, chunk_index, chunk_text, metadata_json, embedding, create_time)
                    VALUES (?, ?, ?, ?, ?, ?, ?)
                    """)) {
                statement.setLong(1, entity.getJobDescriptionId());
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

    public List<JobDescriptionChunkEntity> findByJobDescriptionIdAndUserId(Long jobDescriptionId, Long userId) {
        return withRegisteredTypes(connection -> {
            try (PreparedStatement statement = connection.prepareStatement("""
                    SELECT id, job_description_id, user_id, chunk_index, chunk_text, metadata_json, embedding, create_time
                    FROM job_description_chunk
                    WHERE job_description_id = ? AND user_id = ?
                    ORDER BY chunk_index ASC
                    """)) {
                statement.setLong(1, jobDescriptionId);
                statement.setLong(2, userId);
                try (var resultSet = statement.executeQuery()) {
                    List<JobDescriptionChunkEntity> result = new ArrayList<>();
                    while (resultSet.next()) {
                        result.add(mapChunk(resultSet));
                    }
                    return result;
                }
            }
        });
    }

    private String buildCosineIndexSql() {
        int dimensions = resumeProperties.getEmbedding().getDimensions();
        return """
                CREATE INDEX IF NOT EXISTS idx_job_description_chunk_embedding_cosine
                ON job_description_chunk
                USING hnsw ((embedding::vector(%d)) vector_cosine_ops)
                WHERE embedding IS NOT NULL AND vector_dims(embedding) = %d
                """.formatted(dimensions, dimensions);
    }

    private JobDescriptionChunkEntity mapChunk(java.sql.ResultSet resultSet) throws java.sql.SQLException {
        PGvector vector = (PGvector) resultSet.getObject("embedding");
        JobDescriptionChunkEntity entity = new JobDescriptionChunkEntity();
        entity.setId(resultSet.getLong("id"));
        entity.setJobDescriptionId(resultSet.getLong("job_description_id"));
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

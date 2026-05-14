package com.techmatch.system.service;

import com.rabbitmq.client.Channel;
import com.techmatch.system.dto.InfraComponentStatus;
import com.techmatch.system.dto.InfraStatusResponse;
import io.minio.MinioClient;
import io.minio.messages.Bucket;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SystemService {

    private final JdbcTemplate jdbcTemplate;
    private final StringRedisTemplate stringRedisTemplate;
    private final RabbitTemplate rabbitTemplate;
    private final MinioClient minioClient;

    public InfraStatusResponse checkInfrastructure() {
        InfraStatusResponse response = new InfraStatusResponse();
        response.setDatabase(checkDatabase());
        response.setRedis(checkRedis());
        response.setRabbitmq(checkRabbitMq());
        response.setMinio(checkMinio());
        return response;
    }

    private InfraComponentStatus checkDatabase() {
        try {
            Integer result = jdbcTemplate.queryForObject("SELECT 1", Integer.class);
            return new InfraComponentStatus(true, "SELECT 1 => " + result);
        } catch (DataAccessException exception) {
            return new InfraComponentStatus(false, exception.getMostSpecificCause().getMessage());
        }
    }

    private InfraComponentStatus checkRedis() {
        try (RedisConnection connection = stringRedisTemplate.getConnectionFactory().getConnection()) {
            String pong = connection.ping();
            return new InfraComponentStatus(true, pong);
        } catch (Exception exception) {
            return new InfraComponentStatus(false, exception.getMessage());
        }
    }

    private InfraComponentStatus checkRabbitMq() {
        try {
            Boolean connected = rabbitTemplate.execute(channel -> isChannelOpen(channel));
            return new InfraComponentStatus(Boolean.TRUE.equals(connected), "channel open=" + connected);
        } catch (Exception exception) {
            return new InfraComponentStatus(false, exception.getMessage());
        }
    }

    private InfraComponentStatus checkMinio() {
        try {
            List<Bucket> buckets = minioClient.listBuckets();
            return new InfraComponentStatus(true, "bucket count=" + buckets.size());
        } catch (Exception exception) {
            return new InfraComponentStatus(false, exception.getMessage());
        }
    }

    private Boolean isChannelOpen(Channel channel) {
        return channel != null && channel.isOpen();
    }
}

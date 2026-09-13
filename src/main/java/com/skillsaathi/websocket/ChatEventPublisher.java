package com.skillsaathi.websocket;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

/**
 * Publishes WS events to Redis. Called from the STOMP controller / services after a message
 * is persisted — never sends to WebSocket clients directly (that's the Subscriber's job on
 * every instance, including this one, once the Redis message round-trips back).
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class ChatEventPublisher {

    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;

    public static final String CHANNEL = "skillsaathi:ws-events";

    public void publish(WsEventType type, String destination, Object payload) {
        try {
            String payloadJson = objectMapper.writeValueAsString(payload);
            WsEventEnvelope envelope = WsEventEnvelope.builder()
                    .type(type)
                    .destination(destination)
                    .payloadJson(payloadJson)
                    .build();
            String envelopeJson = objectMapper.writeValueAsString(envelope);
            redisTemplate.convertAndSend(CHANNEL, envelopeJson);
        } catch (Exception e) {
            log.error("Failed to publish WS event to Redis: {}", e.getMessage());
        }
    }
}

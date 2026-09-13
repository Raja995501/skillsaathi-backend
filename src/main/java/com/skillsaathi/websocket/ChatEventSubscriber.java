package com.skillsaathi.websocket;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

/**
 * Runs on EVERY backend instance. Whenever any instance publishes to the Redis channel
 * (see ChatEventPublisher), all instances — including the publisher itself — receive it here
 * and forward it to their own locally-connected STOMP clients. This is what makes a message
 * sent while connected to Instance-A visible to a recipient connected to Instance-B.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class ChatEventSubscriber implements MessageListener {

    private final SimpMessagingTemplate messagingTemplate;
    private final ObjectMapper objectMapper;

    @Override
    public void onMessage(Message message, byte[] pattern) {
        try {
            String body = new String(message.getBody());
            WsEventEnvelope envelope = objectMapper.readValue(body, WsEventEnvelope.class);
            // payloadJson is forwarded as-is (already JSON) so the client gets the same
            // shape regardless of which DTO type produced it.
            messagingTemplate.convertAndSend(envelope.getDestination(), envelope.getPayloadJson());
        } catch (Exception e) {
            log.error("Failed to process WS event from Redis: {}", e.getMessage());
        }
    }
}

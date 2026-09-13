package com.skillsaathi.websocket;

import lombok.*;

/**
 * Everything that needs to reach a WebSocket client goes through Redis wrapped in this envelope,
 * regardless of which backend instance originally produced the event. Every instance subscribes
 * to the same Redis channel and re-broadcasts locally to whichever clients are connected to IT —
 * this is what makes chat/typing/presence correct across multiple horizontally-scaled instances.
 *
 * destination: the STOMP destination to broadcast to, e.g. "/topic/connection.42"
 * payloadJson: the actual DTO (MessageResponse / TypingEventResponse / PresenceEventResponse),
 *              pre-serialized to JSON so the subscriber doesn't need to know every DTO type.
 */
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class WsEventEnvelope {
    private WsEventType type;
    private String destination;
    private String payloadJson;
}

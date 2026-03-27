package com.example.growl.ads.opportunity;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Export shape for missing ad opportunities (one row per inferred gap after an assistant message).
 */
public record PotentialMissingAdOpportunityRow(
        @JsonProperty("visitor_id") String visitorId,
        @JsonProperty("session_id") String sessionId,
        @JsonProperty("chat_id") String chatId,
        @JsonProperty("assistant_message_count") int assistantMessageCount
) {
}

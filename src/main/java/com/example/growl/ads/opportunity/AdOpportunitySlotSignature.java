package com.example.growl.ads.opportunity;

/**
 * Mirrors counters embedded in ad-opportunities {@code slot_data} JSON.
 */
public record AdOpportunitySlotSignature(
        int assistantMessageCount,
        int userMessageCount,
        int chatHistoryLength,
        int opportunityIndex
) {
}

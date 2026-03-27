package com.example.growl.ads.opportunity;

/**
 * A placement point inferred from the transcript that does not appear in ad-opportunities.csv
 * for this visitor + chat (same signature as {@code slot_data} counters).
 */
public record PotentialAdOpportunitySlot(
        int transcriptIndex,
        String anchorCreatedAt,
        int assistantMessageCount,
        int userMessageCount,
        int chatHistoryLength,
        int opportunityIndex,
        SlotKind kind
) {
    public enum SlotKind {
        /** Expected one ad opportunity after this assistant turn (opportunity_index 0) but no matching CSV row. */
        MISSING_ASSISTANT_OPPORTUNITY
    }
}

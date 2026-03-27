package com.example.growl.ads.opportunity;

import java.util.List;

public record PotentialAdOpportunityReport(
        String visitorId,
        String chatId,
        int transcriptMessageCount,
        /** Number of chat rows with role assistant (1:1 ad opportunity expectation). */
        int assistantMessageCount,
        /** Rows in ad-opportunities.csv for this visitor_id + slot_data.chat_id. */
        int recordedOpportunityCount,
        /** {@code assistantMessageCount > recordedOpportunityCount}. */
        boolean hasMoreAdOpportunitiesThanRecorded,
        /** {@code max(0, assistantMessageCount - recordedOpportunityCount)} — net gap by counting. */
        int missingOpportunityCountByAssistantVersusRows,
        /** {@code max(0, recordedOpportunityCount - assistantMessageCount)} — duplicate or inconsistent logging. */
        int surplusRecordedRowsVersusAssistants,
        List<PotentialAdOpportunitySlot> missingSlots,
        String methodologyNote
) {
}

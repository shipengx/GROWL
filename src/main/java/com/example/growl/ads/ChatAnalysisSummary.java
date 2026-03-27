package com.example.growl.ads;

/**
 * How the decision engine used chat history for this opportunity.
 */
public record ChatAnalysisSummary(
        boolean transcriptFound,
        String visitorId,
        String chatId,
        int transcriptRowCount,
        double bestRelevanceScore
) {
    public static ChatAnalysisSummary missingKeys(String visitorId, String chatId) {
        return new ChatAnalysisSummary(false, visitorId, chatId, 0, 0);
    }

    public static ChatAnalysisSummary notFound(String visitorId, String chatId) {
        return new ChatAnalysisSummary(false, visitorId, chatId, 0, 0);
    }

    public static ChatAnalysisSummary found(String visitorId, String chatId, int rows, double bestRelevance) {
        return new ChatAnalysisSummary(true, visitorId, chatId, rows, bestRelevance);
    }
}

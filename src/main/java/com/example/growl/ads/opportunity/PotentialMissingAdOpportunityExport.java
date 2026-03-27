package com.example.growl.ads.opportunity;

import com.example.growl.csv.model.ChatDataRow;

import java.util.ArrayList;
import java.util.List;

public final class PotentialMissingAdOpportunityExport {

    private PotentialMissingAdOpportunityExport() {
    }

    public static List<PotentialMissingAdOpportunityRow> toRows(
            String visitorId,
            String chatId,
            List<ChatDataRow> transcript,
            PotentialAdOpportunityReport report) {
        String fallbackSession = resolveFallbackSession(transcript);
        List<PotentialMissingAdOpportunityRow> out = new ArrayList<>();
        for (PotentialAdOpportunitySlot slot : report.missingSlots()) {
            int idx = slot.transcriptIndex();
            if (idx < 0 || idx >= transcript.size()) {
                continue;
            }
            String sessionId = firstNonBlank(transcript.get(idx).getSessionId(), fallbackSession);
            out.add(new PotentialMissingAdOpportunityRow(
                    visitorId,
                    sessionId,
                    chatId,
                    slot.assistantMessageCount()));
        }
        return List.copyOf(out);
    }

    public static String toCsv(List<PotentialMissingAdOpportunityRow> rows) {
        StringBuilder sb = new StringBuilder();
        sb.append("visitor_id,session_id,chat_id,assistant_message_count\n");
        for (PotentialMissingAdOpportunityRow r : rows) {
            sb.append(csvEscape(r.visitorId()))
                    .append(',')
                    .append(csvEscape(r.sessionId()))
                    .append(',')
                    .append(csvEscape(r.chatId()))
                    .append(',')
                    .append(r.assistantMessageCount())
                    .append('\n');
        }
        return sb.toString();
    }

    private static String resolveFallbackSession(List<ChatDataRow> transcript) {
        for (ChatDataRow row : transcript) {
            if (row.getSessionId() != null && !row.getSessionId().isBlank()) {
                return row.getSessionId().trim();
            }
        }
        return "";
    }

    private static String firstNonBlank(String preferred, String fallback) {
        if (preferred != null && !preferred.isBlank()) {
            return preferred.trim();
        }
        return fallback == null ? "" : fallback;
    }

    private static String csvEscape(String s) {
        if (s == null) {
            return "";
        }
        if (s.contains(",") || s.contains("\"") || s.contains("\r") || s.contains("\n")) {
            return "\"" + s.replace("\"", "\"\"") + "\"";
        }
        return s;
    }
}

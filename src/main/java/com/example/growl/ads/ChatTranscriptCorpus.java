package com.example.growl.ads;

import com.example.growl.csv.model.ChatDataRow;

import java.util.List;
import java.util.Locale;

/**
 * Builds a single lowercase string from chat rows for keyword-style relevance checks.
 * User messages are duplicated so intent is weighted slightly higher.
 */
public final class ChatTranscriptCorpus {

    private ChatTranscriptCorpus() {
    }

    public static String build(List<ChatDataRow> rows) {
        if (rows == null || rows.isEmpty()) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        for (ChatDataRow row : rows) {
            String text = row.getText();
            if (text == null || text.isBlank()) {
                continue;
            }
            sb.append(text).append('\n');
            if ("user".equalsIgnoreCase(row.getRole())) {
                sb.append(text).append('\n');
            }
        }
        return sb.toString().toLowerCase(Locale.ROOT);
    }
}

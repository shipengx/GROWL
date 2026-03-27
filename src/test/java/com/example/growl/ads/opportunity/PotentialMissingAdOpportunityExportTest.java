package com.example.growl.ads.opportunity;

import com.example.growl.csv.model.AdOpportunity;
import com.example.growl.csv.model.ChatDataRow;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class PotentialMissingAdOpportunityExportTest {

    @Test
    void rowsIncludeSessionFromAnchorMessage() {
        List<ChatDataRow> transcript = new ArrayList<>();
        transcript.add(user("s1", "u1"));
        transcript.add(assistant("s1", "a1"));
        transcript.add(user("s1", "u2"));
        transcript.add(assistant("s1", "a2"));

        AdOpportunity logged = new AdOpportunity();
        logged.setVisitorId("v1");
        logged.setSlotData(
                "{\"assistant_message_count\": \"1\", \"user_message_count\": \"1\", \"chat_history_length\": \"2\", \"chat_id\": \"1\", \"opportunity_index\": \"0\"}");

        PotentialAdOpportunityReport report = new PotentialAdOpportunityAnalyzer(new ObjectMapper())
                .analyze("v1", "1", transcript, List.of(logged));

        List<PotentialMissingAdOpportunityRow> rows =
                PotentialMissingAdOpportunityExport.toRows("v1", "1", transcript, report);

        assertThat(rows).hasSize(1);
        assertThat(rows.get(0).visitorId()).isEqualTo("v1");
        assertThat(rows.get(0).sessionId()).isEqualTo("s1");
        assertThat(rows.get(0).chatId()).isEqualTo("1");
        assertThat(rows.get(0).assistantMessageCount()).isEqualTo(2);
    }

    @Test
    void csvHasHeaderAndRow() {
        List<PotentialMissingAdOpportunityRow> rows = List.of(
                new PotentialMissingAdOpportunityRow("vid", "sid", "10", 3));
        String csv = PotentialMissingAdOpportunityExport.toCsv(rows);
        assertThat(csv).isEqualTo("visitor_id,session_id,chat_id,assistant_message_count\nvid,sid,10,3\n");
    }

    private static ChatDataRow user(String sessionId, String createdAt) {
        ChatDataRow r = new ChatDataRow();
        r.setRole("user");
        r.setSessionId(sessionId);
        r.setText("x");
        r.setCreatedAt(createdAt);
        return r;
    }

    private static ChatDataRow assistant(String sessionId, String createdAt) {
        ChatDataRow r = new ChatDataRow();
        r.setRole("assistant");
        r.setSessionId(sessionId);
        r.setText("y");
        r.setCreatedAt(createdAt);
        return r;
    }
}

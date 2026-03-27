package com.example.growl.ads.opportunity;

import com.example.growl.csv.model.AdOpportunity;
import com.example.growl.csv.model.ChatDataRow;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class PotentialAdOpportunityAnalyzerTest {

    private final PotentialAdOpportunityAnalyzer analyzer = new PotentialAdOpportunityAnalyzer(new ObjectMapper());

    @Test
    void surplusWhenMoreCsvRowsThanAssistantMessages() {
        List<ChatDataRow> transcript = List.of(user("u1"), assistant("a1"));
        AdOpportunity r1 = opportunity("v1", slotJson("1", 1, 1, 2, 0));
        AdOpportunity r2 = opportunity("v1", slotJson("1", 1, 1, 2, 0));

        PotentialAdOpportunityReport report = analyzer.analyze("v1", "1", transcript, List.of(r1, r2));

        assertThat(report.assistantMessageCount()).isEqualTo(1);
        assertThat(report.recordedOpportunityCount()).isEqualTo(2);
        assertThat(report.hasMoreAdOpportunitiesThanRecorded()).isFalse();
        assertThat(report.missingOpportunityCountByAssistantVersusRows()).isZero();
        assertThat(report.surplusRecordedRowsVersusAssistants()).isEqualTo(1);
    }

    @Test
    void findsMissingTurnAfterFirstAssistantWhenOnlyFirstTurnLogged() {
        List<ChatDataRow> transcript = new ArrayList<>();
        transcript.add(user("u1"));
        transcript.add(assistant("a1"));
        transcript.add(user("u2"));
        transcript.add(assistant("a2"));

        AdOpportunity logged = opportunity(
                "v1",
                slotJson("1", 1, 1, 2, 0));

        PotentialAdOpportunityReport report = analyzer.analyze("v1", "1", transcript, List.of(logged));

        assertThat(report.transcriptMessageCount()).isEqualTo(4);
        assertThat(report.assistantMessageCount()).isEqualTo(2);
        assertThat(report.recordedOpportunityCount()).isEqualTo(1);
        assertThat(report.hasMoreAdOpportunitiesThanRecorded()).isTrue();
        assertThat(report.missingOpportunityCountByAssistantVersusRows()).isEqualTo(1);
        assertThat(report.surplusRecordedRowsVersusAssistants()).isZero();
        assertThat(report.missingSlots())
                .anyMatch(s -> s.assistantMessageCount() == 2
                        && s.userMessageCount() == 2
                        && s.chatHistoryLength() == 4
                        && s.opportunityIndex() == 0
                        && s.kind() == PotentialAdOpportunitySlot.SlotKind.MISSING_ASSISTANT_OPPORTUNITY);
    }

    private static ChatDataRow user(String createdAt) {
        ChatDataRow r = new ChatDataRow();
        r.setRole("user");
        r.setText("hi");
        r.setCreatedAt(createdAt);
        return r;
    }

    private static ChatDataRow assistant(String createdAt) {
        ChatDataRow r = new ChatDataRow();
        r.setRole("assistant");
        r.setText("hello");
        r.setCreatedAt(createdAt);
        return r;
    }

    private static AdOpportunity opportunity(String visitorId, String slotData) {
        AdOpportunity o = new AdOpportunity();
        o.setVisitorId(visitorId);
        o.setSlotData(slotData);
        return o;
    }

    private static String slotJson(String chatId, int a, int u, int h, int oi) {
        return "{\"assistant_message_count\": \"%d\", \"user_message_count\": \"%d\", \"chat_history_length\": \"%d\", \"chat_id\": \"%s\", \"opportunity_index\": \"%d\"}"
                .formatted(a, u, h, chatId, oi);
    }
}

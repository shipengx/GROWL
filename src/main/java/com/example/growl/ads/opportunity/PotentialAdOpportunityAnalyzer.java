package com.example.growl.ads.opportunity;

import com.example.growl.csv.model.AdOpportunity;
import com.example.growl.csv.model.ChatDataRow;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

@Component
public class PotentialAdOpportunityAnalyzer {

    private static final String NOTE = "Model: each assistant message implies one ad opportunity (opportunity_index 0). "
            + "We compare (1) assistantMessageCount vs recordedOpportunityCount — rows in ad-opportunities.csv for "
            + "this visitor_id with slot_data.chat_id matching this chat — and (2) replay the transcript in "
            + "created_at order; after each assistant row, if no CSV row has the same "
            + "(assistant_message_count, user_message_count, chat_history_length, opportunity_index=0), that turn "
            + "is listed in missingSlots. If duplicate CSV rows share one signature, counts can disagree with "
            + "missingSlots.size().";

    private final ObjectMapper objectMapper;

    public PotentialAdOpportunityAnalyzer(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public PotentialAdOpportunityReport analyze(
            String visitorId,
            String chatId,
            List<ChatDataRow> transcript,
            List<AdOpportunity> allOpportunities) {
        Objects.requireNonNull(visitorId, "visitorId");
        Objects.requireNonNull(chatId, "chatId");
        Objects.requireNonNull(transcript, "transcript");
        Objects.requireNonNull(allOpportunities, "allOpportunities");

        String v = visitorId.trim();
        String c = chatId.trim();

        Set<AdOpportunitySlotSignature> recordedSignatures = new HashSet<>();
        int recordedForChat = 0;
        for (AdOpportunity row : allOpportunities) {
            if (row.getVisitorId() == null || !v.equals(row.getVisitorId().trim())) {
                continue;
            }
            var parsed = AdOpportunitySlotDataParser.parse(row.getSlotData(), objectMapper);
            if (parsed.isEmpty() || !c.equals(parsed.get().chatId())) {
                continue;
            }
            recordedSignatures.add(parsed.get().signature());
            recordedForChat++;
        }

        int assistantTurns = 0;
        for (ChatDataRow line : transcript) {
            if ("assistant".equalsIgnoreCase(line.getRole())) {
                assistantTurns++;
            }
        }

        int missingByCount = Math.max(0, assistantTurns - recordedForChat);
        int surplusRows = Math.max(0, recordedForChat - assistantTurns);

        List<PotentialAdOpportunitySlot> missing = new ArrayList<>();
        int u = 0;
        int a = 0;
        for (int i = 0; i < transcript.size(); i++) {
            ChatDataRow row = transcript.get(i);
            String role = row.getRole();
            if ("user".equalsIgnoreCase(role)) {
                u++;
            } else if ("assistant".equalsIgnoreCase(role)) {
                a++;
            }
            int h = i + 1;
            if (!"assistant".equalsIgnoreCase(role)) {
                continue;
            }
            AdOpportunitySlotSignature sig = new AdOpportunitySlotSignature(a, u, h, 0);
            if (!recordedSignatures.contains(sig)) {
                missing.add(new PotentialAdOpportunitySlot(
                        i,
                        row.getCreatedAt(),
                        a,
                        u,
                        h,
                        0,
                        PotentialAdOpportunitySlot.SlotKind.MISSING_ASSISTANT_OPPORTUNITY));
            }
        }

        return new PotentialAdOpportunityReport(
                v,
                c,
                transcript.size(),
                assistantTurns,
                recordedForChat,
                assistantTurns > recordedForChat,
                missingByCount,
                surplusRows,
                List.copyOf(missing),
                NOTE);
    }
}

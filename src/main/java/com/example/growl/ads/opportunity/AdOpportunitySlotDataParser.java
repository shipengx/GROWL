package com.example.growl.ads.opportunity;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.Optional;

public final class AdOpportunitySlotDataParser {

    private AdOpportunitySlotDataParser() {
    }

    public static Optional<ParsedSlotData> parse(String slotDataJson, ObjectMapper mapper) {
        if (slotDataJson == null || slotDataJson.isBlank()) {
            return Optional.empty();
        }
        try {
            JsonNode root = mapper.readTree(slotDataJson);
            String chatId = text(root, "chat_id");
            Integer assistant = intField(root, "assistant_message_count");
            Integer user = intField(root, "user_message_count");
            Integer history = intField(root, "chat_history_length");
            Integer oppIdx = intField(root, "opportunity_index");
            if (chatId == null || assistant == null || user == null || history == null || oppIdx == null) {
                return Optional.empty();
            }
            return Optional.of(new ParsedSlotData(
                    chatId,
                    new AdOpportunitySlotSignature(assistant, user, history, oppIdx)));
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    private static String text(JsonNode root, String field) {
        JsonNode n = root.get(field);
        if (n == null || n.isNull()) {
            return null;
        }
        String s = n.asText();
        return s == null || s.isBlank() ? null : s.trim();
    }

    private static Integer intField(JsonNode root, String field) {
        JsonNode n = root.get(field);
        if (n == null || n.isNull()) {
            return null;
        }
        try {
            return Integer.parseInt(n.asText().trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    public record ParsedSlotData(String chatId, AdOpportunitySlotSignature signature) {
    }
}

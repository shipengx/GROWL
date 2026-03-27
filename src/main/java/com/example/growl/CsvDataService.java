package com.example.growl;

import com.example.growl.csv.CsvResourceParser;
import com.example.growl.csv.model.AdOpportunity;
import com.example.growl.csv.model.ChatDataRow;
import com.example.growl.csv.model.Offer;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class CsvDataService {

    private List<Offer> offers = List.of();
    private List<AdOpportunity> adOpportunities = List.of();
    private List<ChatDataRow> chatData = List.of();
    private Map<String, List<ChatDataRow>> chatTranscriptIndex = Map.of();

    @PostConstruct
    public void loadFromClasspath() {
        ClassLoader cl = getClass().getClassLoader();
        try {
            offers = List.copyOf(CsvResourceParser.parseOffers(cl));
            adOpportunities = List.copyOf(CsvResourceParser.parseAdOpportunities(cl));
            chatData = List.copyOf(CsvResourceParser.parseChatData(cl));
            chatTranscriptIndex = buildChatTranscriptIndex(chatData);
        } catch (IOException e) {
            throw new UncheckedIOException("Failed to load CSV resources", e);
        }
    }

    private static Map<String, List<ChatDataRow>> buildChatTranscriptIndex(List<ChatDataRow> rows) {
        Map<String, List<ChatDataRow>> tmp = new HashMap<>();
        for (ChatDataRow row : rows) {
            if (row.getVisitorId() == null || row.getVisitorId().isBlank()
                    || row.getChatId() == null || row.getChatId().isBlank()) {
                continue;
            }
            String key = transcriptKey(row.getVisitorId(), row.getChatId());
            tmp.computeIfAbsent(key, k -> new ArrayList<>()).add(row);
        }
        Map<String, List<ChatDataRow>> frozen = new HashMap<>();
        for (Map.Entry<String, List<ChatDataRow>> e : tmp.entrySet()) {
            List<ChatDataRow> sorted = new ArrayList<>(e.getValue());
            sorted.sort(Comparator.comparing(ChatDataRow::getCreatedAt, Comparator.nullsLast(String::compareTo)));
            frozen.put(e.getKey(), List.copyOf(sorted));
        }
        return Collections.unmodifiableMap(frozen);
    }

    private static String transcriptKey(String visitorId, String chatId) {
        return visitorId.trim() + "\0" + chatId.trim();
    }

    /**
     * All chat rows for the same visitor and chat id, ordered by {@code created_at}.
     */
    public List<ChatDataRow> findChatTranscript(String visitorId, String chatId) {
        if (visitorId == null || visitorId.isBlank() || chatId == null || chatId.isBlank()) {
            return List.of();
        }
        return chatTranscriptIndex.getOrDefault(transcriptKey(visitorId, chatId), List.of());
    }

    public List<Offer> getOffers() {
        return offers;
    }

    public List<AdOpportunity> getAdOpportunities() {
        return adOpportunities;
    }

    public List<ChatDataRow> getChatData() {
        return chatData;
    }

    public Optional<AdOpportunity> findAdOpportunityById(String id) {
        if (id == null || id.isBlank()) {
            return Optional.empty();
        }
        return adOpportunities.stream().filter(o -> id.equals(o.getId())).findFirst();
    }
}

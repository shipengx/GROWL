package com.example.growl.ads;

import com.example.growl.CsvDataService;
import com.example.growl.csv.model.AdOpportunity;
import com.example.growl.csv.model.ChatDataRow;
import com.example.growl.csv.model.Offer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;

@Component
public class DefaultAdServingDecisionEngine implements AdServingDecisionEngine {

    static final String NOT_PAYOUT_ELIGIBLE = "NOT_PAYOUT_ELIGIBLE";
    static final String OPPORTUNITY_INDEX_CAP = "OPPORTUNITY_INDEX_CAP";
    static final String NO_GEO_OR_GLOBAL_MATCH = "NO_GEO_OR_GLOBAL_MATCH";
    static final String SELECTED_BEST_SCORED_OFFER_NO_CHAT = "SELECTED_BEST_SCORED_OFFER_NO_CHAT";
    static final String SELECTED_BEST_SCORED_OFFER_MISSING_KEYS = "SELECTED_BEST_SCORED_OFFER_MISSING_KEYS";
    static final String NO_CHAT_RELEVANT_OFFER = "NO_CHAT_RELEVANT_OFFER";
    static final String SELECTED_CHAT_RELEVANT_OFFER = "SELECTED_CHAT_RELEVANT_OFFER";
    static final String AD_LOAD_SUPPRESSED = "AD_LOAD_SUPPRESSED";

    private static final int MAX_OPPORTUNITY_INDEX_TO_SERVE = 1;
    /** Minimum lexical relevance before we consider an offer "about" the conversation. */
    private static final double MIN_CHAT_RELEVANCE = 2.0;
    private static final double CHAT_WEIGHT = 18.0;

    private final CsvDataService csvDataService;
    private final ObjectMapper objectMapper;
    private List<Offer> offers = List.of();

    public DefaultAdServingDecisionEngine(CsvDataService csvDataService, ObjectMapper objectMapper) {
        this.csvDataService = csvDataService;
        this.objectMapper = objectMapper;
    }

    @PostConstruct
    void loadOffers() {
        offers = List.copyOf(csvDataService.getOffers());
    }

    @Override
    public AdServingDecision decide(AdOpportunity opportunity, double adLoadPercent) {
        Objects.requireNonNull(opportunity, "opportunity");
        String id = opportunity.getId() == null ? "" : opportunity.getId();
        String adSlotId = opportunity.getSlotId();

        if (!isPayoutEligible(opportunity)) {
            return AdServingDecision.noAd(id, adSlotId, NOT_PAYOUT_ELIGIBLE);
        }

        int oppIndex = parseOpportunityIndex(opportunity.getSlotData());
        if (oppIndex > MAX_OPPORTUNITY_INDEX_TO_SERVE) {
            return AdServingDecision.noAd(id, adSlotId, OPPORTUNITY_INDEX_CAP);
        }

        double load = clampAdLoadPercent(adLoadPercent);
        if (load <= 0 || ThreadLocalRandom.current().nextDouble(100.0) >= load) {
            return AdServingDecision.noAd(id, adSlotId, AD_LOAD_SUPPRESSED);
        }

        String visitorId = opportunity.getVisitorId();
        String chatId = parseChatIdFromSlotData(opportunity.getSlotData());
        List<ChatDataRow> transcript = csvDataService.findChatTranscript(
                visitorId == null ? "" : visitorId,
                chatId == null ? "" : chatId);
        String corpus = ChatTranscriptCorpus.build(transcript);

        String geo = normalizeGeo(opportunity.getGeoCountryCode());

        boolean keysMissing = visitorId == null || visitorId.isBlank() || chatId == null || chatId.isBlank();

        if (corpus.isBlank()) {
            List<Offer> candidates = filterCandidatesStrict(geo);
            if (candidates.isEmpty()) {
                ChatAnalysisSummary s = buildSummaryForEmptyCorpus(keysMissing, visitorId, chatId, transcript);
                return AdServingDecision.noAd(id, adSlotId, NO_GEO_OR_GLOBAL_MATCH, s);
            }
            Offer best = pickBestRevenue(candidates);
            ChatAnalysisSummary s = buildSummaryForEmptyCorpus(keysMissing, visitorId, chatId, transcript);
            String reason = keysMissing
                    ? SELECTED_BEST_SCORED_OFFER_MISSING_KEYS
                    : SELECTED_BEST_SCORED_OFFER_NO_CHAT;
            return AdServingDecision.serveOffer(id, adSlotId, best, reason, s);
        }

        List<Offer> candidates = filterCandidatesUnion(geo);
        if (candidates.isEmpty()) {
            return AdServingDecision.noAd(
                    id,
                    adSlotId,
                    NO_GEO_OR_GLOBAL_MATCH,
                    ChatAnalysisSummary.found(visitorId, chatId, transcript.size(), 0));
        }

        double maxRelevance = candidates.stream()
                .mapToDouble(o -> OfferChatRelevance.score(o, corpus))
                .max()
                .orElse(0);

        ChatAnalysisSummary summary = ChatAnalysisSummary.found(visitorId, chatId, transcript.size(), maxRelevance);

        if (maxRelevance < MIN_CHAT_RELEVANCE) {
            return AdServingDecision.noAd(id, adSlotId, NO_CHAT_RELEVANT_OFFER, summary);
        }

        List<Offer> chatQualified = candidates.stream()
                .filter(o -> OfferChatRelevance.score(o, corpus) >= MIN_CHAT_RELEVANCE)
                .toList();

        Offer bestChat = chatQualified.stream()
                .max(Comparator
                        .comparingDouble(
                                (Offer o) -> OfferChatRelevance.score(o, corpus) * CHAT_WEIGHT
                                        + OfferValueScorer.score(o))
                        .thenComparing(o -> o.getOfferId() == null ? "" : o.getOfferId()))
                .orElseThrow();

        return AdServingDecision.serveOffer(id, adSlotId, bestChat, SELECTED_CHAT_RELEVANT_OFFER, summary);
    }

    private static ChatAnalysisSummary buildSummaryForEmptyCorpus(
            boolean keysMissing,
            String visitorId,
            String chatId,
            List<ChatDataRow> transcript) {
        if (keysMissing) {
            return ChatAnalysisSummary.missingKeys(visitorId, chatId);
        }
        if (transcript.isEmpty()) {
            return ChatAnalysisSummary.notFound(visitorId, chatId);
        }
        return ChatAnalysisSummary.found(visitorId, chatId, transcript.size(), 0);
    }

    private Offer pickBestRevenue(List<Offer> candidates) {
        return candidates.stream()
                .max(Comparator
                        .comparingDouble(OfferValueScorer::score)
                        .thenComparing(o -> o.getOfferId() == null ? "" : o.getOfferId()))
                .orElseThrow();
    }

    /**
     * Geo-specific offers if any exist for this region; otherwise global (no region tags in name).
     */
    private List<Offer> filterCandidatesStrict(String geo) {
        List<Offer> geoSpecific = new ArrayList<>();
        List<Offer> global = new ArrayList<>();
        partitionByGeo(geo, geoSpecific, global);
        if (!geoSpecific.isEmpty()) {
            return geoSpecific;
        }
        return global;
    }

    /**
     * Geo-targeted offers plus global offers (deduped) so "All countries" style campaigns can match chat intent.
     */
    private List<Offer> filterCandidatesUnion(String geo) {
        List<Offer> geoSpecific = new ArrayList<>();
        List<Offer> global = new ArrayList<>();
        partitionByGeo(geo, geoSpecific, global);
        Map<String, Offer> byId = new LinkedHashMap<>();
        for (Offer o : geoSpecific) {
            byId.putIfAbsent(offerKey(o), o);
        }
        for (Offer o : global) {
            byId.putIfAbsent(offerKey(o), o);
        }
        return new ArrayList<>(byId.values());
    }

    private void partitionByGeo(String geo, List<Offer> geoSpecific, List<Offer> global) {
        for (Offer offer : offers) {
            Set<String> regions = OfferRegionExtractor.regionsForOfferName(offer.getName());
            if (regions.isEmpty()) {
                global.add(offer);
            } else if (geo != null && regions.contains(geo)) {
                geoSpecific.add(offer);
            }
        }
    }

    private static String offerKey(Offer o) {
        return o.getOfferId() == null ? "noid-" + System.identityHashCode(o) : o.getOfferId();
    }

    private String parseChatIdFromSlotData(String slotData) {
        if (slotData == null || slotData.isBlank()) {
            return null;
        }
        try {
            JsonNode root = objectMapper.readTree(slotData);
            JsonNode n = root.get("chat_id");
            if (n == null || n.isNull()) {
                return null;
            }
            String s = n.asText();
            return s == null || s.isBlank() ? null : s.trim();
        } catch (Exception e) {
            return null;
        }
    }

    private static String normalizeGeo(String code) {
        if (code == null || code.isBlank()) {
            return null;
        }
        return code.trim().toUpperCase(Locale.ROOT);
    }

    private static double clampAdLoadPercent(double adLoadPercent) {
        if (adLoadPercent < 0) {
            return 0;
        }
        if (adLoadPercent > 100) {
            return 100;
        }
        return adLoadPercent;
    }

    private static boolean isPayoutEligible(AdOpportunity o) {
        String p = o.getPayoutEligible();
        if (p == null) {
            return false;
        }
        return p.equalsIgnoreCase("true") || p.equalsIgnoreCase("1") || p.equalsIgnoreCase("yes");
    }

    private int parseOpportunityIndex(String slotData) {
        if (slotData == null || slotData.isBlank()) {
            return 0;
        }
        try {
            JsonNode root = objectMapper.readTree(slotData);
            JsonNode n = root.get("opportunity_index");
            if (n == null || n.isNull()) {
                return 0;
            }
            return Integer.parseInt(n.asText().trim());
        } catch (Exception e) {
            return 0;
        }
    }
}

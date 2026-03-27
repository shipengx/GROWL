package com.example.growl.ads;

import com.example.growl.csv.model.AdOpportunity;
import com.example.growl.csv.model.ChatDataRow;
import com.example.growl.csv.model.Offer;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class DefaultAdServingDecisionEngineTest {

    private DefaultAdServingDecisionEngine engine;

    @BeforeEach
    void setUp() {
        Offer usHigh = offer("9001", "Big CPA - US", "CPA", "$99.00", "0%");
        Offer usLow = offer("9002", "Small CPA - US", "CPA", "$1.00", "0%");
        Offer ukOnly = offer("9003", "UK Thing - UK", "CPA", "$50.00", "0%");
        Offer global = offer("9004", "No region markers", "CPA", "$10.00", "0%");

        StubCsv csv = new StubCsv(List.of(usHigh, usLow, ukOnly, global), List.of());
        engine = new DefaultAdServingDecisionEngine(csv, new ObjectMapper());
        engine.loadOffers();
    }

    @Test
    void adLoadZero_alwaysSuppressesAfterEligibility() {
        AdOpportunity opp = baseOpportunity("opp-load", "US", "TRUE");
        opp.setSlotId("slot-load-1");
        AdServingDecision d = engine.decide(opp, 0.0);
        assertThat(d.serve()).isFalse();
        assertThat(d.reasonCode()).isEqualTo(DefaultAdServingDecisionEngine.AD_LOAD_SUPPRESSED);
        assertThat(d.adSlotId()).isEqualTo("slot-load-1");
    }

    @Test
    void notPayoutEligible_noAd() {
        AdOpportunity opp = baseOpportunity("opp-1", "US", "FALSE");
        AdServingDecision d = engine.decide(opp);
        assertThat(d.serve()).isFalse();
        assertThat(d.reasonCode()).isEqualTo(DefaultAdServingDecisionEngine.NOT_PAYOUT_ELIGIBLE);
    }

    @Test
    void usVisitor_prefersHighestScoredGeoMatch() {
        AdOpportunity opp = baseOpportunity("opp-2", "US", "TRUE");
        AdServingDecision d = engine.decide(opp);
        assertThat(d.serve()).isTrue();
        assertThat(d.offer().getOfferId()).isEqualTo("9001");
        assertThat(d.reasonCode()).isEqualTo(DefaultAdServingDecisionEngine.SELECTED_BEST_SCORED_OFFER_NO_CHAT);
    }

    @Test
    void nlVisitor_fallsBackToGlobalWhenNoGeoMatch() {
        AdOpportunity opp = baseOpportunity("opp-3", "NL", "TRUE");
        AdServingDecision d = engine.decide(opp);
        assertThat(d.serve()).isTrue();
        assertThat(d.offer().getOfferId()).isEqualTo("9004");
        assertThat(d.reasonCode()).isEqualTo(DefaultAdServingDecisionEngine.SELECTED_BEST_SCORED_OFFER_NO_CHAT);
    }

    @Test
    void opportunityIndexAboveCap_noAd() {
        AdOpportunity opp = baseOpportunity("opp-4", "US", "TRUE");
        opp.setSlotData("{\"opportunity_index\": \"2\"}");
        AdServingDecision d = engine.decide(opp);
        assertThat(d.serve()).isFalse();
        assertThat(d.reasonCode()).isEqualTo(DefaultAdServingDecisionEngine.OPPORTUNITY_INDEX_CAP);
    }

    @Test
    void chatPrefersLexicallyRelevantOfferAmongGeoMatches() {
        Offer generic = offer("9101", "Generic Widget Store - US", "CPA", "$100.00", "0%");
        Offer travel = offer(
                "9102",
                "EKTA Budget Travel Insurance Coverage - US",
                "CPA",
                "$2.00",
                "0%");
        ChatDataRow userMsg = new ChatDataRow();
        userMsg.setRole("user");
        userMsg.setText("I need travel insurance ideas before my trip to Spain.");
        userMsg.setVisitorId("v1");
        userMsg.setChatId("1");

        StubCsv csv = new StubCsv(List.of(generic, travel), List.of(userMsg));
        DefaultAdServingDecisionEngine chatEngine = new DefaultAdServingDecisionEngine(csv, new ObjectMapper());
        chatEngine.loadOffers();

        AdOpportunity opp = baseOpportunity("opp-chat", "US", "TRUE");
        opp.setVisitorId("v1");
        opp.setSlotData("{\"opportunity_index\": \"0\", \"chat_id\": \"1\"}");

        AdServingDecision d = chatEngine.decide(opp);
        assertThat(d.serve()).isTrue();
        assertThat(d.offer().getOfferId()).isEqualTo("9102");
        assertThat(d.reasonCode()).isEqualTo(DefaultAdServingDecisionEngine.SELECTED_CHAT_RELEVANT_OFFER);
        assertThat(d.chatAnalysis().transcriptFound()).isTrue();
        assertThat(d.chatAnalysis().bestRelevanceScore()).isGreaterThanOrEqualTo(2.0);
    }

    private static Offer offer(String id, String name, String type, String amount, String pct) {
        Offer o = new Offer();
        o.setOfferId(id);
        o.setName(name);
        o.setPayoutType(type);
        o.setPayoutAmount(amount);
        o.setPayoutPercentage(pct);
        return o;
    }

    private static AdOpportunity baseOpportunity(String id, String geo, String payoutEligible) {
        AdOpportunity o = new AdOpportunity();
        o.setId(id);
        o.setVisitorId("stub-visitor");
        o.setGeoCountryCode(geo);
        o.setPayoutEligible(payoutEligible);
        o.setSlotData("{\"opportunity_index\": \"0\", \"chat_id\": \"stub-chat\"}");
        return o;
    }

    private static final class StubCsv extends com.example.growl.CsvDataService {
        private final List<Offer> stubOffers;
        private final List<ChatDataRow> stubTranscript;

        StubCsv(List<Offer> stubOffers, List<ChatDataRow> stubTranscript) {
            this.stubOffers = stubOffers;
            this.stubTranscript = stubTranscript;
        }

        @Override
        public List<Offer> getOffers() {
            return stubOffers;
        }

        @Override
        public List<ChatDataRow> findChatTranscript(String visitorId, String chatId) {
            if (stubTranscript.isEmpty()) {
                return List.of();
            }
            ChatDataRow first = stubTranscript.get(0);
            if (visitorId != null && visitorId.equals(first.getVisitorId())
                    && chatId != null && chatId.equals(first.getChatId())) {
                return stubTranscript;
            }
            return List.of();
        }
    }
}

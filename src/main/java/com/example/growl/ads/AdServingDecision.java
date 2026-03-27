package com.example.growl.ads;

import com.example.growl.csv.model.Offer;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Result of choosing an offer for an ad opportunity, or choosing not to serve.
 */
public record AdServingDecision(
        String opportunityId,
        @JsonProperty("ad_slot_id") String adSlotId,
        boolean serve,
        Offer offer,
        String reasonCode,
        ChatAnalysisSummary chatAnalysis
) {

    public static AdServingDecision noAd(String opportunityId, String adSlotId, String reasonCode) {
        return new AdServingDecision(opportunityId, adSlotId, false, null, reasonCode, null);
    }

    public static AdServingDecision noAd(
            String opportunityId,
            String adSlotId,
            String reasonCode,
            ChatAnalysisSummary chatAnalysis) {
        return new AdServingDecision(opportunityId, adSlotId, false, null, reasonCode, chatAnalysis);
    }

    public static AdServingDecision serveOffer(String opportunityId, String adSlotId, Offer offer, String reasonCode) {
        return new AdServingDecision(opportunityId, adSlotId, true, offer, reasonCode, null);
    }

    public static AdServingDecision serveOffer(
            String opportunityId,
            String adSlotId,
            Offer offer,
            String reasonCode,
            ChatAnalysisSummary chatAnalysis) {
        return new AdServingDecision(opportunityId, adSlotId, true, offer, reasonCode, chatAnalysis);
    }
}

package com.example.growl.ads;

import com.example.growl.csv.model.AdOpportunity;

public interface AdServingDecisionEngine {

    /**
     * Same as {@link #decide(AdOpportunity, double)} with {@code adLoadPercent = 100} (no throttling).
     */
    default AdServingDecision decide(AdOpportunity opportunity) {
        return decide(opportunity, 100.0);
    }

    /**
     * @param adLoadPercent fraction of opportunities that proceed to normal ad selection (0–100). Values outside
     *                      range are clamped. When throttled, returns {@code serve=false} with
     *                      {@code AD_LOAD_SUPPRESSED}.
     */
    AdServingDecision decide(AdOpportunity opportunity, double adLoadPercent);
}

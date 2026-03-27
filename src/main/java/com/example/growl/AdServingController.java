package com.example.growl;

import com.example.growl.ads.AdServingDecision;
import com.example.growl.ads.AdServingDecisionEngine;
import com.example.growl.csv.model.AdOpportunity;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
public class AdServingController {

    private final CsvDataService csvDataService;
    private final AdServingDecisionEngine adServingDecisionEngine;

    public AdServingController(CsvDataService csvDataService, AdServingDecisionEngine adServingDecisionEngine) {
        this.csvDataService = csvDataService;
        this.adServingDecisionEngine = adServingDecisionEngine;
    }

    /**
     * Ad decision for every row in ad-opportunities.csv (same rules as single decision), in CSV load order.
     */
    @GetMapping("/ad-opportunities/decisions")
    public List<AdServingDecision> allDecisions(
            @RequestParam(name = "ad_load", required = false) Double adLoadPercent) {
        double load = adLoadPercent == null ? 100.0 : adLoadPercent;
        return csvDataService.getAdOpportunities().stream()
                .map(opp -> adServingDecisionEngine.decide(opp, load))
                .toList();
    }

    /**
     * @param adLoadPercent optional 0–100: expected fraction of eligible opportunities that receive an ad attempt
     *                      (default 100). Clamped to [0, 100].
     */
    @GetMapping("/ad-opportunities/{id}/decision")
    public AdServingDecision decision(
            @PathVariable String id,
            @RequestParam(name = "ad_load", required = false) Double adLoadPercent) {
        AdOpportunity opportunity = csvDataService.findAdOpportunityById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Unknown opportunity id"));
        double load = adLoadPercent == null ? 100.0 : adLoadPercent;
        return adServingDecisionEngine.decide(opportunity, load);
    }
}

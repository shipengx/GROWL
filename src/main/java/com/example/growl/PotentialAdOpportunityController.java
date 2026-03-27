package com.example.growl;

import com.example.growl.ads.opportunity.PotentialAdOpportunityAnalyzer;
import com.example.growl.ads.opportunity.PotentialAdOpportunityReport;
import com.example.growl.ads.opportunity.PotentialMissingAdOpportunityExport;
import com.example.growl.ads.opportunity.PotentialMissingAdOpportunityRow;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
public class PotentialAdOpportunityController {

    private final CsvDataService csvDataService;
    private final PotentialAdOpportunityAnalyzer analyzer;

    public PotentialAdOpportunityController(
            CsvDataService csvDataService,
            PotentialAdOpportunityAnalyzer analyzer) {
        this.csvDataService = csvDataService;
        this.analyzer = analyzer;
    }

    /**
     * Lists inferred ad placement signatures present in the chat transcript but absent from ad-opportunities.csv
     * for this visitor + chat_id (matched via slot_data.chat_id).
     */
    @GetMapping("/visitors/{visitorId}/chats/{chatId}/potential-ad-opportunities")
    public PotentialAdOpportunityReport potentialAdOpportunities(
            @PathVariable String visitorId,
            @PathVariable String chatId) {
        validateIds(visitorId, chatId);
        String v = visitorId.trim();
        String c = chatId.trim();
        return analyzer.analyze(v, c, csvDataService.findChatTranscript(v, c), csvDataService.getAdOpportunities());
    }

    /**
     * Missing ad opportunities as {@code visitor_id,session_id,chat_id,assistant_message_count} (JSON objects).
     */
    @GetMapping("/visitors/{visitorId}/chats/{chatId}/potential-missing-ad-opportunities")
    public List<PotentialMissingAdOpportunityRow> potentialMissingAdOpportunitiesJson(
            @PathVariable String visitorId,
            @PathVariable String chatId) {
        validateIds(visitorId, chatId);
        String v = visitorId.trim();
        String c = chatId.trim();
        var transcript = csvDataService.findChatTranscript(v, c);
        PotentialAdOpportunityReport report = analyzer.analyze(v, c, transcript, csvDataService.getAdOpportunities());
        return PotentialMissingAdOpportunityExport.toRows(v, c, transcript, report);
    }

    /**
     * Same data as CSV (header + one row per missing opportunity).
     */
    @GetMapping(value = "/visitors/{visitorId}/chats/{chatId}/potential-missing-ad-opportunities.csv", produces = "text/csv")
    public String potentialMissingAdOpportunitiesCsv(
            @PathVariable String visitorId,
            @PathVariable String chatId) {
        List<PotentialMissingAdOpportunityRow> rows = potentialMissingAdOpportunitiesJson(visitorId, chatId);
        return PotentialMissingAdOpportunityExport.toCsv(rows);
    }

    private static void validateIds(String visitorId, String chatId) {
        if (visitorId == null || visitorId.isBlank() || chatId == null || chatId.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "visitorId and chatId are required");
        }
    }
}

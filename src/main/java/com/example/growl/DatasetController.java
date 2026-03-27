package com.example.growl;

import com.example.growl.csv.model.AdOpportunity;
import com.example.growl.csv.model.ChatDataRow;
import com.example.growl.csv.model.Offer;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class DatasetController {

    private final CsvDataService csvDataService;

    public DatasetController(CsvDataService csvDataService) {
        this.csvDataService = csvDataService;
    }

    @GetMapping("/offers")
    public List<Offer> offers() {
        return csvDataService.getOffers();
    }

    @GetMapping("/ad-opportunities")
    public List<AdOpportunity> adOpportunities() {
        return csvDataService.getAdOpportunities();
    }

    @GetMapping("/chat-data")
    public List<ChatDataRow> chatData() {
        return csvDataService.getChatData();
    }
}

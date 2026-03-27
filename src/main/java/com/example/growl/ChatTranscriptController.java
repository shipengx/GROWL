package com.example.growl;

import com.example.growl.csv.model.ChatDataRow;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
public class ChatTranscriptController {

    private final CsvDataService csvDataService;

    public ChatTranscriptController(CsvDataService csvDataService) {
        this.csvDataService = csvDataService;
    }

    /**
     * Full conversation for a visitor + chat id (same join as the ad decision engine), ordered by {@code created_at}.
     */
    @GetMapping("/visitors/{visitorId}/chats/{chatId}")
    public List<ChatDataRow> chatByVisitorAndChat(
            @PathVariable String visitorId,
            @PathVariable String chatId) {
        if (visitorId == null || visitorId.isBlank() || chatId == null || chatId.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "visitorId and chatId are required");
        }
        return csvDataService.findChatTranscript(visitorId.trim(), chatId.trim());
    }
}

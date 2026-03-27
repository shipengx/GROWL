package com.example.growl.ads;

import com.example.growl.csv.model.Offer;

import java.net.URI;
import java.util.LinkedHashSet;
import java.util.Locale;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * Cheap lexical overlap between chat text and offer copy (name + URL host).
 * Not semantic search; tuned to reduce false positives from very short tokens.
 */
public final class OfferChatRelevance {

    private static final Pattern NON_TOKEN = Pattern.compile("[^a-z0-9]+");
    private static final Set<String> STOPWORDS = Set.of(
            "the", "and", "for", "with", "from", "your", "this", "that", "are", "not", "you", "any", "can",
            "our", "all", "new", "get", "now", "off", "more", "here", "some", "very", "keep", "response",
            "short", "long", "only", "needed", "proof", "social", "push", "best", "free", "win", "gift"
    );
    /** Tokens that match too many unrelated chats (e.g. travel visa vs card brand). */
    private static final Set<String> WEAK_TOKENS = Set.of(
            "visa", "card", "cash", "paypal", "gift", "app", "com", "www", "http", "https"
    );

    private OfferChatRelevance() {
    }

    public static double score(Offer offer, String chatCorpusLowercase) {
        if (offer == null || chatCorpusLowercase == null || chatCorpusLowercase.isEmpty()) {
            return 0;
        }
        double total = 0;
        for (String token : tokensFromOffer(offer)) {
            if (token.length() < 4) {
                continue;
            }
            if (!chatCorpusLowercase.contains(token)) {
                continue;
            }
            double weight = WEAK_TOKENS.contains(token) ? 0.35 : 1.0;
            total += Math.min(10, token.length()) * 0.45 * weight;
        }
        return total;
    }

    private static Set<String> tokensFromOffer(Offer offer) {
        Set<String> out = new LinkedHashSet<>();
        if (offer.getName() != null) {
            for (String w : NON_TOKEN.split(offer.getName().toLowerCase(Locale.ROOT))) {
                if (w.length() >= 4 && !STOPWORDS.contains(w)) {
                    out.add(w);
                }
            }
        }
        if (offer.getPreviewUrl() != null) {
            try {
                URI uri = URI.create(offer.getPreviewUrl().trim());
                String host = uri.getHost();
                if (host != null) {
                    for (String part : host.toLowerCase(Locale.ROOT).split("\\.")) {
                        if (part.length() >= 4 && !STOPWORDS.contains(part) && !"www".equals(part)) {
                            out.add(part);
                        }
                    }
                }
            } catch (Exception ignored) {
                // skip bad URLs
            }
        }
        return out;
    }
}

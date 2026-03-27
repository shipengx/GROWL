package com.example.growl.ads;

import com.example.growl.csv.model.Offer;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Rough revenue proxy for ranking offers (sorting only; not a forecast).
 */
public final class OfferValueScorer {

    private static final Pattern NUMBER = Pattern.compile("([0-9]+(?:\\.[0-9]+)?)");

    private OfferValueScorer() {
    }

    public static double score(Offer offer) {
        if (offer == null) {
            return 0;
        }
        String type = offer.getPayoutType() == null ? "" : offer.getPayoutType().trim();
        double amount = firstPositiveNumber(offer.getPayoutAmount());
        double pct = parsePercent(offer.getPayoutPercentage());
        if ("CPA".equalsIgnoreCase(type)) {
            return amount;
        }
        if ("CPS".equalsIgnoreCase(type)) {
            return amount + pct * 0.25;
        }
        return amount + pct * 0.1;
    }

    private static double parsePercent(String raw) {
        if (raw == null) {
            return 0;
        }
        String s = raw.trim();
        if (s.endsWith("%")) {
            s = s.substring(0, s.length() - 1).trim();
        }
        try {
            return Double.parseDouble(s);
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    private static double firstPositiveNumber(String raw) {
        if (raw == null) {
            return 0;
        }
        Matcher m = NUMBER.matcher(raw);
        if (m.find()) {
            try {
                return Double.parseDouble(m.group(1));
            } catch (NumberFormatException ignored) {
                return 0;
            }
        }
        return 0;
    }
}

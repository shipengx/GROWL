package com.example.growl.ads;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * Derives ISO-like region codes from offer display names (e.g. {@code " - US,CA"}, {@code " - UK"}).
 * Empty set means the offer is treated as globally targetable when no geo-specific match exists.
 */
public final class OfferRegionExtractor {

    private OfferRegionExtractor() {
    }

    public static Set<String> regionsForOfferName(String name) {
        if (name == null || name.isBlank()) {
            return Set.of();
        }
        Set<String> out = new LinkedHashSet<>();
        String[] parts = name.split(" - ");
        for (int i = 1; i < parts.length; i++) {
            String seg = parts[i].trim();
            if (seg.matches("^[A-Z]{2}(,[A-Z]{2})*$")) {
                for (String c : seg.split(",")) {
                    out.add(c.trim());
                }
            }
        }
        return Collections.unmodifiableSet(out);
    }
}

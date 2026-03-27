package com.example.growl.ads;

import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class OfferRegionExtractorTest {

    @Test
    void extractsCommaSeparatedRegions() {
        assertThat(OfferRegionExtractor.regionsForOfferName("Yourself First - US,CA - (Proof Needed)"))
                .containsExactlyInAnyOrder("US", "CA");
    }

    @Test
    void extractsMultiCountryList() {
        assertThat(OfferRegionExtractor.regionsForOfferName("Freecash - DE,FR,SE,AU,UK,AT,BE,CA,JP,CH,PL,US,NL"))
                .contains("NL", "US", "DE");
    }

    @Test
    void emptyWhenNoRegionSegment() {
        assertThat(OfferRegionExtractor.regionsForOfferName("Some Generic Offer")).isEmpty();
    }
}

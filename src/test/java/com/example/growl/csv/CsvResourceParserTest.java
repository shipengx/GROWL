package com.example.growl.csv;

import com.example.growl.csv.model.AdOpportunity;
import com.example.growl.csv.model.ChatDataRow;
import com.example.growl.csv.model.Offer;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class CsvResourceParserTest {

    @Test
    void parsesAllBundledCsvFiles() throws IOException {
        ClassLoader cl = Thread.currentThread().getContextClassLoader();

        List<Offer> offers = CsvResourceParser.parseOffers(cl);
        List<ChatDataRow> chat = CsvResourceParser.parseChatData(cl);
        List<AdOpportunity> ads = CsvResourceParser.parseAdOpportunities(cl);

        assertThat(offers).hasSize(500);
        assertThat(offers.get(0).getOfferId()).isEqualTo("9764");
        assertThat(offers.get(0).getName()).isNotBlank();

        assertThat(chat).hasSizeGreaterThan(1000);
        assertThat(chat.get(0).getRole()).isNotBlank();

        assertThat(ads).hasSize(121);
        assertThat(ads.get(0).getSessionId()).isNotBlank();
    }
}
